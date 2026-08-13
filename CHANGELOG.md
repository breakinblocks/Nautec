# Changelog

## [Unreleased]

### Added
- The **Sea Scout**, a two seat powered submersible. Right-click to
  board; first in drives. W/S throttle, the mouse steers, Space rises, C dives,
  hold right mouse to look around. Sneak plus right-click picks it back up with
  its charge. Charge it in a Charger.
- Sealed and powered, it keeps its crew breathing.
- The **Sea Scout Dock**, a powered pad to park it on. It fills the hull's cells without
  you carrying the thing to a Charger, keeps anyone sitting inside breathing on the
  dock's power rather than the hull's, and clamps an empty hull in place so what you
  left on the pad is still there when you get back. A pilot always overrides the clamps.
- Piloting shows the controls on the HUD, so the scheme is not something you have to
  guess at or go looking in the guide for.
- Driving shows a compact power and hull readout, repositionable with Ctrl+H. It stands in
  for the vanilla vehicle health hearts, which eighty hull points would otherwise spread
  across four rows of the screen. A passenger in the back keeps their ordinary hotbar.
- Collision follows the actual hull shape as it turns, so it does not snag
  in tight caverns. It only collides with the world: fish and other creatures
  get shoved aside rather than bringing several tonnes of hull to a dead stop.
- Creative pilots use no power.
- The **Wave Jet**, a handheld thruster. Hold it in your main hand and hold use while
  under water and it drags you along wherever you are looking, both hands on the grips
  and flat out like a swimmer. It runs dry or breaks the surface and you simply let go.
  Charge it in a Charger like anything else.
- Gateways have a screen. Right-click one empty-handed and you get the four fins laid out with
  every colour you can put on them, what the change will cost, and what you are holding. Dyeing a
  fin by hand still works and costs the same one dye per fin, so the screen is a better way to do
  the same thing rather than a cheaper one.
- Gateways look alive: a spark orbits an idle pad, travel bursts at both ends, and a gateway that
  finds no partner puffs where it would have sent you.
- Sonar draws its pulse. The ring expands from the hull at the speed the scan is actually
  travelling, through terrain, so you can see the front reach a contact as it lights up.
- Teleporting streaks the screen from the centre and pulls back as you arrive.
- Two new guide categories: **Deep Engineering** for beam optics, the Resonance Chamber,
  gateways and the Pressure Forge, and **Naval** for the Sea Scout, its dock, its modules
  and the Wave Jet.
- The guide has a story. None of this technology is yours... The sea floor is covered in
  working machinery somebody else built and then stopped maintaining, and the book is the
  notes of whoever is getting it running again. The gateways you find already share one address,
  so they were a network before you arrived. Machine parts can be cleaned and repaired
  but never manufactured. The Pressure Forge only runs under a real column of water.
  The Augmentation Station was built to fit a body for the sea.
  The book now points at all of that instead of leaving it lying there.
- Five more guide entries, covering the parts of the mod the book never mentioned. "The
  Deep Oceans" and "Life in the Deep" introduce the four biomes, the six plants and the
  four creatures, which until now were only ever named in passing as graft targets and
  augment ingredients. "Fishing" covers lucky zones, the Prismatic Fishing Rod and the
  three catch minigames, which had no coverage at all and are easy to sit through without
  realising there was anything to react to. "Budding Prismarine" and "Fishing Station"
  document two blocks you could already craft and find but could not read about.
- Sound for the machines that had none. Gateways hum while idle and answer with a low
  note when their address pairs with nothing; the Resonance Chamber's tone climbs as it
  charges, chimes on a craft and cracks when it overloads; the Pressure Forge works and
  finishes audibly; the dock clamps and releases; optics carry a faint hum while a beam
  is passing through them; a Bacterial Fuel Cell bubbles as it burns. The Wave Jet spins
  up, drives on a loop that follows its rider and tracks their speed, and winds down.
