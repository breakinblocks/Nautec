package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.conduits.TapFlow;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ConduitTapTint implements BlockColor {
    public static final ConduitTapTint INSTANCE = new ConduitTapTint();

    private static final int[] COLORS = {0xFF91E8AC, 0xFF6FB4FF, 0xFFFFA070, 0xFFC98CFF};
    private static final Direction[] DIRECTIONS = Direction.values();

    private ConduitTapTint() {
    }

    public static int color(TapFlow flow) {
        return COLORS[flow.ordinal()];
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex <= 0 || tintIndex > DIRECTIONS.length) {
            return -1;
        }
        if (level == null || pos == null) {
            return color(TapFlow.IDLE);
        }
        Direction face = DIRECTIONS[tintIndex - 1];
        return level.getBlockEntity(pos) instanceof ConduitTapBlockEntity tap ? color(tap.flow(face)) : color(TapFlow.IDLE);
    }
}
