---
navigation:
  title: The Deep Oceans
  icon: nautec:luminescent_algae
  position: 10
  parent: getting_started/getting_started-index.md
---

# <Color id="light_purple">The Deep Oceans</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="luminescent_algae" scale="2"/>
  NauTec adds four ocean biomes, each with its own plants and creatures.
</Column>

The four biomes are added alongside the vanilla oceans rather than replacing them. They only generate in chunks that have not been generated yet, so in an existing world you find them past the edge of explored ground.

All four count as oceans. Salt water collection, the [Deep Sea Drain](nautec:laser_chemistry/drain.md), lucky fishing zones, NauTec structures, vanilla ocean ruins and shipwrecks all work in them.

NauTec also makes oceans deeper than vanilla. With Tectonic installed, Tectonic decides ocean depth instead.

| Biome | Where | Visibility |
|---|---|---|
| Abyssal Trench | farthest from land, cold climates | about 20 blocks |
| Bioluminescent Grove | deep ocean, temperate climates | about 130 blocks |
| Hydrothermal Vents | deep ocean, warm climates | about 50 blocks |
| Prismarine Reef | shallow water near the coast, hot climates | about 145 blocks |

***

## <Color id="gold">Abyssal Trench</Color>

The deepest and darkest of the four, with near-black water and drifting motes.

* Drowned spawn far more often than in other oceans, and the Abyssal Maw hunts in the dark below y 40.
* Abyssal Coral grows on the floor. Silt Skippers, cod, squid and glow squid swim here.
* <ItemLink id="budding_prismarine"/> forms rarely in the rock 4 to 16 blocks under the floor.
* The best place for the [Abyssal Pressure Forge](nautec:deep_engineering/pressure_forge.md), which needs depth and a column of water above it.

***

## <Color id="gold">Bioluminescent Grove</Color>

Clear teal water with glowing spores drifting through it.

* Deep Kelp, Luminescent Algae, seagrass and sea pickles on the floor, and Glow Polyp spreading over stone on floors, walls and ceilings.
* Lantern Jellies, Silt Skippers and extra glow squid.
* Budding Prismarine forms rarely in the rock under the floor, as in the Trench.

***

## <Color id="gold">Hydrothermal Vents</Color>

Murky brown water over a floor patched with basalt and magma, with bubbles rising all the time.

* Vent Tubeworms grow on the floor.
* Vent Crawlers walk the floor and drop Chitin Plate.
* Magma exposed on the floor makes bubble columns, which fill Glass Bottles with pressurized air for the [Diving Suit](diving_gear.md).

***

## <Color id="gold">Prismarine Reef</Color>

Bright, clear shallow water.

* Coral, sea pickles and patches of prismarine in the sea floor, with Prismarine Fronds growing between them.
* Tropical fish, Silt Skippers, squid and dolphins.

***

## <Color id="gold">For Pack Makers</Color>

`enableBiomeInjection` in `nautec-worldgen.toml` turns the biomes off. Packs using a custom overworld preset should add it to `injectableWorldPresets`. With Lithostitched installed, placement comes from the biome injector files in `data/nautec/lithostitched/biome_injector` instead, which also covers Terralith and Tectonic.

Plants and creatures are covered in [Life in the Deep](deep_life.md).
