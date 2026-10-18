"""Detailed code-native statue geometry and cathedral-era resources."""
import json
import math
from PIL import Image, ImageDraw

ART = {'annunciation_icon': ('annunciation', 'The Annunciation — Fra Angelico'),
       'sistine_madonna_icon': ('sistine_madonna', 'Sistine Madonna — Raphael'),
       'last_supper_icon': ('last_supper', 'The Last Supper — Leonardo da Vinci')}
NEW_ITEMS = ['wine_cruet', 'chant_veni_creator', 'chant_veni_sancte', 'chant_dies_irae']

def statues(g):
    c = g.cuboid
    for name, mantle in [('our_lady_statue','blue_cloth'), ('st_joseph_statue','brown_cloth')]:
        parts = [c([2.5,0,2.5],[13.5,.65,13.5],'quartz'), c([3.2,.65,3.2],[12.8,1.3,12.8],'quartz'),
                 c([4,1.3,4],[12,1.7,12],'gold')]
        # Layered sculpted silhouette: flared hem, shoulders, neck and head.
        for y, half, depth in [(1.7,3.2,2.4),(2.5,3.1,2.3),(3.3,2.9,2.2),(4.1,2.7,2.1),(4.9,2.5,2),(5.7,2.3,1.9),(6.5,2.1,1.8),(7.3,2,1.7),(8.1,2.1,1.6),(8.9,2.25,1.6),(9.7,2.5,1.6),(10.5,2.3,1.6)]:
            parts.append(c([8-half,y,8-depth],[8+half,y+.8,8+depth],'linen'))
            parts.append(c([7.9-half,y,8.8],[8.1+half,y+.8,10.6],mantle))
        for x in [5.1,6.35,7.7,9.05,10.3]:
            parts.append(c([x,1.9,5.45],[x+.18,6.4,5.68],'linen'))
        parts += [c([7.4,11.3,7],[8.6,11.9,8.7],'skin'),c([6.75,11.9,6.5],[9.25,14,9],'skin'),
                  c([7.1,14,6.9],[8.9,14.3,8.6],'skin'),c([7.05,11.55,6.8],[8.95,12,8.6],'skin'),
                  c([7.82,12.5,6.2],[8.18,13.1,6.6],'skin')]
        # Individual eyes, brows, mouth; side cheeks and front nose avoid a blank cube.
        for x in [7.2,8.55]:
            parts += [c([x,13,6.44],[x+.3,13.16,6.52],'wood'),c([x-.1,13.35,6.44],[x+.4,13.47,6.52],'brown_cloth')]
        parts.append(c([7.7,12.12,6.44],[8.3,12.24,6.52],'red_cloth'))
        # Halo made of sixteen small gold pieces behind the head.
        for i in range(16):
            a = i*math.tau/16; x=8+2.65*math.cos(a); y=12.5+2.65*math.sin(a)
            parts.append(c([x-.3,y-.3,9.35],[x+.3,y+.3,9.65],'gold'))
        if name == 'our_lady_statue':
            parts += [c([6.35,11.5,8.8],[9.65,14.6,9.4],mantle),c([6.35,14,6.8],[9.65,14.6,9.4],'linen'),
                      c([6.25,11.8,6.4],[6.85,14.2,9.2],'linen'),c([9.15,11.8,6.4],[9.75,14.2,9.2],'linen')]
            for x in [5.5,9.5]:
                parts.append(c([x,6.8,5.8],[x+1,10.9,7.3],mantle))
            parts += [c([6.1,7.9,5],[7.4,8.8,6.2],'linen',rotation={'origin':[7,8,6],'axis':'z','angle':-22.5}),
                      c([8.6,7.9,5],[9.9,8.8,6.2],'linen',rotation={'origin':[9,8,6],'axis':'z','angle':22.5}),
                      c([7.35,8.2,4.7],[7.95,9.8,5.5],'skin',rotation={'origin':[8,8,5],'axis':'z','angle':-22.5}),
                      c([8.05,8.2,4.7],[8.65,9.8,5.5],'skin',rotation={'origin':[8,8,5],'axis':'z','angle':22.5})]
        else:
            parts += [c([6.7,13.65,6.45],[9.3,14.5,9.1],'brown_cloth'), c([6.8,11.65,6.25],[9.2,12.2,6.7],'brown_cloth'),
                      c([6.6,12,6.55],[7.05,13,7],'brown_cloth'),c([8.95,12,6.55],[9.4,13,7],'brown_cloth'),
                      c([4.7,7.2,6],[6.2,11,8],mantle),c([9.8,7.2,6],[11.3,11,8],mantle),
                      c([5.5,7.4,4.8],[9.4,8.4,6.5],'skin'),
                      c([9.2,6.6,4.5],[11.3,9.5,6.4],'linen'),c([9.55,9.5,4.4],[11.05,11.1,5.9],'skin'),
                      c([9.5,10.7,4.8],[11.1,11.3,6],'brown_cloth'),c([9.4,7.8,3.9],[11.2,8.3,4.6],'skin'),
                      c([4.4,2,6],[4.7,12.8,6.3],'wood'),c([4.3,11.4,5.7],[4.8,13.5,6.6],'leaves')]
            for dx,dz in [(-.7,0),(.7,0),(0,-.7),(0,.7)]:
                parts.append(c([4.1+dx,12.8,5.8+dz],[5+dx,13.4,6.7+dz],'linen'))
            parts.append(c([4.45,13.1,6.15],[4.65,13.8,6.35],'gold'))
            for x in [9.8,10.65]:parts.append(c([x,10.3,4.36],[x+.18,10.45,4.42],'wood'))
            parts.append(c([10.1,9.8,4.36],[10.5,9.9,4.42],'red_cloth'))
            for i in range(12):
                angle=i*math.tau/12; x=10.3+1.15*math.cos(angle); y=10.35+1.15*math.sin(angle)
                parts.append(c([x-.13,y-.13,6.1],[x+.13,y+.13,6.3],'gold'))
        g.model(name,parts,{"ambientocclusion":False})


