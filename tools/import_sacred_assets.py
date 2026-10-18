#!/usr/bin/env python3
"""Fetch the individually verified public-domain files; package textures and mono Vorbis.

Normal builds use committed assets and never need network access. This explicit
maintenance command requires Pillow and ffmpeg. Sources/rights/hashes are bundled
in the JAR so every imported asset remains traceable.
"""
import hashlib
from io import BytesIO
import json
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.request
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/catholicraft'
FILES = [
    ('annunciation', 'Fra Angelico — Annunciation of Cortona (1433–1434)', 'Fra Angelico / The Yorck Project', 'Fra_Angelico_069.jpg', '9/96/Fra_Angelico_069.jpg', 'PD-art', 'Faithful reproduction of a public-domain painting; the individual image and depicted work are public domain. The Yorck compilation is separately licensed.'),
    ('sistine_madonna', 'Raphael — Sistine Madonna (1512–1513)', 'Raphael / The Yorck Project', 'Raffael_051.jpg', 'e/ea/Raffael_051.jpg', 'PD-art', 'Faithful reproduction of a public-domain painting; Commons marks the individual image PD-art / Public Domain Mark 1.0.'),
    ('last_supper', 'Leonardo da Vinci — The Last Supper (1495–1498)', 'Leonardo da Vinci', 'Leonardo_da_Vinci_(1452-1519)_-_The_Last_Supper_(1495-1498).jpg', '0/08/Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg', 'PD-art', 'Faithful reproduction of a public-domain painting; Commons marks the individual image PD-art / Public Domain Mark 1.0.'),
    ('veni_creator', 'Veni Creator Spiritus', 'Membeth', 'Veni.creator.spiritus.ogg', '0/01/Veni.creator.spiritus.ogg', 'PD-self', 'The performer and recording copyright holder released this recording into the public domain worldwide, with an unconditional fallback permission.'),
    ('veni_sancte', 'Veni Sancte Spiritus', 'Membeth', 'Veni.sancte.spiritus.ogg', '9/96/Veni.sancte.spiritus.ogg', 'PD-self', 'The performer and recording copyright holder released this recording into the public domain worldwide, with an unconditional fallback permission.'),
    ('dies_irae', 'Dies Irae', 'Membeth', 'Dies.irae.ogg', 'f/f2/Dies.irae.ogg', 'PD-self', 'The performer and recording copyright holder released this recording into the public domain worldwide, with an unconditional fallback permission.'),
]

def main():
    manifest = []
    for name, title, creator, page, upload, license_id, rights in FILES:
        url = 'https://upload.wikimedia.org/wikipedia/commons/' + upload
        target = A / (f'textures/block/art_{name}.png' if page.endswith('.jpg') else f'sounds/chant/{name}.ogg')
        original = None
        if not target.exists():
            for attempt in range(4):
                try:
                    request = urllib.request.Request(url + '?download=1', headers={'User-Agent': 'CatholiCraft/1.2 (public-domain asset import)'})
                    with urllib.request.urlopen(request, timeout=45) as response: original = response.read()
                    break
                except urllib.error.HTTPError as error:
                    if error.code != 429 or attempt == 3: raise
                    time.sleep(15)
        
        if page.endswith('.jpg') and original is not None:
            picture = Image.open(BytesIO(original)).convert('RGB')
            # Preserve the full painting and its proportions with a dark museum mat.
            texture = Image.new('RGB', (256, 256), '#30251c')
            picture = ImageOps.contain(picture, (256, 256), Image.Resampling.LANCZOS)
            texture.paste(picture, ((256 - picture.width)//2, (256 - picture.height)//2))
            target = A / f'textures/block/art_{name}.png'
            target.parent.mkdir(parents=True, exist_ok=True); texture.save(target)
            changes = 'Full image reduced to fit a 256×256 texture, preserving aspect ratio with a dark mat; no painting content cropped.'
        elif original is not None:
            target = A / f'sounds/chant/{name}.ogg'
            target.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.TemporaryDirectory(prefix='catholicraft-audio-') as temp:
                source = Path(temp)/'source.ogg'; source.write_bytes(original)
                subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(source), '-ac', '1', '-ar', '44100', '-c:a', 'libvorbis', '-q:a', '4', str(target)], check=True)
            changes = 'Entire recording converted to mono 44.1 kHz Ogg Vorbis for positional Minecraft jukebox playback; no excerpt or synthesized replacement.'
        changes = ('Full image reduced to fit a 256×256 texture, preserving aspect ratio with a dark mat; no painting content cropped.' if page.endswith('.jpg') else 'Entire recording converted to mono 44.1 kHz Ogg Vorbis for positional Minecraft jukebox playback; no excerpt or synthesized replacement.')
        record = {'id':name, 'title':title, 'creator':creator, 'license':license_id, 'rights':rights,
                  'source_page':'https://commons.wikimedia.org/wiki/File:'+page, 'download_url':url,
                  'verified_on':'2026-10-11', 'source_sha256':hashlib.sha256(original).hexdigest() if original is not None else None,
                  'asset':target.relative_to(ROOT/'src/main/resources').as_posix(),
                  'asset_sha256':hashlib.sha256(target.read_bytes()).hexdigest(), 'changes':changes}
        if page.endswith('.ogg'):
            info = subprocess.check_output(['ffprobe','-v','error','-show_entries','format=duration:stream=codec_name,channels,sample_rate','-of','json',str(target)], text=True)
            record['audio'] = json.loads(info)
        manifest.append(record)
        print(f'Imported {name}: {target.stat().st_size:,} bytes', flush=True)
    target = ROOT/'src/main/resources/META-INF/catholicraft-assets.json'
    target.write_text(json.dumps(manifest, indent=2, ensure_ascii=False)+'\n')
    lines = ['# Third-party asset credits', '', 'CatholiCraft source, models, architecture, and original pixel art are MIT licensed. The six files below are separately public domain; their source pages document the individual artwork reproductions and recording rights. Public-domain status is distinct from the licensing of Wikimedia page text or an image compilation.', '']
    for record in manifest:
        lines += [f'## {record["title"]}', '', f'Creator: {record["creator"]}. Status: {record["license"]}.', f'Source: {record["source_page"]}', '', record['rights'], '', record['changes'], '', f'SHA-256 of bundled asset: `{record["asset_sha256"]}`', '']
    (ROOT/'THIRD_PARTY_ASSETS.md').write_text('\n'.join(lines)+'\n')

if __name__ == '__main__': main()