- Now requires **GeckoLib** 5.5.2 or newer.
- Four new ocean biomes, added to the world's biome layout without replacing
  any vanilla ocean outright:
  - **Abyssal Trench** - the deepest, coldest water, near-black fog you can
    barely see through, and far more Drowned than anywhere else.
  - **Bioluminescent Grove** - clear teal water, glow squid, kelp and sea
    pickles.
  - **Hydrothermal Vents** - murky warm water over a basalt and magma floor.
  - **Prismarine Reef** - bright shallow water at the edge of the drop-off,
    coral, tropical fish and prismarine outcrops.
- All four count as oceans, so salt water collection, the Deep Sea Drain,
  Crystal Geodes, Ocean Ruins and shipwrecks all work in them.
- `enableBiomeInjection` and `injectableWorldPresets` config options. Packs
  using a custom overworld preset should add it to the second one.
- Budding Prismarine, which grows Prismarine Buds and Clusters the way Budding
  Amethyst does. Clusters drop Prismarine Crystal Shards, so shards are now
  renewable. It forms rarely in the rock under the Abyssal Trench and the
  Bioluminescent Grove. Silk Touch picks the block up to move it; mining it
  any other way breaks it into four to six shards and destroys it.
- Six new plants: Deep Kelp, Luminescent Algae, Prismarine Frond, Vent
  Tubeworm, Abyssal Coral and Glow Polyp. Three of them give off light:
  Luminescent Algae brightly, Glow Polyp a little less, Vent Tubeworm faintly.
- Four new creatures: the schooling Silt Skipper, the drifting Lantern Jelly,
  the armoured Vent Crawler, and the Abyssal Maw, which hunts in the dark
  below y=40. They drop Luminous Membrane, Chitin Plate and Abyssal Organ.
- Ambient particles in three of the new biomes: vent bubbles, glow spores and
  abyssal motes.
- Twelve augments that were already in the mod but impossible to install now
  have body slots, parts and recipes: Hydraulic Leg, Servo Knee, Shock
  Absorber, Tendon Weave, Magnetic Coil Arm, Ender Coil Arm, Hydro Drill Arm,
  Trident Launcher Arm, Volley Trident Arm, Syringe Robot Arm, Buoyancy Tank
  and Auxiliary Ventricle.
- Three augments built from the new creatures' drops:
  - **Abyssal Eyes** (Abyssal Organ, eye slot) grants night vision, but only
    at depth. Near the surface it stays dormant.
  - **Photophore Skin** (Luminous Membrane, body slot) lights up every living
    thing near you while you are in water, through terrain.
  - **Vent Carapace** (4 Chitin Plates, head or body slot) adds armour,
    knockback resistance, and halves how long you burn.
- Two new guide entries: "Crafted Augments" for the twelve buildable parts and
  "Deep Fauna Augments" for the three organ augments.
- Lucky fishing zones: patches of open water that appear near you while you
  are out on an ocean or a river, marked by drifting glow particles. Fish one
  and you get an extra roll on the zone's loot on top of your normal catch.
  A zone only forms where every block within its radius is open water, so they
  keep to real open water rather than hugging a shoreline. Configurable
  interval, spacing, per-chunk cap, lifetime and radius.
- The Prismatic Fishing Rod. When something bites you get one of three catch
  minigames, picked at random:
  - **Something is biting** - strike once as the marker crosses the green.
  - **It is fighting you** - strike on all three marks, in order.
  - **It is running with the line** - hold through the green, then let go.
  Winning always adds a treasure roll to your catch, and if the catch was
  already a treasure one you get two treasure rolls and a normal roll on top.
  Missing, or ignoring the bar entirely, just gives you the ordinary catch.
  It is never a punishment.
- Lucky fishing zones can hook live creatures, not just items. Reeling one in
  releases the animal at your bobber. Which creatures, and where, is set in the
  loot table with the new `nautec:catch_as_entity` function, so packs can make
  anything catchable.
- Lucky fishing zone loot tables, split into a common catch pool and a rarer
  treasure pool. Both vary by biome: rivers, oceans in general, and each of the
  four Nautec biomes have their own entries on top of the shared ones. Packs
  can override `nautec:gameplay/lucky_fishing_zone` and its two sub-tables.
- Bacteria colonies now age. An analyzed Petri Dish shows Vitality, which drops
  while the colony works and goes back to full whenever you feed it in an
  Incubator. Let it hit zero and the colony starts dying off.
