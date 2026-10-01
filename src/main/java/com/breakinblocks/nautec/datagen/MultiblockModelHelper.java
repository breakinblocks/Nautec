package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.AugmentationStationBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.multiblocks.AugmentationStationMultiblock;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

public class MultiblockModelHelper {
    private final BlockModelProvider bmp;
    private final Set<Identifier> createdFaceModels = new HashSet<>();

    public MultiblockModelHelper(BlockModelProvider bmp) {
        this.bmp = bmp;
    }

    public void augmentationStationController(AugmentationStationBlock augmentationStationBlock) {
        MultiVariant formedModel = BlockModelGenerators.plainVariant(bmp.existingModelFile("multiblock/augmentation_station_4"));
        MultiVariant unformedModel = BlockModelGenerators.plainVariant(unformedAugmentationStationPart(augmentationStationBlock, "controller"));
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(augmentationStationBlock)
                .with(BlockModelGenerators.createBooleanModelDispatch(Multiblock.FORMED, formedModel, unformedModel)));
    }

    public void augmentationStationExtension(Block augmentationStationExtensionBlock) {
        MultiVariant unformedModel = BlockModelGenerators.plainVariant(unformedAugmentationStationPart(augmentationStationExtensionBlock, "extension"));
        MultiVariant formedModel = BlockModelGenerators.plainVariant(bmp.existingModelFile("multiblock/augmentation_station_extension"));
        PropertyDispatch.C2<MultiVariant, Boolean, Direction> dispatch = PropertyDispatch.initial(Multiblock.FORMED, BlockStateProperties.HORIZONTAL_FACING);
        for (Direction dir : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            dispatch = dispatch
                    .select(false, dir, unformedModel)
                    .select(true, dir, BlockModelProvider.rotated(formedModel, 0, ((int) dir.toYRot() + 180) % 360));
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(augmentationStationExtensionBlock).with(dispatch));
    }

    public void augmentationStationPart(Block augmentationStationPartBlock, IntegerRange range) {
        MultiVariant unformedModel = BlockModelGenerators.plainVariant(drainPartModel(augmentationStationPartBlock, 0, false));
        PropertyDispatch.C2<MultiVariant, Boolean, Integer> dispatch = PropertyDispatch.initial(Multiblock.FORMED, AugmentationStationMultiblock.AS_PART);
        for (int i : AugmentationStationMultiblock.AS_PART.getPossibleValues()) {
            dispatch = dispatch.select(false, i, unformedModel);
        }
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
            MultiVariant formedModel = BlockModelGenerators.plainVariant(bmp.existingModelFile("multiblock/augmentation_station_" + (8 - i)));
            int index = i;
            if (i == 0 || i == 3 || i == 6) {
                index += 2;
            } else if (i == 2 || i == 5 || i == 8) {
                index -= 2;
            }
            dispatch = dispatch.select(true, index, formedModel);
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(augmentationStationPartBlock).with(dispatch));
    }

    public @NotNull Identifier unformedAugmentationStationPart(Block augmentationStationController, String part) {
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
        MultiVariant unformedModel = BlockModelGenerators.plainVariant(drainControllerModel(drainController, multiblock, false));
        MultiVariant formedModel = BlockModelGenerators.plainVariant(drainControllerModel(drainController, multiblock, true));
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(drainController)
                .with(BlockModelGenerators.createBooleanModelDispatch(DrainMultiblock.FORMED, formedModel, unformedModel)));
    }

    public @NotNull Identifier drainControllerModel(Block drainController, Multiblock multiblock, boolean formed) {
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
        MultiVariant unformedModel = BlockModelGenerators.plainVariant(drainPartModel(drainPartBlock, 0, false));
        PropertyDispatch.C3<MultiVariant, Boolean, Integer, Boolean> dispatch = PropertyDispatch.initial(DrainMultiblock.FORMED, DrainMultiblock.DRAIN_PART, DrainPartBlock.LASER_PORT);
        for (int i : DrainMultiblock.DRAIN_PART.getPossibleValues()) {
            dispatch = dispatch
                    .select(false, i, false, unformedModel)
                    .select(false, i, true, unformedModel);
        }
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
            dispatch = dispatch
                    .select(true, i, false, BlockModelGenerators.plainVariant(drainPartModel(drainPartBlock, i, false)))
                    .select(true, i, true, BlockModelGenerators.plainVariant(drainPartModel(drainPartBlock, i, true)));
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(drainPartBlock).with(dispatch));
    }

    public Identifier drainPartModel(Block drainPartBlock, int index, boolean laserPort) {
        String postfix = laserPort ? "_open" : "";
        String name = bmp.name(drainPartBlock) + "_" + index + postfix;
        Multiblock multiblock = NTMultiblocks.DRAIN.get();
        Material up = bmp.multiblockTexture(multiblock, "top_" + index);
        Material down = bmp.multiblockTexture(multiblock, "bottom_" + index);
        SideTextures sides = sideTextures(multiblock, index, "side_1" + postfix, corner -> "side_" + corner);
        return bmp.cube(name, down, up, sides.north(), sides.south(), sides.east(), sides.west(), sides.north());
    }

    public void bioReactorController(Block block) {
        Multiblock multiblock = NTMultiblocks.BIO_REACTOR.get();
        Material side = bmp.multiblockTexture(multiblock, "controller_side");
        Identifier unformed = bmp.cube(bmp.name(block),
                bmp.multiblockTexture(multiblock, "controller_bottom"),
                bmp.multiblockTexture(multiblock, "controller_top"),
                side, side, side, side, side);
        Material hidden = bmp.blockTexture(NTBlocks.POLISHED_PRISMARINE.get());
        PropertyDispatch.C2<MultiVariant, Boolean, Boolean> dispatch = PropertyDispatch.initial(Multiblock.FORMED, BioReactorMultiblock.ACTIVE);
        for (boolean active : new boolean[]{false, true}) {
            String suffix = active ? "_active" : "";
            Identifier formed = bmp.cube(bmp.name(block) + "_formed" + suffix, hidden,
                    bmp.multiblockTexture(multiblock, "top_c1_r1" + suffix), hidden, hidden, hidden, hidden,
                    bmp.multiblockTexture(multiblock, "top_c1_r1"));
            dispatch = dispatch
                    .select(false, active, BlockModelGenerators.plainVariant(unformed))
                    .select(true, active, BlockModelGenerators.plainVariant(formed));
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    public void bioReactorPart(Block block) {
        PropertyDispatch.C4<MultiVariant, Integer, Boolean, Boolean, Boolean> dispatch = PropertyDispatch.initial(
                BioReactorMultiblock.BIO_REACTOR_PART, BioReactorMultiblock.TOP, BioReactorMultiblock.HATCH, BioReactorMultiblock.ACTIVE);
        for (int i : BioReactorMultiblock.BIO_REACTOR_PART.getPossibleValues()) {
            for (boolean top : new boolean[]{false, true}) {
                for (boolean hatch : new boolean[]{false, true}) {
                    for (boolean active : new boolean[]{false, true}) {
                        boolean shownHatch = hatch && top && i % 2 != 0;
                        dispatch = dispatch.select(i, top, hatch, active,
                                BlockModelGenerators.plainVariant(bioReactorPartModel(block, i, top, shownHatch, active)));
                    }
                }
            }
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private Identifier bioReactorPartModel(Block block, int index, boolean top, boolean hatch, boolean active) {
        Multiblock multiblock = NTMultiblocks.BIO_REACTOR.get();
        String suffix = active ? "_active" : "";
        String name = bmp.name(block) + "_" + index + "_" + (top ? "top" : "bottom") + (hatch ? "_hatch" : "") + suffix;
        int x = index % 3;
        int z = index / 3;
        int row = top ? 0 : 1;
        Material hidden = bmp.blockTexture(NTBlocks.POLISHED_PRISMARINE.get());
        Material up = top ? bmp.multiblockTexture(multiblock, slice("top", x, z) + (hatch ? "_hatch" : "") + suffix) : hidden;
        Material down = top ? hidden : bmp.multiblockTexture(multiblock, slice("bottom", x, 2 - z) + suffix);
        Material north = z == 0 ? bmp.multiblockTexture(multiblock, slice("side", 2 - x, row) + suffix) : hidden;
        Material south = z == 2 ? bmp.multiblockTexture(multiblock, slice("side", x, row) + suffix) : hidden;
        Material east = x == 2 ? bmp.multiblockTexture(multiblock, slice("side", 2 - z, row) + suffix) : hidden;
        Material west = x == 0 ? bmp.multiblockTexture(multiblock, slice("side", z, row) + suffix) : hidden;
        return bmp.cube(name, down, up, north, south, east, west, bmp.multiblockTexture(multiblock, "side_c0_r1"));
    }

    public void industrialBioReactorController(Block block) {
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        Material side = bmp.multiblockTexture(multiblock, "controller_side");
        Identifier unformed = bmp.cube(bmp.name(block),
                bmp.multiblockTexture(multiblock, "controller_bottom"),
                bmp.multiblockTexture(multiblock, "controller_top"),
                side, side, side, side, side);
        PropertyDispatch.C2<MultiVariant, Boolean, Boolean> dispatch = PropertyDispatch.initial(Multiblock.FORMED, BioReactorMultiblock.ACTIVE);
        for (boolean active : new boolean[]{false, true}) {
            Identifier formed = industrialModel(bmp.name(block) + "_formed" + (active ? "_active" : ""),
                    IndustrialBioReactorMultiblock.CONTROLLER_LAYER, IndustrialBioReactorMultiblock.CONTROLLER_CELL, false, active);
            dispatch = dispatch
                    .select(false, active, BlockModelGenerators.plainVariant(unformed))
                    .select(true, active, BlockModelGenerators.plainVariant(formed));
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    public void industrialBioReactorPart(Block block) {
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        Identifier invalid = bmp.cubeAll(bmp.name(block) + "_invalid", bmp.multiblockTexture(multiblock, "interior"));
        PropertyDispatch.C4<MultiVariant, Integer, Integer, Boolean, Boolean> dispatch = PropertyDispatch.initial(
                IndustrialBioReactorMultiblock.LAYER, IndustrialBioReactorMultiblock.CELL, BioReactorMultiblock.HATCH, BioReactorMultiblock.ACTIVE);
        for (int layer : IndustrialBioReactorMultiblock.LAYER.getPossibleValues()) {
            for (int cell : IndustrialBioReactorMultiblock.CELL.getPossibleValues()) {
                int key = IndustrialBioReactorMultiblock.keyAt(layer, cell);
                boolean part = key != IndustrialBioReactorMultiblock.CHAMBER && key != IndustrialBioReactorMultiblock.CONTROLLER;
                for (boolean hatch : new boolean[]{false, true}) {
                    for (boolean active : new boolean[]{false, true}) {
                        Identifier model = invalid;
                        if (part) {
                            boolean shownHatch = hatch && IndustrialBioReactorMultiblock.isHatchCandidate(layer, cell);
                            model = industrialModel(bmp.name(block) + "_l" + layer + "_c" + cell
                                    + (shownHatch ? "_hatch" : "") + (active ? "_active" : ""), layer, cell, shownHatch, active);
                        }
                        dispatch = dispatch.select(layer, cell, hatch, active, BlockModelGenerators.plainVariant(model));
                    }
                }
            }
        }
        bmp.blockModels().blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(dispatch));
    }

    private Identifier industrialModel(String name, int layer, int cell, boolean hatch, boolean active) {
        Identifier id = Nautec.rl("block/" + name);
        if (!createdFaceModels.add(id)) {
            return id;
        }
        Multiblock multiblock = NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
        int size = IndustrialBioReactorMultiblock.SIZE;
        int x = cell % size;
        int z = cell / size;
        boolean window = industrialWindow(layer, cell);
        Material interior = bmp.multiblockTexture(multiblock, "interior");
        Material interiorGlass = bmp.multiblockTexture(multiblock, "interior_glass");
        Map<Direction, Material> textures = new EnumMap<>(Direction.class);
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

        JsonObject textureJson = new JsonObject();
        textureJson.addProperty("particle", bmp.multiblockTexture(multiblock, "side_c0_r1").sprite().toString());
        JsonObject faces = new JsonObject();
        for (Map.Entry<Direction, Material> entry : textures.entrySet()) {
            String key = entry.getKey().getSerializedName();
            textureJson.addProperty(key, entry.getValue().sprite().toString());
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#" + key);
            if (cull.get(entry.getKey())) {
                face.addProperty("cullface", key);
            }
            faces.add(key, face);
        }
        JsonObject element = new JsonObject();
        element.add("from", vector(0, 0, 0));
        element.add("to", vector(16, 16, 16));
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        root.add("textures", textureJson);
        root.add("elements", elements);
        ModelInstance instance = () -> root;
        bmp.blockModels().modelOutput.accept(id, instance);
        return id;
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

    private static JsonArray vector(int x, int y, int z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private SideTextures sideTextures(Multiblock multiblock, int index, String uniformKey, IntFunction<String> cornerKey) {
        if (index % 2 != 0) {
            Material uniform = bmp.multiblockTexture(multiblock, uniformKey);
            return new SideTextures(uniform, uniform);
        }

        int corner = index % 3;
        int opposite = 2 - corner;
        boolean mirrored = index == 0 || index == 2;
        return new SideTextures(
                bmp.multiblockTexture(multiblock, cornerKey.apply(mirrored ? opposite : corner)),
                bmp.multiblockTexture(multiblock, cornerKey.apply(mirrored ? corner : opposite)));
    }

    private record SideTextures(Material north, Material east) {
        Material south() {
            return north;
        }

        Material west() {
            return east;
        }
    }
}
