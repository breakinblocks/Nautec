package com.breakinblocks.nautec.content.bubble;


import com.breakinblocks.nautec.api.blockentities.NTBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class AirPocketBlockEntity extends NTBlockEntity {
    private static final Set<AirPocketBlockEntity> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int WARNING_TICKS = 100;
    private static final int HOLD_INTERVAL = 20;

    private int radius;
    private int ticks;

    public AirPocketBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.AIR_POCKET.get(), pos, state);
    }

    public static boolean covered(Level level, BlockPos pos, @Nullable Object self) {
        for (AirPocketBlockEntity pocket : ACTIVE) {
            if (pocket != self && !pocket.isRemoved() && pocket.level == level && pocket.covers(pos)) {
                return true;
            }
        }
        return false;
    }

    public void start(ServerLevel serverLevel, int radius, int ticks) {
        this.radius = radius;
        this.ticks = ticks;
        ACTIVE.add(this);
        holdAll(serverLevel);
        setChanged();
    }

    private boolean covers(BlockPos pos) {
        return Math.abs(pos.getX() - worldPosition.getX()) <= radius && Math.abs(pos.getY() - worldPosition.getY()) <= radius
                && Math.abs(pos.getZ() - worldPosition.getZ()) <= radius;
    }

    public void serverTick(ServerLevel serverLevel) {
        ACTIVE.add(this);
        if (ticks <= 0) {
            collapse(serverLevel);
            return;
        }
        if (ticks % HOLD_INTERVAL == 0) {
            holdAll(serverLevel);
            setChanged();
        }
        if (ticks == WARNING_TICKS) {
            for (Player player : serverLevel.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(radius + 4))) {
                player.displayClientMessage(Component.translatable("nautec.bubble_capsule.warning").withStyle(ChatFormatting.GOLD), true);
            }
        }
        ticks--;
    }

    private void holdAll(ServerLevel serverLevel) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        BubbleAnchorBlockEntity.hold(serverLevel, cursor.setWithOffset(worldPosition, dx, dy, dz).immutable());
                    }
                }
            }
        }
    }

    private void releaseAll(Level world) {
        ACTIVE.remove(this);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        BubbleAnchorBlockEntity.release(world, cursor.setWithOffset(worldPosition, dx, dy, dz).immutable(), true, this);
                    }
                }
            }
        }
    }

    private void collapse(ServerLevel serverLevel) {
        releaseAll(serverLevel);
        serverLevel.playSound(null, worldPosition, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 1.0F, 0.8F);
        serverLevel.setBlock(worldPosition, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            releaseAll(level);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void setRemoved() {
        ACTIVE.remove(this);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("radius", radius);
        output.putInt("ticks", ticks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getIntOr("radius", 0);
        this.ticks = input.getIntOr("ticks", 0);
    }
}
