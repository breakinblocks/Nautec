---
navigation:
  title: Abyssal Pressure Forge
  icon: nautec:pressure_forge
  position: 3
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:pressure_forge
---

# <Color id="light_purple">Abyssal Pressure Forge</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="pressure_forge" scale="2"/>
  Presses materials under deep water using a laser beam and Etching Acid.
</Column>

The Abyssal Pressure Forge makes Flawless Prismarine Crystals and Deep Steel Plating. It only runs deep underground and under a column of water, so build it on the sea floor or flood a deep shaft.

***

## <Color id="gold">Requirements</Color>

All of these have to be true at the same time. If any one of them stops being true, the current press starts again from zero.

* Depth: the Forge must be at Y 0 or lower (`pressureForgeDepth`). Each recipe also has its own maximum Y, listed below.
* Water: the 8 blocks directly above the Forge must all be water source blocks (`pressureForgeWaterColumn`). Depth alone is not enough; a dry shaft does not count.
* Power: a beam of at least 40 AP per tick (`pressureForgePowerUsage`). The Forge takes beams on its four sides and its top.
* Purity: the beam must meet the recipe's purity.
* Etching Acid: at least 250 mb in the tank. Each finished press uses 250 mb (`pressureForgeAcidUsage`). The tank holds 4,000 mb (`pressureForgeCapacity`).

All of these values are in `config/nautec-common.toml`.

***

## <Color id="gold">Recipes</Color>

| Input | Output | Purity | Max Y | Time |
|---|---|---|---|---|
| Resonant Shard | Flawless Prismarine Crystal | 2.0 | -20 | 10 seconds |
| Aquarine Steel Ingot | Deep Steel Plating | 2.5 | -40 | 15 seconds |

The recipes are also shown in JEI. See [Deep Materials](materials.md) for what the outputs are used for.

***

## <Color id="gold">Using It</Color>

Right-click with an item to put it in, and with an <ItemLink id="etching_acid_bucket"/> to fill the tank. Right-click with an empty hand to take the output, or the input when the output slot is empty.

For automation, items go in from the top and the four sides, finished items come out of the bottom, and Etching Acid can be piped in from any side. Since the top has to stay under water, feed it from the sides. The Forge presses one item at a time and only finishes a press when there is room in the output slot.

Look at it through a <ItemLink id="prism_monocle"/> to check it. It shows "Under pressure" with the acid level and beam purity when it is set up correctly, or the depth and water it still needs when it is not.

<Color id="gold">Tip</Color>: Laser beams pass through water. A <ItemLink id="bacterial_fuel_cell"/> at full purity (2.5) covers both recipes, and so does a <ItemLink id="prismarine_crystal"/> behind one [Prismatic Mirror](beam_optics.md) (3.0 x 0.9 = 2.7).

***

### <Color id="aqua">Abyssal Pressure Forge Recipe</Color>

<Recipe id="nautec:pressure_forge"/>
