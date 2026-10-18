#!/usr/bin/env python3
"""Generate a furnished 41×25×29 Gothic cathedral for vanilla jigsaw worldgen."""
import gzip
import json
from pathlib import Path
import struct

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'; DATA=RES/'data/catholicraft'
SIZE=[41,25,29]
plan={}; inventories={}

def block(x,y,z,name,**props):
    plan[(x,y,z)]=(name if ':' in name else 'minecraft:'+name,tuple(sorted((k,str(v).lower()) for k,v in props.items())))
def box(x1,y1,z1,x2,y2,z2,name,**props):
    for x in range(x1,x2+1):
        for y in range(y1,y2+1):
            for z in range(z1,z2+1):block(x,y,z,name,**props)
def furnish(x,y,z,name,**props):block(x,y,z,'catholicraft:'+name,**props)
def inventory(x,y,z,items): inventories[(x,y,z)]=items

def blueprint():
    # A complete air interior removes vegetation in natural generation.
    box(0,0,0,40,24,28,'air')
    box(2,0,5,39,0,23,'stone_bricks');box(22,0,0,30,0,28,'stone_bricks')
    for x in range(3,39):
        for z in range(6,23):block(x,0,z,'polished_diorite' if (x+z)%2 else 'smooth_quartz')
    # Nave, lower aisles and pointed roof.
    for x in range(3,39):
        for z in [6,22]:
            box(x,1,z,x,9,z,'stone_bricks')
            if x in range(11,35) and x%6 in [0,1]:
                box(x,3,z,x,7,z,'catholicraft:cross_stained_glass')
        for z in range(6,23):
            roof=10+min(z-6,22-z)
            block(x,roof,z,'deepslate_tiles')
            if x in [3,38]: box(x,1,z,x,roof-1,z,'stone_bricks')
    # Clerestory band and internal column/arch ribs.
    for x in range(10,37):
        for z in [10,18]:
            box(x,10,z,x,13,z,'stone_bricks')
            if x%6 in [0,1]:box(x,11,z,x,13,z,'catholicraft:cross_stained_glass')
    for x in [11,17,23,29,35]:
        for z in [9,19]:
            box(x,1,z,x,9,z,'quartz_pillar',axis='y');box(x-1,9,z,x+1,9,z,'chiseled_stone_bricks')
        for z in range(10,19):
            y=14+min(z-10,18-z)//2;block(x,y,z,'smooth_quartz')
    # Flying buttresses, with sloping supports and exterior piers.
    for x in [11,17,23,29,35]:
        for z in [3,25]:
            box(x,0,z,x,7,z,'stone_bricks');block(x,8,z,'chiseled_stone_bricks')
        for dz in range(1,4):
            block(x,8+dz,3+dz,'stone_bricks');block(x,8+dz,25-dz,'stone_bricks')
    # Low aisle lamps illuminate the floor as well as the high vaults.
    for x in [11,17,23,29,35]:
        for z in [10,18]:
            block(x,1,z,"chiseled_stone_bricks");block(x,2,z,"lantern")
    # Transept wings have walkable openings into the nave.
    for z1,z2 in [(1,5),(23,27)]:
        box(23,1,z1,23,9,z2,'stone_bricks');box(29,1,z1,29,9,z2,'stone_bricks')
        edge=1 if z1==1 else 27;box(23,1,edge,29,10,edge,'stone_bricks')
        box(25,3,edge,27,7,edge,'catholicraft:cross_stained_glass')
        for z in range(z1,z2+1):
            for x in range(23,30):block(x,10+min(x-23,29-x),z,'deepslate_tiles')
    box(24,1,6,28,7,6,'air');box(24,1,22,28,7,22,'air')
    # Twin west towers with accessible stairs and belfries.
    for z1 in [4,18]:
        z2=z1+6
        for y in range(1,21):
            for x in range(3,10):
                for z in range(z1,z2+1):
                    if x in [3,9] or z in [z1,z2]:block(x,y,z,'stone_bricks')
            if y in [8,15]:box(3,y,z1,9,y,z2,'chiseled_stone_bricks')
        box(4,16,z1+1,8,16,z2-1,'dark_oak_planks')
        for x in [3,9]:box(x,17,z1+2,x,19,z2-2,'air')
        for z in [z1,z2]:box(5,17,z,7,19,z,'air')
        furnish(6,17,z1+3,'church_bell')
        for y in range(1,16):block(8,y,z1+1,'ladder',facing='west')
        box(7,1,z1+2,9,3,z1+4,'air')
        for layer in range(4):box(3+layer,21+layer,z1+layer,9-layer,21+layer,z2-layer,'deepslate_tiles')
        # Last roof layer has a single ridge block, not an inverted cuboid.
    for z in range(12,17):block(2,0,z,"stone_brick_stairs",facing="east")
    # Entrance arch and rose window between the towers.
    box(3,1,12,3,5,16,'air');box(3,6,13,3,6,15,'air');block(3,7,14,'air')
    for z in range(10,19):
        for y in range(9,17):
            distance=(z-14)**2+(y-12.5)**2
            if distance<=12:block(3,y,z,'catholicraft:cross_stained_glass')
            elif distance<=19:block(3,y,z,'smooth_quartz')
    block(3,18,14,'gold_block');block(3,19,14,'gold_block');block(3,20,14,'gold_block');block(3,19,13,'gold_block');block(3,19,15,'gold_block')
    # Aisle lighting; sanctuary raised one block above the nave.
    for x in range(11,35,6):
        for z in [7,21]:block(x,8,z,'lantern',hanging=True)
    box(31,1,10,37,1,18,'smooth_quartz')
    for z in range(10,19):block(30,1,z,'quartz_stairs',facing='east')
    for x in range(12,29,3):
        for z in [11,12,16,17]:furnish(x,1,z,'pew',facing='east')
    for z in [13,14,15]:furnish(34,2,z,'altar',facing='west')
    furnish(34,3,13,'altar_candle',lit=True);furnish(34,3,15,'altar_candle',lit=True);furnish(34,3,14,'chalice')
    block(36,2,14,'smooth_quartz');furnish(36,3,14,'tabernacle',facing='west');furnish(36,4,14,'crucifix',facing='west')
    furnish(36,3,15,'sanctuary_lamp',lit=True);furnish(32,2,11,'ambo',facing='west')
    for z,name in [(11,'our_lady_statue'),(17,'st_joseph_statue')]:
        block(36,2,z,'smooth_quartz');furnish(36,3,z,name,facing='west')
    furnish(33,2,17,'credence_table');furnish(33,3,17,'ciborium');furnish(32,2,17,'incense_burner')
    furnish(29,1,14,'kneeler',facing='east');furnish(6,1,12,'holy_water_font',water=3)
    furnish(11,1,20,'baptismal_font',water=3);furnish(7,1,16,'votive_rack',lit=True)
    for i in range(14):furnish(11+(i%7)*3,3,7 if i<7 else 21,'stations_of_the_cross',station=i+1,facing='south' if i<7 else 'north')
    for x,z,name,facing in [(26,2,'annunciation_icon','south'),(26,26,'sistine_madonna_icon','north'),(37,14,'last_supper_icon','west')]:furnish(x,4,z,name,facing=facing)
    # A small sacristy with stocked cabinet, reading desk and music alcove.
    furnish(25,1,25,'sacristy_cabinet',facing='north')
    inventory(25,1,25,[('field_guide',1),('missal_1962',1),('modern_missal',1),('alb',1),('white_chasuble',1),('altar_bread',24),('wine_cruet',8),('incense',32),('chant_veni_creator',1),('chant_veni_sancte',1),('chant_dies_irae',1)])
    block(27,1,25,'jukebox');block(26,1,3,'bookshelf')
    return plan

