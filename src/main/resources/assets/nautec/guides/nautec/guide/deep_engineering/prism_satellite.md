---
navigation:
  title: Prism Satellite
  icon: nautec:prism_satellite
  position: 10
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:prism_satellite
  - nautec:uplink_array
  - nautec:downlink_array
---

# <Color id="aqua">Prism Satellite</Color>

<Column alignItems="center" fullWidth={true}>
  <Row>
    <ItemImage id="uplink_array" scale="2"/>
    <ItemImage id="prism_satellite" scale="2"/>
    <ItemImage id="downlink_array" scale="2"/>
  </Row>
  Carries laser power (AP) and FE anywhere on a network, with no relay chain.
</Column>

An <ItemLink id="uplink_array"/> takes AP from your lasers and beams it up to a satellite in orbit above it. The satellite beams it back down to every <ItemLink id="downlink_array"/> on the same network, however far away they are, and each downlink fires it into the laser blocks around it.

***

## <Color id="gold">Setting Up</Color>

1. Place an <ItemLink id="uplink_array"/>. It is two blocks tall.
2. Right-click it with an empty hand and pick or create a network, the same way as on a <ItemLink id="resonance_pylon"/>. Pylons, arrays and [Resonance Nodes](resonance_node.md) share networks and their trusted players.
3. Right-click the uplink with a <ItemLink id="prism_satellite"/> to launch it. The satellite climbs into orbit and stays there until the uplink is broken, which drops it back.
4. Fire lasers into the uplink's sides or underside.
5. Place a <ItemLink id="downlink_array"/> where the power is needed, put it on the same network, and point a laser block's input at one of its sides or its underside.

***

## <Color id="gold">Rules</Color>

* **Sky:** both arrays need open sky above the dish. Only solid blocks get in the way. Water, glass and leaves count as open, so arrays on the sea floor or under a glass roof work.
* **Dimension:** every uplink with a launched satellite feeds one pool for the whole network, in every dimension. A downlink in a dimension with no uplink of its own still gets power, but its AP arrives with 5% less purity (`satelliteCrossDimensionPurityLoss`). Build an uplink in that dimension to avoid the loss.
* **Sharing:** downlinks with the highest priority are filled first, and downlinks with the same priority split evenly. 10% is lost on the way. Purity carries through unchanged within a dimension that has an uplink.
* **Several uplinks:** every uplink on a network adds to the same pool, whatever dimension it is in. Their beams merge, so the pool's purity stays close to the purest uplink's.
* **Loaded chunks:** an array only works while its chunk is loaded. Turn on **Load** in its screen and it keeps its own chunk loaded, unless the server has turned that off (`resonanceChunkLoading`).

The screen shows whether the array is online and, if not, what it is waiting for, along with how much AP and FE it holds.

***

## <Color id="gold">Buffers and FE</Color>

Each array holds up to 5,000,000 AP and 5,000,000 FE (`satelliteApBuffer` and `satelliteFeBuffer`).

An uplink stores the AP beamed into it, along with its purity, and also takes FE from any cable or machine that pushes into it. It keeps both until downlinks on the network want them, so an uplink can fill up before its downlinks are built.

A downlink stores what it takes from the network. It fires its AP into the laser blocks around it and pushes its FE into anything next to it that takes FE, so it keeps supplying power through a short break in the uplinks.

***

## <Color id="gold">Priority and Limits</Color>

The two small controls at the top of a downlink's screen set its priority and its limit.

Priority runs from -100 to 100. Each tick the network fills the highest priority first, as far as each downlink's limit and free space allow, and only then moves on to the next priority. Downlinks that share a priority split what is left evenly. Click the number to reset it to 0.

The limit is the most AP, and separately the most FE, the downlink takes each tick. It steps through set values up to the server's maximum (`satelliteTransferLimit`, 100,000 by default). Click the number to reset it to that maximum.

Output [Resonance Nodes](resonance_node.md) and Resonance Charms bound to the network take part in the same priority order, so a node or a charm can be set to draw before or after your downlinks. See [Resonance Network](resonance_network.md) for the charm.

A <ItemLink id="configuration_card"/> copies a downlink's network, priority and limit onto another downlink.
