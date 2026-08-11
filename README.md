# Nautec

Nautec is an underwater tech mod for NeoForge. Nautec moves progression off the land and onto the ocean floor. Powered by aquatic laser beams you aim by line of sight, resources are grown from bacteria
colonies, and the endgame is rebuilding your own body part by part with ancient atlantean technology.

Minecraft 26.1.2, NeoForge 26.1.2.94, Java 25.

## Aquatic Power

Power in Nautec is a beams, not cables. An Aquatic Catalyst fed prismarine fires a laser out of its
opposite face, and that beam carries Aquatic Power (AP) to whatever it hits. Relays, Laser Junctions
and Long Distance Lasers redirect and split it, so laying out a base is a question of sightlines and
angles rather than of running cables behind a wall.

Beams carry a second value alongside power: purity. Shooting a beam through a Prismarine Crystal
raises its purity, and higher tiers of processing refuse to run below a threshold. Recipes state the
purity they need, so a working setup has to deliver both enough AP and clean enough AP.

Item transformation happens in the open beam. Drop an item into a laser of sufficient purity and it
becomes something else, no machine block involved.

## Machines

Dozens of custom machines and blocks cover processing and logistics:

- Aquatic Catalyst, Prismarine Laser Relay, Laser Junction and Long Distance Laser for generating and
  routing beams
- Prismarine Crystal for raising purity
- Mixer, Mutator, Incubator and Bacterial Analyzer for fluid and bacteria work
- Energy Converter, which accepts Forge Energy and feeds it into a beam as AP, and Charger for equipment
- Fishing Station, Oil Barrel, Crate and Anchor for gathering and storage
- Bio Reactor, Deep Sea Drain and Augmentation Station as multiblocks

The Deep Sea Drain pulls salt water from the ocean around it. The Bio Reactor runs up to three
bacteria colonies at once, with power cost scaling per colony and output speed set by each colony's
size and production rate.

## Bacteria

Bacteria are the mod's biological resource line. Forty-six species exist by default with others enabled depending on what mods are installed. Each is an instance with its own size, growth rate, production rate, lifespan and mutation resistance rather than a fixed template.
You collect them in Petri Dishes and Vials, read their stats with a Bacterial Analyzer, push them
toward better numbers in a Mutator, grow them in an Incubator, and put finished colonies to work in
a Bio Reactor to produce materials. Every stat has a configurable cap.

## Fluids

Salt water, Etching Acid, Electrolyte Algae Serum and oil, mixed and processed on the way to
Aquarine Steel, Atlantic Gold and the rest of the material line.

## Augments

Almost 2 dozen augments fit across nine body slots (head, eyes, body, lung, heart, and each arm and leg).
Applying one is done by standing in a built Augmentation Station with a Robot Arm and the augment part
loaded into each extension, and enough power behind it. `B` opens your active augment screen.

They range from utility to outright overpowered. Drowned Lung and Dolphin Fin for staying under, Step Up,
Leap, Magnet and Ender Magnet for movement and pickup, Guardian Eye and the trident launchers for
combat, Bonus Hearts and Eldritch Heart for survivability, and Creative Flight at the top end. Three
are built from deep-sea creature drops: Abyssal Eyes (night vision, but only below a configured
depth), Photophore Skin (lights up living things around you underwater, through terrain) and Vent
Carapace (armor, knockback resistance, and half burn duration).

## The ocean

Four biomes are added to the overworld's ocean layout without replacing any vanilla ocean entirely:

| Biome | What it is |
| --- | --- |
| Abyssal Trench | Deepest and coldest water, near-black fog, far more Drowned |
| Bioluminescent Grove | Clear teal water with glow squid, kelp and sea pickles |
| Hydrothermal Vents | Murky warm water over basalt and magma |
| Prismarine Reef | Bright shallow water at the drop-off, coral and tropical fish |

All four count as oceans, so salt water collection, the Deep Sea Drain, Crystal Geodes, ocean ruins
and shipwrecks all work in them. Injection is configurable, and packs on a custom overworld preset
can add their preset to `injectableWorldPresets`.

They come with six plants (Deep Kelp, Luminescent Algae, Prismarine Frond, Vent Tubeworm, Abyssal
Coral, Glow Polyp), four creatures (Silt Skipper, Lantern Jelly, Vent Crawler, Abyssal Maw) and
Budding Prismarine, which grows buds and clusters the way Budding Amethyst does and makes Prismarine
Crystal Shards renewable.

## Fishing

Lucky fishing zones drift into open water near you, marked by glow particles, and only form where
every block in radius is genuinely open water. Fishing one adds a roll on the zone's loot table on
top of your normal catch. Zones can hook live creatures as well as items, controlled by the
`nautec:catch_as_entity` loot function, so a pack can make anything catchable.

