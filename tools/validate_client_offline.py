#!/usr/bin/env python3
"""Use Minecraft's model parsers to validate client assets without a display."""
import os
import json
from io import BytesIO
from pathlib import Path
import subprocess
import zipfile
from PIL import Image

root=Path(__file__).resolve().parents[1]
cp=(root/'build/runtime-classpath.txt').read_text()
cp=str(root/'build/classes/java/main')+os.pathsep+cp
# Derive glyph advance widths from the exact vanilla bitmap and provider metadata.
with zipfile.ZipFile(cp.split(os.pathsep)[1]) as archive:
    providers=json.loads(archive.read('assets/minecraft/font/include/default.json'))['providers']
    provider=next(p for p in providers if p.get('file')=='minecraft:font/ascii.png')
    image=Image.open(BytesIO(archive.read('assets/minecraft/textures/font/ascii.png'))).convert('RGBA')
    cell_width=image.width//len(provider['chars'][0])
    cell_height=image.height//len(provider['chars'])
    scale=provider.get('height',8)/cell_height
    widths={'32':4}
    for row,characters in enumerate(provider['chars']):
        for column,char in enumerate(characters):
            if char in ('\0',' '): continue
            occupied=0
            for x in range(cell_width):
                if any(image.getpixel((column*cell_width+x,row*cell_height+y))[3] for y in range(cell_height)): occupied=x+1
            widths[str(ord(char))]=int(.5+occupied*scale)+1
metrics=root/'build/book-font-widths.json'
metrics.write_text(json.dumps(widths))
classes=root/'build/classes/java/resourceValidation'
classes.mkdir(parents=True,exist_ok=True)
subprocess.run(['javac','-proc:none','-cp',cp,'-d',str(classes),str(root/'tools/java/ValidateClientResources.java')],check=True)
raise SystemExit(subprocess.call(['java','--sun-misc-unsafe-memory-access=allow','-cp',str(classes)+os.pathsep+cp,'ValidateClientResources',str(root/'src/main/resources/assets/catholicraft'),str(metrics)]))
