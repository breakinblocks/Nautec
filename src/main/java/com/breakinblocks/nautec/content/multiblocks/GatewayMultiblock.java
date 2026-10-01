package com.breakinblocks.nautec.content.multiblocks;

import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.api.multiblocks.MultiblockLayer;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blocks.GatewayRingPartBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GatewayMultiblock implements Multiblock {
    public static final int CONTROLLER = 0;
    public static final int RING = 1;
    public static final int ANY = 2;

    private static final MultiblockLayer[] LAYOUT = buildLayout();

    private static MultiblockLayer[] buildLayout() {
        MultiblockLayer[] layers = new MultiblockLayer[GatewayRing.SIZE];
        for (int y = 0; y < GatewayRing.SIZE; y++) {
            int[] row = new int[GatewayRing.SIZE];
            for (int x = 0; x < GatewayRing.SIZE; x++) {
                row[x] = GatewayRing.isCore(x, y) ? CONTROLLER : GatewayRing.isRingCell(x, y) ? RING : ANY;
            }
            layers[y] = new MultiblockLayer(false, IntegerRange.of(1, 1), row).setWidths(GatewayRing.SIZE, 1);
        }
        return layers;
    }

    public static int keyAt(int x, int y) {
        if (x < 0 || y < 0 || x >= GatewayRing.SIZE || y >= GatewayRing.SIZE) {
            return ANY;
        }
        return LAYOUT[y].layer()[x];
    }

    public static boolean submerged(Level level, BlockPos pos) {
        if (level.getFluidState(pos).is(Fluids.WATER)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Block getUnformedController() {
        return NTBlocks.GATEWAY.get();
    }

    @Override
    public Block getFormedController() {
        return NTBlocks.GATEWAY.get();
    }

    @Override
    public MultiblockLayer[] getLayout() {
        return LAYOUT;
    }

    @Override
    public Map<Integer, Block> getDefinition() {
        Map<Integer, Block> definition = new HashMap<>();
        definition.put(CONTROLLER, NTBlocks.GATEWAY.get());
        definition.put(RING, NTBlocks.GATEWAY_RING.get());
        definition.put(ANY, null);
        return definition;
    }

    @Override
    public BlockEntityType<? extends MultiblockEntity> getMultiBlockEntityType() {
        return NTBlockEntityTypes.GATEWAY.get();
    }

    @Override
    public @Nullable BlockState formBlock(Level level, BlockPos blockPos, BlockPos controllerPos, int layerIndex, int layoutIndex, MultiblockData multiblockData, @Nullable Player player) {
        int key = keyAt(layerIndex, layoutIndex);
        boolean water = submerged(level, blockPos);
        if (key == CONTROLLER) {
            return NTBlocks.GATEWAY.get().defaultBlockState()
                    .setValue(FORMED, true)
                    .setValue(BlockStateProperties.WATERLOGGED, water);
        }
        if (key == RING) {
            return NTBlocks.GATEWAY_RING_PART.get().defaultBlockState()
                    .setValue(GatewayRingPartBlock.CELL, GatewayRing.cellIndex(layerIndex, layoutIndex))
                    .setValue(GatewayRingPartBlock.ORIENTATION, multiblockData.direction())
                    .setValue(BlockStateProperties.WATERLOGGED, water);
        }
        return null;
    }

    @Override
    public void afterFormBlock(Level level, BlockPos blockPos, BlockPos controllerPos, int layerIndex, int layoutIndex, MultiblockData multiblockData, @Nullable Player player) {
        if (keyAt(layerIndex, layoutIndex) == CONTROLLER && level.getBlockEntity(blockPos) instanceof GatewayBlockEntity gateway) {
            gateway.onFormed(multiblockData, player);
        }
    }

    @Override
    public void afterUnformBlock(Level level, BlockPos blockPos, BlockPos controllerPos, int layerIndex, int layoutIndex, HorizontalDirection direction, @Nullable Player player) {
        if (keyAt(layerIndex, layoutIndex) == CONTROLLER && level.getBlockEntity(blockPos) instanceof GatewayBlockEntity gateway) {
            gateway.onUnformed();
        }
    }

    @Override
    public boolean isFormed(Level level, BlockPos blockPos) {
        BlockState state = level.getBlockState(blockPos);
        return state.is(NTBlocks.GATEWAY_RING_PART.get()) || (state.hasProperty(FORMED) && state.getValue(FORMED));
    }
}
