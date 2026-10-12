#!/usr/bin/env python3
"""Audit every registered content ID, resource reference, and survival recipe."""
import json
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
        require(recipe.get('result',{}).get('id')=='catholicraft:'+name,f'{name}: missing survival output')
        load(D/f'advancement/recipes/{name}.json')
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

if errors: raise SystemExit('\n'.join(errors))
print(f'PASS: {len(all_ids)} registered items, {len(catalog["blocks"])} blocks, all model/texture links, recipes, loot, translations, lectern tags, and {len(catalog["vestments"])} equipment assets.')
