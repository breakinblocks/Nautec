---
navigation:
  title: Crates and Etching
  icon: nautec:rusty_crate
  position: 6
  parent: getting_started/getting_started-index.md
item_ids:
  - nautec:etching_acid_bucket
  - nautec:crate
  - nautec:rusty_crate
---

# <Color id="light_purple">Crates and Etching</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="rusty_crate" scale="2"/>
  Pry Crates open with a Crowbar, and clean rusty salvage in Etching Acid.
</Column>

***

<Row>
  <ItemImage id="crate"/>
  ### <Color id="aqua">Crate</Color>
</Row>

A locked 27 slot container found in geodes and in vanilla shipwreck and ruin loot. Right-click it with a <ItemLink id="crowbar"/> to pry it open. After that it stays open and works like a barrel: right-click to use it, and hoppers can move items in and out once it is open.

Breaking a crate drops the crate itself with its contents still inside, opened or not, so you can carry a find home before opening it.

***

<Row>
  <ItemImage id="rusty_crate"/>
  ### <Color id="aqua">Rusty Crate</Color>
</Row>

A corroded crate from ruins and geodes. The Crowbar only works on a clean Crate, so etch a Rusty Crate first. It turns into an ordinary Crate with the same loot, which you then pry open.

Rusty Crates hold better salvage than plain ones. Break one to carry it; it keeps its loot the same way a Crate does.

***

## <Color id="gold">Etching</Color>

Drop an item into Etching Acid and it comes out cleaned after the recipe time, 8 to 10 seconds. The whole dropped stack converts at once.

| Input | Output | Time |
|---|---|---|
| Rusty Crate | Crate, loot kept | 10 s |
| <ItemLink id="rusty_gear"/> | <ItemLink id="gear"/> | 8 s |
| <ItemLink id="ancient_valve"/> | <ItemLink id="valve"/> | 10 s |

JEI lists these under Item Etching.

One etching in three uses up the Etching Acid source block the item is in. Flowing acid is never used up, and neither is the block under the pool. A single acid source in a one block deep pit is enough, as long as you keep a spare bucket to refill it.

***

<Row>
  <ItemImage id="etching_acid_bucket"/>
  ### <Color id="aqua">Etching Acid</Color>
</Row>

<RecipeFor id="etching_acid_bucket"/>

The [Mixer](nautec:laser_chemistry/mixer.md) makes it from Salt Water once you have one. Etching Acid is thick: you sink in it and wade slowly, and mobs path around it. The [Abyssal Pressure Forge](nautec:deep_engineering/pressure_forge.md) also runs on it.
