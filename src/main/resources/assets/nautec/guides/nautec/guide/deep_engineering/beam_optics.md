---
navigation:
  title: Beam Optics
  icon: nautec:prismatic_mirror
  position: 0
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:prismatic_mirror
  - nautec:beam_splitter
  - nautec:focusing_lens
---

# <Color id="light_purple">Beam Optics</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="prismatic_mirror" scale="2"/>
  Optics turn, split and clean up a laser beam.
</Column>

A <ItemLink id="prismarine_laser_relay"/> passes a beam on in a straight line. The three optics on this page change where the beam goes, and each one changes its purity on the way through. Recipes check purity before they run, so plan your route around the purity the machine at the end needs.

Each optic sends its beam up to 16 blocks, the same as any other laser (`laserDistance` in `config/nautec-common.toml`). All three can be placed under water.

***

## <Color id="gold">Purity</Color>

Purity is set by the source and then changed by every optic the beam passes through.

| Source | Purity |
|---|---|
| <ItemLink id="aquatic_catalyst"/> on Prismarine Shards | 0.4 |
| Aquatic Catalyst on Prismarine Crystals | 0.8 |
| Aquatic Catalyst on Prismarine Crystal Shards | 1.2 |
| <ItemLink id="bacterial_fuel_cell"/> | up to 2.5 |
| <ItemLink id="prismarine_crystal"/>, hit from the side | 3.0 |

The machines in this section need these purities:

| Output | Machine | Purity |
|---|---|---|
| Flawless Prismarine Crystal | [Abyssal Pressure Forge](pressure_forge.md) | 2.0 |
| Deep Steel Plating | Abyssal Pressure Forge | 2.5 |
| Resonant Shard | [Resonance Chamber](resonance_chamber.md) | 3.0 |

When two or more beams feed the same block, their power adds up and their purity is averaged.

***

<Row>
  <ItemImage id="prismatic_mirror"/>
  ### <Color id="aqua">Prismatic Mirror</Color>
</Row>

Turns a beam through a right angle. The mirror sends its beam out in the direction you were looking when you placed it, and accepts beams on the four sides around that line (not the front or the back).

The outgoing beam keeps 0.9 of the incoming purity (`mirrorPurityFactor`). Power passes through unchanged. Because it takes input on four sides, a mirror also merges beams: two sources pointed into one mirror come out as one beam with their combined power and their average purity.

<Recipe id="nautec:prismatic_mirror"/>

***

<Row>
  <ItemImage id="beam_splitter"/>
  ### <Color id="aqua">Beam Splitter</Color>
</Row>

Splits one beam into up to four. The face pointing back at you when you place it is the input. The beam leaves through the four sides around that line; nothing comes out of the far side.

Only sides with a laser block in range count as outputs. The power is divided evenly between them, so two outputs get half each and four get a quarter each. Every branch keeps 0.8 of the incoming purity (`splitterPurityFactor`).

<Recipe id="nautec:beam_splitter"/>

***

<Row>
  <ItemImage id="focusing_lens"/>
  ### <Color id="aqua">Focusing Lens</Color>
</Row>

Raises the purity of a beam that passes straight through it by 0.5 (`lensPurityBonus`), up to a limit of 2.0. Place it while looking the way the beam travels: the face toward you takes the beam in, the far face sends it on. Power passes through unchanged.

The lens only adds purity to a beam that already has some, so put it after a working source. A beam at 1.8 comes out at 2.0, and a beam already at 2.0 or higher passes through unchanged, so a row of lenses tops out at 2.0. Use one after a mirror or splitter to win back what the turn cost, or run two in a row to lift a Prismarine Crystal Shard catalyst (1.2) to the 2.0 the Pressure Forge needs for Flawless Prismarine Crystals. For 2.5 and 3.0, use a Bacterial Fuel Cell or a Prismarine Crystal as the source.

<Recipe id="nautec:focusing_lens"/>
