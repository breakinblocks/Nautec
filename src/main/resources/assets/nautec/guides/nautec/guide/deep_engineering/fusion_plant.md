---
navigation:
  title: Fusion Plant
  icon: nautec:fusion_controller
  position: 8
  parent: deep_engineering/deep_engineering-index.md
item_ids:
  - nautec:fusion_controller
  - nautec:fusion_casing
  - nautec:aquamarine_structural_glass
  - nautec:laser_injector
  - nautec:fusion_collector
  - nautec:containment_coil
  - nautec:fusion_port
---

# <Color id="light_purple">Fusion Plant</Color>

<Column alignItems="center" fullWidth={true}>
  <Row>
    <ItemImage id="fusion_controller" scale="2"/>
    <ItemImage id="laser_injector" scale="2"/>
    <ItemImage id="containment_coil" scale="2"/>
  </Row>
  A laser fusion reactor built around a Cultivated Prismarine Crystal. It burns Salt Water and makes Forge Energy (FE) for other mods' machines.
</Column>

Laser Injectors fire beams into the crystal heart. The crystal fires them on as plasma beams into two Fusion Collectors, and the plasma fuses Salt Water into FE. A small chamber makes around 20,000 FE/t; a large one with a full set of Containment Coils makes 300,000 FE/t.

***

## <Color id="gold">What It Needs</Color>

* A Cultivated Prismarine Crystal, grown as described on [Crystal Cultivation](crystal_cultivation.md). The heart must be cultivated.
* Laser beams with a purity of 2.0 or more. Purity 3.0 gives full output, so the strongest plants are fed by another crystal or by [Bacterial Fuel Cells](nautec:aquatic_biology/bacterial_fuel_cell.md).
* A steady supply of Salt Water from [Deep Sea Drains](nautec:laser_chemistry/drain.md).
* The parts below. JEI shows their recipes: they take Deep Steel Plating, Atlantic Gold, Ender Pearls, Flawless Prismarine Crystals and, for the controller, a Nether Star.

***

## <Color id="gold">Building the Chamber</Color>

The crystal stands in the middle of a hollow, square box. Inside, the box is exactly as tall as the crystal (6 blocks) and 3, 5, 7 or 9 blocks wide and deep. The walls, floor and ceiling are one block thick around that space.

<GameScene zoom="2.5" background="#333333" interactive={true}>
  <Block id="nautec:fusion_casing" x="0" y="0" z="0"/>
  <Block id="nautec:fusion_casing" x="1" y="0" z="0"/>
  <Block id="nautec:fusion_casing" x="2" y="0" z="0"/>
  <Block id="nautec:fusion_casing" x="3" y="0" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="0" z="0"/>
  <Block id="nautec:fusion_casing" x="0" y="0" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="0" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="0" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="0" z="1"/>
  <Block id="nautec:fusion_casing" x="4" y="0" z="1"/>
  <Block id="nautec:fusion_casing" x="0" y="0" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="0" z="2"/>
  <Block id="nautec:fusion_collector" x="2" y="0" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="0" z="2"/>
  <Block id="nautec:fusion_casing" x="4" y="0" z="2"/>
  <Block id="nautec:fusion_casing" x="0" y="0" z="3"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="0" z="3"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="0" z="3"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="0" z="3"/>
  <Block id="nautec:fusion_casing" x="4" y="0" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="0" z="4"/>
  <Block id="nautec:fusion_casing" x="1" y="0" z="4"/>
  <Block id="nautec:fusion_casing" x="2" y="0" z="4"/>
  <Block id="nautec:fusion_casing" x="3" y="0" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="0" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="1" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="1" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="1" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="1" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="1" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="1" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="1" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="1" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="1" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="1" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="2" z="0"/>
  <Block id="nautec:containment_coil" x="1" y="2" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="2" z="0"/>
  <Block id="nautec:containment_coil" x="3" y="2" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="2" z="0"/>
  <Block id="nautec:fusion_port" x="0" y="2" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="2" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="2" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="2" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="2" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="3" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="3" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="3" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="3" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="3" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="3" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="3" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="3" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="3" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="3" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="4" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="4" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="4" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="4" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="4" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="4" z="1"/>
  <Block id="nautec:laser_injector" x="0" y="4" z="2" p:facing="east"/>
  <Block id="nautec:laser_injector" x="4" y="4" z="2" p:facing="west"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="4" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="4" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="4" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="5" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="5" z="0"/>
  <Block id="nautec:fusion_controller" x="2" y="5" z="0" p:facing="north"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="5" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="5" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="5" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="5" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="5" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="5" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="5" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="6" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="1" y="6" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="2" y="6" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="3" y="6" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="6" z="0"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="6" z="1"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="6" z="2"/>
  <Block id="nautec:aquamarine_structural_glass" x="0" y="6" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="6" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="6" z="4"/>
  <Block id="nautec:fusion_casing" x="0" y="7" z="0"/>
  <Block id="nautec:fusion_casing" x="1" y="7" z="0"/>
  <Block id="nautec:fusion_casing" x="2" y="7" z="0"/>
  <Block id="nautec:fusion_casing" x="3" y="7" z="0"/>
  <Block id="nautec:fusion_casing" x="4" y="7" z="0"/>
  <Block id="nautec:fusion_casing" x="0" y="7" z="1"/>
  <Block id="nautec:fusion_casing" x="4" y="7" z="1"/>
  <Block id="nautec:fusion_casing" x="0" y="7" z="2"/>
  <Block id="nautec:fusion_collector" x="2" y="7" z="2"/>
  <Block id="nautec:fusion_casing" x="4" y="7" z="2"/>
  <Block id="nautec:fusion_casing" x="0" y="7" z="3"/>
  <Block id="nautec:fusion_casing" x="4" y="7" z="3"/>
  <Block id="nautec:fusion_casing" x="0" y="7" z="4"/>
  <Block id="nautec:fusion_casing" x="1" y="7" z="4"/>
  <Block id="nautec:fusion_casing" x="2" y="7" z="4"/>
  <Block id="nautec:fusion_casing" x="3" y="7" z="4"/>
  <Block id="nautec:fusion_casing" x="4" y="7" z="4"/>
  <BoxAnnotation min="2 1 2" max="3 7 3" color="#52e8ff" thickness="0.6">
    The Cultivated Prismarine Crystal stands here, filling the chamber from floor to ceiling.
  </BoxAnnotation>
  <BlockAnnotation x="2" y="5" z="0" color="#ffd86b">
    Fusion Controller, in a side wall, facing out.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="4" z="2" color="#52e8ff">
    Laser Injector, in the middle of a side wall at the height of the crystal core, pointing at it.
  </BlockAnnotation>
  <BlockAnnotation x="2" y="0" z="2" color="#b8a6ff">
    Fusion Collector, in the middle of the floor. Another goes in the middle of the ceiling.
  </BlockAnnotation>
  <BlockAnnotation x="1" y="2" z="0" color="#ffd86b">
    Containment Coil, anywhere in a wall, floor or ceiling.
  </BlockAnnotation>
  <BlockAnnotation x="0" y="2" z="1" color="#8cc4f2">
    Fusion Port, anywhere in a wall, floor or ceiling.
  </BlockAnnotation>
  <IsometricCamera yaw="135" pitch="25"/>
