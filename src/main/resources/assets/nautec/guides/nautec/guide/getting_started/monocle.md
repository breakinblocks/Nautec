---
navigation:
  title: Prism Monocle
  icon: nautec:prism_monocle
  position: 8
  parent: getting_started/getting_started-index.md
item_ids:
  - nautec:prism_monocle
---

# <Color id="light_purple">Prism Monocle</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="prism_monocle" scale="2"/>
  Shows a readout for the NauTec block you are looking at.
</Column>

Wear the monocle in your helmet slot or in its own Monocle curio slot. While it is on, looking at a NauTec machine or laser block shows its readout in the middle of the screen. It also clears the underwater fog, the same as the Diving Helmet.

<Recipe id="nautec:prism_monocle"/>

***

## <Color id="gold">Readouts</Color>

| Block | Shows |
|---|---|
| Any machine, relay, optic or other laser block | Power arriving (AP per tick) and Purity |
| <ItemLink id="aquatic_catalyst"/> | Duration: ticks the current fuel item has been burning |
| <ItemLink id="bacterial_fuel_cell"/> | Output (AP per tick), Purity and Fuel, or No colony |
| <ItemLink id="pressure_forge"/> | Under pressure, Acid (mb) and Purity, or what depth and water it still needs |
| <ItemLink id="resonance_chamber"/> | Charge, Ceiling and Purity, or Cracked while it cools down |
| <ItemLink id="gateway"/> and its ring | Its Address; whether it is open, ready, idle, out of power, held shut by redstone or blocked; whether it is a wild ring; and its stored power |
| <ItemLink id="submarine_dock"/> | Whether a Sea Scout is docked |
| Deep Sea Drain wall | Power and Fluid Stored |

Use it to check that a beam is arriving and pure enough before you blame a recipe. Purity drops when a pure beam is averaged with a weaker one feeding the same block, and when it passes through a mirror or splitter.

***

## <Color id="gold">With Jade</Color>

If Jade is installed it adds its own tooltips for some blocks: the Aquatic Catalyst's status, queued fuel, remaining ticks and AP per tick, a Crate's locked state, and the Mixer and Laser Junction.