def icons(g):
    c=g.cuboid
    for name,(art,title) in ART.items():
        parts=[c([0,0,13],[16,16,16],'wood'), c([.7,.7,12.9],[15.3,15.3,13.1],'gold'),
               c([1.5,1.5,12.8],[14.5,14.5,12.95],'linen', {'north':'art_'+art})]
        for start,end in [([0,0,12.4],[16,1.4,13.4]),([0,14.6,12.4],[16,16,13.4]),([0,1.4,12.4],[1.4,14.6,13.4]),([14.6,1.4,12.4],[16,14.6,13.4])]: parts.append(c(start,end,'gold'))
        for x in [0.8,15.2]:
            for y in [0.8,15.2]: parts.append(c([x-.4,y-.4,12.1],[x+.4,y+.4,12.5],'quartz'))
        g.model(name,parts,{'textures':{**{t:'catholicraft:block/'+t for t in ['wood','gold','linen','quartz']},'art_'+art:'catholicraft:block/art_'+art,'particle':'catholicraft:block/gold'}})


def icon(g,name):
    im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
    if name.startswith('chant_'):
        d.ellipse((3,3,29,29), fill='#252b35',outline='#c9ac62',width=2)
        color={'chant_veni_creator':'#a44d43','chant_veni_sancte':'#507d8a','chant_dies_irae':'#78617d'}[name]
        d.ellipse((10,10,22,22),fill=color);d.rectangle((15,6,16,24),fill=g.GOLD);d.rectangle((11,11,21,12),fill=g.GOLD)
        d.ellipse((14,14,18,18),fill='#171b25')
    else:
        d.rectangle((13,3,19,7),fill=g.GOLD);d.rectangle((12,7,20,12),fill='#b5c7c2')
        d.rounded_rectangle((7,11,25,28),radius=4,fill='#a9c2be',outline=g.INK,width=2)
        d.rectangle((10,16,22,25),fill='#883641'); d.line((10,12,10,23),fill='#f4e3c3',width=2)
        d.rectangle((13,17,19,23),fill='#d8c58e');g.cross(d,14,18,4,5,g.INK,1)
    g.save(g.ASSETS/f'textures/item/{name}.png',im)


