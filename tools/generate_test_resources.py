#!/usr/bin/env python3
"""Create empty NBT templates for development-only Minecraft GameTests."""
import gzip
from pathlib import Path
import struct

ROOT=Path(__file__).resolve().parents[1]
def string(s):
    data=s.encode('utf-8')
    return struct.pack('>H',len(data))+data
def named(kind,name,data): return bytes([kind])+string(name)+data
def ints(values): return bytes([3])+struct.pack('>i',len(values))+b''.join(struct.pack('>i',v) for v in values)
for name,size in [('empty',[4,4,4]),('empty_chapel',[16,9,12]),('empty_cathedral',[43,27,31])]:
    # A valid vanilla structure with an air palette and no occupied positions.
    palette=named(8,'Name',string('minecraft:air'))+b'\0'
    nbt=b'\x0a\x00\x00'+named(9,'size',ints(size))
    nbt+=named(9,'palette',b'\x0a'+struct.pack('>i',1)+palette)
    nbt+=named(9,'blocks',b'\x0a'+struct.pack('>i',0))
    nbt+=named(9,'entities',b'\x0a'+struct.pack('>i',0))
    nbt+=named(3,'DataVersion',struct.pack('>i',4903))+b'\0'
    target=ROOT/f'src/gameTest/resources/data/catholicraft_tests/structure/{name}.nbt'
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_bytes(gzip.compress(nbt,mtime=0))
print('Generated three empty GameTest structures.')