- Seven new places to graft bacteria, on the deep ocean plants.
- Seventy eight ready-made bacteria for materials that other mods add, covering
  metals, alloys and gems from tin and steel up to titanium, iridium and beyond.
  Each one appears once you are playing with a mod that adds that material, and
  stays out of the way otherwise. They ship as ordinary datapack files, so a pack
  can override or disable any of them the same way it would any other content.
- Nautec now fills in the common material tags for a number of mods that keep
  their alloys to themselves, including the Tinkers family, Botania, Ad Astra,
  Draconic Evolution and Big Reactors. That makes those materials usable by
  Nautec and by anything else that reads the common tags.
- A bacteria's output can now be a material tag rather than one specific item, so
  a single definition covers every mod that supplies that material.
- A new admin command for making your own bacteria for any item you like.
- The Bacterial Fuel Cell, a second thing to do with a colony. It eats the
  colony and gives you a laser back. Right-click it with a Petri Dish to load
  one, right-click with an empty dish to take back what is left. Production
  Rate sets how strong the beam is and how fast the colony goes; Mutation
  Resistance sets its purity, up to 2.5, which is nearly a Prismarine Crystal
  without building one. It only burns while its beam has somewhere to go, and
  it never ages a colony, so it is the right home for one that has grown too
  old to be worth feeding. The Bio Reactor wants the same colonies, so you have
  to pick: resources or power.
- Config options for the speed, power draw and decay of all three biology
  machines, and for the Fuel Cell's output, purity and burn rate.
- Three ways to route a laser, so beams no longer have to run in straight lines:
  - **Prismatic Mirror** takes a beam from any side and sends it out the way it
    faces, turning corners without a relay run each way. It keeps nine tenths of
    the purity.
  - **Beam Splitter** sends one beam out of every side that has somewhere to go,
    sharing the power evenly and giving each branch four fifths of the purity.
  - **Focusing Lens** is the only one that gives something back, adding half a
    point of purity to a beam passing straight through. It does nothing to a dead
    line.
- Because recipes refuse to run below a purity, the route you build now decides
  what you can make at the end of it. Long winding runs are cheap to lay out but
  cannot feed the better recipes.
- A "Beam Optics" guide entry covering all three and setting out the purity tiers,
  from any beam at all up to what only a Prismarine Crystal can hold.
- Config options for how much purity each optic costs or adds.
- The **Resonance Chamber**, which hoards a beam instead of spending it. Charge
  builds while a beam feeds it and does not stop on its own. How much it can hold
  before it comes apart depends on how clean that beam is, so a filthy beam gives
  a low ceiling and a Prismarine Crystal gives a much higher one.
- Near the top of its range the Chamber goes critical, and that is the only moment
  it will craft. Put an item in and it pulls the charge back down and makes
  something. Leave it empty, or leave its output full, and it keeps climbing until
  it vents, which hurts everything nearby and leaves it cracked and useless for a
  while. It does not break, and it will not take the room with it.
- **Resonant Shards**, folded from Prismarine Crystal Shards in a Resonance Chamber
  on the cleanest beam you can build. Nothing below the top purity band will do it,
  so this is the first thing that needs a genuinely good beam rather than a strong
  one.
- Two recipes that want a mid-tier beam, giving the middle purity band something to
  do: denser Aquarine Steel, and Prismarine Crystal Shards straight from crystals.
- A "Resonance Chamber" guide entry covering the ceiling, the critical band and what
  venting costs you.
- **Gateways**, flat prismarine plates that move whatever stands on them to another
  plate wearing the same address. No power, no beam and no upkeep; the whole cost is
  paid in Resonant Shards up front.
- Each Gateway wears four coloured fins, one per corner of its top face. Right-click a
  fin with a dye to set that corner, or right-click with an empty hand to read the
  address back. Eight colours across four corners is 4096 addresses.
- You arrive at the nearest other Gateway sharing the address, so a shared address is a
  network rather than a single link. Whatever you are riding comes with you, and so does
  anything riding you, which means a submersible arrives still under you and still
  sealed.
