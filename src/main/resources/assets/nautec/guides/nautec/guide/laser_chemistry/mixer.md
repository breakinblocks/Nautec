---
navigation:
  title: Mixer
  icon: nautec:mixer
  position: 4
  parent: laser_chemistry/laser_chemistry-index.md
item_ids:
  - nautec:mixer
---

# <Color id="light_purple">Mixer</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="mixer" scale="2"/>
  Mixes items into Salt Water to make chemicals, Aquarine Steel Compound and machine parts.
</Column>

## <Color id="gold">Setting It Up</Color>

The Mixer takes a laser beam into any face and runs while at least 10 AP per tick arrives (configurable). One <ItemLink id="aquatic_catalyst"/> burning Prismarine Shards or Prismarine Crystal Shards is enough. One burning Prismarine Crystals is not; add a second source through a [Laser Junction](laser_manipulation.md).

Right-click it with an empty hand to open it. It has four ingredient slots, one output slot, an input tank and an output tank, each tank holding 32,000 mB (32 buckets, configurable).

* Right-click with a <ItemLink id="saltwater_bucket"/> to fill the input tank. Salt Water comes from the [Deep Sea Drain](drain.md), or from the Mixer itself with a Water Bucket and Salt.
* Right-click with an empty Bucket to take fluid out of the output tank.
* Hoppers and pipes can insert ingredients, fill the input tank and drain the output tank.
* Item results can be pulled out of the output slot.
* Every face does all of this by default. [Side Configuration](nautec:getting_started/utilities.md) sets each face separately for items and fluids.

The whisk spins while it works. The <ItemLink id="prism_monocle"/> shows the power arriving, and Jade also shows its tanks and mixing progress.

***

## <Color id="gold">Recipes</Color>

JEI lists the ingredients under Mixing.

| Result | Fluid used | Time |
|---|---|---|
| 1,000 mB Salt Water | 1,000 mB Water and 1 <ItemLink id="salt"/> | 5 seconds |
| 1,000 mB Electrolyte Algae Serum | 1,000 mB Salt Water | 10 seconds |
| 1,000 mB Etching Acid | 1,000 mB Salt Water | 7.5 seconds |
| 5 Aquarine Steel Compound | 1,000 mB Salt Water | 5 seconds |
| 1 <ItemLink id="burnt_coil"/> | 1,000 mB Salt Water | 10 seconds |
| 2 <ItemLink id="aquatic_chip"/>s | 1,000 mB Salt Water | 10 seconds |
| 1 <ItemLink id="pressure_synthesizer"/> | 8,000 mB Salt Water | 20 seconds |
| 1 <ItemLink id="atlantean_pressure_synthesizer"/> | 8,000 mB Salt Water | 30 seconds |

A recipe only starts when its result fits in the output, so empty the output tank or slot between batches. Losing power pauses a mix without resetting it.

<Color id="gold">Tip</Color>: while the output tank holds fluid, right-clicking with a bucket works on the output tank, so a Salt Water Bucket will not go in. Empty the output first.

See [Chemistry Fluids](chemistry_introduction.md) for what each fluid is for.

***

### <Color id="aqua">Mixer Recipe</Color>

The <ItemLink id="whisk"/> is repaired from a Broken Whisk, or crafted from Cast Iron Rods, a Prismarine Crystal Shard and an Aquarine Steel Ingot.

<RecipeFor id="mixer"/>