</GameScene>

The scene leaves out two walls and the ceiling so you can see inside.

* <ItemLink id="fusion_casing"/> goes on every edge of the box.
* The rest of the walls, floor and ceiling can be <ItemLink id="aquamarine_structural_glass"/>, Fusion Casing, Containment Coils or Fusion Ports. Glass lets you watch the plasma.
* A <ItemLink id="fusion_collector"/> goes in the middle of the ceiling and another in the middle of the floor, in line with the crystal.
* A <ItemLink id="laser_injector"/> goes in the middle of any side wall, level with the crystal's core (the fourth block up from its base). Place it while looking into the chamber so it points at the crystal. Up to four fit, one per wall.
* The <ItemLink id="fusion_controller"/> goes anywhere in a side wall away from the edges, facing out. It looks for the crystal 2 to 5 blocks behind it.
* Leave the inside empty apart from the crystal. Air and water are both fine, so the plant can be built underwater.

Right-click the controller, or any Fusion Port, to open its screen. While the chamber is incomplete, the bottom line names the first thing that is wrong.

***

## <Color id="gold">Running It</Color>

1. **Fuel.** Pipe Salt Water into the controller or a <ItemLink id="fusion_port"/>. They take nothing else.
2. **Ignition.** Fire beams into the backs of the injectors from outside. A cold plant soaks up its first 5,000,000 AP as heat before it makes anything, which is about 40 seconds at 6,000 AP/t or several minutes for a small feed. The left bar on the screen shows the heat.
3. **Power.** Once the heat bar is full, each AP injected makes up to 50 FE. Lower purity scales that down: purity 2.4 gives 40 FE per AP.
4. **Burn.** Each mB of Salt Water makes 2,000 FE, so a plant at 300,000 FE/t burns 150 mB every tick. That is what six Deep Sea Drains pump.
5. **Output.** The controller and every Fusion Port push FE into any cable or machine touching them.

If the fuel or the beam stops, the plasma cools and the plant goes cold again over 20 seconds. A short gap costs only the heat it lost, which the beams put back quickly.

***

## <Color id="gold">Output Ceiling</Color>

The chamber caps how much plasma it can hold. Beams past the ceiling are wasted, and the screen shows **At ceiling**.

| Inside | Ceiling |
|---|---|
| 3 x 3 | 20,000 FE/t |
| 5 x 5 | 50,000 FE/t |
| 7 x 7 | 100,000 FE/t |
| 9 x 9 | 200,000 FE/t |

Each <ItemLink id="containment_coil"/> in the shell adds 4,000 FE/t, up to 300,000 FE/t in total. A 9 x 9 chamber reaches that with 25 coils, fed by 6,000 AP/t of purity 3.0 beams.

<Color id="gold">Tip</Color>: an [Energy Converter](nautec:laser_chemistry/energy_converter.md) can feed the plant some of its own FE back as AP. Converter beams have a purity of 0, so send them through another Prismarine Crystal first to bring them up to 3.0.