- Arriving bars you from another Gateway for a few seconds, so a pair cannot throw you
  back and forth. Configurable.
- Gateways only reach others in the same world, so any you build in the Nether or the End
  form their own separate network.
- Breaking a Gateway keeps its address on the dropped block, so moving one does not mean
  dyeing it again.
- Gateways generate on the ocean floor as small prismarine pads, spread thinly so the
  nearest one is a real distance away. Every one that generates shares the same address, so
  the ones you find are already a network before you touch any of them.
- A "Gateway" guide entry covering addresses, what travels with you, and the limits.
- The **Abyssal Pressure Forge**, the first machine that cares where you build it. It
  only runs deep, under an unbroken column of water. Take the water away and it stops
  however deep it is, so a dry shaft to bedrock builds you nothing.
- It wants four things at once: depth, water overhead, a clean beam, and Etching Acid,
  which it drinks steadily. The deepest recipes want to be well below sea level, which
  is the point. The Abyssal Trench is where it belongs.
- **Flawless Prismarine Crystals**, pressed from Resonant Shards. The Forge is the only
  way to get them.
- **Deep Steel Plating**, pressed from Aquarine Steel further down still. It repairs a
  breached Sea Scout hull on an anvil, so the thing that carries you into the deep is
  mended with something only the deep can make. Packs can point the repair item at
  diamonds instead in the config.
- An "Abyssal Pressure Forge" guide entry, and config for its depth, water column, power
  draw, acid use and tank size.

### Fixed
- Breaking a Crate now drops the Crate again. Its loot table referenced a data
  component that no longer exists, which stopped the whole table loading.
- Rusty Crates in ocean ruins now generate their loot. Their loot table still
  used the old `laser_relay` id from an old rename.
- Augments that change your attributes or abilities no longer stop working
  after you die. Bonus Hearts, Step Up, Walking Speed and Creative Flight kept
  showing as installed after a respawn but had no effect until you reapplied
  them at the Augmentation Station.
- The Incubator and Mutator progress bars no longer sit at zero while the
  machine is clearly running.
- The biology machines now remember their progress when you leave and come
  back, instead of starting over.
- The Bacterial Analyzer no longer leaves its progress bar full after it
  finishes.
- The Incubator guide page showed a Mutator, and claimed incubating doubles a
  colony, which it never did.
- Recipe pages no longer show bacteria percentages like 7.0000005%.
- Prismarine Sand no longer spills into neighbouring biomes. It was missing the check
  that keeps a feature inside the biome it belongs to.

### Compatibility
- The four new oceans work with Terralith and Tectonic out of the box. Where Lithostitched
  is installed they place themselves through it, the same way those mods do, rather than
  through the vanilla overworld layout that Terralith replaces. No injector files to write.
- Packs can take placement over themselves by overriding the four biome injector files
  Nautec ships.
- Prismarine Sand now sits on the real sea floor wherever it happens to be, instead of at
  a fixed height that assumed Nautec was the only mod shaping the ocean. Everything the
  new biomes place follows the same rule.
- Nautec's Deeper Oceans has no effect when Tectonic is installed, because Tectonic
  replaces the terrain system Nautec builds on. Tectonic decides ocean depth in that
  case, which is the sensible outcome, and Nautec now says so in the log at startup
  rather than looking like it worked.

### Changed
- The Spreading Trident augment is now on 'U' by default. It shared 'Y' with
  the Bouncing Trident augment, so both fired at once when installed together.
- Bacteria stats finally do something. Production Rate, Growth Rate, Mutation
  Resistance and Lifespan were all being ignored; now every one of them
  matters, so a good colony is worth breeding for.
- Bio Reactor speed depends on the colony instead of being the same five
  seconds per item for everything. A fresh colony is about as fast as before, a
  big well bred one is many times faster.
- The Bio Reactor needs more power the more colonies you put in it. A full one
  wants a whole laser line.
- Mutation can now fail. Big, stubborn colonies are harder to change, so mutate
  them while they are still small. A failure only costs you part of the colony
  and the machine tries again. Success keeps the parent's stats, so good
  bacteria stay good after the jump.
