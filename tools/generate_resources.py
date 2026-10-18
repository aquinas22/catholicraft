#!/usr/bin/env python3
"""Generate original, deterministic pixel art, models, recipes, and translations.

Requires Pillow. All generated game resources are committed; players and Java
builds do not need Python. Run from anywhere with python3 tools/generate_resources.py.
"""
import json
from pathlib import Path
import random
from PIL import Image, ImageDraw, ImageFont
import expanded_resources as expanded
import sys

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/catholicraft'
DATA = RES / 'data/catholicraft'
COLORS = {'white': '#eee7cf', 'red': '#a52b38', 'green': '#347250',
          'violet': '#66397e', 'rose': '#c8738c', 'black': '#303039'}
GOLD = '#d9b35b'
DARK_GOLD = '#82652f'
INK = '#292531'
BLOCKS = ['tabernacle', 'altar', 'crucifix', 'holy_water_font', 'baptismal_font',
          'altar_candle', 'sanctuary_lamp', 'church_bell', 'kneeler', 'pew',
          'chalice', 'ciborium', 'monstrance', 'cross_stained_glass', 'sacristy_cabinet',
          'advent_wreath', 'votive_rack', 'stations_of_the_cross', 'incense_burner',
          'our_lady_statue', 'st_joseph_statue', 'credence_table', 'ambo', *expanded.ART, 'st_josephs_workbench']
ITEMS = ['missal_1962', 'modern_missal', 'field_guide', 'holy_water', 'rosary', 'thurible',
         'incense', 'st_benedict_medal', 'miraculous_medal', 'st_michael_medal',
         *[color + '_chasuble' for color in COLORS], 'alb', 'biretta', 'mitre', 'liturgical_shoes',
         *[color + '_dalmatic' for color in COLORS], 'cassock', 'surplice',
         *[color + '_altar_cloth' for color in COLORS], 'prayer_book', 'stations_book',
         'aspergillum', 'altar_bell', 'processional_cross', 'scapular', 'beeswax', 'altar_bread', *expanded.NEW_ITEMS]
VESTMENTS=[n for n in ITEMS if n.endswith(('_chasuble','_dalmatic')) or n in ('alb','biretta','mitre','liturgical_shoes','cassock','surplice')]
BOOKS=['missal_1962','modern_missal','field_guide','prayer_book','stations_book']


# The pack's shared Patchouli-style book renderer (tools/art/bookart.py at the workspace root).
for _parent in Path(__file__).resolve().parents:
    if (_parent / 'tools/art/bookart.py').is_file():
        sys.path.insert(0, str(_parent / 'tools/art'))
        break
import bookart

BOOK_LOOKS = {
    # name: (cover, emblem, emblem colour, ribbon, gilt page edges)
    'field_guide': ((40, 67, 94), 'cross', bookart.GOLD, (177, 57, 68), False),
    'missal_1962': ((150, 45, 62), 'chalice', bookart.GOLD, (238, 238, 226), True),
    'modern_missal': ((48, 104, 83), 'chi_rho', bookart.GOLD, bookart.GOLD, False),
    'prayer_book': ((70, 102, 139), 'sacred_heart', (214, 64, 72), bookart.GOLD, False),
    'stations_book': ((93, 54, 105), 'calvary', bookart.GOLD, (177, 57, 68), False),
}


def book_art(name):
    cover, emblem, emblem_color, ribbon, gilt = BOOK_LOOKS[name]
    return bookart.book(32, cover=cover, emblem=emblem, emblem_color=emblem_color, ribbon=ribbon, edge_gilt=gilt)

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n')

def save(path, image):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)

def rgb(color):
    return tuple(bytes.fromhex(color.lstrip('#')))

def shade(color, amount):
    return tuple(max(0, min(255, c + amount)) for c in rgb(color))

