#!/usr/bin/env python3
"""Audit every registered content ID, resource reference, and survival recipe."""
import json
import hashlib
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
A=RES/'assets/catholicraft'
D=RES/'data/catholicraft'
catalog=json.loads((ROOT/'content.json').read_text())
lang=json.loads((A/'lang/en_us.json').read_text())
errors=[]

def require(condition, message):
    if not condition: errors.append(message)

def load(path):
    require(path.exists(),f'Missing {path.relative_to(ROOT)}')
    if path.exists():
        try: return json.loads(path.read_text())
        except (ValueError,OSError) as e: errors.append(str(e))
    return {}

def reference(ref, kind, owner):
    if isinstance(ref,dict): ref=ref.get('sprite')
    if not isinstance(ref,str) or ref.startswith('#') or not ref.startswith('catholicraft:'): return
    path=ref.split(':',1)[1]
    target=A/(f'models/{path}.json' if kind=='model' else f'textures/{path}.png')
    require(target.exists(),f'{owner}: missing {kind} {ref}')

all_ids=set(catalog['blocks']+catalog['items'])
source=(ROOT/'src/main/java/com/catholicraft/registry/ModContent.java').read_text()
registered=set(re.findall(r'(?:registerBlock|furnishing|registerItem|registerSimpleItem|medal|vestment|cloth)\("([a-z0-9_]+)"',source))
require(registered==all_ids,f'Registry/catalog mismatch: {registered ^ all_ids}')

for file in sorted(RES.rglob('*.json')):
    data=load(file)
    if '/models/' in str(file):
        reference(data.get('parent'),'model',file.name)
        for ref in data.get('textures',{}).values(): reference(ref,'texture',file.name)
        tex=data.get('textures',{})
        for element in data.get('elements',[]):
            require(all(a<=b for a,b in zip(element['from'],element['to'])),f'{file.name}: inverted cuboid')
            for face in element.get('faces',{}).values():
                key=face['texture'].removeprefix('#')
                require(key in tex,f'{file.name}: undefined texture variable {key}')
    if '/items/' in str(file) and '/assets/' in str(file): reference(data.get('model',{}).get('model'),'model',file.name)

for name in all_ids:
    load(A/f'items/{name}.json')
    prefix='block' if name in catalog['blocks'] else 'item'
    require(f'{prefix}.catholicraft.{name}' in lang,f'{name}: missing name')
    if name not in catalog['interaction_only_items']:
        recipe=load(D/f'recipe/{name}.json')
        if recipe.get('type')=='catholicraft:workbench':
            recipe=recipe['recipe']  # made at St. Joseph's Workbench; no recipe-book advancement
        else:
            require(name in ('field_guide','st_josephs_workbench'),f'{name}: should be a St. Joseph\'s Workbench recipe')
            load(D/f'advancement/recipes/{name}.json')
        require(recipe.get('result',{}).get('id')=='catholicraft:'+name,f'{name}: missing survival output')
        keys=recipe.get('key',{})
        if keys:
            used=set(''.join(recipe['pattern']))-{' '}
            require(used==set(keys),f'{name}: invalid recipe key set')
        for value in [*keys.values(),*recipe.get('ingredients',[])]:
            if value.startswith('catholicraft:'): require(value.split(':')[1] in all_ids,f'{name}: missing ingredient {value}')
    if name in catalog['blocks']:
        states=load(A/f'blockstates/{name}.json')
        for variant in states.get('variants',{}).values(): reference(variant['model'],'model',name)
        load(D/f'loot_table/blocks/{name}.json')
    if name in catalog['vestments']:
        equipment=load(A/f'equipment/{name}.json')
        for layer,textures in equipment.get('layers',{}).items():
            for texture in textures:
                path=A/f'textures/entity/equipment/{layer}/{texture["texture"].split(":")[1]}.png'
                require(path.exists(),f'{name}: missing worn texture for {layer}')

booktag=load(RES/'data/minecraft/tags/item/lectern_books.json')
require(set(booktag.get('values',[]))=={'catholicraft:'+book for book in catalog['books']},'All books must work on lecterns')
for key in re.findall(r'Component\.translatable\("([^"]+)"', ''.join(p.read_text() for p in (ROOT/'src/main/java').rglob('*.java'))):
    if key.endswith('.'): continue
    require(key in lang,f'Missing translation {key}')

# Imported media must retain independently verified rights, credits and exact bundled hashes.
credits=load(RES/'META-INF/catholicraft-assets.json')
require(isinstance(credits,list) and len(credits)==6,'Six verified public-domain assets must be credited')
for asset in credits if isinstance(credits,list) else []:
    require(asset.get('license') in ('PD-self','PD-art'),f'{asset.get("id")}: unapproved media rights')
    require(asset.get('source_page','').startswith('https://commons.wikimedia.org/wiki/File:'),f'{asset.get("id")}: missing source page')
    target=RES/asset['asset']
    require(target.exists(),f'{asset["id"]}: missing imported file')
    if target.exists():require(hashlib.sha256(target.read_bytes()).hexdigest()==asset['asset_sha256'],f'{asset["id"]}: asset hash changed without updating credits')
    if 'audio' in asset:
        streams=asset['audio']['streams']
        require(any(s.get('codec_name')=='vorbis' and s.get('channels')==1 for s in streams),f'{asset["id"]}: chant must be mono Vorbis')
sounds=load(A/'sounds.json')
for name,definition in sounds.items():
    require(definition.get('subtitle') in lang,f'{name}: missing chant subtitle')
    for sound in definition['sounds']:
        require(sound.get('stream') is True,f'{name}: long recording must stream')
        path=A/'sounds'/(sound['name'].split(':')[1]+'.ogg')
        require(path.exists(),f'{name}: missing recording')
for name in ('veni_creator','veni_sancte','dies_irae'):
    song=load(D/f'jukebox_song/{name}.json')
    require(song.get('sound_event','').split(':')[-1] in sounds,f'{name}: unregistered jukebox sound')
    require(song.get('length_in_seconds',0)>25,f'{name}: missing full recording duration')
structure=load(D/'worldgen/structure/cathedral.json')
require(structure.get('size',0)>0,'Minecraft jigsaw generation must add the initial piece')
require((D/'structure/cathedral.nbt').is_file(),'Missing cathedral structure template')
pool=load(D/'worldgen/template_pool/cathedral/start.json')
require(pool.get('elements',[{}])[0].get('element',{}).get('location')=='catholicraft:cathedral','Cathedral pool must point to template')
placement=load(D/'worldgen/structure_set/cathedrals.json').get('placement',{})
require(placement.get('spacing',0)>placement.get('separation',0)>0,'Cathedral spacing must exceed separation')
require('catholicraft:cathedral'==load(D/'worldgen/structure_set/cathedrals.json').get('structures',[{}])[0].get('structure'),'Natural structure set must include cathedral')

if errors: raise SystemExit('\n'.join(errors))
print(f'PASS: {len(all_ids)} registered items, {len(catalog["blocks"])} blocks, all model/texture links, recipes, loot, translations, lectern tags, and {len(catalog["vestments"])} equipment assets; public-domain media hashes, sounds, and cathedral worldgen.')
