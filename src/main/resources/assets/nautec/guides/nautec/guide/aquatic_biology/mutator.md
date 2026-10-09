---
navigation:
  title: Mutator
  icon: nautec:mutator
  position: 4
  parent: aquatic_biology/aquatic_biology-index.md
item_ids:
  - nautec:mutator
---

# <Color id="light_purple">Mutator</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="mutator" scale="2"/>
  The Mutator turns a colony of one strain into another.
</Column>

Put the colony in the left bacteria slot and the catalyst in the item slot. Every 12 seconds the Mutator makes one attempt, and keeps making attempts until one succeeds. The right bacteria slot must be empty before it starts, so take each new colony out as soon as it appears.

The catalyst is not used up, so one item covers every attempt. Hoppers and pipes can insert it, through any face by default ([Side Configuration](nautec:getting_started/utilities.md)).

The two slots at the bottom left are the Dish Port: a dish goes in on the left and new mutations come out on the right. A Petri Dish holding a colony loads it into the left bacteria slot when that slot is free and the catalyst in the Mutator works on that strain. The emptied dish stays in the port, and as soon as the mutation appears in the right bacteria slot it takes it out and moves it to the output slot. That frees the port for the next colony, so a pipe feeding colony dishes in and another pulling mutations out keeps the Mutator working with no empty dishes to handle. The port only accepts a dish it can use right then, so put the catalyst in first, and a pipe keeps hold of anything else instead of jamming the machine.

 JEI's Bacteria Mutations category lists the catalyst and base chance for every mutation.

***

## <Color id="gold">Power</Color>

It needs a beam of at least 10 AP entering its top or bottom face. Beams into both faces add together. Below 10 AP it pauses. The time (240 ticks) and the power are configurable.

***

## <Color id="gold">Success</Color>

The chance shown in JEI is the best case. Two things lower it:

* Mutation Resistance: at the resistance cap the chance is halved.
* Size: at the size cap the chance is a quarter lower.

A 25% mutation on a fresh colony of 400 with 0.05 resistance succeeds about 24% of the time. The same mutation on a full 40,000 colony drops to about 18%.

On a success the whole input colony is used up and the new strain appears in the right slot. It starts at its own starting size, whatever size the parent was. It inherits the parent's four stats with a small random shift, starts at full Vitality, and comes out unanalyzed.

***

## <Color id="gold">Failure</Color>

A failed attempt kills a quarter of the colony, reduced by its Mutation Resistance: a colony at the resistance cap loses nothing. A failure never takes the last of a colony, so the Mutator always keeps trying. A filled [booster](#booster) slot stops failures from costing anything.

Since the new colony's size does not depend on the parent, mutate colonies while they are small. Growing a colony first only lowers the chance and gives failures more to eat.

***

## <Color id="gold">Stat Breeding</Color>

Mutation is the only thing that changes a colony's stats, and a colony keeps its stats through each step. Analyze each result in the [Bacterial Analyzer](bacterial_analyzer.md) before you commit to it.

You do not have to change strain to breed stats. Use the storage block of the item a colony makes as the catalyst (Iron Blocks for Ferrophiles, Hay Bales for Rhizobacteria), or the item itself if it has no storage block, and the Mutator refines the colony instead. A refine has a 25% base chance, lowered by size and Mutation Resistance like any other attempt. A success keeps the strain and the colony's size and shifts the stats the same way a mutation does, and a failure costs the same as a failed mutation. JEI lists each refine in the Bacteria Mutations category as a strain turning into itself.

***

## <Color id="gold">Booster</Color>

The slot to the right of the catalyst takes Electrolyte Algae Serum Vials. While it holds one, every attempt is three times as likely to succeed, whether it mutates or refines, and a failed attempt does not shrink the colony. A vial is only used up when an attempt succeeds, so each new colony costs one vial however many tries it took. Hoppers and pipes can fill it.

***

## <Color id="gold">Recipe</Color>

<Recipe id="nautec:mutator"/>

***

## <Color id="gold">Stronger Beams</Color>

The Mutator needs 10 AP. A stronger beam makes its attempts faster: its speed is the square root of (beam ÷ 10).

| Beam | Speed |
|---|---|
| 10 AP | ×1 (normal) |
| 40 AP | ×2 |
| 90 AP | ×3 |
| 160 AP | ×4 |

Each attempt has the same chance as before, so a faster Mutator simply gets more tries per minute. The 12 seconds per attempt is on a 10 AP beam.

See [Stronger Beams](nautec:getting_started/stronger_beams.md) for the rule and every machine it applies to.