# Minimal deterministic NBT writer (only the types used by structure templates).
def text(s):
    data=s.encode();return struct.pack('>H',len(data))+data
def payload(t,v):
    if t==1:return struct.pack('>b',v)
    if t==3:return struct.pack('>i',v)
    if t==6:return struct.pack('>d',v)
    if t==8:return text(v)
    if t==9:
        kind,values=v;return bytes([kind])+struct.pack('>i',len(values))+b''.join(payload(kind,a) for a in values)
    if t==10:return b''.join(bytes([kind])+text(name)+payload(kind,value) for name,(kind,value) in v.items())+b'\0'
    raise ValueError(t)
def il(values):return (9,(3,list(values)))
def compound(values):return (10,values)
def write_json(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n')

def main():
    blueprint();palette=[];indexes={};blocks=[]
    for pos,state in sorted(plan.items()):
        if state not in indexes:
            indexes[state]=len(palette);name,props=state
            row={'Name':(8,name)}
            if props:row['Properties']=compound({k:(8,v) for k,v in props})
            palette.append(row)
        row={'pos':il(pos),'state':(3,indexes[state])}
        if pos in inventories:
            items=[{'Slot':(1,i),'id':(8,'catholicraft:'+name),'count':(3,count)} for i,(name,count) in enumerate(inventories[pos])]
            row['nbt']=compound({'id':(8,'catholicraft:tabernacle'),'Items':(9,(10,items))})
        blocks.append(row)
    entities=[]
    for x,y,z in [(18,1,14),(26,1,24),(26,1,4)]:
        entities.append({'pos':(9,(6,[x+.5,float(y),z+.5])),'blockPos':il([x,y,z]),'nbt':compound({'id':(8,'catholicraft:monk'),'PersistenceRequired':(1,1)})})
    root={'DataVersion':(3,4903),'size':il(SIZE),'palette':(9,(10,palette)),'blocks':(9,(10,blocks)),'entities':(9,(10,entities))}
    target=DATA/'structure/cathedral.nbt';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(gzip.compress(b'\x0a\x00\x00'+payload(10,root),mtime=0))
    write_json(DATA/'worldgen/template_pool/cathedral/start.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'catholicraft:cathedral','processors':{'processors':[]},'projection':'rigid'}}]})
    write_json(DATA/'worldgen/structure/cathedral.json',{'type':'minecraft:jigsaw','biomes':'#catholicraft:has_cathedral','step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_box','start_pool':'catholicraft:cathedral/start','size':1,'start_height':{'absolute':0},'project_start_to_heightmap':'WORLD_SURFACE_WG','max_distance_from_center':80,'use_expansion_hack':False})
    write_json(DATA/'worldgen/structure_set/cathedrals.json',{'structures':[{'structure':'catholicraft:cathedral','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':40,'separation':16,'salt':19431225,'exclusion_zone':{'other_set':'minecraft:villages','chunk_count':6}}})
    write_json(DATA/'tags/worldgen/biome/has_cathedral.json',{'replace':False,'values':['minecraft:plains','minecraft:sunflower_plains','minecraft:meadow','minecraft:birch_forest','minecraft:forest','minecraft:flower_forest','minecraft:cherry_grove']})
    (ROOT/'docs/cathedral-blueprint.json').write_text(json.dumps({'size':SIZE,'blocks':[{'pos':list(pos),'name':state[0],'properties':dict(state[1])} for pos,state in plan.items() if state[0]!='minecraft:air']},separators=(',',':'))+'\n')
    print(f'Generated cathedral: {SIZE}, {len(palette)} palette entries, {len(blocks)} positions, {len(entities)} monks.')
if __name__=='__main__':main()
