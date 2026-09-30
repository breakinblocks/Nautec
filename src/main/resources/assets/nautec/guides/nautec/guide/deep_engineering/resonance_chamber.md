---
navigation:
  title: Resonance Chamber
  icon: nautec:resonance_chamber
  position: 1
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:resonance_chamber
---

# <Color id="light_purple">Resonance Chamber</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="resonance_chamber" scale="2"/>
  Stores up a laser beam as charge and crafts when the charge reaches a critical level.
</Column>

The Resonance Chamber turns <ItemLink id="prismarine_crystal_shard"/>s into Resonant Shards. It needs a beam of purity 3.0 or higher. It takes beams on its four sides and its top.

***

## <Color id="gold">Charge and Ceiling</Color>

While the incoming beam carries at least 20 AP per tick (`resonancePowerUsage`), the Chamber adds the beam's full power to its charge every tick. Below that, the charge drains away.

The ceiling is how much charge the Chamber can safely hold. It is 2,000 x (1 + purity), so a purity 3.0 beam gives a ceiling of 8,000. The base of 2,000 is `resonanceBaseCeiling` in `nautec-common.toml`.

| Charge | What happens |
|---|---|
| Below 90% of the ceiling | Keeps charging |
| 90% to 110% (critical) | Crafts one item if it can, then drops to zero charge |
| Above 110% | Vents |

At a purity of 3.0, critical runs from 7,200 to 8,800 charge. A 40 AP beam gets there in 180 ticks (9 seconds).

***

## <Color id="gold">Crafting</Color>

Put the Prismarine Crystal Shards in before the charge reaches critical. Each time it goes critical, the Chamber uses one shard, puts a Resonant Shard in its output and starts charging again from zero, so a full stack works through on its own.

Right-click with a shard to load it. Right-click with an empty hand to take the output, or the input when the output is empty. Hoppers and pipes can insert shards from any side, but the output has to be taken out by hand.

Look at it through a <ItemLink id="prism_monocle"/> to see the charge as a percentage of the ceiling, the ceiling itself and the beam purity. The charge turns gold while it is critical.

***

## <Color id="gold">Venting</Color>

If the charge passes 110% of the ceiling, the Chamber vents. Every living thing within 4 blocks takes 8 damage, and the Chamber is cracked for 10 seconds, with no charge and no crafting. It does not break.

It vents when it reaches critical and cannot craft, which happens when:

* the input slot is empty
* the input has no recipe, or the beam purity is below 3.0
* the output slot is full

Keep the output emptied and switch the beam off when you run out of shards. A very strong beam can also jump from below 90% to above 110% in a single tick (at purity 3.0, anything over 1,600 AP per tick can do it), so keep the power moderate.

Vent damage, radius and cooldown are `resonanceVentDamage`, `resonanceVentRadius` and `resonanceVentCooldown` in `nautec-common.toml`.

***

## <Color id="gold">Getting Purity 3.0</Color>

* A <ItemLink id="prismarine_crystal"/> hit from the side emits 3.0. Point it straight at the Chamber, or turn it with a Prismatic Mirror (2.7) and follow that with a [Focusing Lens](beam_optics.md) (3.2). After a Beam Splitter (2.4) it takes two lenses.
* A <ItemLink id="bacterial_fuel_cell"/> at full purity (2.5) followed by a Focusing Lens gives 3.0.

***

### <Color id="aqua">Resonance Chamber Recipe</Color>

<Recipe id="nautec:resonance_chamber"/>
