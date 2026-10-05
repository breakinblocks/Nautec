---
navigation:
  title: Combustion Dynamo
  icon: nautec:combustion_dynamo
  position: 16
  parent: getting_started/getting_started-index.md
item_ids:
  - nautec:combustion_dynamo
  - nautec:kelp_slurry
  - nautec:algal_lipid
---

# <Color id="light_purple">Combustion Dynamo</Color>

<Column alignItems="center" fullWidth={true}>
  <Row>
    <ItemImage id="combustion_dynamo" scale="2"/>
  </Row>
  An engine that burns oil and water into Forge Energy (FE).
</Column>

The Combustion Dynamo makes 320 FE/t while it has oil and water, and pushes it into any cable or machine touching it. Oil comes from a bacteria strain that turns kelp into lipids, so a kelp farm and a Bio Reactor keep it running forever.

***

## <Color id="gold">Running It</Color>

* **Oil.** Any oil in the `c:oil` tag works, NauTec's own and other mods'. It burns 1 mB every 10 ticks, so one bucket lasts over 8 minutes.
* **Water.** It also uses 1 mB of water every tick, 20 mB a second.
* **Tanks.** It has a separate 8,000 mB tank for each. Pipe both fluids into any side: oil only goes into the oil tank and water only into the water tank, so one pipe can carry both.
* **Additive.** Put Redstone Dust in the slot to raise output to 480 FE/t. One dust lasts a minute of burning and makes the engine use 10% more oil and water while it lasts. The timer only runs while the engine is burning.

The screen shows the output, what the engine is missing, and how much boost is left. Jade shows the same from outside.

***

## <Color id="gold">Making Oil</Color>

<Row>
  <ItemImage id="kelp_slurry"/>
  ### <Color id="aqua">Kelp Slurry</Color>
</Row>

Mix 4 Kelp with 1,000 mB of Salt Water in the [Mixer](nautec:laser_chemistry/mixer.md) to make 2 Kelp Slurry.

<Row>
  <ItemImage id="algal_lipid"/>
  ### <Color id="aqua">Lipophiles and Algal Lipid</Color>
</Row>

Lipophiles are a bacteria strain that makes Algal Lipid. Mutate them from Halotrophs in the [Mutator](nautec:aquatic_biology/mutator.md) with Kelp Slurry as the catalyst (10% a try), then grow and feed them on Kelp Slurry like any other strain. A colony in a [Bio Reactor](nautec:aquatic_biology/bio_reactor.md) turns out a steady flow of Algal Lipid.

### <Color id="aqua">Oil</Color>

Mix 1 Algal Lipid with 250 mB of Salt Water in the Mixer to make 1,000 mB of oil.

<RecipeFor id="combustion_dynamo"/>
