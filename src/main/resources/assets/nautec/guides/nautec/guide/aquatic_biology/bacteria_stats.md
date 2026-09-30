---
navigation:
  title: Bacteria Stats
  icon: nautec:petri_dish
  position: 3
  parent: aquatic_biology/aquatic_biology-index.md
---

# <Color id="light_purple">Bacteria Stats</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="petri_dish" scale="2"/>
  Size, Vitality and four stats decide what a colony is worth.
</Column>

Each colony rolls its stats from its strain's starting range when it is grafted or mutated. After that the stats only change when the colony mutates, and the new colony carries them forward with a small random shift. Breeding a strong colony means a chain of mutations. Growing, feeding and working a colony never changes its stats.

Analyze a dish in the [Bacterial Analyzer](bacterial_analyzer.md) to read them, then hold Shift over it.

***

## <Color id="gold">The Four Stats</Color>

| Stat | Cap | What it does |
|---|---|---|
| Growth Rate | 5 | Multiplies the size an [Incubator](incubator.md) cycle adds. |
| Mutation Resistance | 1 | Lowers the [Mutator](mutator.md)'s success chance by up to half, and cuts what a failed attempt costs down to nothing at the cap. Sets the purity of a [Bacterial Fuel Cell](bacterial_fuel_cell.md) beam. |
| Production Rate | 2 | Multiplies how fast the [Bio Reactor](bio_reactor.md) makes items. Sets a Fuel Cell's power and how fast it burns the colony. |
| Lifespan | 24,000 | The ticks a colony can work in the Bio Reactor before it starts dying off. |

All four caps are configurable. A stat shown in red in the tooltip is at its cap.

Every strain starts low: a Growth Rate between 0.4 and 1.2, a Production Rate between 0.08 and 0.7, a Mutation Resistance of 0.2 at most, and a Lifespan between 900 and 2,600 ticks, which is under a minute to just over two minutes of Bio Reactor work. JEI's Bio Reactor category shows each strain's starting Production Rate range.

***

## <Color id="gold">Size</Color>

Size is how much colony you have. A grafted or mutated colony starts somewhere between 120 and 600 and can grow to the size cap of 40,000 (configurable) in the Incubator.

* In the Bio Reactor, a colony at the cap works twice as fast as a tiny one.
* In the Mutator, a bigger colony has a slightly lower success chance, up to a quarter lower at the cap, and the new colony's size does not depend on the old one.
* In the Fuel Cell, size is simply how long the colony lasts.

***

## <Color id="gold">Vitality</Color>

Vitality is the part of the Lifespan a colony has left. It only drops while the colony sits in a powered Bio Reactor, by one tick of age per tick, whether or not the colony makes anything. Dishes, the Incubator, the Mutator and the Fuel Cell never age a colony.

The tooltip shows it as a percentage: green from 50%, yellow from 20%, red below that. At 0% it reads Senescent. A senescent colony loses a tenth of its size every time it makes an item in the Bio Reactor (configurable), so it shrinks fast: about half of it is gone after seven items.

Every completed [Incubator](incubator.md) cycle sets Vitality back to 100%. A senescent colony still burns at full strength in a Fuel Cell.
