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
  Mixes items into Salt Water to make chemicals and Aquarine Steel Compound.
</Column>

## <Color id="gold">Setting It Up</Color>

The Mixer takes a laser beam into any face and runs while at least 10 AP per tick arrives (configurable). One <ItemLink id="aquatic_catalyst"/> burning Prismarine Shards or Prismarine Crystal Shards is enough. One burning Prismarine Crystals is not; add a second source through a [Laser Junction](laser_manipulation.md).

Right-click it with an empty hand to open it. It has four ingredient slots, one output slot, an input tank and an output tank, each tank holding 1,000 mB (configurable).

* Right-click with a <ItemLink id="saltwater_bucket"/> to fill the input tank.
* Right-click with an empty Bucket to take fluid out of the output tank.
* Hoppers and pipes on any of the four sides can insert ingredients, fill the input tank and drain the output tank. The top and bottom take nothing.
* Take item results out of the output slot by hand.

The whisk spins while it works. The <ItemLink id="prism_monocle"/> shows the power arriving, and Jade also shows its tanks and mixing progress.

***

## <Color id="gold">Recipes</Color>

Every recipe uses 1,000 mB of Salt Water. JEI lists the ingredients under Mixing.

| Result | Time |
|---|---|
| 1,000 mB Electrolyte Algae Serum | 10 seconds |
| 1,000 mB Etching Acid | 7.5 seconds |
| 5 Aquarine Steel Compound | 5 seconds |

A recipe only starts when its result fits in the output, so empty the output tank or slot between batches. Losing power pauses a mix without resetting it.

<Color id="gold">Tip</Color>: while the output tank holds fluid, right-clicking with a bucket works on the output tank, so a Salt Water Bucket will not go in. Empty the output first.

See [Chemistry Fluids](chemistry_introduction.md) for what each fluid is for.

***

### <Color id="aqua">Mixer Recipe</Color>

The <ItemLink id="whisk"/> is repaired from a Broken Whisk.

<RecipeFor id="mixer"/>
