---
navigation:
  title: Production Rates
  icon: minecraft:clock
  position: 8
  parent: aquatic_biology/aquatic_biology-index.md
---

# <Color id="light_purple">Production Rates</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="minecraft:clock" scale="2"/>
  How fast a colony makes items in a reactor, and how much power that takes.
</Column>

The numbers here apply to both the [Bio Reactor](bio_reactor.md) and the [Industrial Bio Reactor](industrial_bio_reactor.md). The Industrial Bio Reactor is not faster per colony; it runs more colonies with more upgrades.

***

## <Color id="gold">The Formula</Color>

Every tick, a working colony adds progress:

progress per tick = Production Rate x size factor x base speed x Strain Yield x speed bonus

* Production Rate is the colony's stat, up to 2 ([Bacteria Stats](bacteria_stats.md)).
* The size factor is 0.5 + 0.5 x size / 40,000, so 0.5 for a tiny colony and 1 at the size cap.
* The base speed is 5.6 in both reactors, configurable for each.
* Strain Yield belongs to the strain, see the table below.
* The speed bonus is 1, plus 0.5 for each [Reactor Speed Upgrade](reactor_upgrades.md).

Every 100 progress completes a cycle, which puts 1 item into the colony's output slot, plus 1 more for each Reactor Yield Upgrade. Leftover progress carries into the next cycle. A minute is 1,200 ticks, so:

items per minute = progress per tick x 12 x items per cycle

***

## <Color id="gold">Strain Yield</Color>

| Strain Yield | NauTec strains | Other mods' materials |
|---|---|---|
| 1 | Lithophiles (stone), Silicophiles (sand), Calciophiles (bone meal), Cryobionts (ice), Carnivorous Bacteria (rotten flesh), and every wood, plant, crop, fungus and mushroom strain | aluminum, bauxite, brass, bronze, graphite, lead, pig iron, steel, tin and zinc ingots; apatite |
| 0.8 | Carbophages (coal), Metallophiles (copper ingots), Sulfurophiles (gunpowder) | silver, nickel, antimony, bismuth, boron, constantan, electrum, invar, rose gold, stainless steel and amethyst bronze ingots; amber, fluorite, jade, peridot, ruby, sapphire and topaz |
| 0.6 | Ferrophiles (iron ingots) | osmium, uranium, cobalt, titanium, tungsten, chromium, manganese, molybdenum, neodymium, niobium, thorium, vanadium, beryllium, monazite, cyanite, desh, hepatizon, iesnium, knightslime, slimesteel, lumium, signalum, manasteel, refined glowstone and refined obsidian ingots; certus quartz, black quartz, cinnabar, dark gem, moonstone and sunstone |
| 0.5 | Acidophiles (redstone), Azuritophiles (lapis lazuli) | |
| 0.4 | Aurrophiles (gold ingots) | platinum, iridium, palladium, enderium, elementium, manyullyn, queen's slime, blutonium, calorite, draconium and ostrum ingots |
| 0.12 | Adamantophiles (diamonds) | netherite, allthemodium, vibranium, unobtainium, neutronium, plutonium and terrasteel ingots |
| 0.1 | Smaragdophiles (emeralds) | |

Strains for other mods' materials only exist when a mod that uses that material is in the pack. Cyanobacteria, Halobacteria, Methanogens and Thermophiles make nothing, so their Strain Yield does not matter.

JEI's Bio Reactor category and an analyzed dish's tooltip both show a strain's Strain Yield. Packs can set it with the optional `production_multiplier` field in a strain's json, which defaults to 1.

***

## <Color id="gold">Worked Examples</Color>

A colony at the stat caps (Production Rate 2, size 40,000) with no upgrades:

| Strain Yield | Progress per tick | Time per item | Items per minute |
|---|---|---|---|
| 1 (stone) | 11.2 | 0.45 seconds | about 134 |
| 0.8 (coal, copper) | 8.96 | 0.56 seconds | about 108 |
| 0.6 (iron) | 6.72 | 0.74 seconds | about 81 |
| 0.5 (redstone, lapis) | 5.6 | 0.89 seconds | about 67 |
| 0.4 (gold) | 4.48 | 1.1 seconds | about 54 |
| 0.12 (diamond) | 1.34 | 3.7 seconds | about 16 |
| 0.1 (emerald) | 1.12 | 4.5 seconds | about 13 |

A fresh colony is far slower. Ferrophiles with a Production Rate of 0.4 at size 400 makes 0.4 x 0.505 x 5.6 x 0.6 = 0.68 progress per tick: one iron ingot every 7.4 seconds, about 8 a minute.

Upgrades multiply these. The capped diamond colony makes about 32 diamonds a minute with two Reactor Speed Upgrades, or about 48 with two Reactor Yield Upgrades, since every cycle then gives 3.

A capped stone colony fills its 64-item output slot in under half a minute, and a full slot pauses that colony, so keep the output moving.

***

## <Color id="gold">Power Needed</Color>

Power is a threshold. The reactor needs this much AP arriving every tick or nothing runs and all progress resets. More power than that makes nothing faster.

AP needed = (base + per colony x filled colony slots) x upgrade multipliers, rounded up

| Reactor | Base | Per colony | Full, no upgrades |
|---|---|---|---|
| Bio Reactor | 25 | 25 | 100 AP with 3 colonies |
| Industrial Bio Reactor | 100 | 50 | 550 AP with 9 colonies |

All four values are configurable. Empty colony slots do not count. [Reactor Upgrades](reactor_upgrades.md) lists the multipliers and what common upgrade sets cost.
