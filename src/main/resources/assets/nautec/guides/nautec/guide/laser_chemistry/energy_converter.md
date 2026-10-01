---
navigation:
  title: Energy Converter
  icon: nautec:energy_converter
  position: 1
  parent: laser_chemistry/laser_chemistry-index.md
item_ids:
  - nautec:energy_converter
  - nautec:creative_power_source
  - nautec:creative_energy_source
---

# <Color id="light_purple">Energy Converter</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="energy_converter" scale="2"/>
  Turns Forge Energy (FE) from other mods into a laser beam.
</Column>

The conversion goes one way only: FE in, AP out.

***

## <Color id="gold">Using It</Color>

Push FE into any side with a cable or generator from another mod. It holds up to 100,000 FE.

Each tick it turns up to 100 FE into 100 AP and fires it out of every side that has a laser receiver in line, up to 16 blocks away. The 100 AP is split evenly between the connected receivers, rounded down, so two receivers get 50 AP each and three get 33. It only spends FE while at least one receiver is connected, so a converter with nothing to power keeps its buffer.

Its beam has a purity of 0. That runs the [Mixer](mixer.md), the [Deep Sea Drain](drain.md), the [Charger](charger.md) and any Item Transformation that needs no purity. For purity, use an Aquatic Catalyst or see [Beam Optics](nautec:deep_engineering/beam_optics.md).

<Recipe id="nautec:energy_converter"/>

***

<Row>
  <ItemImage id="creative_power_source"/>
  ### <Color id="aqua">Creative Power Source</Color>
</Row>

A creative-only laser source for testing builds. It needs no fuel and sends 100 AP per tick at purity 0, split evenly between every side that has a receiver in line. It is in the NauTec creative tab.

***

<Row>
  <ItemImage id="creative_energy_source"/>
  ### <Color id="aqua">Creative Energy Source</Color>
</Row>

An endless supply of FE for testing an Energy Converter. It does not push energy out, so connect it with a cable that pulls from it. It is in the NauTec creative tab.
