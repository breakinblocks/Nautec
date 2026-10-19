package com.breakinblocks.nautec.content.items.tools;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockPartEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class WrenchLinking {
    private WrenchLinking() {
    }

    public enum Result {
        LINKED,
        LINKED_BLOCKED,
        LINKED_CUT_SOURCE,
        LINKED_CUT_TARGET,
        NOT_IN_LINE,
        TOO_FAR,
        CANNOT_SEND,
        CANNOT_RECEIVE
    }

    public static @Nullable GlobalPos start(ItemStack stack) {
        return stack.get(NTDataComponents.WRENCH_LINK.get());
    }

    public static void clear(ItemStack stack) {
        stack.remove(NTDataComponents.WRENCH_LINK.get());
    }

    public static String describe(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static boolean canSend(BlockEntity blockEntity) {
        return blockEntity instanceof LaserJunctionBlockEntity
                || blockEntity instanceof LaserBlockEntity laser && !laser.getPotentialLaserOutputs().isEmpty();
    }

    public static boolean canReceive(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof LaserBlockEntity) {
            return true;
        }
        if (blockEntity == null) {
            return false;
        }
        for (Direction side : Direction.values()) {
            if (level.getCapability(NTCapabilities.BeamReceiver.BLOCK, pos, side) != null) {
                return true;
            }
        }
        return false;
    }

    public static InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Component name = level.getBlockState(pos).getBlock().getName();
        GlobalPos start = start(stack);

        if (start == null) {
            if (!canSend(blockEntity)) {
                clear(stack);
                tell(player, blockEntity instanceof LaserBlockEntity
                        ? Component.translatable("nautec.wrench.link.not_source", name).withStyle(ChatFormatting.GOLD)
                        : Component.translatable("nautec.wrench.link.cleared").withStyle(ChatFormatting.GRAY));
                return InteractionResult.SUCCESS;
            }
            stack.set(NTDataComponents.WRENCH_LINK.get(), GlobalPos.of(level.dimension(), pos.immutable()));
            tell(player, Component.translatable("nautec.wrench.link.start", name, describe(pos)).withStyle(ChatFormatting.AQUA));
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7f, 1.2f);
            return InteractionResult.SUCCESS;
        }

        if (!start.dimension().equals(level.dimension()) || start.pos().equals(pos) || !canReceive(level, pos, blockEntity)) {
            clear(stack);
            tell(player, Component.translatable("nautec.wrench.link.cleared").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }
        if (!level.isLoaded(start.pos()) || !canSend(level.getBlockEntity(start.pos()))) {
            clear(stack);
            tell(player, Component.translatable("nautec.wrench.link.start_gone").withStyle(ChatFormatting.GOLD));
            return InteractionResult.SUCCESS;
        }

        if (player != null && (!level.mayInteract(player, start.pos()) || !level.mayInteract(player, pos))) {
            return InteractionResult.FAIL;
        }
        Component sourceName = level.getBlockState(start.pos()).getBlock().getName();
        Result result = link(level, start.pos(), pos);
        switch (result) {
            case LINKED_CUT_SOURCE, LINKED_CUT_TARGET -> {
                clear(stack);
                tell(player, Component.translatable(result == Result.LINKED_CUT_SOURCE ? "nautec.wrench.link.cut_input" : "nautec.wrench.link.cut_output",
                        result == Result.LINKED_CUT_SOURCE ? sourceName : name).withStyle(ChatFormatting.GOLD));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
            case LINKED, LINKED_BLOCKED -> {
                clear(stack);
                BlockPos blocker = blocker(level, start.pos(), pos);
                tell(player, blocker == null
                        ? Component.translatable("nautec.wrench.link.linked", sourceName, name, distance(start.pos(), pos)).withStyle(ChatFormatting.AQUA)
                        : Component.translatable("nautec.wrench.link.blocked", level.getBlockState(blocker).getBlock().getName(), describe(blocker))
                                .withStyle(ChatFormatting.GOLD));
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8f, 1.2f);
            }
            case NOT_IN_LINE -> tell(player, Component.translatable("nautec.wrench.link.not_in_line", name).withStyle(ChatFormatting.GOLD));
            case TOO_FAR -> tell(player, Component.translatable("nautec.wrench.link.too_far", distance(start.pos(), pos),
                    ((LaserBlockEntity) level.getBlockEntity(start.pos())).getMaxLaserDistance()).withStyle(ChatFormatting.GOLD));
            case CANNOT_SEND -> tell(player, Component.translatable("nautec.wrench.link.cannot_send", sourceName, name).withStyle(ChatFormatting.GOLD));
            case CANNOT_RECEIVE -> tell(player, Component.translatable("nautec.wrench.link.cannot_receive", name, sourceName).withStyle(ChatFormatting.GOLD));
        }
        return InteractionResult.SUCCESS;
    }

    public static Result link(Level level, BlockPos from, BlockPos to) {
        Direction toward = toward(from, to);
        if (toward == null) {
            return Result.NOT_IN_LINE;
        }
        if (!(level.getBlockEntity(from) instanceof LaserBlockEntity source)) {
            return Result.CANNOT_SEND;
        }
        if (distance(from, to) > source.getMaxLaserDistance()) {
            return Result.TOO_FAR;
        }
        BlockEntity targetEntity = level.getBlockEntity(to);
        Direction back = toward.getOpposite();

        BlockState sourceState = source instanceof LaserJunctionBlockEntity ? source.getBlockState() : aim(source, toward, true);
        if (sourceState == null) {
            return Result.CANNOT_SEND;
        }
        BlockState targetState;
        if (targetEntity instanceof LaserJunctionBlockEntity junction) {
            targetState = junction.getBlockState();
        } else if (targetEntity instanceof LaserBlockEntity target) {
            targetState = aim(target, back, false);
        } else {
            targetState = level.getCapability(NTCapabilities.BeamReceiver.BLOCK, to, back) != null ? level.getBlockState(to) : null;
        }
        if (targetState == null) {
            return Result.CANNOT_RECEIVE;
        }

        boolean sourceFed = source.getPower() > 0;
        Set<Direction> sourceInputs = Set.copyOf(source.getLaserInputs());
        boolean targetSending = targetEntity instanceof LaserBlockEntity sending && sending.apSent() > 0;
        Set<Direction> targetOutputs = targetEntity instanceof LaserBlockEntity sending ? Set.copyOf(sending.getLaserOutputs()) : Set.of();

        if (source instanceof LaserJunctionBlockEntity junction) {
            if (junction.getBlockState().getValue(LaserJunctionBlock.CONNECTION[toward.get3DDataValue()]) != LaserJunctionBlock.ConnectionType.OUTPUT) {
                junction.setConnection(toward, LaserJunctionBlock.ConnectionType.OUTPUT);
            }
        } else if (sourceState != level.getBlockState(from)) {
            level.setBlock(from, sourceState, Block.UPDATE_ALL);
        }
        if (targetEntity instanceof LaserJunctionBlockEntity junction) {
            if (junction.getBlockState().getValue(LaserJunctionBlock.CONNECTION[back.get3DDataValue()]) != LaserJunctionBlock.ConnectionType.INPUT) {
                junction.setConnection(back, LaserJunctionBlock.ConnectionType.INPUT);
            }
        } else if (targetState != level.getBlockState(to)) {
            level.setBlock(to, targetState, Block.UPDATE_ALL);
        }
        if (sourceFed && !source.getLaserInputs().containsAll(sourceInputs)) {
            return Result.LINKED_CUT_SOURCE;
        }
        if (targetSending && level.getBlockEntity(to) instanceof LaserBlockEntity target && !target.getLaserOutputs().containsAll(targetOutputs)) {
            return Result.LINKED_CUT_TARGET;
        }
        return blocker(level, from, to) == null ? Result.LINKED : Result.LINKED_BLOCKED;
    }

    private static @Nullable BlockState aim(LaserBlockEntity laser, Direction face, boolean output) {
        BlockState original = laser.getBlockState();
        if (faces(laser, output).contains(face)) {
            return original;
        }
        if (laser instanceof MultiblockPartEntity || original.hasProperty(Multiblock.FORMED)) {
            return null;
        }
        EnumProperty<Direction> facing = facingProperty(original);
        if (facing == null) {
            return null;
        }
        try {
            for (Direction value : facing.getPossibleValues()) {
                BlockState candidate = original.setValue(facing, value);
                laser.setBlockState(candidate);
                if (faces(laser, output).contains(face)) {
                    return candidate;
                }
            }
        } finally {
            laser.setBlockState(original);
        }
        return null;
    }

    private static Set<Direction> faces(LaserBlockEntity laser, boolean output) {
        return output ? laser.getPotentialLaserOutputs() : laser.getLaserInputs();
    }

    @SuppressWarnings("unchecked")
    private static @Nullable EnumProperty<Direction> facingProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof EnumProperty<?> enumProperty && enumProperty.getValueClass() == Direction.class && property.getName().equals("facing")) {
                return (EnumProperty<Direction>) enumProperty;
            }
        }
        return null;
    }

    public static @Nullable Direction toward(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int axes = (dx != 0 ? 1 : 0) + (dy != 0 ? 1 : 0) + (dz != 0 ? 1 : 0);
        if (axes != 1) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            if (direction.getStepX() == Integer.signum(dx) && direction.getStepY() == Integer.signum(dy) && direction.getStepZ() == Integer.signum(dz)) {
                return direction;
            }
        }
        return null;
    }

    public static int distance(BlockPos from, BlockPos to) {
        return from.distManhattan(to);
    }

    private static @Nullable BlockPos blocker(Level level, BlockPos from, BlockPos to) {
        Direction toward = toward(from, to);
        if (toward == null) {
            return null;
        }
        Vec3 start = Vec3.atCenterOf(from);
        Vec3 end = Vec3.atCenterOf(to);
        for (int i = 1; i < distance(from, to); i++) {
            BlockPos between = from.relative(toward, i);
            if (LaserBlockEntity.stopsBeam(level, between, level.getBlockState(between), start, end)) {
                return between;
            }
        }
        return null;
    }

    private static void tell(@Nullable Player player, Component message) {
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }
}
