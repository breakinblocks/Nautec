---
navigation:
  title: Bacterial Analyzer
  icon: nautec:bacterial_analyzer
  position: 2
  parent: aquatic_biology/aquatic_biology-index.md
item_ids:
  - nautec:bacterial_analyzer
---

# <Color id="light_purple">Bacterial Analyzer</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="bacterial_analyzer" scale="2"/>
  The Bacterial Analyzer reveals a colony's size, Vitality and stats.
</Column>

Put a <ItemLink id="petri_dish"/> holding an unanalyzed colony in the left slot. After 3 seconds the analyzed dish moves to the right slot. Empty the right slot before the next dish can start.

The machines all work on unanalyzed colonies too. Analysis only lets you read the numbers, but you need those numbers to choose which colony to mutate, grow or burn. A colony that comes out of the [Mutator](mutator.md) is unanalyzed again.

***

## <Color id="gold">Setup</Color>

The Analyzer is two blocks tall, so it needs a free block above it when you place it. Right-click either half to open it.

It needs a beam of at least 5 AP entering its bottom face from below. The power is a threshold, not a cost: below 5 AP it pauses, and at or above it runs at full speed.

Both the time (60 ticks) and the power (5 AP) are configurable.

***

## <Color id="gold">Reading an Analyzed Dish</Color>

The tooltip shows Size and Vitality. Hold Shift to see the item the strain produces and its four stats. A stat shown in red is at its cap. [Bacteria Stats](bacteria_stats.md) explains each one.

***

## <Color id="gold">Recipe</Color>

<Recipe id="nautec:bacterial_analyzer"/>
