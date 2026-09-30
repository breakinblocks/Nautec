---
navigation:
  title: Augmentation Station
  icon: nautec:augmentation_station
  position: 1
  parent: laser_augmentation/laser_augmentation-index.md
item_ids:
  - nautec:augmentation_station
  - nautec:augmentation_station_extension
  - nautec:claw_robot_arm
---

# <Color id="light_purple">Augmentation Station</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="augmentation_station" scale="2"/>
  The Augmentation Station is the multiblock that installs augments into your body.
</Column>

You stand on the middle block, the four extensions around you hold the part, and their robot arms fit it.

***

## <Color id="gold">Building It</Color>

The structure is one layer, 5 by 5, with the corners left empty:

* The Augmentation Station in the middle.
* Polished Prismarine directly north, south, east and west of it.
* Aquarine Steel Blocks on the four diagonals.
* An Augmentation Station Extension two blocks out on each side.

<GameScene zoom="4" background="#333333" interactive={true}>
  <Block id="nautec:augmentation_station_extension" x="2" y="0" z="0"/>
  <Block id="nautec:aquarine_steel_block" x="1" y="0" z="1"/>
  <Block id="nautec:polished_prismarine" x="2" y="0" z="1"/>
  <Block id="nautec:aquarine_steel_block" x="3" y="0" z="1"/>
  <Block id="nautec:augmentation_station_extension" x="0" y="0" z="2"/>
  <Block id="nautec:polished_prismarine" x="1" y="0" z="2"/>
  <Block id="nautec:augmentation_station" x="2" y="0" z="2"/>
  <Block id="nautec:polished_prismarine" x="3" y="0" z="2"/>
  <Block id="nautec:augmentation_station_extension" x="4" y="0" z="2"/>
  <Block id="nautec:aquarine_steel_block" x="1" y="0" z="3"/>
  <Block id="nautec:polished_prismarine" x="2" y="0" z="3"/>
  <Block id="nautec:aquarine_steel_block" x="3" y="0" z="3"/>
  <Block id="nautec:augmentation_station_extension" x="2" y="0" z="4"/>
  <IsometricCamera yaw="225" pitch="30"/>
</GameScene>

Right-click the middle Augmentation Station block with the <ItemLink id="aquarine_steel_wrench"/> without crouching to form it. All four extensions have to be in place, even if you only load one of them. Breaking any block of the formed station unforms all of it; replace the block and use the wrench again.

***

## <Color id="gold">Loading an Extension</Color>

Right-click a formed extension to open it. It has two slots, each holding one item:

* The top slot takes the <ItemLink id="claw_robot_arm"/>. Without it the extension is ignored.
* The bottom slot takes the part you want installed.

Load exactly one extension with a part. Every current augment uses a single part, and the station only recognises the recipe when the number of loaded extensions matches it, so a second loaded extension stops anything from being offered. Leave the others empty, or take their arms out.

***

## <Color id="gold">Powering It</Color>

The extension holding the part needs a laser beam of at least 25 AP coming up into it from the block below. The power is only checked, not used up, but it has to stay at 25 AP or more for the whole install. Empty extensions need no beam.

Since the beam has to enter from underneath, put your catalyst or relay under the floor and fire it straight up. See [Laser Power](nautec:getting_started/laser_power.md) for getting a beam started.

The 25 AP threshold is configurable (`augmentationPowerRequirement`).

***

## <Color id="gold">Using It</Color>

Stand on the middle block. After half a second a screen opens showing the part and the body slots it fits. Pick a slot and press Apply. If you closed the screen, step off and back on to open it again.

For the next four seconds you are held in place while the robot arms work. Stepping off, the beam dropping below 25 AP, or the part being taken out cancels the install and nothing is used. When it finishes, the part is used up, the Claw Robot Arm stays, and the augment is in your chosen slot.

If the screen opens with no slots listed, the station does not recognise what is loaded. Check that the part sits in the bottom slot, that exactly one extension is loaded, and that its arm is in the top slot.

[Player Augmentation](augmentation.md) lists what each slot takes and how replacing an augment works.

***

## <Color id="gold">Recipes</Color>

<Recipe id="nautec:augmentation_station"/>

<Recipe id="nautec:augmentation_station_extension"/>

<Recipe id="nautec:claw_robot_arm"/>