def cross(draw, x, y, width, height, color, thickness=2):
    left = x + width//2-thickness//2
    draw.rectangle((left, y, left+thickness-1, y+height-1), fill=color)
    draw.rectangle((x, y+height//3, x+width-1, y+height//3+thickness-1), fill=color)

def tile(name, color, style='metal'):
    rng = random.Random(name)
    im = Image.new('RGBA', (32, 32), color)
    d = ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):
            n = rng.randrange(-8, 9)
            if style == 'wood': n += (x % 7 == 0)*-25
            im.putpixel((x,y), (*shade(color, n), 255))
    if style == 'metal':
        d.rectangle((0,0,31,31), outline=shade(color,-30), width=2)
        d.line((2,2,29,2), fill=shade(color,35))
        d.line((2,3,2,28), fill=shade(color,25))
    elif style == 'linen':
        for y in range(0,32,4): d.line((0,y,31,y), fill=shade(color,-10))
        for x in range(0,32,4): d.line((x,0,x,31), fill=shade(color,5))
    save(ASSETS / f'textures/block/{name}.png', im)
    return im

def textures():
    for name, color, style in [('gold', '#c7a34b','metal'), ('quartz','#d9d5c9','stone'),
                              ('wood','#573a2b','wood'), ('linen','#eee8d6','linen'),
                              ('red_cloth','#962d3e','linen'), ('wax','#f3deb2','linen'),
                              ('skin','#c99d72','stone'), ('silver','#b9c2ce','metal'),
                              ('water','#4885b4','stone')]:
        tile(name,color,style)
    im = tile('tabernacle_front','#c7a34b')
    d = ImageDraw.Draw(im)
    d.rectangle((4,3,27,29), outline=DARK_GOLD, width=2)
    d.rectangle((7,6,24,26), outline='#f7d680')
    cross(d,10,8,12,16,'#fff2bf',3)
    d.rectangle((25,17,26,19),fill=INK)
    save(ASSETS/'textures/block/tabernacle_front.png',im)
    im = tile('altar_front','#eee8d6','linen')
    d = ImageDraw.Draw(im)
    d.rectangle((0,27,31,31),fill=GOLD)
    cross(d,12,8,8,13,'#bc9748',2)
    save(ASSETS/'textures/block/altar_front.png',im)
    for color,value in COLORS.items():
        im=tile(color+'_cloth',value,'linen')
        d=ImageDraw.Draw(im)
        d.rectangle((0,27,31,31),fill=GOLD)
        cross(d,12,7,8,14,GOLD,2)
        save(ASSETS/f'textures/block/{color}_cloth.png',im)
    tile('leaves','#35613d','stone')
    tile('blue_cloth','#45759e','linen')
    tile('brown_cloth','#77584a','linen')
    romans=['I','II','III','IV','V','VI','VII','VIII','IX','X','XI','XII','XIII','XIV']
    font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',10)
    for i,roman in enumerate(romans,1):
        im=tile('station_'+str(i),'#e5d5ab','linen')
        d=ImageDraw.Draw(im)
        d.rectangle((2,2,29,29),outline=DARK_GOLD,width=2)
        cross(d,11,6,10,12,'#865c3a',2)
        bounds=d.textbbox((0,0),roman,font=font)
        d.text((16-(bounds[2]-bounds[0])/2,18),roman,fill=INK,font=font)
        save(ASSETS/f'textures/block/station_{i}.png',im)
    im = Image.new('RGBA',(32,32),'#294473')
    d = ImageDraw.Draw(im)
    for y in range(32):
        for x in range(32):
            cell = ((x//5)+(y//5))%4
            im.putpixel((x,y), [(45,78,129,220),(63,113,145,220),(143,49,75,220),(50,135,142,220)][cell])
    d.rectangle((0,0,31,31),outline='#242734',width=2)
    d.line((0,16,31,16), fill='#252632', width=1)
    d.line((16,0,16,31), fill='#252632', width=1)
    d.ellipse((6,4,25,25),outline='#c4a565',width=2)
    cross(d,10,7,12,19,'#f1d88b',3)
    save(ASSETS/'textures/block/cross_stained_glass.png',im)

def icon(name):
    im = Image.new('RGBA',(32,32))
    d = ImageDraw.Draw(im)
    if name in expanded.NEW_ITEMS:
        expanded.icon(sys.modules[__name__], name); return
    if name in BOOKS:
        # Patchouli-style three-quarter books from the pack's shared book art, each with its own emblem.
        im = book_art(name)
    elif name == 'holy_water':
        d.rectangle((12,3,19,6),fill=INK)
        d.rectangle((13,3,18,5),fill='#a77843')
        d.rectangle((12,6,19,11),fill='#b0d8e2')
        d.rounded_rectangle((7,10,24,28),radius=4,fill='#b0d8e2',outline=INK,width=2)
        d.rectangle((9,17,22,25),fill='#467daa')
        d.rectangle((10,12,11,23),fill='#eef9e3')
        d.rectangle((14,15,19,23),fill='#eee3c5')
        cross(d,14,16,6,7,GOLD,2)
    elif name.endswith('_medal'):
        d.line((8,3,12,12,20,12,24,3),fill=GOLD,width=2)
        d.ellipse((8,11,24,29),fill=DARK_GOLD,outline=INK,width=1)
        d.ellipse((9,12,23,27),fill=GOLD,outline='#f4d57f',width=1)
        if name == 'st_benedict_medal': cross(d,12,15,8,10,'#75502d',2)
        elif name == 'miraculous_medal':
            d.polygon([(16,14),(12,23),(20,23)],fill='#fbecd0')
            d.ellipse((14,14,17,17),fill='#fbecd0')
            d.line((11,25,21,25),fill='#8a672f')
        else:
            d.line((16,14,16,24), fill='#f8eacd', width=2)
            d.line((11,17,21,17), fill='#6b5430', width=2)
            d.polygon([(16,18),(10,15),(11,21)],fill='#f8eacd')
            d.polygon([(17,18),(23,15),(22,21)],fill='#f8eacd')
    elif name == 'rosary':
        coords=[(12,4),(17,3),(22,6),(24,11),(23,16),(19,20),(14,21),(9,17),(7,12),(8,7)]
        d.line(coords+[coords[0]],fill=DARK_GOLD,width=1)
        for x,y in coords: d.ellipse((x-2,y-2,x+1,y+1),fill='#863649',outline=GOLD)
        d.line((15,22,15,25),fill=GOLD,width=1)
        cross(d,12,24,7,8,GOLD,2)
    elif name == 'thurible':
        d.line((15,2,15,7),fill='#dee0d9',width=2)
        d.line((15,6,8,22),fill='#b4af9d',width=1)
        d.line((16,6,24,22),fill='#b4af9d',width=1)
        d.line((16,8,16,20),fill='#b4af9d',width=1)
        d.polygon([(7,21),(25,21),(22,28),(10,28)],fill=GOLD,outline=INK)
        d.polygon([(9,20),(16,16),(23,20)],fill='#edca74',outline=INK)
        for x in (12,16,20): d.rectangle((x,23,x+1,24),fill=DARK_GOLD)
        d.line((12,12,10,9,12,6),fill='#b9bcc6',width=2)
        d.line((21,14,23,10,21,7),fill='#b9bcc6',width=2)
    elif name == 'incense':
        d.polygon([(6,11),(24,9),(28,24),(9,28),(5,22)],fill=INK)
        d.polygon([(8,12),(23,11),(25,23),(10,25)],fill='#bea678')
        d.rectangle((9,6,23,13),fill='#9e8056')
        d.line((8,13,23,11),fill='#ead0a0',width=2)
        for x,y in [(12,18),(18,15),(21,20),(15,22)]: d.rectangle((x,y,x+2,y+2),fill='#84582c')
    elif name.endswith(('_chasuble','_dalmatic')) or name in ('alb','cassock','surplice'):
        color = COLORS.get(name.removesuffix('_chasuble').removesuffix('_dalmatic'), '#28232e' if name=='cassock' else '#eee7cf')
        d.polygon([(11,5),(20,5),(25,10),(28,27),(23,29),(8,29),(3,27),(6,10)],fill=INK)
        d.polygon([(11,6),(20,6),(24,11),(26,26),(22,27),(9,27),(5,26),(8,11)],fill=color)
        d.rectangle((13,6,18,10),fill='#6a553e')
        d.line((8,14,7,24),fill=shade(color,-22),width=2)
        d.line((23,14,24,24),fill=shade(color,-22),width=2)
        d.rectangle((14,11,17,25),fill=GOLD)
        d.rectangle((10,13,21,15),fill=GOLD)
        d.line((8,26,23,26),fill='#eaca75',width=1)
        if name.endswith('_dalmatic'):
            d.rectangle((2,10,7,17),fill=color,outline=INK)
            d.rectangle((24,10,29,17),fill=color,outline=INK)
            d.line((10,11,10,24),fill=GOLD,width=2)
            d.line((21,11,21,24),fill=GOLD,width=2)
            d.rectangle((12,14,19,19),fill=color)
        if name=='cassock':
            d.rectangle((13,10,18,25),fill=color)
            for y in range(12,25,4): d.point((16,y),fill='#c8baa0')
        if name=='surplice':
            d.rectangle((12,12,19,22),fill=color)
            for x in range(7,25,3): d.point((x,25),fill='#8b887e')
    elif name.endswith('_altar_cloth'):
        color=COLORS[name.removesuffix('_altar_cloth')]
        d.polygon([(4,7),(24,5),(28,11),(25,27),(8,29),(3,24)],fill=INK)
        d.polygon([(6,9),(23,7),(26,12),(23,25),(9,27),(5,23)],fill=color)
        d.line((7,23,24,21),fill=GOLD,width=2)
        cross(d,12,11,8,10,GOLD,2)
    elif name=='aspergillum':
        d.line((10,29,20,11),fill=INK,width=6)
        d.line((10,29,20,11),fill=DARK_GOLD,width=4)
        d.ellipse((17,3,28,14),fill='#bfc9d4',outline=INK,width=1)
        for x,y in [(20,6),(25,6),(22,10)]: d.point((x,y),fill='#3d5c70')
        d.line((7,8,7,13),fill='#61b8da',width=2)
        d.line((11,3,11,7),fill='#61b8da',width=2)
    elif name=='altar_bell':
        d.ellipse((4,25,27,29),fill=INK)
        d.polygon([(6,25),(11,13),(21,13),(26,25)],fill=GOLD,outline=INK)
        d.rectangle((14,4,18,14),fill=DARK_GOLD)
        d.ellipse((12,2,20,8),fill=GOLD,outline=INK)
        d.line((9,23,23,23),fill='#f3d786',width=2)
    elif name=='processional_cross':
        d.rectangle((14,3,18,30),fill=INK)
        d.rectangle((15,4,17,29),fill=GOLD)
        d.rectangle((5,8,27,12),fill=INK)
        d.rectangle((6,9,26,11),fill=GOLD)
        d.rectangle((15,12,17,22),fill='#a17a4e')
    elif name=='scapular':
        d.line((8,12,12,3,21,3,25,12),fill='#a88155',width=2)
        d.rectangle((4,12,15,27),fill=INK)
        d.rectangle((6,14,13,25),fill='#86553e')
        cross(d,7,17,5,7,'#ded0ad',1)
        d.rectangle((20,13,29,27),fill=INK)
        d.rectangle((22,15,27,25),fill='#a16d4b')
    elif name=='beeswax':
        d.polygon([(6,9),(22,6),(28,12),(25,25),(10,28),(4,21)],fill=INK)
        d.polygon([(8,10),(21,8),(25,13),(23,23),(11,25),(7,20)],fill='#e5c369')
        for x,y in [(11,14),(18,12),(20,20),(13,21)]: d.ellipse((x,y,x+3,y+3),fill='#b68a44')
    elif name=='altar_bread':
        d.ellipse((4,4,28,28),fill='#bcb298',outline=INK)
        d.ellipse((6,5,26,26),fill='#eee7d6')
        cross(d,11,9,10,14,'#d0c1a0',2)
    elif name == 'biretta':
        d.polygon([(5,14),(11,9),(21,9),(27,14),(26,24),(7,24)],fill=INK)
        d.rectangle((9,12,23,22),fill='#34313d')
        d.line((10,10,10,21),fill='#59515d',width=2)
        d.line((22,10,22,21),fill='#59515d',width=2)
        d.rectangle((14,6,18,10),fill='#262330')
        d.line((8,23,24,23),fill='#ad6a52')
    elif name == 'mitre':
        d.polygon([(6,27),(6,13),(15,3),(17,3),(26,13),(26,27)],fill=INK)
        d.polygon([(8,25),(8,14),(16,5),(24,14),(24,25)],fill='#eee6c9')
        d.rectangle((8,22,24,25),fill=GOLD)
        cross(d,12,10,8,11,GOLD,2)
        d.rectangle((9,27,12,31),fill=GOLD)
        d.rectangle((20,27,23,31),fill=GOLD)
    elif name == 'liturgical_shoes':
        d.rounded_rectangle((3,12,14,28),radius=2,fill=INK)
        d.rounded_rectangle((18,12,29,28),radius=2,fill=INK)
        d.rectangle((5,14,12,24),fill='#4d3a36')
        d.rectangle((20,14,27,24),fill='#4d3a36')
        d.line((5,26,12,26),fill='#80624b')
        d.line((20,26,27,26),fill='#80624b')
    else:
        raise ValueError(name)
    save(ASSETS / f'textures/item/{name}.png', im)

def cuboid(a, b, texture, faces=None, rotation=None):
    element = {'from': a, 'to': b, 'faces': {side: {'texture': '#'+texture} for side in ('north','south','east','west','up','down')}}
    if faces:
        for side, t in faces.items(): element['faces'][side] = {'texture':'#'+t, 'uv':[0,0,16,16]}
    if rotation: element['rotation'] = rotation
    return element

DISPLAY = {'gui': {'rotation':[30,225,0],'translation':[0,0,0],'scale':[.65,.65,.65]},
           'ground': {'translation':[0,3,0],'scale':[.4,.4,.4]},
           'fixed': {'rotation':[0,180,0],'scale':[.5,.5,.5]}}

def model(name, elements, extra=None):
    tex = {t: 'catholicraft:block/'+t for t in ['gold','wood','quartz','linen','red_cloth','wax','skin','silver','water','tabernacle_front','altar_front','leaves','blue_cloth','brown_cloth',*[c+'_cloth' for c in COLORS],*[f'station_{i}' for i in range(1,15)]]}
    tex['particle'] = tex['gold'] if name.split('_')[0] in ('tabernacle','chalice','ciborium','monstrance','church') else tex['quartz']
    result = {'parent':'minecraft:block/block','textures':tex,'elements':elements,'display':DISPLAY}
    if extra: result.update(extra)
    write(ASSETS / f'models/block/{name}.json', result)

def workbench():
    """St. Joseph's Workbench: a dark-oak carpenter's bench with his square and saw on top and a carved lily in front."""
    oak, oak_dark, oak_light, gold, steel, lily = '#4a3018', '#2f1d0e', '#6b4626', '#d9b35b', '#b9bec6', '#f3efe2'
    def planks(seed):
        rng = random.Random(seed)
        im = Image.new('RGBA', (16, 16))
        for y in range(16):
            for x in range(16):
                n = rng.randrange(-6, 7) + (-22 if y % 4 == 3 else 0) + (-14 if (x + (y // 4) * 5) % 16 == 0 else 0)
                im.putpixel((x, y), (*shade(oak, n), 255))
        return im
    top = planks('workbench_top'); d = ImageDraw.Draw(top)
    d.rectangle((0, 0, 15, 15), outline=rgb(oak_dark))
    d.line((2, 3, 9, 3), fill=rgb(gold)); d.line((2, 3, 2, 10), fill=rgb(gold))          # carpenter's square
    d.line((3, 4, 9, 4), fill=shade(gold, -50)); d.line((3, 4, 3, 10), fill=shade(gold, -50))
    for i in range(6): d.point((7 + i, 12 - i // 2), fill=rgb(steel))                     # saw blade
    d.line((6, 13, 7, 12), fill=rgb(oak_light)); d.point((5, 13), fill=rgb(oak_light))
    for x, y in ((11, 9), (12, 10), (13, 8)): d.point((x, y), fill=shade(oak_light, 40))  # shavings
    save(ASSETS/'textures/block/st_josephs_workbench_top.png', top)
    side = planks('workbench_side'); d = ImageDraw.Draw(side)
    d.rectangle((0, 0, 15, 2), fill=rgb(oak_dark)); d.rectangle((0, 13, 2, 15), fill=rgb(oak_dark)); d.rectangle((13, 13, 15, 15), fill=rgb(oak_dark))
    d.rectangle((3, 5, 12, 10), outline=rgb(oak_dark)); d.rectangle((7, 7, 8, 8), fill=rgb(gold))   # drawer and pull
    d.rectangle((3, 13, 12, 15), fill=(26, 15, 7, 255))  # shadow under the bench
    save(ASSETS/'textures/block/st_josephs_workbench_side.png', side)
    front = planks('workbench_front'); d = ImageDraw.Draw(front)
    d.rectangle((0, 0, 15, 2), fill=rgb(oak_dark)); d.rectangle((0, 13, 2, 15), fill=rgb(oak_dark)); d.rectangle((13, 13, 15, 15), fill=rgb(oak_dark))
    d.rectangle((3, 13, 12, 15), fill=(26, 15, 7, 255))  # shadow under the bench
    # A white lily, St. Joseph's flower: stem, leaves and a three-petal bloom.
    d.line((8, 6, 8, 12), fill=(86, 140, 70, 255)); d.point((7, 10), fill=(86, 140, 70, 255)); d.point((9, 9), fill=(86, 140, 70, 255))
    for x, y in ((8, 3), (8, 4), (7, 5), (9, 5), (6, 4), (10, 4), (8, 5)): d.point((x, y), fill=rgb(lily))
    d.point((8, 5), fill=rgb(gold))
    save(ASSETS/'textures/block/st_josephs_workbench_front.png', front)
    write(ASSETS/'models/block/st_josephs_workbench.json', {'parent': 'minecraft:block/orientable_with_bottom', 'textures': {
        'top': 'catholicraft:block/st_josephs_workbench_top', 'front': 'catholicraft:block/st_josephs_workbench_front',
        'side': 'catholicraft:block/st_josephs_workbench_side', 'bottom': 'minecraft:block/dark_oak_planks',
        'particle': 'catholicraft:block/st_josephs_workbench_side'}})

def models():
    c = cuboid
    workbench()
    base = [c([2,0,2],[14,2,14],'gold'), c([3,2,3],[13,12,13],'gold'), c([2,12,2],[14,14,14],'gold'),
            c([7,14,7],[9,16,9],'gold'),c([5,14.5,7],[11,15.5,9],'gold')]
    model('tabernacle', base+[c([3,2,2.5],[13,12,3],'gold',{'north':'tabernacle_front'})])
    model('tabernacle_open', base+[c([3,2,2],[8,12,2.5],'gold',rotation={'origin':[3,7,2],'axis':'y','angle':-45}),
                                   c([8,2,2],[13,12,2.5],'gold',rotation={'origin':[13,7,2],'axis':'y','angle':45}),
                                   c([4,3,2.4],[12,11,2.6],'wood')])
    model('altar',[c([0,12,0],[16,15,16],'quartz'),c([0,15,0],[16,16,16],'linen'),
                   c([2,0,2],[5,12,14],'quartz'),c([11,0,2],[14,12,14],'quartz'),
                   c([0,11,.1],[16,15,.4],'linen',{'north':'altar_front'})])
    for index,color in enumerate(COLORS):
        model('altar'+('_'+str(index) if index else ''),[c([0,12,0],[16,15,16],'quartz'),c([0,15,0],[16,16,16],'linen'),
            c([2,0,2],[5,12,14],'quartz'),c([11,0,2],[14,12,14],'quartz'),c([0,3,.1],[16,15,.4],color+'_cloth',{'north':color+'_cloth'})])
    cabinet=[c([1,0,1],[15,2,15],'wood'),c([1,14,1],[15,16,15],'wood'),c([1,2,1],[3,14,15],'wood'),c([13,2,1],[15,14,15],'wood'),
             c([3,2,13],[13,14,15],'wood'),c([3,7,3],[13,8,13],'wood')]
    doors=[c([3,2,1],[8,14,2],'wood'),c([8,2,1],[13,14,2],'wood'),c([6.5,7,.5],[7.5,9,1],'gold'),c([8.5,7,.5],[9.5,9,1],'gold')]
    model('sacristy_cabinet',cabinet+doors)
    model('sacristy_cabinet_open',cabinet+[c([3,2,1],[8,14,2],'wood',rotation={'origin':[3,8,1],'axis':'y','angle':-45}),
        c([8,2,1],[13,14,2],'wood',rotation={'origin':[13,8,1],'axis':'y','angle':45})])
    wreath=[c([2,0,2],[14,3,5],'leaves'),c([2,0,11],[14,3,14],'leaves'),c([2,0,5],[5,3,11],'leaves'),c([11,0,5],[14,3,11],'leaves')]
    for lit in range(5):
        candles=[]
        for i,(x,z) in enumerate([(4,4),(10,4),(4,10),(10,10)]):
            candles += [c([x,3,z],[x+2,11,z+2],'rose_cloth' if i==2 else 'violet_cloth')]
            wick=c([x+.75,11,z+.75],[x+1.25,12,z+1.25],'gold' if i<lit else 'wood')
            if i<lit: wick['light_emission']=15
            candles.append(wick)
        model('advent_wreath'+('_'+str(lit) if lit else ''),wreath+candles)
    rack=[c([1,0,2],[3,10,14],'gold'),c([13,0,2],[15,10,14],'gold'),c([0,8,1],[16,10,15],'gold')]
    for lit in (False,True):
        candles=[]
        for x in (3,7,11):
            for z in (3,9): candles.append(c([x,10,z],[x+2,14,z+2],'wax'))
        model('votive_rack'+('_lit' if lit else ''),rack+candles)
    for station in range(1,15):
        model('stations_of_the_cross'+('_'+str(station) if station>1 else ''),[c([1,0,6],[15,16,10],'wood'),c([2,1,5.8],[14,15,6],'linen',{'north':'station_'+str(station)})])
    burner=[c([3,0,3],[13,3,13],'gold'),c([5,3,5],[11,7,11],'gold'),c([2,7,2],[14,9,14],'gold'),
            c([2,9,2],[14,12,3],'gold'),c([2,9,13],[14,12,14],'gold'),c([2,9,3],[3,12,13],'gold'),c([13,9,3],[14,12,13],'gold')]
    model('incense_burner',burner+[c([3,9,3],[13,10,13],'wood')])
    coal=c([3,9,3],[13,10,13],'red_cloth'); coal['light_emission']=6
    model('incense_burner_lit',burner+[coal])
    model('credence_table',[c([0,11,0],[16,14,16],'wood'),c([1,0,1],[3,11,3],'wood'),c([13,0,1],[15,11,3],'wood'),c([1,0,13],[3,11,15],'wood'),c([13,0,13],[15,11,15],'wood')])
    model('ambo',[c([2,0,2],[14,2,14],'wood'),c([4,2,6],[12,12,14],'wood'),c([1,12,3],[15,15,15],'wood'),c([3,15,5],[13,15.5,13],'linen'),c([6,5,5.8],[10,10,6],'gold')])
    model('crucifix',[c([6.5,0,7],[9.5,16,9],'wood'),c([1,10,7],[15,13,9],'wood'),
                      c([7,10,6],[9,13,7],'skin'),c([7,13,6],[9,15,8],'skin'),
                      c([3,11,6],[13,12,7],'skin'),c([7,6,6],[9,10,7],'linen'),c([7.5,2,6],[8.5,6,7],'skin')])
    fontbase = [c([3,0,3],[13,2,13],'quartz'),c([6,2,6],[10,10,10],'quartz'),c([2,10,2],[14,11,14],'quartz'),
                c([2,11,2],[14,14,4],'quartz'),c([2,11,12],[14,14,14],'quartz'),
                c([2,11,4],[4,14,12],'quartz'),c([12,11,4],[14,14,12],'quartz')]
    for name in ('holy_water_font','baptismal_font'):
        for level in range(4):
            model(name+('_'+str(level) if level else ''),fontbase+([c([4,11,4],[12,11+level*.65,12],'water')] if level else []))
    candlebase = [c([4,0,4],[12,2,12],'gold'),c([7,2,7],[9,5,9],'gold'),c([6,5,6],[10,14,10],'wax'),c([7.5,14,7.5],[8.5,15,8.5],'wood')]
    model('altar_candle',candlebase)
    model('altar_candle_lit',candlebase,{'textures':{**{t:'catholicraft:block/'+t for t in ('gold','wax','wood')},'particle':'catholicraft:block/wax'}})
    lampbase=[c([4,0,4],[12,2,12],'gold'),c([5,2,5],[11,12,11],'red_cloth'),c([4,12,4],[12,14,12],'gold'),
              c([5,2,5],[6,12,6],'gold'),c([10,2,5],[11,12,6],'gold'),c([5,2,10],[6,12,11],'gold'),c([10,2,10],[11,12,11],'gold')]
    model('sanctuary_lamp',lampbase)
    model('sanctuary_lamp_lit',lampbase+[c([7,3,7],[9,11,9],'wax')])
    model('church_bell',[c([0,0,5],[3,16,11],'wood'),c([13,0,5],[16,16,11],'wood'),c([0,14,5],[16,16,11],'wood'),
                         c([7,12,7],[9,14,9],'gold'),c([5,7,5],[11,12,11],'gold'),c([4,4,4],[12,7,12],'gold'),c([7,3,7],[9,4,9],'wood')])
    model('kneeler',[c([0,2,3],[16,5,8],'red_cloth'),c([0,12,12],[16,15,16],'wood'),c([1,0,12],[3,12,15],'wood'),c([13,0,12],[15,12,15],'wood')])
    model('pew',[c([0,5,2],[16,8,14],'wood'),c([0,8,12],[16,16,15],'wood'),c([1,0,3],[3,5,13],'wood'),c([13,0,3],[15,5,13],'wood')])
    vesselbase=[c([4,0,4],[12,2,12],'gold'),c([7,2,7],[9,7,9],'gold')]
    cup=[c([4,7,4],[12,8,12],'gold'),c([4,8,4],[12,11,5],'gold'),c([4,8,11],[12,11,12],'gold'),c([4,8,5],[5,11,11],'gold'),c([11,8,5],[12,11,11],'gold')]
    model('chalice',vesselbase+cup)
    model('ciborium',vesselbase+cup+[c([3,11,3],[13,12,13],'gold'),c([7,12,7],[9,16,9],'gold'),c([5,14,7],[11,15,9],'gold')])
    model('monstrance',[c([4,0,4],[12,2,12],'gold'),c([7,2,7],[9,6,9],'gold'),c([4,6,7],[12,14,9],'gold'),
                        c([2,9,7],[14,11,9],'gold'),c([7,5,7],[9,15,9],'gold'),c([6,8,6.5],[10,12,7],'linen'),
                        c([7,14,7],[9,16,9],'gold')])
    expanded.statues(sys.modules[__name__]); expanded.icons(sys.modules[__name__])
    write(ASSETS/'models/block/cross_stained_glass.json', {'parent':'minecraft:block/cube_all','textures':{'all':{'sprite':'catholicraft:block/cross_stained_glass','force_translucent':True}}})
    for name in BLOCKS:
        variants = {}
        rotations = {'north':0,'east':90,'south':180,'west':270}
        if name == 'cross_stained_glass': variants[''] = {'model':'catholicraft:block/'+name}
        else:
            for facing, rotation in rotations.items():
                if name in ('tabernacle','sacristy_cabinet'): states=[('open=false',name),('open=true',name+'_open')]
                elif name=='altar': states=[('cloth='+str(n),name+('_'+str(n) if n else '')) for n in range(6)]
                elif name=='advent_wreath': states=[('candles='+str(n),name+('_'+str(n) if n else '')) for n in range(5)]
                elif name=='stations_of_the_cross': states=[('station='+str(n),name+('_'+str(n) if n>1 else '')) for n in range(1,15)]
                elif name in ('holy_water_font','baptismal_font'): states=[('water='+str(n),name+('_'+str(n) if n else '')) for n in range(4)]
                elif name in ('altar_candle','sanctuary_lamp','votive_rack','incense_burner'): states=[('lit=false',name),('lit=true',name+'_lit')]
                else: states=[('',name)]
                for state, target in states:
                    variants['facing='+facing+(','+state if state else '')]={'model':'catholicraft:block/'+target,'y':rotation}
        write(ASSETS/f'blockstates/{name}.json',{'variants':variants})
        write(ASSETS/f'models/item/{name}.json',{'parent':'catholicraft:block/'+name})
        write(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'catholicraft:item/'+name}})
    for name in ITEMS:
        icon(name)
        write(ASSETS/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'catholicraft:item/'+name}})
        write(ASSETS/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'catholicraft:item/'+name}})

def vestments():
    names = VESTMENTS
    for name in names:
        im=Image.new('RGBA',(64,64))
        d=ImageDraw.Draw(im)
        color=COLORS.get(name.removesuffix('_chasuble').removesuffix('_dalmatic'),'#28232e' if name=='cassock' else '#eee7cf')
        if name.endswith(('_chasuble','_dalmatic')) or name in ('cassock','surplice'):
            # Classic humanoid UV: body front 20,20..28,32; back 32,20..40,32.
            d.rectangle((16,16,39,31),fill=color)
            d.rectangle((40,16,55,31),fill=color)
            d.rectangle((32,48,47,63),fill=color)
            for x in (23,35):
                d.rectangle((x,20,x+1,31),fill=GOLD)
                d.rectangle((x-3,23,x+4,24),fill=GOLD)
            d.rectangle((16,30,39,31),fill=GOLD)
            # Sleeves show the matching fabric and a gold cuff.
            d.rectangle((40,30,55,31),fill=GOLD)
            d.rectangle((32,62,47,63),fill=GOLD)
            if name.endswith('_dalmatic'):
                for start in (20,32):
                    d.rectangle((start+1,20,start+2,31),fill=GOLD)
                    d.rectangle((start+5,20,start+6,31),fill=GOLD)
                    d.rectangle((start+3,20,start+4,31),fill=color)
            if name=='cassock':
                d.rectangle((16,16,39,31),fill=color)
                d.rectangle((40,16,55,31),fill=color)
                d.rectangle((32,48,47,63),fill=color)
                for y in (21,24,27,30): d.point((24,y),fill='#a59b89')
            if name=='surplice':
                d.rectangle((16,16,39,31),fill=color)
                for x in range(16,40,2): d.point((x,30),fill='#aaa69b')
        elif name == 'alb':
            d.rectangle((16,16,39,31),fill=color)
            d.rectangle((0,16,15,31),fill=color)
            d.rectangle((16,48,31,63),fill=color)
            d.rectangle((16,19,39,20),fill=GOLD)
            d.rectangle((0,30,15,31),fill='#cbbfa0')
            d.rectangle((16,62,31,63),fill='#cbbfa0')
        elif name in ('biretta','mitre'):
            color='#27232e' if name=='biretta' else '#eee7cf'
            d.rectangle((0,0,31,15),fill=color)
            if name=='mitre':
                d.rectangle((0,14,31,15),fill=GOLD)
                cross(d,10,8,5,6,GOLD,1)
                cross(d,26,8,5,6,GOLD,1)
            else:
                d.line((8,0,8,15),fill='#4b3b4b')
                d.line((24,0,24,15),fill='#4b3b4b')
        else:
            d.rectangle((0,24,15,31),fill='#3d2d2a')
            d.rectangle((16,56,31,63),fill='#3d2d2a')
            d.rectangle((0,30,15,31),fill='#795e48')
            d.rectangle((16,62,31,63),fill='#795e48')
        layer='humanoid_leggings' if name=='alb' else 'humanoid'
        save(ASSETS/f'textures/entity/equipment/{layer}/{name}.png',im)
        layers={layer:[{'texture':'catholicraft:'+name}]}
        if layer=='humanoid':
            save(ASSETS/f'textures/entity/equipment/humanoid_baby/{name}.png',im)
            layers['humanoid_baby']=[{'texture':'catholicraft:'+name}]
        write(ASSETS/f'equipment/{name}.json',{'layers':layers})

# Made at an ordinary crafting table; every other CatholiCraft recipe is made at St. Joseph's Workbench, so none of
# them can collide with vanilla or other mods' recipes (the aspergillum once shadowed the iron shovel).
CRAFTING_TABLE = ('field_guide', 'st_josephs_workbench')

def recipe_file(name, recipe):
    if name not in CRAFTING_TABLE: recipe = {'type': 'catholicraft:workbench', 'recipe': recipe}
    write(DATA/f'recipe/{name}.json', recipe)

def shaped(name, pattern, key, count=1):
    ingredient = lambda value: value if ':' in value else 'minecraft:'+value
    recipe_file(name, {'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,
          'key':{k:ingredient(v) for k,v in key.items()},'result':{'id':'catholicraft:'+name,'count':count}})

def shapeless(name, ingredients, count=1):
    recipe_file(name, {'type':'minecraft:crafting_shapeless','category':'misc',
          'ingredients':[s if ':' in s else 'minecraft:'+s for s in ingredients],
          'result':{'id':'catholicraft:'+name,'count':count}})

def recipes():
    shaped('st_josephs_workbench',[' B ','NCN','PPP'],{'B':'book','N':'gold_nugget','C':'crafting_table','P':'dark_oak_planks'})
    shaped('tabernacle',['GGG','GCG','GGG'],{'G':'gold_ingot','C':'chest'})
    shaped('altar',['WWW','QQQ','Q Q'],{'W':'white_wool','Q':'quartz_block'})
    shaped('crucifix',[' N ','SNS',' S '],{'N':'gold_nugget','S':'stick'})
    shaped('holy_water_font',['Q Q',' Q ','QQQ'],{'Q':'quartz_block'})
    shaped('baptismal_font',['Q Q','QCQ','QQQ'],{'Q':'quartz_block','C':'cauldron'})
    shaped('altar_candle',[' C ',' N ','GGG'],{'C':'candle','N':'gold_nugget','G':'gold_ingot'})
    shaped('sanctuary_lamp',[' G ','GRG',' G '],{'G':'gold_ingot','R':'red_stained_glass'})
    shaped('church_bell',['PPP','PBP','P P'],{'P':'dark_oak_planks','B':'bell'})
    shaped('kneeler',['  P','PPP','WRW'],{'P':'dark_oak_planks','W':'stick','R':'red_wool'},2)
    shaped('pew',['PPP','SSS','P P'],{'P':'dark_oak_planks','S':'dark_oak_slab'},3)
    shaped('chalice',['G G',' G ','GGG'],{'G':'gold_ingot'})
    shapeless('ciborium',['catholicraft:chalice','gold_ingot','gold_nugget'])
    shaped('monstrance',[' G ','GQG',' G '],{'G':'gold_ingot','Q':'quartz'})
    shaped('cross_stained_glass',['BGB','GGG','BGB'],{'B':'blue_stained_glass','G':'gold_nugget'},4)
    shapeless('missal_1962',['book','red_dye','gold_nugget'])
    shapeless('modern_missal',['book','green_dye','gold_nugget'])
    shapeless('field_guide',['book','gold_nugget'])
    # Holy water comes from a font, avoiding a recipe that discards a container.
    shaped('rosary',['SSS','S S',' G '],{'S':'string','G':'gold_nugget'})
    shaped('thurible',[' C ','C C',' G '],{'C':'iron_chain','G':'gold_ingot'})
    shapeless('incense',['charcoal','honeycomb','sugar'],4)
    for name,center in [('st_benedict_medal','stick'),('miraculous_medal','blue_dye'),('st_michael_medal','iron_nugget')]:
        shaped(name,[' S ','GCG',' G '],{'S':'string','G':'gold_nugget','C':center})
    dyes={'white':'white_dye','red':'red_dye','green':'green_dye','violet':'purple_dye','rose':'pink_dye','black':'black_dye'}
    for color,dye in dyes.items():
        shaped(color+'_chasuble',['W W','WGW','WDW'],{'W':'white_wool','G':'gold_nugget','D':dye})
    shaped('alb',['WWW','W W','W W'],{'W':'white_wool'})
    shaped('biretta',[' W ','WWW'],{'W':'black_wool'})
    shaped('mitre',[' G ','WGW','WWW'],{'W':'white_wool','G':'gold_nugget'})
    shaped('liturgical_shoes',['L L','L L'],{'L':'leather'})
    shaped('sacristy_cabinet',['PPP','C C','PPP'],{'P':'dark_oak_planks','C':'chest'})
    shaped('advent_wreath',['LPL','PBP','LRL'],{'L':'spruce_leaves','P':'purple_candle','R':'pink_candle','B':'catholicraft:beeswax'})
    shaped('votive_rack',['WWW','IGI','I I'],{'W':'catholicraft:beeswax','I':'iron_ingot','G':'gold_nugget'})
    shaped('stations_of_the_cross',['PPP','PQP','PPP'],{'P':'dark_oak_planks','Q':'quartz'},4)
    shaped('incense_burner',['G G','GCG',' G '],{'G':'gold_ingot','C':'charcoal'})
    shaped('our_lady_statue',[' Q ','BQB','QQQ'],{'Q':'quartz','B':'blue_dye'})
    shaped('st_joseph_statue',[' Q ','BQB','QQQ'],{'Q':'quartz','B':'brown_dye'})
    shaped('credence_table',['PPP','S S','S S'],{'P':'dark_oak_slab','S':'stick'})
    shaped('ambo',['PPP',' B ','PPP'],{'P':'dark_oak_planks','B':'bookshelf'})
    for color,dye in dyes.items():
        shaped(color+'_dalmatic',['WWW','WGW',' D '],{'W':'white_wool','G':'gold_nugget','D':dye})
        shapeless(color+'_altar_cloth',['white_wool','white_wool','white_wool','gold_nugget',dye])
    shaped('cassock',['B B','BBB','BBB'],{'B':'black_wool'})
    shaped('surplice',['W W','WWW',' S '],{'W':'white_wool','S':'string'})
    shapeless('prayer_book',['book','blue_dye','gold_nugget'])
    shapeless('stations_book',['book','brown_dye','gold_nugget'])
    shaped('aspergillum',[' I ',' S ',' S '],{'I':'iron_ingot','S':'stick'})
    shaped('altar_bell',[' N ','G G','GGG'],{'N':'gold_nugget','G':'gold_ingot'})
    shaped('processional_cross',[' G ','GGG',' S '],{'G':'gold_nugget','S':'stick'})
    shaped('scapular',['SSS','W W'],{'S':'string','W':'brown_wool'})
    shapeless('beeswax',['honeycomb'],2)
    shaped('altar_bread',['WWW'],{'W':'wheat'},6)
    for name in expanded.ART:
        dye={'annunciation_icon':'light_blue_dye','sistine_madonna_icon':'blue_dye','last_supper_icon':'red_dye'}[name]
        shaped(name,['GGG','DPD','GGG'],{'G':'gold_nugget','D':dye,'P':'painting'})
    shapeless('wine_cruet',['glass_bottle','sweet_berries','sugar'])
    for track,dye in [('veni_creator','white_dye'),('veni_sancte','red_dye'),('dies_irae','black_dye')]:
        shapeless('chant_'+track,['copper_ingot','paper',dye,'gold_nugget'])
    for name in BLOCKS:
        if name=='cross_stained_glass':
            conditions=[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]
        else: conditions=[{'condition':'minecraft:survives_explosion'}]
        entry={'type':'minecraft:item','name':'catholicraft:'+name}
        if name in ('altar','stations_of_the_cross'):
            entry['functions']=[{'function':'minecraft:copy_state','block':'catholicraft:'+name,'properties':['cloth' if name=='altar' else 'station']}]
        write(DATA/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry], 'conditions':conditions}]})
    wooden=('crucifix','kneeler','pew','sacristy_cabinet','stations_of_the_cross','credence_table','ambo','st_josephs_workbench')
    pickaxe=[name for name in BLOCKS if name not in (*wooden,'cross_stained_glass','advent_wreath')]
    write(RES/'data/minecraft/tags/block/mineable/pickaxe.json',{'replace':False,'values':['catholicraft:'+n for n in pickaxe]})
    write(RES/'data/minecraft/tags/block/mineable/axe.json',{'replace':False,'values':['catholicraft:'+n for n in wooden]})
    write(RES/'data/minecraft/tags/item/lectern_books.json',{'replace':False,'values':['catholicraft:'+n for n in BOOKS]})
    write(DATA/'tags/item/devotional_medals.json',{'replace':False,'values':['catholicraft:'+n for n in ITEMS if n.endswith('_medal')]})
    # Discover crafting-table recipes through the recipe book when acquiring their first ingredient.
    for old in (DATA/'advancement/recipes').glob('*.json'): old.unlink()
    for file in sorted((DATA/'recipe').glob('*.json')):
        recipe=json.loads(file.read_text())
        if recipe['type']=='catholicraft:workbench': continue
        first=next(iter(recipe['key'].values())) if 'key' in recipe else recipe['ingredients'][0]
        write(DATA/f'advancement/recipes/{file.stem}.json',{
            'parent':'minecraft:recipes/root',
            'criteria':{'has_ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':first}]}},
                        'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipe':'catholicraft:'+file.stem}}},
            'requirements':[['has_ingredient','has_recipe']], 'rewards':{'recipes':['catholicraft:'+file.stem]}})