The Prismatic Fishing Rod adds a catch minigame, picked at random from three: strike as the marker
crosses the green, strike on three marks in order, or hold through the green and release. Winning
adds a treasure roll. Missing gives you the ordinary catch, so it is never a punishment for ignoring
it.

Loot lives in `nautec:gameplay/lucky_fishing_zone` and its catch and treasure sub-tables, which vary
by biome and are meant to be overridden.

## Tools and gear

Aquarine Steel tools and armour that gain their real stats once powered, a Diving Suit, Neptune's
Trident, a Wrench, Batteries, a Grafting Tool, and the Prism Monocle for reading beam purity and
block state in the world.

## Vehicles

The Abyssal Submersible is a two seat crewed vehicle: place it on water, right-click to board, hold
right mouse to steer with the mouse, A/D for the rudder, W/S for the throttle. It runs on stored
power, charges in a Charger, and keeps its crew breathing while it is sealed and powered.

## Compatibility

GeckoLib is a required dependency; it animates the submersible.

JEI for recipes, Jade for in-world block info, Curios for equipment slots, and Durability Display.
The in-game guide book is built on Modonomicon and is the intended starting point: craft it and work
forward from "An Introduction to Laser Power".

## Configuration

Most numbers are able to be tweaked in the config files, including laser range, per-machine power draw and speed, bacteria stat caps, drain capacity and rate, augment tuning (Abyssal Eyes depth, Photophore Skin radius, Guardian augment damage), biome injection, the full lucky fishing zone set (interval, spacing, per-chunk cap, lifetime, radius), and submarine tuning (power capacity, idle/movement/oxygen draw, thrust, top speed, third person camera distance). The submarine's power HUD position is client config, repositionable in game with Ctrl+H.

## For pack developers

Most of the mod's content is data-driven and safe to override from a datapack.

**Recipes.** Seven custom types, all plain JSON in `data/<namespace>/recipe/`:

| Type | Machine |
| --- | --- |
| `nautec:aquatic_catalyst_channeling` | Aquatic Catalyst fuel and beam output |
| `nautec:item_transformation` | Items transformed in an open beam |
| `nautec:item_etching` | Etching Acid processing |
| `nautec:mixing` | Mixer, fluid plus items |
| `nautec:augmentation` | Augmentation Station |
| `nautec:bacteria_mutation` | Mutator |
| `nautec:bacteria_incubation` | Incubator |

Transformation recipes carry a `purity` float, which is the minimum beam purity the recipe will
accept. Setting it gates a recipe behind crystal infrastructure rather than behind a material cost:

```json
{
  "type": "nautec:item_transformation",
  "duration": 100,
  "ingredient": { "ingredient": "nautec:aquarine_steel_compound" },
  "purity": 0.0,
  "result": { "id": "nautec:aquarine_steel_ingot" }
}
```

**Fishing loot.** Override `nautec:gameplay/lucky_fishing_zone` and its `catch` and `treasure`
sub-tables to change what zones produce. Two custom loot entries are available anywhere loot is
written:

- `nautec:catch_as_entity`, a function that turns a rolled item into a live creature released at the
  bobber. This is what makes fish catchable as mobs, and it works for anything you point it at.
- `nautec:in_lucky_fishing_zone`, a condition for restricting entries to zone catches.

**Bacteria sources.** The `nautec:bacteria_obtaining` data map maps a block to the bacteria it can
yield, with an optional biome tag and a chance:

```json
{
  "values": {
    "minecraft:stone": {
      "bacteria": "nautec:cyanobacteria",
      "biome": "minecraft:is_ocean",
      "chance": 0.4
    }
  }
}
```

Adding an entry is how you make a new block a bacteria source without touching code.

**Tags.** Item tags cover the repair materials for each armour and tool set
(`repairs_aquarine_tools`, `repairs_aquarine_armor`, `repairs_diving_suit`,
`repairs_prismarine_armor`) plus `aquarine_steel`, `aquatic_catalyst` and `corals`. Each new biome
has its own tag (`abyssal`, `bioluminescent`, `reef`, `vents`), so pack content can target one
biome family without hardcoding biome ids.

**Worldgen.** Biome injection is on by default and can be turned off with `enableBiomeInjection`. A
pack running a custom overworld preset needs to add that preset to `injectableWorldPresets` for the
biomes to appear.

**Guide book.** The in-game guide is a Modonomicon book, so entries and categories are datapack
content and can be extended or replaced alongside your own recipes.

`ASSETS.md` tracks which textures are placeholder art awaiting a real pass. `TESTING.md` lists
behavior that automated tests cannot cover.

## Credits and license

Nautec began as Porting Dead Mods' entry in the CurseForge 2024 Modjam, created by Thepigcat76,
Leclowndu93150, Ktpatient, Reclipse and Iglee42. Released under the MIT license; see `LICENSE`.
It is now maintained and continued to be developed by Saereth and BreakinBlocks.
