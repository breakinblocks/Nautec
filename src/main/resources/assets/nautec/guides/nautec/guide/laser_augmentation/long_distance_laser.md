---
navigation:
  title: Long Distance Laser
  icon: nautec:long_distance_laser
  position: 7
  parent: laser_augmentation/laser_augmentation-index.md
item_ids:
  - nautec:long_distance_laser
---

# <Color id="light_purple">Long Distance Laser</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="long_distance_laser" scale="2"/>
  The Long Distance Laser relays a beam up to 64 blocks, where other laser blocks reach 16.
</Column>

It takes a beam in at the back and sends the same power and purity out of the front. Use it to carry a beam across open water or down to a machine far below.

***

## <Color id="gold">Placing It</Color>

The front, where the beam leaves, faces you when you place it. So stand where the beam should go and look back toward the source when you place it. If it ends up facing the wrong way, right-click it with the <ItemLink id="aquarine_steel_wrench"/> in Rotate mode (sneak and right-click the air to change the wrench's mode); each click turns it to the next of the six directions.

The beam only connects to a block that takes a beam on the side facing it, such as a machine, a <ItemLink id="prismarine_laser_relay"/> or another Long Distance Laser. It passes through water, kelp and seagrass but stops at the first solid block, glass included. A new connection can take up to half a second to appear.

Like any beam, it burns mobs and players that stand in it.

Both ranges are configurable: `longDistanceLaserDistance` for this block (default 64) and `laserDistance` for the others (default 16).

***

## <Color id="gold">Recipe</Color>

<Recipe id="nautec:long_distance_laser"/>