def translations():
    names={n:n.replace('_',' ').title() for n in ITEMS+BLOCKS}
    names.update({'missal_1962':'1962 Roman Missal','modern_missal':'Modern Roman Missal','st_benedict_medal':'St. Benedict Medal',
                  'st_michael_medal':'St. Michael Medal','cross_stained_glass':'Cross Stained Glass','field_guide':'CatholiCraft Field Guide','st_josephs_workbench':"St. Joseph's Workbench"})
    names.update({'our_lady_statue':'Statue of Our Lady','st_joseph_statue':'Statue of St. Joseph','altar_bread':'Unconsecrated Altar Bread','prayer_book':'Catholic Prayer Book','stations_book':'Stations of the Cross Companion'})
    lang={'itemGroup.catholicraft':'CatholiCraft'}
    for name,title in names.items(): lang[('block' if name in BLOCKS else 'item')+'.catholicraft.'+name]=title
    tips={
        'tabernacle':'Right-click to open nine storage slots.', 'altar':'A linen-covered altar for your chapel.',
        'crucifix':'An altar cross depicting Christ crucified.', 'holy_water_font':'Water bucket: refill. Glass bottle: collect holy water.',
        'baptismal_font':'Water bucket: refill. Glass bottle: collect holy water.', 'altar_candle':'Flint and steel: light. Empty hand: extinguish.',
        'sanctuary_lamp':'Place near a tabernacle. Flint and steel lights it.', 'church_bell':'Right-click or use redstone to ring.',
        'kneeler':'A cushioned place for prayer.', 'pew':'A wooden pew for the nave.', 'chalice':'A placeable golden liturgical vessel.',
        'ciborium':'A placeable covered liturgical vessel.', 'monstrance':'A placeable golden monstrance.', 'cross_stained_glass':'A luminous cross in colored glass. Silk Touch retrieves it.',
        'missal_1962':'Read an original companion to the 1962 Roman Rite.', 'modern_missal':'Read an original companion to the modern Roman Rite.',
        'holy_water':'Use to extinguish yourself and remove four harmful effects.', 'rosary':'Use: next mystery. Sneak-use: switch mystery sets.',
        'thurible':'Use with incense in your inventory to release smoke.', 'incense':'Fuel for the thurible.',
        'st_benedict_medal':'A devotional medal with the Cross of St. Benedict.', 'miraculous_medal':'A Marian devotional medal.',
        'st_michael_medal':'A devotional medal honoring St. Michael the Archangel.', 'vestment':'Right-click to equip. Liturgical clothing.'}
    tips.update({'st_josephs_workbench':'Every CatholiCraft item is made here, not at a crafting table.',
        'sacristy_cabinet':'Right-click to open 27 storage slots.','advent_wreath':'Flint and steel: light one candle. Empty hand: extinguish one.',
        'votive_rack':'Flint and steel: light. Empty hand: extinguish.','stations_of_the_cross':'Use: next station. Sneak-use: previous station.',
        'incense_burner':'Use incense to add one minute of fragrant smoke.','our_lady_statue':'A placeable devotional statue of Our Lady.',
        'st_joseph_statue':'A placeable devotional statue of St. Joseph.','credence_table':'A side table for chapel vessels.','ambo':'A wooden stand for your chapel.',
        'altar_cloth':'Use on an altar to change its frontal. The top linen stays white.','aspergillum':'Refill at a font. Use to sprinkle nearby players.',
        'altar_bell':'Use for a gentle altar-bell chime.','processional_cross':'A processional cross for the off hand.','scapular':'A brown devotional scapular for the off hand.',
        'beeswax':'Crafted from honeycomb; used in the wreath and votive rack.','altar_bread':'Unconsecrated bread for a chapel display.'})
    tips['pew']='Use an empty hand to sit. Sneak to dismount.'
    for key,value in tips.items(): lang['tooltip.catholicraft.'+key]=value
    lang.update({'message.catholicraft.empty_font':'The font is empty. Refill it with a water bucket.',
                 'message.catholicraft.font_level':'Holy water: %s / 3 charges. Use a glass bottle to collect.',
                 'message.catholicraft.need_incense':'Carry incense to use the thurible.',
                 'message.catholicraft.rosary':'%s mystery %s: %s', 'message.catholicraft.kit':'Your chapel kit is ready.',
                 'message.catholicraft.chapel_blocked':'Chapel area is obstructed at %s, %s, %s. Nothing was changed.',
                 'message.catholicraft.chapel_built':'Chapel built at %s, %s, %s. Enter from the west.'})
    lang.update({'message.catholicraft.aspergillum_charges':'Aspergillum: %s / 8 uses.',
        'message.catholicraft.empty_aspergillum':'The aspergillum is empty. Refill it at a font.',
        'message.catholicraft.burner_full':'The incense burner already holds five minutes of fuel.',
        'message.catholicraft.burner_time':'Incense: %s seconds remaining. Add incense to refill.',
        'message.catholicraft.station':'Station %s: %s','message.catholicraft.pew_occupied':'Someone is already sitting here.',
        'message.catholicraft.pew_sit':'Seated. Sneak to stand up.'})
    lang['container.catholicraft.st_josephs_workbench']="St. Joseph's Workbench"
    write(ASSETS/'lang/en_us.json',lang)
    write(ROOT/'content.json',{'minecraft':'26.2','loader':'NeoForge 26.2.0.90','blocks':BLOCKS,'items':ITEMS,
                             'interaction_only_items':['holy_water'],'vestments':VESTMENTS,'books':BOOKS})

