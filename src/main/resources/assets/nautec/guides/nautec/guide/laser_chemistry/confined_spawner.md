---
navigation:
  title: Confined Spawner
  icon: nautec:spawner_confinement_matrix
  position: 12
  parent: laser_chemistry/laser_chemistry-index.md
item_ids:
  - nautec:spawner_confinement_matrix
---

# <Color id="light_purple">Confined Spawner</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="spawner_confinement_matrix" scale="2"/>
  Turns a mob spawner into a machine that makes the mob's drops straight into its own storage, without spawning a single mob.
</Column>

## <Color id="gold">Confining a Spawner</Color>

Use a Spawner Confinement Matrix on a spawner. The cage gains a band of glowing runes and a metal frame, and becomes a Confined Spawner. It keeps everything about the original spawner: the mob, how many it spawns at once, how often, and any upgrades. Spawners improved with Apothic Spawners keep their spawn count and delay and run faster for them.

Sneak and use the Confined Spawner with an empty hand, or with a Matrix, to release it. The spawner goes back exactly as it was and the Matrix returns to your inventory. Anything still in the storage drops on the ground.

Breaking a Confined Spawner with a pickaxe drops the Confined Spawner itself, with or without Silk Touch. It keeps the mob, the spawner's settings and upgrades, and the filter, so you can move it and place it back exactly as it was. Anything still in the storage drops on the ground. To get a plain spawner back instead, release it first.

## <Color id="gold">Power</Color>

Aim a laser beam into any face. The spawner holds up to 500 AP and spends 50 AP each tick while it runs, so a beam of 50 AP or more keeps it running nonstop. A weaker beam still works, in bursts, as the buffer refills. See [Laser Power](nautec:getting_started/laser_power.md) for ways to supply it.

It runs whenever it has power and a free storage slot, with no player needed nearby and no light or space rules. The runes brighten and the mob inside spins faster while it is working.

## <Color id="gold">Drops</Color>

Each cycle takes as long as the spawner's own spawn delay. At the end of it, the spawner rolls the mob's loot once for every mob it would have spawned, counted as a player kill, so drops that need a player kill are included. The results go into 54 slots of storage, the same as a double chest.

When every slot is taken, the spawner pauses and keeps its buffer full until a slot opens up. Any part of a roll that does not fit is lost.

Hoppers and pipes pull drops out from any side. Only the spawner puts items in.

## <Color id="gold">The Screen</Color>

Right-click the spawner to see its storage and take items out. The bar in the corner shows stored AP, with the progress of the current cycle underneath. The status beside the title says why it is stopped when it is not running.

## <Color id="gold">Filtering Drops</Color>

The funnel button beside the title opens the filter panel, with 18 filter slots.

* Click a slot while holding an item to filter that item. You can also drag an item in from JEI.
* Right-click a filled slot to step through the item's tags, then back to the exact item. A tag filter matches every item in that tag and shows a gold **#**.
* Click a slot with an empty hand to clear it.

The button at the top switches between two modes:

* **Blacklist** makes everything except the filtered drops.
* **Whitelist** makes only the filtered drops.

Filtered drops are never made, so they never take up storage. A whitelist with nothing in it stops the spawner until you add a filter.

## <Color id="gold">Jade</Color>

Looking at a Confined Spawner with Jade shows its mob, whether it is running, its stored AP and what is in its storage.

***

### <Color id="aqua">Spawner Confinement Matrix Recipe</Color>

<RecipeFor id="spawner_confinement_matrix"/>
