---
navigation:
  title: Gateway
  icon: nautec:gateway
  position: 2
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:gateway
---

# <Color id="light_purple">Gateway</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="gateway" scale="2"/>
  Teleports whatever stands on it to the nearest other Gateway with the same address.
</Column>

A Gateway is a flat plate. It needs no power, no beam and no fuel. Place two with the same address and anything that steps onto one is sent to the other.

Gateways also generate on the ocean floor as small prismarine pads. Every generated Gateway has the default address, four Cyan fins, and so does every Gateway you craft. A new Gateway on the default address joins that network straight away.

***

## <Color id="gold">Addresses</Color>

A Gateway has four coloured fins, one on each corner of its top. The four colours together are its address. Each fin can be one of eight colours, giving 4,096 addresses:

White, Light Blue, Cyan, Blue, Purple, Magenta, Lime and Black.

Setting a fin costs one dye of the new colour. There are two ways to do it:

* Right-click a corner of the top face with one of those dyes. That corner's fin changes and one dye is used.
* Right-click the Gateway with an empty hand to open the address screen. Rows 1 to 4 are the north-west, north-east, south-west and south-east fins. Pick a colour for each, check the cost line, and press Set. It takes one dye from your inventory for each fin you changed.

Look at a Gateway through a <ItemLink id="prism_monocle"/> to read its address.

Breaking a Gateway keeps its address on the dropped item, so you can move it without dyeing it again.

***

## <Color id="gold">Travelling</Color>

Step onto a Gateway and within half a second you are moved to the nearest other Gateway with the same address. Anything you are riding goes with you, along with anyone else riding it, so a <ItemLink id="submarine"/> arrives with its crew on board. Mobs and dropped items travel too.

After arriving, you cannot use a Gateway for 5 seconds (`gatewayCooldown` in `nautec-common.toml`, 100 ticks). Step off the destination plate before that runs out, or it sends you straight back.

Things to check when a Gateway does not send you:

* The destination has to be in the same dimension, and its chunk has to be loaded. Gateways in unloaded chunks are skipped, so you may be sent to a closer loaded one instead.
* There has to be room for you (and your vehicle) on top of the destination.
* The destination is always the nearest match. If several Gateways share one address, give each pair its own address so you arrive where you meant to.

A Gateway with no match makes a low sound and puffs particles when something stands on it.

***

### <Color id="aqua">Gateway Recipe</Color>

<Recipe id="nautec:gateway"/>