def preview():
    names=ITEMS
    atlas=Image.new('RGB',(840,((len(names)+6)//7)*134),'#171c26')
    d=ImageDraw.Draw(atlas)
    for i,name in enumerate(names):
        x,y=(i%7)*120,(i//7)*134
        d.rounded_rectangle((x+4,y+4,x+115,y+127),radius=8,fill='#252b38')
        im=Image.open(ASSETS/f'textures/item/{name}.png').resize((88,88),Image.Resampling.NEAREST)
        atlas.paste(im,(x+16,y+8),im)
        words=name.replace('_',' ').split()
        if len(' '.join(words))>17:
            label=' '.join(words[:-1])+'\n'+words[-1]
        else: label=' '.join(words)
        bounds=d.multiline_textbbox((0,0),label)
        d.multiline_text((x+60-(bounds[2]-bounds[0])/2,y+100),label,fill='#ddd4bd',align='center')
    save(ROOT/'docs/item-preview.png',atlas)

def main():
    textures(); models(); vestments(); recipes(); translations(); expanded.extras(sys.modules[__name__]); preview()
    print(f'Generated {len(BLOCKS)} blocks, {len(ITEMS)} items, {len(list((DATA/"recipe").glob("*.json")))} recipes.')

if __name__=='__main__': main()
