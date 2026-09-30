---
navigation:
  title: Impulse Laser
  icon: nautec:impulse_laser_module
  position: 13
  parent: submarine/submarine-index.md
item_ids:
  - nautec:impulse_laser_module
---

# <Color id="light_purple">Impulse Laser</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="impulse_laser_module" scale="2"/>
  Twin forward beams.
</Column>

The Impulse Laser Module fires two beams straight ahead of the hull's nose that damage the first creature each one meets, for as long as you leave it on.

***

## <Color id="gold">Using It</Color>

| | Default |
|---|---|
| Power | 10,000 every half second |
| Range | 64 blocks |
| Damage per beam | 10, plus 2.5% of the target's maximum health, every half second |
| Cooldown | None |

Fire it once to switch the beams on and again to switch them off. Aim by pointing the hull: the beams follow the nose, not your view, even in free look.

Each beam stops at the first creature or solid block in its path. Every half second, a beam that is on a creature deals 10 damage plus 2.5% of that creature's maximum health, and the extra share makes it effective against large targets. A target caught by both beams takes both hits. The damage ignores armour, and the beams pass the hull's own crew by.

***

## <Color id="gold">Power</Color>

The laser uses 20,000 power a second, so a full cell runs it for under a minute on top of everything else. It switches itself off when the cell drops below 10,000, when the last person leaves the hull, or when the module is taken out. Switch it off between targets.

All the figures are configurable.

***

### <Color id="aqua">Impulse Laser Module Recipe</Color>

<Recipe id="nautec:impulse_laser_module"/>
