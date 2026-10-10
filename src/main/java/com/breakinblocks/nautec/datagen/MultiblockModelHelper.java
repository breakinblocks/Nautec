package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.AugmentationStationBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.multiblocks.AugmentationStationMultiblock;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class MultiblockModelHelper {
    private final BlockModelProvider bmp;
    private final Map<ResourceLocation, ModelFile> createdFaceModels = new HashMap<>();

    public MultiblockModelHelper(BlockModelProvider bmp) {
        this.bmp = bmp;
    }

    public void augmentationStationController(AugmentationStationBlock augmentationStationBlock) {
        ModelFile formedModel = bmp.existingModelFile("multiblock/augmentation_station_4");
        ModelFile unformedModel = unformedAugmentationStationPart(augmentationStationBlock, "controller");
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(augmentationStationBlock);
        builder.partialState().with(Multiblock.FORMED, true).setModels(new ConfiguredModel(formedModel));
        builder.partialState().with(Multiblock.FORMED, false).setModels(new ConfiguredModel(unformedModel));
    }

    public void augmentationStationExtension(Block augmentationStationExtensionBlock) {
        ModelFile unformedModel = unformedAugmentationStationPart(augmentationStationExtensionBlock, "extension");
        ModelFile formedModel = bmp.existingModelFile("multiblock/augmentation_station_extension");
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(augmentationStationExtensionBlock);
        for (Direction dir : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            builder.partialState().with(Multiblock.FORMED, false).with(BlockStateProperties.HORIZONTAL_FACING, dir)
                    .setModels(new ConfiguredModel(unformedModel));
            builder.partialState().with(Multiblock.FORMED, true).with(BlockStateProperties.HORIZONTAL_FACING, dir)
                    .setModels(BlockModelProvider.rotated(formedModel, 0, ((int) dir.toYRot() + 180) % 360));
        }
    }

    public void augmentationStationPart(Block augmentationStationPartBlock, IntegerRange range) {
        ModelFile unformedModel = drainPartModel(augmentationStationPartBlock, 0, false);
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(augmentationStationPartBlock);
        for (int i : AugmentationStationMultiblock.AS_PART.getPossibleValues()) {
            builder.partialState().with(Multiblock.FORMED, false).with(AugmentationStationMultiblock.AS_PART, i)
                    .setModels(new ConfiguredModel(unformedModel));
        }
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
            ModelFile formedModel = bmp.existingModelFile("multiblock/augmentation_station_" + (8 - i));
            int index = i;
            if (i == 0 || i == 3 || i == 6) {
                index += 2;
            } else if (i == 2 || i == 5 || i == 8) {
                index -= 2;
            }
            builder.partialState().with(Multiblock.FORMED, true).with(AugmentationStationMultiblock.AS_PART, index)
                    .setModels(new ConfiguredModel(formedModel));
        }
    }

    public @NotNull ModelFile unformedAugmentationStationPart(Block augmentationStationController, String part) {
        Multiblock multiblock = NTMultiblocks.AUGMENTATION_STATION.get();
        return bmp.cube(bmp.name(augmentationStationController),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_bottom"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_top"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_side"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_side"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_side"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_side"),
                bmp.multiblockTexture(multiblock, "unformed/" + part + "_side"));
    }

    public void drainController(Block drainController) {
        Multiblock multiblock = NTMultiblocks.DRAIN.get();
        ModelFile unformedModel = drainControllerModel(drainController, multiblock, false);
        ModelFile formedModel = drainControllerModel(drainController, multiblock, true);
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(drainController);
        builder.partialState().with(DrainMultiblock.FORMED, true).setModels(new ConfiguredModel(formedModel));
        builder.partialState().with(DrainMultiblock.FORMED, false).setModels(new ConfiguredModel(unformedModel));
    }

    public @NotNull ModelFile drainControllerModel(Block drainController, Multiblock multiblock, boolean formed) {
        return bmp.cube(bmp.name(drainController) + (formed ? "_formed" : ""),
                bmp.multiblockTexture(multiblock, formed ? "bottom_4" : "drain_bottom_unformed"),
                bmp.multiblockTexture(multiblock, formed ? "top_4" : "drain_top_unformed"),
                bmp.multiblockTexture(multiblock, "drain_side_unformed"),
                bmp.multiblockTexture(multiblock, "drain_side_unformed"),
                bmp.multiblockTexture(multiblock, "drain_side_unformed"),
                bmp.multiblockTexture(multiblock, "drain_side_unformed"),
                bmp.multiblockTexture(multiblock, "drain_side_unformed"));
    }

    public void drainPart(Block drainPartBlock, IntegerRange range) {
        ModelFile unformedModel = drainPartModel(drainPartBlock, 0, false);
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(drainPartBlock);
        for (int i : DrainMultiblock.DRAIN_PART.getPossibleValues()) {
            for (boolean laserPort : new boolean[]{false, true}) {
                builder.partialState().with(DrainMultiblock.FORMED, false).with(DrainMultiblock.DRAIN_PART, i)
                        .with(DrainPartBlock.LASER_PORT, laserPort)
                        .setModels(new ConfiguredModel(unformedModel));
            }
        }
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
            for (boolean laserPort : new boolean[]{false, true}) {
                builder.partialState().with(DrainMultiblock.FORMED, true).with(DrainMultiblock.DRAIN_PART, i)
                        .with(DrainPartBlock.LASER_PORT, laserPort)
                        .setModels(new ConfiguredModel(drainPartModel(drainPartBlock, i, laserPort)));
            }
        }
    }

    public ModelFile drainPartModel(Block drainPartBlock, int index, boolean laserPort) {
        String postfix = laserPort ? "_open" : "";
        String name = bmp.name(drainPartBlock) + "_" + index + postfix;
        Multiblock multiblock = NTMultiblocks.DRAIN.get();
        ResourceLocation up = bmp.multiblockTexture(multiblock, "top_" + index);
        ResourceLocation down = bmp.multiblockTexture(multiblock, "bottom_" + index);
        SideTextures sides = sideTextures(multiblock, index, "side_1" + postfix, corner -> "side_" + corner);
        return bmp.cube(name, down, up, sides.north(), sides.south(), sides.east(), sides.west(), sides.north());
    }

    public void bioReactorController(Block block) {
        Multiblock multiblock = NTMultiblocks.BIO_REACTOR.get();
        ResourceLocation side = bmp.multiblockTexture(multiblock, "controller_side");
        ModelFile unformed = bmp.cube(bmp.name(block),
                bmp.multiblockTexture(multiblock, "controller_bottom"),
                bmp.multiblockTexture(multiblock, "controller_top"),
                side, side, side, side, side);
        ResourceLocation hidden = bmp.blockTexture(NTBlocks.POLISHED_PRISMARINE.get());
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(block);
        for (boolean active : new boolean[]{false, true}) {
            String suffix = active ? "_active" : "";
            ModelFile formed = bmp.cube(bmp.name(block) + "_formed" + suffix, hidden,
                    bmp.multiblockTexture(multiblock, "top_c1_r1" + suffix), hidden, hidden, hidden, hidden,
                    bmp.multiblockTexture(multiblock, "top_c1_r1"));
            builder.partialState().with(Multiblock.FORMED, false).with(BioReactorMultiblock.ACTIVE, active)
                    .setModels(new ConfiguredModel(unformed));
            builder.partialState().with(Multiblock.FORMED, true).with(BioReactorMultiblock.ACTIVE, active)
                    .setModels(new ConfiguredModel(formed));
        }
    }

    public void bioReactorPart(Block block) {
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(block);
        for (int i : BioReactorMultiblock.BIO_REACTOR_PART.getPossibleValues()) {
            for (boolean top : new boolean[]{false, true}) {
                for (boolean hatch : new boolean[]{false, true}) {
                    for (boolean active : new boolean[]{false, true}) {
                        boolean shownHatch = hatch && top && i % 2 != 0;
                        builder.partialState()
                                .with(BioReactorMultiblock.BIO_REACTOR_PART, i)
                                .with(BioReactorMultiblock.TOP, top)
                                .with(BioReactorMultiblock.HATCH, hatch)
                                .with(BioReactorMultiblock.ACTIVE, active)
                                .setModels(new ConfiguredModel(bioReactorPartModel(block, i, top, shownHatch, active)));
                    }
                }
            }
        }
    }

    private ModelFile bioReactorPartModel(Block block, int index, boolean top, boolean hatch, boolean active) {
        Multiblock multiblock = NTMultiblocks.BIO_REACTOR.get();
        String suffix = active ? "_active" : "";
        String name = bmp.name(block) + "_" + index + "_" + (top ? "top" : "bottom") + (hatch ? "_hatch" : "") + suffix;
        int x = index % 3;
        int z = index / 3;
        int row = top ? 0 : 1;
        ResourceLocation hidden = bmp.blockTexture(NTBlocks.POLISHED_PRISMARINE.get());
        ResourceLocation up = top ? bmp.multiblockTexture(multiblock, slice("top", x, z) + (hatch ? "_hatch" : "") + suffix) : hidden;
        ResourceLocation down = top ? hidden : bmp.multiblockTexture(multiblock, slice("bottom", x, 2 - z) + suffix);
        ResourceLocation north = z == 0 ? bmp.multiblockTexture(multiblock, slice("side", 2 - x, row) + suffix) : hidden;
        ResourceLocation south = z == 2 ? bmp.multiblockTexture(multiblock, slice("side", x, row) + suffix) : hidden;
        ResourceLocation east = x == 2 ? bmp.multiblockTexture(multiblock, slice("side", 2 - z, row) + suffix) : hidden;
        ResourceLocation west = x == 0 ? bmp.multiblockTexture(multiblock, slice("side", z, row) + suffix) : hidden;
        return bmp.cube(name, down, up, north, south, east, west, bmp.multiblockTexture(multiblock, "side_c0_r1"));
    }

    public void industrialBioReactorController(Block block) {
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        ResourceLocation side = bmp.multiblockTexture(multiblock, "controller_side");
        ModelFile unformed = bmp.cube(bmp.name(block),
                bmp.multiblockTexture(multiblock, "controller_bottom"),
                bmp.multiblockTexture(multiblock, "controller_top"),
                side, side, side, side, side);
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(block);
        for (boolean active : new boolean[]{false, true}) {
            ModelFile formed = industrialModel(bmp.name(block) + "_formed" + (active ? "_active" : ""),
                    IndustrialBioReactorMultiblock.CONTROLLER_LAYER, IndustrialBioReactorMultiblock.CONTROLLER_CELL, false, active);
            for (HorizontalDirection orientation : HorizontalDirection.values()) {
                builder.partialState().with(Multiblock.FORMED, false).with(BioReactorMultiblock.ACTIVE, active)
                        .with(IndustrialBioReactorMultiblock.ORIENTATION, orientation)
                        .setModels(new ConfiguredModel(unformed));
                builder.partialState().with(Multiblock.FORMED, true).with(BioReactorMultiblock.ACTIVE, active)
                        .with(IndustrialBioReactorMultiblock.ORIENTATION, orientation)
                        .setModels(industrialVariant(formed, orientation));
            }
        }
    }

    public void industrialBioReactorPart(Block block) {
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        ModelFile invalid = bmp.cubeAll(bmp.name(block) + "_invalid", bmp.multiblockTexture(multiblock, "interior"));
        VariantBlockStateBuilder builder = bmp.getVariantBuilder(block);
        for (int layer : IndustrialBioReactorMultiblock.LAYER.getPossibleValues()) {
            for (int cell : IndustrialBioReactorMultiblock.CELL.getPossibleValues()) {
                int key = IndustrialBioReactorMultiblock.keyAt(layer, cell);
                boolean part = key != IndustrialBioReactorMultiblock.CHAMBER && key != IndustrialBioReactorMultiblock.CONTROLLER;
                for (boolean hatch : new boolean[]{false, true}) {
                    for (boolean active : new boolean[]{false, true}) {
                        ModelFile model = invalid;
                        if (part) {
                            boolean shownHatch = hatch && IndustrialBioReactorMultiblock.isHatchCandidate(layer, cell);
                            model = industrialModel(bmp.name(block) + "_l" + layer + "_c" + cell
                                    + (shownHatch ? "_hatch" : "") + (active ? "_active" : ""), layer, cell, shownHatch, active);
                        }
                        for (HorizontalDirection orientation : HorizontalDirection.values()) {
                            builder.partialState()
                                    .with(IndustrialBioReactorMultiblock.LAYER, layer)
                                    .with(IndustrialBioReactorMultiblock.CELL, cell)
                                    .with(BioReactorMultiblock.HATCH, hatch)
                                    .with(BioReactorMultiblock.ACTIVE, active)
                                    .with(IndustrialBioReactorMultiblock.ORIENTATION, orientation)
                                    .setModels(industrialVariant(model, orientation));
                        }
                    }
                }
            }
        }
    }

    private static ConfiguredModel industrialVariant(ModelFile model, HorizontalDirection orientation) {
        return BlockModelProvider.rotated(model, 0, orientation.ordinal() * 90);
    }

    private ModelFile industrialModel(String name, int layer, int cell, boolean hatch, boolean active) {
        ResourceLocation id = Nautec.rl("block/" + name);
        ModelFile existing = createdFaceModels.get(id);
        if (existing != null) {
            return existing;
        }
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        int size = IndustrialBioReactorMultiblock.SIZE;
        int x = cell % size;
        int z = cell / size;
        boolean window = industrialWindow(layer, cell);
        ResourceLocation interior = bmp.multiblockTexture(multiblock, "interior");
        ResourceLocation interiorGlass = bmp.multiblockTexture(multiblock, "interior_glass");
        Map<Direction, ResourceLocation> textures = new EnumMap<>(Direction.class);
        Map<Direction, Boolean> cull = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            int nx = x + direction.getStepX();
            int ny = layer + direction.getStepY();
            int nz = z + direction.getStepZ();
            if (nx < 0 || nx >= size || nz < 0 || nz >= size || ny < 0 || ny >= IndustrialBioReactorMultiblock.HEIGHT) {
                textures.put(direction, bmp.multiblockTexture(multiblock, industrialOutward(direction, layer, cell, hatch, active)));
                cull.put(direction, true);
                continue;
            }
            int neighbour = nz * size + nx;
            if (IndustrialBioReactorMultiblock.keyAt(ny, neighbour) == IndustrialBioReactorMultiblock.CHAMBER) {
                textures.put(direction, window ? interiorGlass : interior);
                cull.put(direction, false);
            } else if (!window && industrialWindow(ny, neighbour)) {
                textures.put(direction, interior);
                cull.put(direction, false);
            }
        }

        BlockModelBuilder model = bmp.models().withExistingParent(id.toString(), ResourceLocation.withDefaultNamespace("block/block"))
                .texture("particle", bmp.multiblockTexture(multiblock, "side_c0_r1"));
        ModelBuilder<BlockModelBuilder>.ElementBuilder element = model.element().from(0, 0, 0).to(16, 16, 16);
        for (Map.Entry<Direction, ResourceLocation> entry : textures.entrySet()) {
            String key = entry.getKey().getSerializedName();
            model.texture(key, entry.getValue());
            element.face(entry.getKey()).texture("#" + key).cullface(cull.get(entry.getKey()) ? entry.getKey() : null);
        }
        if (window) {
            model.renderType(BlockModelProvider.TRANSLUCENT);
        }
        createdFaceModels.put(id, model);
        return model;
    }

    private static boolean industrialWindow(int layer, int cell) {
        int key = IndustrialBioReactorMultiblock.keyAt(layer, cell);
        return key == IndustrialBioReactorMultiblock.SHIELD || key == IndustrialBioReactorMultiblock.CONTROLLER;
    }

    private static String industrialOutward(Direction direction, int layer, int cell, boolean hatch, boolean active) {
        int size = IndustrialBioReactorMultiblock.SIZE;
        int last = size - 1;
        int x = cell % size;
        int z = cell / size;
        int row = IndustrialBioReactorMultiblock.HEIGHT - 1 - layer;
        String suffix = active ? "_active" : "";
        String hatchSuffix = hatch ? "_hatch" : "";
        boolean controller = layer == IndustrialBioReactorMultiblock.CONTROLLER_LAYER && cell == IndustrialBioReactorMultiblock.CONTROLLER_CELL;
        return switch (direction) {
            case UP -> slice("top", x, z) + hatchSuffix + suffix;
            case DOWN -> slice("bottom", x, last - z) + suffix;
            case NORTH -> controller ? slice("front", last - x, row) + suffix : slice("side", last - x, row) + hatchSuffix + suffix;
            case SOUTH -> slice("side", x, row) + hatchSuffix + suffix;
            case EAST -> slice("side", last - z, row) + hatchSuffix + suffix;
            case WEST -> slice("side", z, row) + hatchSuffix + suffix;
        };
    }

    private static String slice(String face, int column, int row) {
        return face + "_c" + column + "_r" + row;
    }

    private SideTextures sideTextures(Multiblock multiblock, int index, String uniformKey, IntFunction<String> cornerKey) {
        if (index % 2 != 0) {
            ResourceLocation uniform = bmp.multiblockTexture(multiblock, uniformKey);
            return new SideTextures(uniform, uniform);
        }

        int corner = index % 3;
        int opposite = 2 - corner;
        boolean mirrored = index == 0 || index == 2;
        return new SideTextures(
                bmp.multiblockTexture(multiblock, cornerKey.apply(mirrored ? opposite : corner)),
                bmp.multiblockTexture(multiblock, cornerKey.apply(mirrored ? corner : opposite)));
    }

    private record SideTextures(ResourceLocation north, ResourceLocation east) {
        ResourceLocation south() {
            return north;
        }

        ResourceLocation west() {
            return east;
        }
    }
}
