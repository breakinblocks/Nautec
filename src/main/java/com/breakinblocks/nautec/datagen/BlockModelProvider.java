package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blocks.AdvancedBacterialAnalyzerBlock;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blocks.generators.ThermalVentTapBlock;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.CrateBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.OilBarrelBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.IntegerRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockModelProvider extends ModelProvider {
    private static final TextureSlot SLOT_2 = TextureSlot.create("2");
    private static final TextureSlot SLOT_4 = TextureSlot.create("4");
    private static final TextureSlot SLOT_5 = TextureSlot.create("5");

    private final Map<Identifier, Identifier> createdModels = new HashMap<>();
    private BlockModelGenerators blockModels;

    public BlockModelProvider(PackOutput output) {
        super(output, Nautec.MODID);
    }

    @Override
    public String getName() {
        return "NauTec Block Model Definitions";
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.empty();
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        this.blockModels = blockModels;
        MultiblockModelHelper helper = new MultiblockModelHelper(this);

        axisBlock(NTBlocks.DARK_PRISMARINE_PILLAR.get());
        simpleBlock(NTBlocks.CHISELED_DARK_PRISMARINE.get());
        simpleBlock(NTBlocks.PRISMARINE_SAND.get());
        simpleBlock(NTBlocks.POLISHED_PRISMARINE.get());
        simpleBlock(NTBlocks.AQUARINE_STEEL_BLOCK.get());
        simpleBlock(NTBlocks.CAST_IRON_BLOCK.get());

        simpleBlock(NTBlocks.BUDDING_PRISMARINE.get());
        prismarineBud(NTBlocks.SMALL_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.MEDIUM_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.LARGE_PRISMARINE_BUD.get());
        prismarineBud(NTBlocks.PRISMARINE_CLUSTER.get());

        blockModels.createGrowingPlant(NTBlocks.DEEP_KELP.get(), NTBlocks.DEEP_KELP_PLANT.get(), BlockModelGenerators.PlantType.NOT_TINTED);
        blockModels.registerSimpleFlatItemModel(NTBlocks.DEEP_KELP.get());
        waterPlant(NTBlocks.LUMINESCENT_ALGAE.get());
        waterPlant(NTBlocks.PRISMARINE_FROND.get());
        waterPlant(NTBlocks.VENT_TUBEWORM.get());
        waterPlant(NTBlocks.ABYSSAL_CORAL.get());
        blockModels.createMultifaceBlockStates(NTBlocks.GLOW_POLYP.get());
        blockModels.registerSimpleFlatItemModel(NTBlocks.GLOW_POLYP.get());

        simpleBlock(NTBlocks.CREATIVE_POWER_SOURCE.get());
        simpleBlock(NTBlocks.CREATIVE_ENERGY_SOURCE.get(), cubeAll(name(NTBlocks.CREATIVE_ENERGY_SOURCE.get()),
                blockTexture(NTBlocks.CREATIVE_POWER_SOURCE.get())));
        simpleBlock(NTBlocks.ENERGY_CONVERTER.get(), artModel(NTBlocks.ENERGY_CONVERTER.get()));
        fusionPlant();
        simpleBlock(NTBlocks.RESONANCE_PYLON.get(), existingModelFile(NTBlocks.RESONANCE_PYLON.get()));
        simpleBlock(NTBlocks.ABYSSAL_PYLON.get(), existingModelFile(NTBlocks.ABYSSAL_PYLON.get()));
        simpleBlock(NTBlocks.PRISMATIC_EMITTER.get(), existingModelFile(NTBlocks.PRISMATIC_EMITTER.get()));
        simpleBlock(NTBlocks.UPLINK_ARRAY.get(), existingModelFile(NTBlocks.UPLINK_ARRAY.get()));
        simpleBlock(NTBlocks.DOWNLINK_ARRAY.get(), existingModelFile(NTBlocks.DOWNLINK_ARRAY.get()));
        simpleBlock(NTBlocks.SATELLITE_ARRAY_TOP.get(), existingModelFile(NTBlocks.SATELLITE_ARRAY_TOP.get()));
        generators();
        aquaticCatalyst(NTBlocks.AQUATIC_CATALYST.get());

        existingFacingBlock(NTBlocks.PRISMARINE_RELAY.get(), NTBlocks.PRISMARINE_RELAY.get());

        simpleBlock(NTBlocks.SUBMARINE_DOCK.get(), artModel(NTBlocks.SUBMARINE_DOCK.get()));

        simpleBlock(NTBlocks.PRESSURE_FORGE.get(), artModel(NTBlocks.PRESSURE_FORGE.get()));

        simpleBlock(NTBlocks.GATEWAY.get(), artModel(NTBlocks.GATEWAY.get()));
        simpleBlock(NTBlocks.GATEWAY_RING.get());
        simpleBlock(NTBlocks.GATEWAY_RING_PART.get(), ModelTemplates.PARTICLE_ONLY.create(NTBlocks.GATEWAY_RING_PART.get(),
                TextureMapping.particle(blockTexture(NTBlocks.GATEWAY_RING.get())), blockModels.modelOutput));

        simpleBlock(NTBlocks.RESONANCE_CHAMBER.get(), artModel(NTBlocks.RESONANCE_CHAMBER.get()));

        facingBlock(NTBlocks.PRISMATIC_MIRROR.get(), artModel(NTBlocks.PRISMATIC_MIRROR.get()));
        facingBlock(NTBlocks.BEAM_SPLITTER.get(), artModel(NTBlocks.BEAM_SPLITTER.get()));
        facingBlock(NTBlocks.FOCUSING_LENS.get(), artModel(NTBlocks.FOCUSING_LENS.get()));
        longDistanceLaser(NTBlocks.LONG_DISTANCE_LASER.get());
        laserJunction(NTBlocks.LASER_JUNCTION.get());

        simpleBlock(NTBlocks.MIXER.get(), existingModelFile(NTBlocks.MIXER.get()));
        simpleBlock(NTBlocks.CHARGER.get(), existingModelFile(NTBlocks.CHARGER.get()));
        simpleBlock(NTBlocks.CONFINED_SPAWNER.get(), existingModelFile(NTBlocks.CONFINED_SPAWNER.get()));
        simpleBlock(NTBlocks.CRYSTAL_CRADLE.get(), existingModelFile(NTBlocks.CRYSTAL_CRADLE.get()));
        simpleBlock(NTBlocks.FISHING_STATION.get(), existingModelFile(NTBlocks.FISHING_STATION.get()));
        crateBlock(NTBlocks.CRATE.get());
        rustyCrateBlock(NTBlocks.RUSTY_CRATE.get());

        simpleBlock(NTBlocks.MUTATOR.get(), existingModelFile(NTBlocks.MUTATOR.get()));
        simpleBlock(NTBlocks.INCUBATOR.get(), existingModelFile(NTBlocks.INCUBATOR.get()));

        facingBlock(NTBlocks.BACTERIAL_FUEL_CELL.get(), artModel(NTBlocks.BACTERIAL_FUEL_CELL.get()));

        helper.drainController(NTBlocks.DRAIN.get());
        helper.drainPart(NTBlocks.DRAIN_PART.get(), IntegerRange.of(0, 8));

        helper.augmentationStationController(NTBlocks.AUGMENTATION_STATION.get());
        helper.augmentationStationPart(NTBlocks.AUGMENTATION_STATION_PART.get(), IntegerRange.of(0, 8));
        helper.augmentationStationExtension(NTBlocks.AUGMENTATION_STATION_EXTENSION.get());

        helper.bioReactorPart(NTBlocks.BIO_REACTOR_PART.get());
        helper.bioReactorController(NTBlocks.BIO_REACTOR.get());
        helper.industrialBioReactorPart(NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get());
        helper.industrialBioReactorController(NTBlocks.INDUSTRIAL_BIO_REACTOR.get());

        simpleBlock(NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get());

        simpleBlock(NTBlocks.DRAIN_WALL.get());
        simpleBlock(NTBlocks.BROWN_POLYMER_BLOCK.get());

        oilBarrel(NTBlocks.OIL_BARREL.get(), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get())
        ), cubeBottomTop(name(NTBlocks.OIL_BARREL.get()) + "_open",
                blockTexture(NTBlocks.OIL_BARREL.get(), "_side"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_bottom"),
                blockTexture(NTBlocks.OIL_BARREL.get(), "_open")
        ));

        horizontalBlock(NTBlocks.BACTERIAL_ANALYZER.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER.get()));
        horizontalBlock(NTBlocks.BACTERIAL_ANALYZER_TOP.get(), existingModelFile(NTBlocks.BACTERIAL_ANALYZER_TOP.get()));
    }

    private void generators() {
        simpleBlock(NTBlocks.TIDAL_ROTOR.get(), cubeBottomTop("tidal_rotor", blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_side"),
                blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_bottom"), blockTexture(NTBlocks.TIDAL_ROTOR.get(), "_top")));
        Block tap = NTBlocks.THERMAL_VENT_TAP.get();
        Identifier idle = cubeBottomTop("thermal_vent_tap", blockTexture(tap, "_side"), blockTexture(tap, "_bottom"), blockTexture(tap, "_top"));
        Identifier lit = cubeBottomTop("thermal_vent_tap_lit", blockTexture(tap, "_side_lit"), blockTexture(tap, "_bottom"), blockTexture(tap, "_top_lit"));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(tap)
                .with(BlockModelGenerators.createBooleanModelDispatch(ThermalVentTapBlock.LIT,
                        BlockModelGenerators.plainVariant(lit), BlockModelGenerators.plainVariant(idle))));
    }

    private void fusionPlant() {
        Material casing = blockTexture(NTBlocks.FUSION_CASING.get());
        simpleBlock(NTBlocks.FUSION_CASING.get(), cubeAll("fusion_casing", casing));
        simpleBlock(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get(), cubeAll("aquamarine_structural_glass", blockTexture(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get())));
        simpleBlock(NTBlocks.CONTAINMENT_COIL.get(), cubeBottomTop("containment_coil", blockTexture(NTBlocks.CONTAINMENT_COIL.get(), "_side"),
                blockTexture(NTBlocks.CONTAINMENT_COIL.get(), "_end"), blockTexture(NTBlocks.CONTAINMENT_COIL.get(), "_end")));
        simpleBlock(NTBlocks.FUSION_PORT.get(), cubeAll("fusion_port", blockTexture(NTBlocks.FUSION_PORT.get())));
        simpleBlock(NTBlocks.FUSION_COLLECTOR.get(), cubeBottomTop("fusion_collector", blockTexture(NTBlocks.FUSION_COLLECTOR.get(), "_side"),
                blockTexture(NTBlocks.FUSION_COLLECTOR.get(), "_lens"), blockTexture(NTBlocks.FUSION_COLLECTOR.get(), "_lens")));
        facingBlock(NTBlocks.LASER_INJECTOR.get(), cubeBottomTop("laser_injector", blockTexture(NTBlocks.LASER_INJECTOR.get(), "_side"),
                blockTexture(NTBlocks.LASER_INJECTOR.get(), "_back"), blockTexture(NTBlocks.LASER_INJECTOR.get(), "_front")));

        Block controller = NTBlocks.FUSION_CONTROLLER.get();
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/fusion_controller"), key -> ModelTemplates.CUBE_ORIENTABLE.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(controller, "_front"))
                .put(TextureSlot.SIDE, casing)
                .put(TextureSlot.TOP, casing), blockModels.modelOutput));
        Identifier active = createdModels.computeIfAbsent(Nautec.rl("block/fusion_controller_active"), key -> ModelTemplates.CUBE_ORIENTABLE.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(controller, "_front_active"))
                .put(TextureSlot.SIDE, casing)
                .put(TextureSlot.TOP, casing), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(controller)
                .with(BlockModelGenerators.createBooleanModelDispatch(FusionControllerBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));

        graftingStation(NTBlocks.GRAFTING_STATION.get());
        advancedAnalyzer(NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
        bubbleAnchor(NTBlocks.BUBBLE_ANCHOR.get());
        simpleBlock(NTBlocks.DISTRIBUTOR.get(), cubeBottomTop("nautechnical_distributor", blockTexture(NTBlocks.DISTRIBUTOR.get(), "_side"),
                blockTexture(NTBlocks.DISTRIBUTOR.get(), "_bottom"), blockTexture(NTBlocks.DISTRIBUTOR.get(), "_top")));
    }

    private void bubbleAnchor(Block block) {
        Identifier idle = cubeBottomTop("bubble_anchor", blockTexture(block, "_side"), blockTexture(block, "_bottom"), blockTexture(block, "_top"));
        Identifier active = cubeBottomTop("bubble_anchor_active", blockTexture(block, "_side_active"), blockTexture(block, "_bottom"),
                blockTexture(block, "_top_active"));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(BubbleAnchorBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle))));
    }

    private void advancedAnalyzer(Block block) {
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/advanced_bacterial_analyzer"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        Identifier active = createdModels.computeIfAbsent(Nautec.rl("block/advanced_bacterial_analyzer_active"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front_active"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(AdvancedBacterialAnalyzerBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void graftingStation(Block block) {
        Identifier idle = createdModels.computeIfAbsent(Nautec.rl("block/grafting_station"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        Identifier active = createdModels.computeIfAbsent(Nautec.rl("block/grafting_station_active"), key -> ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(key, new TextureMapping()
                .put(TextureSlot.FRONT, blockTexture(block, "_front_active"))
                .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                .put(TextureSlot.TOP, blockTexture(block, "_top_active"))
                .put(TextureSlot.BOTTOM, blockTexture(block, "_bottom")), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(BlockModelGenerators.createBooleanModelDispatch(GraftingStationBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(active), BlockModelGenerators.plainVariant(idle)))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    public BlockModelGenerators blockModels() {
        return blockModels;
    }

    private void axisBlock(Block block) {
        Identifier model = createdModels.computeIfAbsent(ModelLocationUtils.getModelLocation(block), id ->
                ModelTemplates.CUBE_COLUMN.create(id, new TextureMapping()
                        .put(TextureSlot.SIDE, blockTexture(block, "_side"))
                        .put(TextureSlot.END, blockTexture(block, "_end")), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private void simpleBlock(Block block) {
        blockModels.createTrivialCube(block);
    }

    private void prismarineBud(Block block) {
        blockModels.createAmethystCluster(block);
        blockModels.registerSimpleFlatItemModel(block);
    }

    private void waterPlant(Block block) {
        blockModels.createCrossBlock(block, BlockModelGenerators.PlantType.NOT_TINTED);
        blockModels.registerSimpleFlatItemModel(block);
    }

    private void simpleBlock(Block block, Identifier model) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    public void horizontalBlock(Block block, Identifier model) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }

    private void aquaticCatalyst(AquaticCatalystBlock block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.values()) {
            int xRot = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
            int yRot = dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360;
            for (int stage : AquaticCatalystBlock.STAGE.getPossibleValues()) {
                for (boolean active : AquaticCatalystBlock.ACTIVE.getPossibleValues()) {
                    builder = builder.with(BlockModelGenerators.condition()
                                    .term(BlockStateProperties.FACING, dir)
                                    .term(AquaticCatalystBlock.STAGE, stage)
                                    .term(AquaticCatalystBlock.ACTIVE, active),
                            rotated(BlockModelGenerators.plainVariant(createActiveACModel(block, stage, active)), xRot, yRot));
                }
            }
            for (boolean linked : AquaticCatalystBlock.LINKED.getPossibleValues()) {
                builder = builder.with(BlockModelGenerators.condition()
                                .term(BlockStateProperties.FACING, dir)
                                .term(AquaticCatalystBlock.LINKED, linked),
                        rotated(BlockModelGenerators.plainVariant(existingModelFile(linked ? "aquatic_catalyst_lamp_linked" : "aquatic_catalyst_lamp_unlinked")), xRot, yRot));
            }
        }
        blockModels.blockStateOutput.accept(builder);
    }

    private void laserJunction(Block block) {
        MultiPartGenerator builder = MultiPartGenerator.multiPart(block);
        builder = laserJunctionConnection(builder, block, Direction.DOWN, 0, 0);
        builder = laserJunctionConnection(builder, block, Direction.UP, 180, 0);
        builder = laserJunctionConnection(builder, block, Direction.NORTH, 90, 180);
        builder = laserJunctionConnection(builder, block, Direction.EAST, 90, 270);
        builder = laserJunctionConnection(builder, block, Direction.SOUTH, 90, 0);
        builder = laserJunctionConnection(builder, block, Direction.WEST, 90, 90);
        builder = builder.with(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_base")));
        blockModels.blockStateOutput.accept(builder);
    }

    private MultiPartGenerator laserJunctionConnection(MultiPartGenerator builder, Block block, Direction direction, int x, int y) {
        MultiVariant in = rotated(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_connection_in")), x, y);
        MultiVariant out = rotated(BlockModelGenerators.plainVariant(extend(existingModelFile(block), "_connection_out")), x, y);
        return builder
                .with(BlockModelGenerators.condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.INPUT), in)
                .with(BlockModelGenerators.condition(LaserJunctionBlock.CONNECTION[direction.ordinal()], LaserJunctionBlock.ConnectionType.OUTPUT), out);
    }

    public void longDistanceLaser(Block block) {
        Identifier model = cube(name(block),
                blockTexture(block, "_bottom"),
                blockTexture(block, "_top"),
                blockTexture(block, "_side"),
                blockTexture(block, "_side"),
                blockTexture(block, "_side"),
                blockTexture(block, "_side"),
                blockTexture(block, "_side"));
        facingBlock(block, model);
    }

    public void existingFacingBlock(Block block, Block modelOf) {
        facingBlock(block, existingModelFile(modelOf));
    }

    public void facingBlock(Block block, Identifier model) {
        MultiVariant variant = BlockModelGenerators.plainVariant(model);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(BlockStateProperties.FACING)
                        .select(Direction.UP, variant)
                        .select(Direction.DOWN, rotated(variant, 180, 0))
                        .select(Direction.NORTH, rotated(variant, 90, 0))
                        .select(Direction.SOUTH, rotated(variant, 90, 180))
                        .select(Direction.EAST, rotated(variant, 90, 90))
                        .select(Direction.WEST, rotated(variant, 90, 270))));
    }

    public void oilBarrel(Block block, Identifier model, Identifier openModel) {
        PropertyDispatch.C2<MultiVariant, Direction, Boolean> dispatch = PropertyDispatch.initial(BlockStateProperties.FACING, OilBarrelBlock.OPEN);
        for (boolean open : new boolean[]{false, true}) {
            MultiVariant variant = BlockModelGenerators.plainVariant(open ? openModel : model);
            dispatch = dispatch
                    .select(Direction.UP, open, variant)
                    .select(Direction.DOWN, open, rotated(variant, 180, 0))
                    .select(Direction.NORTH, open, rotated(variant, 90, 0))
                    .select(Direction.SOUTH, open, rotated(variant, 90, 180))
                    .select(Direction.EAST, open, rotated(variant, 90, 90))
                    .select(Direction.WEST, open, rotated(variant, 90, 270));
        }
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private void crateBlock(CrateBlock crateBlock) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(crateBlock)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.OPEN,
                        BlockModelGenerators.plainVariant(extend(existingModelFile(crateBlock), "_open")),
                        BlockModelGenerators.plainVariant(existingModelFile(crateBlock)))));
    }

    private void rustyCrateBlock(CrateBlock crateBlock) {
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(crateBlock)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.OPEN,
                        BlockModelGenerators.plainVariant(rustedCrateModel(crateBlock, true)),
                        BlockModelGenerators.plainVariant(rustedCrateModel(crateBlock, false)))));
    }

    private Identifier rustedCrateModel(CrateBlock block, boolean open) {
        Identifier id = Nautec.rl("block/" + name(block) + (open ? "_open" : ""));
        return createdModels.computeIfAbsent(id, key -> {
            ModelTemplate template = new ModelTemplate(
                    Optional.of(extend(existingModelFile(NTBlocks.CRATE.get()), open ? "_open" : "")),
                    Optional.empty(), SLOT_2, SLOT_4, SLOT_5, TextureSlot.PARTICLE);
            return template.create(key, new TextureMapping()
                    .put(SLOT_2, new Material(Nautec.rl("block/crate/rusty_top_inner")))
                    .put(SLOT_4, new Material(Nautec.rl("block/crate/rusty")))
                    .put(SLOT_5, new Material(Nautec.rl("block/crate/rusty_top")))
                    .put(TextureSlot.PARTICLE, new Material(Nautec.rl("block/crate/rusty"))), blockModels.modelOutput);
        });
    }

    public Material multiblockTexture(Multiblock multiblock, String name) {
        return new Material(Nautec.rl("block/multiblock/" + NTRegistries.MULTIBLOCK.getKey(multiblock).getPath() + "/" + name));
    }

    private Identifier createActiveACModel(AquaticCatalystBlock block, int stage, boolean active) {
        String suffix = active ? "_active" : "";
        return cube(name(block) + suffix + (stage != 0 ? ("_" + stage) : ""),
                blockTexture(block, "_bottom" + suffix),
                blockTexture(block, "_top_" + stage),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side" + suffix),
                blockTexture(block, "_side"));
    }

    public Identifier cube(String name, Material down, Material up, Material north, Material south, Material east, Material west, Material particle) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE.create(key, new TextureMapping()
                .put(TextureSlot.DOWN, down)
                .put(TextureSlot.UP, up)
                .put(TextureSlot.NORTH, north)
                .put(TextureSlot.SOUTH, south)
                .put(TextureSlot.EAST, east)
                .put(TextureSlot.WEST, west)
                .put(TextureSlot.PARTICLE, particle), blockModels.modelOutput));
    }

    private Identifier artModel(Block block) {
        Identifier id = ModelLocationUtils.getModelLocation(block);
        ModelTemplate template = new ModelTemplate(Optional.of(Nautec.rl("block/art/" + name(block))), Optional.empty());
        return template.create(id, new TextureMapping(), blockModels.modelOutput);
    }

    public Identifier cubeTop(Block block, Material side, Material top) {
        Identifier id = ModelLocationUtils.getModelLocation(block);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_TOP.create(key, new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, top), blockModels.modelOutput));
    }

    public Identifier cubeAll(String name, Material all) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_ALL.create(key, new TextureMapping()
                .put(TextureSlot.ALL, all), blockModels.modelOutput));
    }

    public Identifier cubeBottomTop(String name, Material side, Material bottom, Material top) {
        Identifier id = Nautec.rl("block/" + name);
        return createdModels.computeIfAbsent(id, key -> ModelTemplates.CUBE_BOTTOM_TOP.create(key, new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top), blockModels.modelOutput));
    }

    public static MultiVariant rotated(MultiVariant variant, int xRot, int yRot) {
        MultiVariant result = variant;
        switch (xRot) {
            case 90 -> result = result.with(BlockModelGenerators.X_ROT_90);
            case 180 -> result = result.with(BlockModelGenerators.X_ROT_180);
            case 270 -> result = result.with(BlockModelGenerators.X_ROT_270);
        }
        switch (yRot) {
            case 90 -> result = result.with(BlockModelGenerators.Y_ROT_90);
            case 180 -> result = result.with(BlockModelGenerators.Y_ROT_180);
            case 270 -> result = result.with(BlockModelGenerators.Y_ROT_270);
        }
        return result;
    }

    public Material blockTexture(Block block) {
        return blockTexture(block, "");
    }

    public Material blockTexture(Block block, String suffix) {
        Identifier name = key(block);
        return new Material(ModelPaths.blockModel(name, suffix));
    }

    public Identifier existingModelFile(Block block) {
        Identifier name = key(block);
        return ModelPaths.blockModel(name);
    }

    public Identifier existingModelFile(String name) {
        return Nautec.rl("block/" + name);
    }

    public Identifier key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public String name(Block block) {
        return key(block).getPath();
    }

    public Identifier extend(Identifier rl, String suffix) {
        return ModelPaths.extend(rl, suffix);
    }
}
