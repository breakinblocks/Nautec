---
navigation:
  title: Bacteria Introduction
  icon: nautec:petri_dish
  position: 0
  parent: aquatic_biology/aquatic_biology-index.md
item_ids:
  - nautec:petri_dish
---

# <Color id="light_purple">Bacteria Introduction</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="petri_dish" scale="2"/>
  Bacteria colonies turn laser power into a steady supply of items or into a beam of their own.
</Column>

Every colony belongs to a strain, has a size, and carries four stats that decide how fast it grows, how well it produces and how long it keeps working. You carry colonies around in Petri Dishes and move them between the biology machines.

***

## <Color id="gold">The Loop</Color>

* [Graft](bacteria_grafting.md) a first colony into an empty Petri Dish with a Grafting Tool.
* [Analyze](bacterial_analyzer.md) the dish to see its size and stats.
* [Mutate](mutator.md) it into the strain you want. The strains you graft from stone, sand, podzol and netherrack only exist to be mutated.
* [Incubate](incubator.md) it to grow it and to restore its Vitality.
* Put it to work in the [Bio Reactor](bio_reactor.md) for items, with its nutrient in the reactor so it does not age, or burn it in the [Bacterial Fuel Cell](bacterial_fuel_cell.md) for a beam.

[Bacteria Stats](bacteria_stats.md) explains what each number does.

***

## <Color id="gold">Petri Dish</Color>

<RecipeFor id="petri_dish"/>

A Petri Dish holds one colony and does not stack. An empty dish shows "Empty" as its bacteria name. An unanalyzed colony shows its strain and "???" for stats.

A colony in a dish is on hold: it neither grows nor ages there.

***

## <Color id="gold">Moving Colonies</Color>

The Incubator, Mutator and Bio Reactor screens have bacteria slots. Pick up a Petri Dish so it sits on your cursor and click a bacteria slot with it:

* An empty dish takes the whole colony out of the slot.
* A dish holding a colony puts it into the slot if the slot is empty.

Two colonies merge only when they are the same strain with exactly the same stats, which in practice means two halves of the same colony. The merged colony keeps the older of the two ages. A colony never grows past the size cap (40,000 by default, configurable), and anything over the cap stays in the dish.

The Bacterial Analyzer takes the dish itself in an item slot, and the Bacterial Fuel Cell is loaded by right-clicking it with the dish.

***

## <Color id="gold">JEI</Color>

JEI has a category for each step: Bacteria Grafting (where to graft each strain), Bacteria Mutations (catalyst and chance), Bacteria Incubation (nutrient, growth range and consume chance), Colony Feeding (the nutrient that keeps each strain from aging in a reactor) and Bio Reactor (what each strain makes, its Strain Yield and its starting Production Rate range). Look there for per-strain details.

Strains for other mods' metals and gems only appear when a mod in the pack adds that material.
