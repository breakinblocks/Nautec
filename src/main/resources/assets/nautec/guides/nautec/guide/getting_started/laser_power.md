---
navigation:
  title: Laser Power
  icon: nautec:aquatic_catalyst
  position: 1
  parent: getting_started/getting_started-index.md
item_ids:
  - nautec:aquatic_catalyst
  - nautec:prismarine_crystal
---

# <Color id="light_purple">Laser Power</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="aquatic_catalyst" scale="2"/>
  The Aquatic Catalyst burns prismarine and fires it as a laser beam.
</Column>

NauTec machines run on laser power, measured in AP per tick (AP/t). A beam carries two values: its power, and its purity. Machines use the power to run, and recipes check the purity before they start.

A beam only forms between two laser blocks. The sending block needs something on that side that accepts a beam (a machine, a relay, an optic), no more than 16 blocks away (`laserDistance` in `config/nautec-common.toml`), with no solid block in between. Beams pass through water.

***

<Row>
  <ItemImage id="aquatic_catalyst"/>
  ### <Color id="aqua">Aquatic Catalyst</Color>
</Row>

Your first power source. It burns prismarine items one at a time and fires a beam out of one face.

<Recipe id="nautec:aquatic_catalyst"/>

Load fuel by right-clicking the catalyst with a stack, or feed it from a hopper on any side. Right-click it with an empty hand to take back the fuel it has not started on yet.

| Fuel | Purity | Power | Burn time |
|---|---|---|---|
| Prismarine Shard | 0.4 | 12 AP/t | 8 s |
| Prismarine Crystals | 0.8 | 6 AP/t | 8 s |
| <ItemLink id="prismarine_crystal_shard"/> | 1.2 | 12 AP/t | 10 s |

The face you are looking at when you place it is the pale intake, with eight sockets that fill as you load fuel. The beam leaves from the opposite face, the dark one with the cyan lens. Look down while placing it to fire downward, look up to fire upward. An <ItemLink id="aquarine_steel_wrench"/> rotates a placed catalyst.

The catalyst only burns while a receiver is on its emitter side. With nothing there it holds its fuel and waits, so a catalyst with no target wastes nothing. It moves straight from one fuel item to the next, so the beam stays on as long as the stack lasts.

The two small lamps near the lens end of each side show whether it has found a receiver. Green means a laser block within range takes the beam. Red means it has not: nothing is in line with the lens, a solid block is in the way, or the block in front does not take a beam from that side (a relay facing the wrong way is the usual cause). Shift-right-click the catalyst with an empty hand for a chat readout of its fuel, which way it fires, what the beam hits and how to fix it.

<Color id="gold">Tip</Color>: the beam burns anything standing in it, you included. Walk around running beams.

***

## <Color id="gold">Purity</Color>

Purity is set where a beam starts. A catalyst beam carries its fuel's purity, 0.4 to 1.2. When two or more beams feed the same block, their power adds up and their purity is averaged.

Higher purity comes from a <ItemLink id="prismarine_crystal"/> (3.0), a <ItemLink id="bacterial_fuel_cell"/> (up to 2.5), or a <ItemLink id="focusing_lens"/> added to an existing beam. [Beam Optics](nautec:deep_engineering/beam_optics.md) covers turning, splitting and focusing beams.

***

<Row>
  <ItemImage id="prismarine_crystal"/>
  ### <Color id="aqua">Prismarine Crystal</Color>
</Row>

A six block tall crystal found standing inside Crystal Geodes under the ocean floor (see [Structures](structures.md)). It turns any beam into a purity 3.0 beam.

Fire a beam into the crystal's core from the side: the core is the fourth block up from the bottom, and it takes a beam on any of its four sides. The top block then fires straight up and the bottom block fires straight down, both with the full incoming power and a purity of 3.0. These two beams run until they reach a receiver or a solid block, so they work into an empty floor or ceiling as well.

The crystal stays where it generated, so build around it. An Aquarine Steel Pickaxe with its ability on chips [Prismarine Crystal Shards](nautec:laser_chemistry/crystal_shards.md) off it, but every hit has a chance to shatter the whole crystal.

***

## <Color id="gold">Moving Power</Color>

* [Laser Manipulation](nautec:laser_chemistry/laser_manipulation.md): the Prismarine Laser Relay passes a beam on, and the Laser Junction merges and splits beams.
* [Beam Optics](nautec:deep_engineering/beam_optics.md): mirrors, splitters and lenses.
* [Long Distance Laser](nautec:laser_augmentation/long_distance_laser.md): for beams longer than 16 blocks.
* [Charger](nautec:laser_chemistry/charger.md) and [Prismatic Battery](nautec:laser_chemistry/prismatic_battery.md): storing power in items.

Wear a <ItemLink id="prism_monocle"/> to read the power and purity arriving at any laser block.