def extras(g):
    # Original villager UV art: brown habit, cord, cowl, tonsure, sandals.
    im=Image.new('RGBA',(64,64),'#70533c'); d=ImageDraw.Draw(im)
    d.rectangle((0,0,31,19),fill='#c9a482'); d.rectangle((32,0,63,19),fill=(0,0,0,0))
    d.rectangle((8,0,15,7),fill='#76614f'); d.rectangle((10,2,13,5),fill='#c9a482')
    d.rectangle((9,10,10,11),fill='#322b2a'); d.rectangle((13,10,14,11),fill='#322b2a')
    d.rectangle((8,8,15,9),fill='#9b775a');d.rectangle((11,14,13,15),fill='#87604b')
    d.rectangle((0,38,31,63),fill='#624632')
    for x in [2,6,10,14,18,22,26,30]:d.line((x,45,x,63),fill='#513925')
    d.rectangle((16,28,35,29),fill='#d9c59a'); d.rectangle((0,48,31,49),fill='#d9c59a')
    d.rectangle((19,49,20,58),fill='#d9c59a'); d.rectangle((16,57,23,58),fill='#d9c59a')
    d.rectangle((0,32,15,37),fill='#352c26');g.save(g.ASSETS/'textures/entity/monk.png',im)
    manifest=json.loads((g.RES/'META-INF/catholicraft-assets.json').read_text())
    sounds={}
    for track in ['veni_creator','veni_sancte','dies_irae']:
        record=next(r for r in manifest if r['id']==track)
        sounds['chant.'+track]={'subtitle':'subtitles.catholicraft.'+track,'sounds':[{'name':'catholicraft:chant/'+track,'stream':True}]}
        g.write(g.DATA/f'jukebox_song/{track}.json',{'sound_event':'catholicraft:chant.'+track,'description':{'translate':'song.catholicraft.'+track},'length_in_seconds':float(record['audio']['format']['duration']),'comparator_output':{'veni_creator':5,'veni_sancte':10,'dies_irae':15}[track]})
    g.write(g.ASSETS/'sounds.json',sounds)
    lang=json.loads((g.ASSETS/'lang/en_us.json').read_text())
    for name,(art,title) in ART.items():
        lang['block.catholicraft.'+name]=title
        lang['tooltip.catholicraft.'+name]='Framed public-domain sacred art. Use to read the artist credit.'
        lang['art.catholicraft.'+name]=title+' • Public domain • Wikimedia Commons'
    for track,title in [('veni_creator','Veni Creator Spiritus'),('veni_sancte','Veni Sancte Spiritus'),('dies_irae','Dies Irae')]:
        lang['item.catholicraft.chant_'+track]='Gregorian Chant — '+title
        lang['song.catholicraft.'+track]=title+' — Membeth (public domain)'
        lang['subtitles.catholicraft.'+track]='Gregorian chant: '+title
    lang.update({'tooltip.catholicraft.wine_cruet':'Wine prop for the offertory. Returns a bottle when used in a Mass session.',
                 'tooltip.catholicraft.chant':'Full public-domain recording by Membeth. Play in a jukebox.',
                 'entity.catholicraft.monk':'Monk',
                 'message.catholicraft.monk_offer':'Pax et bonum. One emerald: %s × %s. Sneak-use to choose another supply; use an emerald to trade.',
                 'message.catholicraft.mass_owned':'This service has a celebrant. You can follow along nearby.',
                 'message.catholicraft.mass_cancelled':'The Mass roleplay session has ended.',
                 'message.catholicraft.mass_missing':'Prepare the sanctuary: %s.',
                 'message.catholicraft.mass_gifts':'Bring one altar bread and one wine cruet for the offertory.',
                 'message.catholicraft.mass_complete':'Mass roleplay complete. Go in peace.',
                 'message.catholicraft.mass_step':'%s Mass • %s/%s — %s\n%s',
                 'message.catholicraft.mass_ready':'Equip an alb and chasuble. Prepare a crucifix, chalice, two lit candles and an ambo or lectern. Use either missal on the altar to begin; use again to continue; sneak-use to end.',
                 'message.catholicraft.cathedral_built':'Cathedral built at %s, %s, %s.',
                 'message.catholicraft.cathedral_blocked':'The cathedral needs a clear, loaded 41 × 25 × 29 space; obstruction at %s, %s, %s.',
                 'message.catholicraft.monk_spawned':'A monk has joined your chapel.'})
    g.write(g.ASSETS/'lang/en_us.json',lang)
