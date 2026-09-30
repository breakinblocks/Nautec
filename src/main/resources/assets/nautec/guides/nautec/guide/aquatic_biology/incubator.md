---
navigation:
  title: Incubator
  icon: nautec:incubator
  position: 5
  parent: aquatic_biology/aquatic_biology-index.md
item_ids:
  - nautec:incubator
---

# <Color id="light_purple">Incubator</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="incubator" scale="2"/>
  The Incubator grows a colony and restores its Vitality.
</Column>

Put the colony in the bacteria slot and its nutrient in the item slot. Each strain has its own nutrient, listed in JEI's Bacteria Incubation category along with its growth range and consume chance. The Incubator only starts when the nutrient matches the strain.

Cyanobacteria, Halobacteria, Methanogens and Thermophiles have no nutrient. [Mutate](mutator.md) them first.

***

## <Color id="gold">Each Cycle</Color>

A cycle takes 5 seconds. At the end of it:

* The colony grows by a roll from the recipe's growth range, multiplied by its Growth Rate.
* The nutrient has the recipe's consume chance of being used up. Growth happens either way.
* The colony's age resets, so its Vitality is back to 100%.

Lithophiles on stone, for example, grows 8 to 25 per cycle with a 5% consume chance. A colony with a Growth Rate of 1 gains about 16 every 5 seconds and eats about one stone every 20 cycles. At Growth Rate 5 it gains five times as much from the same stone.

Growth stops exactly at the size cap of 40,000 (configurable).

***

## <Color id="gold">Power</Color>

It needs a beam of at least 20 AP entering its top or bottom face, and beams into both faces add together. One <ItemLink id="aquatic_catalyst"/> gives 12 AP at most, so run one above and one below, or combine beams in a <ItemLink id="laser_junction"/> first. The cycle time (100 ticks) and the power are configurable.

***

## <Color id="gold">Feeding a Working Colony</Color>

The Incubator is the only way to reset a colony's age. Swap colonies between the [Bio Reactor](bio_reactor.md) and the Incubator before their Vitality runs out. One completed cycle is enough.

<Color id="gold">Tip</Color>: the Incubator only runs a colony that is below the size cap. A colony at exactly 40,000 cannot be fed, so take a colony out a little before it fills up if you plan to keep refreshing it.

***

## <Color id="gold">Recipe</Color>

<Recipe id="nautec:incubator"/>
