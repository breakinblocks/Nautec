package com.breakinblocks.nautec.content.showcase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ShowcasePlaceContext extends BlockPlaceContext {
    private final Direction looking;

    public ShowcasePlaceContext(Level level, BlockPos pos, Direction looking, ItemStack stack) {
        super(level, null, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atBottomCenterOf(pos), Direction.UP, pos, false));
        this.looking = looking;
    }

    @Override
    public BlockPos getClickedPos() {
        return getHitResult().getBlockPos();
    }

    @Override
    public boolean canPlace() {
        return getLevel().getBlockState(getClickedPos()).canBeReplaced(this);
    }

    @Override
    public boolean replacingClickedOnBlock() {
        return canPlace();
    }

    @Override
    public Direction getNearestLookingDirection() {
        return looking;
    }

    @Override
    public Direction getNearestLookingVerticalDirection() {
        return Direction.DOWN;
    }

    @Override
    public Direction[] getNearestLookingDirections() {
        return new Direction[]{Direction.DOWN, looking, looking.getClockWise(), looking.getCounterClockWise(), Direction.UP, looking.getOpposite()};
    }

    @Override
    public Direction getHorizontalDirection() {
        return looking;
    }

    @Override
    public boolean isSecondaryUseActive() {
        return false;
    }

    @Override
    public float getRotation() {
        return looking.toYRot();
    }
}
