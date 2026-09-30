---
navigation:
  title: Item Transformation
  icon: nautec:aquarine_steel_ingot
  position: 2
  parent: getting_started/getting_started-index.md
---

# <Color id="light_purple">Item Transformation</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="aquarine_steel_ingot" scale="2"/>
  A powered laser beam turns items dropped into it into something else.
</Column>

Drop an item so it lies inside a powered beam, anywhere between the sending block and the receiver. If a transformation recipe matches the item and the beam's purity is high enough, the item changes after the recipe time. Nothing needs clicking, and the whole dropped stack changes at once, one output item for each input item.

The item has to stay in the beam for the full time. If it is pushed out, or the beam loses power even for a moment, it starts over.

***

## <Color id="gold">Recipes</Color>

| Input | Output | Purity needed | Time |
|---|---|---|---|
| Aquarine Steel Compound | Aquarine Steel Ingot | any | 5 s |
| <ItemLink id="burnt_coil"/> | Laser Channeling Coil | 1.5 | 10 s |
| Prismarine Crystals | <ItemLink id="prismarine_crystal_shard"/> | 2.0 | 8 s |

JEI lists every transformation, including ones added by other mods or datapacks, under Item Transformation.

***

## <Color id="gold">Aquarine Steel</Color>

Aquarine Steel is the first thing to make. Its compound needs no purity, so any running <ItemLink id="aquatic_catalyst"/> beam will do: point the catalyst at a receiver such as a <ItemLink id="prismarine_laser_relay"/> and drop the compound on the floor under the beam.

<Recipe id="nautec:aquarine_steel_compound"/>

The [Mixer](nautec:laser_chemistry/mixer.md) makes the compound in bulk later on.

***

## <Color id="gold">Higher Purity</Color>

The Laser Channeling Coil and Prismarine Crystal Shards need more purity than a catalyst's 1.2. Use one of these:

* A <ItemLink id="prismarine_crystal"/>, purity 3.0. See the setup below.
* A <ItemLink id="bacterial_fuel_cell"/>, up to 2.5 depending on its colony.
* A <ItemLink id="focusing_lens"/> in the beam's path, which adds 0.5 to the beam passing straight through it.

An item is judged by the purity of the block that sends the beam it lies in. A lens's boost reaches the block after the lens, so send the lens's beam into a <ItemLink id="prismarine_laser_relay"/> and drop items in the relay's beam. A catalyst on Prismarine Crystal Shards, then a lens, then a relay gives 1.7 in the relay's beam, enough for the coil.

***

## <Color id="gold">Under a Prismarine Crystal</Color>

Every Crystal Geode (see [Structures](structures.md)) has a Prismarine Crystal standing with one block of air under it, which is the setup this needs.

* Place an Aquatic Catalyst level with the crystal's core, the fourth crystal block up from the bottom, facing the core from the side. Keep it within 16 blocks with nothing solid in between.
* Load the catalyst with any fuel. The crystal's bottom block now fires a purity 3.0 beam straight down into the floor.
* Drop your items into the gap under the crystal so they lie on the floor inside that beam.

Keep the catalyst fed. When it runs dry the crystal's beams stop and every item in them starts over.

<Color id="gold">Tip</Color>: beams burn anything standing in them. Drop items in from the side instead of stepping under the crystal.
