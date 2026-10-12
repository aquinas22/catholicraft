#!/usr/bin/env python3
"""Launch the compiled mod through NeoForge using build_offline.py's cached classpath."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import zipfile
from build_offline import mod_version

ROOT=Path(__file__).resolve().parents[1]

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('mode', choices=['gameTestServer','client','server'])
    p.add_argument('args', nargs=argparse.REMAINDER)
    options=p.parse_args()
    cp=(ROOT/'build/runtime-classpath.txt').read_text()
    loader=next(Path(s) for s in cp.split(os.pathsep) if '/fancymodloader/loader/' in s or '/net.neoforged.fancymodloader/loader/' in s)
    agent=ROOT/'build/dev-agent.jar'
    with zipfile.ZipFile(loader) as source, zipfile.ZipFile(agent,'w') as target:
        target.writestr('META-INF/MANIFEST.MF','Manifest-Version: 1.0\nPremain-Class: net.neoforged.fml.startup.DevAgent\nCan-Redefine-Classes: true\nCan-Retransform-Classes: true\n\n')
        target.writestr('net/neoforged/fml/startup/DevAgent.class',source.read('net/neoforged/fml/startup/DevAgent.class'))
    folder=ROOT/'runs'/options.mode
    (folder/'mods').mkdir(parents=True,exist_ok=True)
    shutil.copy2(ROOT/f'build/libs/catholicraft-{mod_version()}.jar',folder/'mods/catholicraft.jar')
    if options.mode=='gameTestServer':
        tests=ROOT/'build/libs/catholicraft-tests.jar'
        if tests.exists(): shutil.copy2(tests,folder/'mods/catholicraft-tests.jar')
    entry={'gameTestServer':'GameTestServer','client':'Client','server':'Server'}[options.mode]
    command=['java','-Xmx3G',f'-javaagent:{agent}','--sun-misc-unsafe-memory-access=allow',
             '--enable-native-access=ALL-UNNAMED','--add-opens','java.base/java.lang.invoke=ALL-UNNAMED',
             '--add-exports','jdk.naming.dns/com.sun.jndi.dns=java.naming','-Dneoforge.enableGameTest=true',
             '-cp',cp,'net.neoforged.fml.startup.'+entry]
    if options.mode=='client':
        # NeoFormRuntime's shared assets cache is adjacent to modules-2.
        assets=Path.home()/'.gradle/caches/neoformruntime/assets'
        indexes=list((assets/'indexes').glob('*.json'))
        command += ['--version','26.2','--assetsDir',str(assets),'--assetIndex',indexes[0].stem]
    command += options.args
    raise SystemExit(subprocess.call(command,cwd=folder))

if __name__=='__main__': main()
