#!/usr/bin/env python3
"""Compile/package using an existing NeoForge 26.2 dev cache; never downloads or modifies it.

Standard builds use ./gradlew build. This fallback is for sandboxed environments
where Gradle's lock communication sockets are unavailable.
"""
import argparse
import json
import os
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]

def mod_version():
    return next(line.split('=',1)[1].strip() for line in (ROOT/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))

def linux_library(lib):
    allowed = not lib.get('rules')
    for rule in lib.get('rules', []):
        system = rule.get('os', {})
        if system.get('name', 'linux') == 'linux' and system.get('arch', 'x86_64') in ('x86_64', 'amd64'):
            allowed = rule['action'] == 'allow'
    return allowed and 'natives-linux-arm64' not in lib['name']

def libraries(cache):
    config = next(cache.glob('modules-2/files-2.1/net.neoforged/neoforge/26.2.0.90/*/*moddev-config.json'))
    manifest = cache / 'neoformruntime/artifacts/minecraft_26.2_version_manifest.json'
    coordinates = json.loads(config.read_text())['libraries']
    coordinates += [lib['name'] for lib in json.loads(manifest.read_text())['libraries'] if linux_library(lib)]
    coordinates += ['net.neoforged:neoforge:26.2.0.90:universal']
    jars = []
    for coordinate in dict.fromkeys(coordinates):
        group, name, version, *classifier = coordinate.split(':')
        filename = f'{name}-{version}' + (f'-{classifier[0]}' if classifier else '') + '.jar'
        matches = list((cache / 'modules-2/files-2.1' / group / name / version).glob(f'*/{filename}'))
        if not matches:
            # Mojang libraries cached by NeoFormRuntime use a flattened filename.
            matches = list((cache / 'neoformruntime/artifacts').glob(f'{group}_{filename}'))
        if not matches:
            raise SystemExit(f'Missing cached dependency: {coordinate}')
        jars.append(matches[0].resolve())
    return jars

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--cache', type=Path, default=Path.home() / '.gradle/caches')
    parser.add_argument('--minecraft', type=Path, required=True, help='NeoForge minecraft-patched-26.2.0.90-merged.jar')
    parser.add_argument('--tests', action='store_true', help='Also compile the development-only game tests')
    args = parser.parse_args()
    jars = [args.minecraft.resolve(), *libraries(args.cache)]
    classes = ROOT / 'build/classes/java/main'
    classes.mkdir(parents=True, exist_ok=True)
    sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
    subprocess.run(['javac', '--release', '25', '-encoding', 'UTF-8', '-proc:none', '-cp', os.pathsep.join(map(str, jars)), '-d', str(classes), *map(str, sources)], check=True)
    resources = ROOT / 'src/main/resources'
    version = mod_version()
    output = ROOT / f'build/libs/catholicraft-{version}.jar'
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        for base in (classes, resources):
            for file in sorted(base.rglob('*')):
                if file.is_file():
                    data = file.read_bytes()
                    if file.name == 'neoforge.mods.toml':
                        data = data.replace(b'${version}', version.encode())
                    archive.writestr(file.relative_to(base).as_posix(), data)
        archive.write(ROOT / 'LICENSE', 'META-INF/LICENSE')
        archive.write(ROOT / 'THIRD_PARTY_ASSETS.md', 'META-INF/THIRD_PARTY_ASSETS.md')
    if args.tests:
        test_classes = ROOT / 'build/classes/java/gameTest'
        test_classes.mkdir(parents=True, exist_ok=True)
        test_sources = sorted((ROOT / 'src/gameTest/java').rglob('*.java'))
        subprocess.run(['javac', '--release', '25', '-encoding', 'UTF-8', '-proc:none', '-cp', os.pathsep.join(map(str, [classes, *jars])), '-d', str(test_classes), *map(str, test_sources)], check=True)
        with zipfile.ZipFile(ROOT/'build/libs/catholicraft-tests.jar','w',zipfile.ZIP_DEFLATED) as archive:
            for base in (test_classes,ROOT/'src/gameTest/resources'):
                for file in sorted(base.rglob('*')):
                    if file.is_file(): archive.write(file,file.relative_to(base).as_posix())
    (ROOT / 'build/runtime-classpath.txt').write_text(os.pathsep.join(map(str, jars)))
    print(f'Built {output} ({output.stat().st_size:,} bytes)')

if __name__ == '__main__':
    main()
