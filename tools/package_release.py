#!/usr/bin/env python3
"""Package the tested playable mod, full source, guides, and checksums into a ZIP."""
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile
from build_offline import mod_version

ROOT=Path(__file__).resolve().parents[1]

def digest(data): return hashlib.sha256(data).hexdigest()

def main():
    version=mod_version()
    jar=ROOT/f'build/libs/catholicraft-{version}.jar'
    if not jar.is_file(): raise SystemExit('Build the current release JAR first.')
    subprocess.run([sys.executable,str(ROOT/'tools/verify_resources.py')],check=True)
    # A packaged source tree must agree with the compiled production artifact.
    for base in (ROOT/'src/main/java',ROOT/'src/main/resources'):
        for file in base.rglob('*'):
            if file.is_file() and file.stat().st_mtime_ns>jar.stat().st_mtime_ns:
                raise SystemExit(f'Build is stale: {file.relative_to(ROOT)} changed after the JAR was built.')
    report=ROOT/'build/gametest-results.xml'
    tree=ET.parse(report)
    cases=tree.findall('.//testcase')
    declared=re.findall(r'test\(e,\s*"([^"]+)"',(ROOT/'src/gameTest/java/com/catholicraft/test/CatholiCraftTests.java').read_text())
    expected={'catholicraft_tests:'+name for name in declared}
    if tree.findall('.//failure') or tree.findall('.//error') or {c.get('name') for c in cases}!=expected:
        raise SystemExit('Run all current Minecraft GameTests successfully before packaging.')
    tested=ROOT/'runs/gameTestServer/mods/catholicraft.jar'
    if not tested.is_file() or digest(tested.read_bytes())!=digest(jar.read_bytes()):
        raise SystemExit('The current release JAR differs from the JAR used by the Minecraft tests.')
    test_log=(ROOT/'build/final-gametest.log').read_text()
    if f'All {len(expected)} required tests passed' not in test_log or '/ERROR]' in test_log:
        raise SystemExit('The latest Minecraft run must pass without resource loading errors.')
    client_log=(ROOT/'build/client-validation.log').read_text()
    if 'PASS: Minecraft parsed' not in client_log or 'book pages fit' not in client_log or 'Exception' in client_log:
        raise SystemExit('Run the native client asset and font validation successfully before packaging.')
    if (ROOT/'build/client-validation.log').stat().st_mtime_ns<jar.stat().st_mtime_ns:
        raise SystemExit('Client validation predates the release build.')
    catalog=json.loads((ROOT/'content.json').read_text())
    files={}
    files[f'mods/catholicraft-{version}.jar']=jar.read_bytes()
    files['INSTALL.txt']=f'''CatholiCraft {version}

Minecraft Java Edition 26.2 / NeoForge 26.2.0.90 / Java 25

1. Install the matching NeoForge Minecraft profile.
2. Copy mods/catholicraft-{version}.jar from this ZIP into that profile's mods folder.
3. Remove an older CatholiCraft JAR from that mods folder if upgrading.
4. Start the game. Use the CatholiCraft creative tab, or craft a field guide
   with a book and a gold nugget.

Cheat/operator commands:
  /catholicraft kit
  /catholicraft chapel

The chapel builder needs a clear 15 x 11 x 8 volume east of the player.
The included README explains survival crafting and all interactions.

For a server, install the same mod on the server and every connecting client.
The only required mod loader is NeoForge; no additional mods are required.

source/ contains the complete editable Gradle project. Its main build command
is ./gradlew build (Java 25; network access needed for the first standard build).
Development tests and cached Minecraft dependencies are not bundled in the
playable JAR.

Automated validation passes. Graphical playtesting was unavailable in the
build environment. The missals are introductory companions, with original
explanatory text; the prayer book also includes historic Latin prayers.
'''.encode()
    for name in ('README.md','LICENSE','CHANGELOG.md','content.json'):
        files[name]=(ROOT/name).read_bytes()
    roots=('README.md','LICENSE','CHANGELOG.md','content.json','build.gradle','settings.gradle',
           'gradle.properties','gradlew','gradlew.bat','.gitignore')
    for name in roots: files['source/'+name]=(ROOT/name).read_bytes()
    for directory in ('src','tools','gradle','docs'):
        for file in sorted((ROOT/directory).rglob('*')):
            if file.is_file() and not file.is_symlink() and '__pycache__' not in file.parts and file.suffix not in ('.pyc','.log'):
                relative=file.relative_to(ROOT).as_posix()
                files['source/'+relative]=file.read_bytes()
                if directory=='docs': files[relative]=file.read_bytes()
    files['validation/gametest-results.xml']=report.read_bytes()
    files['validation/client-validation.txt']=client_log.encode()
    validation={
        'mod_version':version,'minecraft':catalog['minecraft'],'loader':catalog['loader'],
        'release_jar_sha256':digest(jar.read_bytes()),'minecraft_tests_passed':len(cases),
        'tests':[c.get('name') for c in cases],
        'registered_items':len(catalog['blocks'])+len(catalog['items']),
        'blocks':len(catalog['blocks']),'vestments':len(catalog['vestments']),
        'recipes':len(list((ROOT/'src/main/resources/data/catholicraft/recipe').glob('*.json'))),
        'graphical_playtest_performed':False
    }
    files['validation/summary.json']=(json.dumps(validation,indent=2)+'\n').encode()
    files['SHA256SUMS.txt']=''.join(f'{digest(data)}  {name}\n' for name,data in sorted(files.items())).encode()
    target=ROOT/f'releases/catholicraft-{version}.zip'
    target.parent.mkdir(parents=True,exist_ok=True)
    prefix=f'CatholiCraft-{version}/'
    with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
        for name,data in sorted(files.items()):
            entry=zipfile.ZipInfo(prefix+name)
            entry.compress_type=zipfile.ZIP_DEFLATED
            entry.external_attr=(0o100755 if name=='source/gradlew' else 0o100644)<<16
            archive.writestr(entry,data)
    # Read the finished archive back; validate CRCs, paths, and every included file.
    with zipfile.ZipFile(target) as archive:
        if archive.testzip() is not None: raise SystemExit('ZIP CRC validation failed.')
        for name,data in files.items():
            if archive.read(prefix+name)!=data: raise SystemExit(f'ZIP content mismatch: {name}')
        if any('/../' in n or n.startswith('/') for n in archive.namelist()): raise SystemExit('Unsafe ZIP path.')
    print(f'Packaged {target} ({target.stat().st_size:,} bytes; {len(files)} verified entries).')
    print(f'SHA-256: {digest(target.read_bytes())}')

if __name__=='__main__': main()
