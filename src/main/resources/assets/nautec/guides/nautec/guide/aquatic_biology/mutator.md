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

The catalyst is not used up, so one item covers every attempt. JEI's Bacteria Mutations category lists the catalyst and base chance for every mutation.

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

A failed attempt kills a quarter of the colony, reduced by its Mutation Resistance: a colony at the resistance cap loses nothing. A failure never takes the last of a colony, so the Mutator always keeps trying.

Since the new colony's size does not depend on the parent, mutate colonies while they are small. Growing a colony first only lowers the chance and gives failures more to eat.

***

## <Color id="gold">Stat Breeding</Color>

Mutation is the only thing that changes a colony's stats, and a colony keeps its stats through each step. A long chain of mutations is how you build a colony worth putting in the [Bio Reactor](bio_reactor.md). Analyze each result in the [Bacterial Analyzer](bacterial_analyzer.md) before you commit to it.

***

## <Color id="gold">Recipe</Color>

<Recipe id="nautec:mutator"/>
