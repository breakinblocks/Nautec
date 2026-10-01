---
navigation:
  title: Deep Sea Drain
  icon: nautec:deep_sea_drain
  position: 5
  parent: laser_chemistry/laser_chemistry-index.md
item_ids:
  - nautec:deep_sea_drain
  - nautec:deep_sea_drain_wall
---

# <Color id="light_purple">Deep Sea Drain</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="deep_sea_drain" scale="2"/>
  A 3x3 multiblock that pumps Salt Water out of the ocean.
</Column>

## <Color id="gold">Building It</Color>

Lay a flat 3x3: the Deep Sea Drain in the middle and eight Deep Sea Drain Walls around it. Right-click the Deep Sea Drain with an <ItemLink id="aquarine_steel_wrench"/> to form it.

<GameScene zoom="4" background="#333333" interactive={true}>
  <Block id="nautec:deep_sea_drain_wall" x="0" y="0" z="0"/>
  <Block id="nautec:deep_sea_drain_wall" x="1" y="0" z="0"/>
  <Block id="nautec:deep_sea_drain_wall" x="2" y="0" z="0"/>
  <Block id="nautec:deep_sea_drain_wall" x="0" y="0" z="1"/>
  <Block id="nautec:deep_sea_drain" x="1" y="0" z="1"/>
  <Block id="nautec:deep_sea_drain_wall" x="2" y="0" z="1"/>
  <Block id="nautec:deep_sea_drain_wall" x="0" y="0" z="2"/>
  <Block id="nautec:deep_sea_drain_wall" x="1" y="0" z="2"/>
  <Block id="nautec:deep_sea_drain_wall" x="2" y="0" z="2"/>
  <IsometricCamera yaw="225" pitch="30"/>
</GameScene>

It only pumps in an ocean biome, and all nine blocks directly above it must be water. Building it on the sea floor covers both.

***

## <Color id="gold">Powering It</Color>

The drain needs more than 20 AP per tick (configurable). One Aquatic Catalyst gives 12, so merge two in a [Laser Junction](laser_manipulation.md), or use a stronger source.

The beam goes in through a laser port. With the wrench, right-click the outer side of one of the four middle wall pieces (not a corner). That face becomes the port; aim your beam into it. The drain has one port at a time, and setting a new one moves it.

***

## <Color id="gold">Running It</Color>

Shift-right-click the drain with an empty hand to open it. It only opens while it has power. The valve turns, then the lid swings open over a few seconds. Shift-right-click again to close it.

While open and powered it pumps 500 mB of Salt Water every second into a 128,000 mB tank (both configurable). It is the steady source of Salt Water for the [Mixer](mixer.md), since ocean water in a Bucket stays plain water unless a pack has turned on `collectSaltWater` in `config/nautec-common.toml`.

* Take Salt Water out with a pipe on the bottom of the centre block.
* Or right-click any part of the drain with a Bucket or other fluid container. A Bucket fills once the tank holds at least 1,000 mB.

Look at a wall piece with the <ItemLink id="prism_monocle"/> to see how much fluid is stored.

<Color id="gold">Careful</Color>: an open, powered drain pulls everything above it down in a bubble column. Anything that gets into the drain takes drowning damage, dropped items included. Close it before you swim over it.

***

### <Color id="aqua">Recipes</Color>

<RecipeFor id="deep_sea_drain"/>

<RecipeFor id="deep_sea_drain_wall"/>
