---
navigation:
  title: Bio Reactor
  icon: nautec:bio_reactor
  position: 6
  parent: aquatic_biology/aquatic_biology-index.md
item_ids:
  - nautec:bio_reactor
  - nautec:bacterial_containment_shield
---

# <Color id="light_purple">Bio Reactor</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="bio_reactor" scale="2"/>
  The Bio Reactor puts up to three colonies to work making items.
</Column>

Each colony makes the item its strain produces, one at a time, for as long as the reactor has power. Producing items does not use up the colony. Only age does, once the colony outlives its Lifespan.

***

## <Color id="gold">Building It</Color>

A 3 by 3 by 2 structure:

* Bottom layer: Dark Prismarine Pillars in the corners, Bacteria Containment Shields on the edges, Polished Prismarine in the middle.
* Top layer: Dark Prismarine Pillars in the corners, Bacteria Containment Shields on the edges, the Bio Reactor in the middle.

<GameScene zoom="4" background="#333333" interactive={true}>
  <Block id="nautec:dark_prismarine_pillar" x="0" y="1" z="0"/>
  <Block id="nautec:bacterial_containment_shield" x="1" y="1" z="0"/>
  <Block id="nautec:dark_prismarine_pillar" x="2" y="1" z="0"/>
  <Block id="nautec:bacterial_containment_shield" x="0" y="1" z="1"/>
  <Block id="nautec:bio_reactor" x="1" y="1" z="1"/>
  <Block id="nautec:bacterial_containment_shield" x="2" y="1" z="1"/>
  <Block id="nautec:dark_prismarine_pillar" x="0" y="1" z="2"/>
  <Block id="nautec:bacterial_containment_shield" x="1" y="1" z="2"/>
  <Block id="nautec:dark_prismarine_pillar" x="2" y="1" z="2"/>
  <Block id="nautec:dark_prismarine_pillar" x="0" y="0" z="0"/>
  <Block id="nautec:bacterial_containment_shield" x="1" y="0" z="0"/>
  <Block id="nautec:dark_prismarine_pillar" x="2" y="0" z="0"/>
  <Block id="nautec:bacterial_containment_shield" x="0" y="0" z="1"/>
  <Block id="nautec:polished_prismarine" x="1" y="0" z="1"/>
  <Block id="nautec:bacterial_containment_shield" x="2" y="0" z="1"/>
  <Block id="nautec:dark_prismarine_pillar" x="0" y="0" z="2"/>
  <Block id="nautec:bacterial_containment_shield" x="1" y="0" z="2"/>
  <Block id="nautec:dark_prismarine_pillar" x="2" y="0" z="2"/>
  <IsometricCamera yaw="225" pitch="30"/>
</GameScene>

Right-click the Bio Reactor with an <ItemLink id="aquarine_steel_wrench"/>, without sneaking, to form it. If a block is wrong, chat names its position, what is there and what should be. Breaking any block of a formed reactor takes it apart again.

Once it is formed, right-click the Bio Reactor to open it.

***

## <Color id="gold">Power and Hatches</Color>

The reactor takes power through hatches. Right-click one of the four Bacteria Containment Shields in the top layer with the wrench to turn it into a hatch (click again to turn it back), then point a beam down into the hatch from above. You can open more than one hatch, and their power adds together.

| Colonies | AP needed |
|---|---|
| 1 | 50 |
| 2 | 75 |
| 3 | 100 |

That is 25 AP for the reactor plus 25 AP per colony, both configurable. The power is a threshold, not a cost. Below it nothing runs and all progress resets.

One <ItemLink id="aquatic_catalyst"/> gives 12 AP at most, so even four hatches of single catalysts fall short. Combine beams in a <ItemLink id="laser_junction"/> before they reach a hatch. A [Bacterial Fuel Cell](bacterial_fuel_cell.md) gives up to 48 AP from a single beam.

***

## <Color id="gold">Production Speed</Color>

Speed is Production Rate times a size factor that runs from 0.5 for a tiny colony to 1 at the size cap.

| Colony | Time per item |
|---|---|
| Production Rate 0.3, size 400 | about 6 seconds |
| Production Rate 1, size 20,000 | about 1.2 seconds |
| Production Rate 2, size 40,000 | under half a second |

The base speed is configurable. JEI's Bio Reactor category shows what each strain makes.

Each colony has its own output slot. Empty them often: once a slot holds a full stack, whatever that colony makes next is lost.

***

## <Color id="gold">Aging</Color>

Every tick the reactor has enough power, each colony in it ages by one tick, whether or not it makes anything. When its age passes its Lifespan the colony is Senescent, and every item it makes costs it a tenth of its size until the slot is empty.

Watch Vitality on the dish tooltip and move the colony to an [Incubator](incubator.md) before it hits 0%. One completed cycle resets its age. A fresh strain's Lifespan is at most about two minutes of work, so early on you will be swapping often.

***

## <Color id="gold">Recipes</Color>

<Recipe id="nautec:bio_reactor"/>

<RecipesFor id="bacterial_containment_shield"/>

<RecipeFor id="polished_prismarine"/>

<RecipeFor id="dark_prismarine_pillar"/>