- The Mutator needs its output slot empty before it starts.

## [0.5.1]

### Fixed
- Spreading Tridents are no longer invisible when thrown.
- The Mutator and Bacterial Analyzer now respect their config settings for
  crafting speed and power requirement.
- Laser purity now drops right away when its source is removed, instead of
  sticking at the old value.
- Prismarine Sand now generates on the ocean floor as intended.
- Crystal Geodes and Ocean Ruins no longer generate on top of each other.
- Salt water can now be collected in modded ocean biomes.
- Machine particle effects no longer stutter when several machines run at
  once.

## [0.5.0]

### Changed
- Updated to Minecraft 26.1.

## [0.4.1]

### Fixed
- Augmentation Station no longer disconnects the player when clicking Apply
  without first selecting an augment slot. The Apply button is now disabled
  until a slot is highlighted in the side panel. ([FTB#12158])

### Internal
- Moved the Augmentation Station's Apply button into `init()` so it is built
  once per screen open instead of being re-added every render frame.

[FTB#12158]: https://github.com/FTBTeam/FTB-Modpack-Issues/issues/12158

## [0.4.0]

### Added
- Prism Monocle now also clears underwater fog (works in the head slot or in
  its curio slot). The Diving Helmet keeps its existing behavior. ([#27])

### Fixed
- Crates obtained from shipwrecks, ocean ruins, and ocean archaeology now
  generate loot when opened. ([#44])
- Game no longer crashes when reaching any GUI screen because Jade was missing
  translation entries for the mixer / aquatic catalyst / laser junction
  tooltip-config keys.
- Bio Reactor, Bacteria Incubation, and Bacteria Mutations recipe screens no
  longer show clipped or hidden tooltips when hovering bacteria slots
  (especially under EMI and TMRV). ([#41])
- Book recipe pages now show the correct recipes — fixed 13 stale recipe IDs
  across the Aquarine Steel tools and armor, Wrench, Prismarine Relay, Diving
  Chestplate Oxygen, and Etching Acid entries. ([#40])
- Last two pages of the Laser Power book entry now render their text and
  recipe contents. ([#39])
- Filled in missing display names for fluid blocks (Oil, Salt Water, EAS,
  Etching Acid) and for 13 augment types (Eldritch Heart, Magnet, Creative
  Flight, …). Fixed the "Eldritch_heart" book title typo. ([#38])
- Mixer Jade tooltip no longer prints an empty fluid line when a tank is
  empty.

### Performance / stability
- Fixed a memory leak in the etching-acid item processor; tracked entries are
  now evicted when the item entity leaves the level.
- Hardened null-safety in the Prism Monocle HUD overlay and the Crate block
  entity around world-transition / chunk-unload edge cases.

### Internal
- Refactored 30 Modonomicon book entries onto a shared `BaseNautecEntry` base
  class (~700 LoC of boilerplate removed).
- Standardized on `Nautec.rl(...)` for resource-location construction (83
  call-sites across 46 files).
- JEI recipe-category titles are now localizable.
- Added a gametest that validates every book recipe-id reference resolves to
  a real recipe.
- Removed dead code in `NTLootTables` and de-duplicated the
  Incubation / Mutation recipe builders.

[#27]: https://github.com/Porting-Dead-Mods/Nautec/issues/27
[#38]: https://github.com/Porting-Dead-Mods/Nautec/issues/38
[#39]: https://github.com/Porting-Dead-Mods/Nautec/issues/39
[#40]: https://github.com/Porting-Dead-Mods/Nautec/issues/40
[#41]: https://github.com/Porting-Dead-Mods/Nautec/issues/41
[#44]: https://github.com/Porting-Dead-Mods/Nautec/issues/44

## [0.3.3]

### Fixed
- Opening the Augment Viewer no longer crashes the game.
- Augments and the items used to install them no longer disappear after
  relogging.
- Augment effects (water breathing from Drowned Lungs, swim speed from Dolphin
  Fin, etc.) keep working reliably after installing multiple augments.

Thanks to FTB OceanBlock 2 for the report
([#12021](https://github.com/FTBTeam/FTB-Modpack-Issues/issues/12021)).
