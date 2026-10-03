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
  Carries laser power (AP) across a whole dimension with no relay chain.
</Column>

An <ItemLink id="uplink_array"/> takes AP from your lasers and beams it up to a satellite in orbit above it. The satellite beams it back down to every <ItemLink id="downlink_array"/> on the same network, however far away they are, and each downlink fires it into the laser blocks around it.

***

## <Color id="gold">Setting Up</Color>

1. Place an <ItemLink id="uplink_array"/>. It is two blocks tall.
2. Right-click it with an empty hand and pick or create a network, the same way as on a <ItemLink id="resonance_pylon"/>. Pylons and arrays share networks and their trusted players.
3. Right-click the uplink with a <ItemLink id="prism_satellite"/> to launch it. The satellite climbs into orbit and stays there until the uplink is broken, which drops it back.
4. Fire lasers into the uplink's sides or underside.
5. Place a <ItemLink id="downlink_array"/> where the power is needed, put it on the same network, and point a laser block's input at one of its sides or its underside.

***

## <Color id="gold">Rules</Color>

* **Sky:** both arrays need open sky above the dish. Water counts as open, so arrays on the sea floor work.
* **Dimension:** AP stays in the dimension it was sent from. Build an uplink with its own satellite in every dimension that needs one.
* **Sharing:** the downlinks split what the uplinks send equally, less 10%. Purity carries through unchanged.
* **Several uplinks:** every uplink on a network in the same dimension adds to the same pool.
* **Loaded chunks:** both ends only work while their chunks are loaded.

The screen shows whether the array is online and, if not, what it is waiting for.
