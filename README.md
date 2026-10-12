# CatholiCraft

A Catholic chapel-building mod for **Minecraft Java Edition 26.2**, **NeoForge 26.2.0.90**, and **Java 25**. Works in singleplayer and on a server with the mod installed on both sides.

## Install and play

1. Install the NeoForge profile for Minecraft **26.2**.
2. Copy `build/libs/catholicraft-1.1.0.jar` into that profile's `mods` folder. If using the release ZIP, take the JAR from its `mods` folder.
3. Start Minecraft and open the **CatholiCraft** creative tab, or craft a **Field Guide** with a book and a gold nugget.

The development test jar is for developers; the main jar is the only CatholiCraft file players need. There are no extra mod dependencies.

## What's included

| Content | What it does |
| --- | --- |
| Tabernacle | Nine persistent inventory slots, an opening door, comparator output, and item drops when broken |
| 1962 Roman Missal | Eight original companion pages with the traditional order of Mass and short Latin prayers |
| Modern Roman Missal | Eight original companion pages introducing the modern order of Mass |
| Field Guide | Sixteen pages explaining crafting, equipment, interactions, and commands |
| Prayer Book | Eight pages of historic Latin prayers and original explanations |
| Stations Companion | Sixteen pages with the fourteen traditional stations and original reflections |
| Vestments | Six chasubles, six dalmatics, an alb, biretta, mitre, shoes, cassock, and surplice |
| Holy water | Collected from a font; extinguishes the player and removes poison, weakness, slowness, and nausea |
| Holy water and baptismal fonts | A water bucket fills three charges; each glass bottle collects one charge |
| Medals | St. Benedict, Miraculous, and St. Michael medals; right-click to equip in the off hand |
| Rosary | Use to advance through five mysteries; sneak-use switches Joyful, Sorrowful, Glorious, and Luminous sets |
| Thurible and incense | Uses one incense from the inventory to create smoke, with a two-second cooldown |
| Candles and sanctuary lamp | Light with flint and steel, extinguish with an empty hand; emit light and flame particles |
| Church bell | Rings when used or on the rising edge of a redstone signal |
| Chapel furnishings | Linen-covered altar, crucifix, pew, kneeler, chalice, ciborium, monstrance, and cross stained glass |
| Altar frontals | Six colors; use a cloth on the altar to swap its frontal and retrieve the old one |
| Advent wreath | Four candles lit individually; the third is rose and the other three are violet |
| Incense burner | Persistent fuel, one minute per incense, with capacity for five minutes |
| Aspergillum | Refillable sprinkling tool; eight uses per font charge, with a water-level item bar |
| Sacristy cabinet | Twenty-seven persistent slots for vestments, books, and supplies |
| Stations plaques | Cycle through I–XIV; the selected numeral survives mining and replacing |
| More furnishings | Our Lady and St. Joseph statues, votive rack, credence table, and ambo |
| Devotional accessories | Altar bell, processional cross, brown scapular, beeswax, and unconsecrated altar bread |

There are **65 registered items**, including **23 placeable blocks**, **18 wearable vestments**, and **64 crafting recipes**. Holy water is obtained through the font interaction. Furnishings have individual 3D models; all textures are original pixel art. Stained glass requires Silk Touch to retrieve.

The missals are readable introductory companions, with original explanatory text and short traditional prayers; they are not complete reproductions of either liturgical book. Furnishings and vestments are game representations. Holy-water crafting and status effects are Minecraft mechanics.

![Original item textures](docs/item-preview.png)

## Controls and commands

All five books can be read in the hand or placed on a vanilla lectern. Right-click a vestment to equip it, or put it in its inventory slot: chasubles, dalmatics, cassock, and surplice on the chest; alb on the legs; biretta or mitre on the head; shoes on the feet. They are cosmetic clothing without armor protection.

Use an empty hand on a pew to sit, and sneak to dismount. Each pew block seats one player. Breaking a pew or leaving it removes its invisible seat automatically.

Use a colored altar cloth to replace the altar's frontal. The white top linen stays in place, and the previous frontal returns to your inventory. Applying the same color consumes nothing. Mining and replacing an altar preserves its frontal.

