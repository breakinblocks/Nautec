package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.conduits.TapFlow;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ConduitTapTint implements BlockTintSource {
    private static final int[] COLORS = {0xFF91E8AC, 0xFF6FB4FF, 0xFFFFA070, 0xFFC98CFF};

    private final Direction face;

    private ConduitTapTint(Direction face) {
        this.face = face;
    }

    public static List<BlockTintSource> sources() {
        List<BlockTintSource> sources = new ArrayList<>();
        sources.add(BlockTintSources.constant(-1));
        for (Direction direction : Direction.values()) {
            sources.add(new ConduitTapTint(direction));
        }
        return List.copyOf(sources);
    }

    public static int color(TapFlow flow) {
        return COLORS[flow.ordinal()];
    }

    @Override
    public int color(BlockState state) {
        return color(TapFlow.IDLE);
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap ? color(tap.flow(face)) : color(TapFlow.IDLE);
    }

    @Override
    public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return -1;
    }
}
