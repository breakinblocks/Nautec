package com.breakinblocks.nautec.content.multiblocks;

import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.api.multiblocks.MultiblockLayer;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class IndustrialBioReactorMultiblock implements Multiblock {
    public static final int SIZE = 5;
    public static final int HEIGHT = 4;
    public static final int CONTROLLER_LAYER = 1;
    public static final int CONTROLLER_CELL = 2;
    public static final IntegerProperty LAYER = IntegerProperty.create("layer", 0, HEIGHT - 1);
    public static final IntegerProperty CELL = IntegerProperty.create("cell", 0, SIZE * SIZE - 1);
    public static final EnumProperty<HorizontalDirection> ORIENTATION = EnumProperty.create("orientation", HorizontalDirection.class);

    public static final int CONTROLLER = 0;
    public static final int PILLAR = 1;
    public static final int POLISHED = 2;
    public static final int SHIELD = 3;
    public static final int STEEL = 4;
    public static final int CHAMBER = 5;

    private static final MultiblockLayer[] LAYOUT = {
            new MultiblockLayer(false, IntegerRange.of(1, 1), new int[]{
                    1, 2, 2, 2, 1,
                    2, 2, 2, 2, 2,
                    2, 2, 2, 2, 2,
                    2, 2, 2, 2, 2,
                    1, 2, 2, 2, 1
            }),
            new MultiblockLayer(false, IntegerRange.of(1, 1), new int[]{
                    1, 3, 0, 3, 1,
                    3, 5, 5, 5, 3,
                    3, 5, 5, 5, 3,
                    3, 5, 5, 5, 3,
                    1, 3, 3, 3, 1
            }),
            new MultiblockLayer(false, IntegerRange.of(1, 1), new int[]{
                    1, 3, 3, 3, 1,
                    3, 5, 5, 5, 3,
                    3, 5, 5, 5, 3,
                    3, 5, 5, 5, 3,
                    1, 3, 3, 3, 1
            }),
            new MultiblockLayer(false, IntegerRange.of(1, 1), new int[]{
                    1, 4, 4, 4, 1,
                    4, 4, 4, 4, 4,
                    4, 4, 4, 4, 4,
                    4, 4, 4, 4, 4,
                    1, 4, 4, 4, 1
            })
    };

    public static int keyAt(int layer, int cell) {
        if (layer < 0 || layer >= LAYOUT.length || cell < 0 || cell >= SIZE * SIZE) {
            return CHAMBER;
        }
        return LAYOUT[layer].layer()[cell];
    }

    public static Direction toWorld(Direction canonical, HorizontalDirection orientation) {
        if (!canonical.getAxis().isHorizontal()) {
            return canonical;
        }
        Direction result = canonical;
        for (int i = 0; i < orientation.ordinal(); i++) {
            result = result.getClockWise();
        }
        return result;
    }

    public static Set<Direction> toWorld(Set<Direction> canonical, HorizontalDirection orientation) {
        Set<Direction> faces = EnumSet.noneOf(Direction.class);
        for (Direction direction : canonical) {
            faces.add(toWorld(direction, orientation));
        }
        return faces;
    }

    public static HorizontalDirection orientation(BlockState state) {
        return state.hasProperty(ORIENTATION) ? state.getValue(ORIENTATION) : HorizontalDirection.NORTH;
    }

    public static boolean isCorner(int cell) {
        return cell == 0 || cell == SIZE - 1 || cell == SIZE * (SIZE - 1) || cell == SIZE * SIZE - 1;
    }

    public static boolean isHatchCandidate(int layer, int cell) {
        return keyAt(layer, cell) == STEEL && layer == HEIGHT - 1;
    }

    public static Set<Direction> outwardFaces(int layer, int cell) {
        Set<Direction> faces = EnumSet.noneOf(Direction.class);
        int x = cell % SIZE;
        int z = cell / SIZE;
        if (x == 0) {
            faces.add(Direction.WEST);
        }
        if (x == SIZE - 1) {
            faces.add(Direction.EAST);
        }
        if (z == 0) {
            faces.add(Direction.NORTH);
        }
        if (z == SIZE - 1) {
            faces.add(Direction.SOUTH);
        }
        if (layer == 0) {
            faces.add(Direction.DOWN);
        }
        if (layer == HEIGHT - 1) {
            faces.add(Direction.UP);
        }
        return faces;
    }

    @Override
    public Block getUnformedController() {
        return NTBlocks.INDUSTRIAL_BIO_REACTOR.get();
    }

    @Override
    public Block getFormedController() {
        return NTBlocks.INDUSTRIAL_BIO_REACTOR.get();
    }

    @Override
    public MultiblockLayer[] getLayout() {
        return LAYOUT;
    }

    @Override
    public Map<Integer, Block> getDefinition() {
        return Map.of(
                CONTROLLER, getUnformedController(),
                PILLAR, NTBlocks.DARK_PRISMARINE_PILLAR.get(),
                POLISHED, NTBlocks.POLISHED_PRISMARINE.get(),
                SHIELD, NTBlocks.BACTERIAL_CONTAINMENT_SHIELD.get(),
                STEEL, NTBlocks.AQUARINE_STEEL_BLOCK.get(),
                CHAMBER, Blocks.AIR
        );
    }

    @Override
    public BlockEntityType<? extends MultiblockEntity> getMultiBlockEntityType() {
        return NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR.get();
    }

    @Override
    public @Nullable BlockState formBlock(Level level, BlockPos blockPos, BlockPos controllerPos, int layerIndex, int layoutIndex, MultiblockData multiblockData, @Nullable Player player) {
        int key = keyAt(layoutIndex, layerIndex);
        if (key == CHAMBER) {
            return null;
        }
        HorizontalDirection orientation = multiblockData.direction() != null ? multiblockData.direction() : HorizontalDirection.NORTH;
        if (key == CONTROLLER) {
            return getFormedController().defaultBlockState().setValue(FORMED, true).setValue(ORIENTATION, orientation);
        }
        return NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get().defaultBlockState()
                .setValue(LAYER, layoutIndex)
                .setValue(CELL, layerIndex)
                .setValue(ORIENTATION, orientation)
                .setValue(FORMED, true);
    }

    @Override
    public boolean isFormed(Level level, BlockPos blockPos) {
        BlockState block = level.getBlockState(blockPos);
        return block.hasProperty(FORMED) && block.getValue(FORMED);
    }
}