Use flint and steel on the Advent wreath to light the next candle, and an empty hand to extinguish one. Use flint and steel or an empty hand to light or extinguish the whole votive rack.

Add incense to the incense burner by using it on the block. One incense lasts sixty seconds; a full burner holds five minutes. Use an empty hand to check the remaining time. Fuel persists through saves and pauses when its chunk is unloaded.

Craft an empty aspergillum and use it on either filled font. One font charge gives eight uses; topping up a full tool consumes nothing. Use it to extinguish and cleanse yourself and players within three blocks. The tool stays intact when empty.

Use a Stations plaque to select the next of fourteen traditional stations, or sneak-use it to select the previous one. The numeral remains on the dropped plaque. The companion book provides an original short reflection for each station.

Operators and players with cheats enabled can use:

```text
/catholicraft kit
/catholicraft chapel
```

The kit gives one of every item and sixteen incense. Overflow is dropped beside the player. The chapel command builds a furnished chapel **15 blocks east-west, 11 blocks north-south, and 8 blocks high**, starting three blocks east and five blocks north of the player's position. It checks the whole volume first, including the interior, and reports an obstruction without changing anything. Stand in a large, clear area at ground level; the entrance faces west and the altar faces east.

The expanded chapel includes both missals on lecterns, fourteen numbered Stations plaques, both statues, a cabinet stocked with devotional books and supplies, a votive rack, an Advent wreath, a credence table, and an incense burner.

## Build and develop

With Java 25 installed:

```sh
./gradlew build
./gradlew runClient
./gradlew runGameTestServer
```

The Gradle wrapper is included. The first standard build needs network access to download Gradle and NeoForge. `build` includes the resource audit; the separate GameTest task runs the real Minecraft integration tests. GameTest classes and templates are kept in their own source set and excluded from the release jar.

Generated resources are included in the source tree, so normal builds don't regenerate them. To change the art, models, or recipes:

```sh
python3 tools/generate_resources.py
python3 tools/generate_test_resources.py
python3 tools/verify_resources.py
```

Asset generation and offline font validation require Pillow. The standalone resource audit uses Python's standard library.

For a restricted environment that already has a compatible NeoForge development cache:

```sh
python3 tools/build_offline.py --minecraft /path/to/minecraft-patched-26.2.0.90-merged.jar --tests
python3 tools/validate_client_offline.py
python3 tools/run_offline.py gameTestServer --tests 'catholicraft_tests:*' --report /absolute/path/to/report.xml
```

These fallback tools read the existing Gradle cache without modifying it or downloading dependencies. `build_offline.py --cache` can point to a different cache. A standard Gradle build remains the usual workflow.

## Validation

The mod has been compiled and loaded through NeoForge in a real Minecraft GameTest server. Eighteen integration tests cover all 64 crafting recipes, tabernacle and cabinet persistence/drops, multiple container users, font refill/depletion, holy water and aspergillum use, all books on lecterns, all equipment, lights/redstone, rosary state, incense fuel and saving, altar frontals, all fourteen stations, pew seating/cleanup, and furnished chapel placement/obstruction checks.

Minecraft's native parsers also validate all 122 models, 65 client item definitions, 18 equipment definitions, and 23 blockstate definitions. Its text wrapping code verifies all 56 book pages fit the reading area using the vanilla font's glyph widths. A resource audit checks model/texture references, translations, loot tables, recipe discovery, and equipment textures. A graphical playtest is still recommended: this build environment cannot create the sockets needed for a display server.

The [Vatican's traditional Stations outline](https://www.vatican.va/news_services/liturgy/2005/documents/ns_lit_doc_20050325_via-crucis_en.html) provides a reference for the station sequence. The reflections in this mod are original. Historic Latin prayers are also documented in the [Compendium's common prayers appendix](https://www.vatican.va/archive/compendium_ccc/documents/archive_2005_compendium-ccc_en.html).

## Package the release

```sh
python3 tools/package_release.py
```

This creates `releases/catholicraft-1.1.0.zip` with the playable JAR, complete source, installation instructions, original art previews, validation results, and file checksums. The packager requires a passing current test report and client validation log; it excludes caches, development run worlds, secrets, and the development test JAR.

Source and original textures are licensed under MIT. Minecraft and NeoForge are separate dependencies and are not bundled in the mod jar.
