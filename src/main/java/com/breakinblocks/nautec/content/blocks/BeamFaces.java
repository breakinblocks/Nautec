package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.network.OpenBeamFacesPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BeamFaces {
    public enum Mode {
        OFF,
        INPUT,
        OUTPUT
    }

    private BeamFaces() {
    }

    public static boolean configurable(BlockState state) {
        return state.getBlock() instanceof PrismarineLaserRelayBlock || state.getBlock() instanceof LaserJunctionBlock;
    }

    public static boolean isRelay(BlockState state) {
        return state.getBlock() instanceof PrismarineLaserRelayBlock;
    }

    public static Mode mode(BlockState state, Direction face) {
        if (state.getBlock() instanceof PrismarineLaserRelayBlock) {
            Direction facing = state.getValue(PrismarineLaserRelayBlock.FACING);
            return face == facing ? Mode.OUTPUT : face == facing.getOpposite() ? Mode.INPUT : Mode.OFF;
        }
        if (state.getBlock() instanceof LaserJunctionBlock) {
            return switch (state.getValue(LaserJunctionBlock.CONNECTION[face.get3DDataValue()])) {
                case INPUT -> Mode.INPUT;
                case OUTPUT -> Mode.OUTPUT;
                case NONE -> Mode.OFF;
            };
        }
        return Mode.OFF;
    }

    public static boolean apply(Level level, BlockPos pos, Direction face, boolean forward) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PrismarineLaserRelayBlock) {
            Direction facing = forward ? face : face.getOpposite();
            if (state.getValue(PrismarineLaserRelayBlock.FACING) == facing) {
                return false;
            }
            level.setBlock(pos, state.setValue(PrismarineLaserRelayBlock.FACING, facing), Block.UPDATE_ALL);
        } else if (state.getBlock() instanceof LaserJunctionBlock && level.getBlockEntity(pos) instanceof LaserJunctionBlockEntity junction) {
            LaserJunctionBlock.ConnectionType current = state.getValue(LaserJunctionBlock.CONNECTION[face.get3DDataValue()]);
            junction.setConnection(face, forward ? next(current) : previous(current));
        } else {
            return false;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.6F, 1.4F);
        return true;
    }

    public static LaserJunctionBlock.ConnectionType next(LaserJunctionBlock.ConnectionType type) {
        return switch (type) {
            case NONE -> LaserJunctionBlock.ConnectionType.INPUT;
            case INPUT -> LaserJunctionBlock.ConnectionType.OUTPUT;
            case OUTPUT -> LaserJunctionBlock.ConnectionType.NONE;
        };
    }

    public static LaserJunctionBlock.ConnectionType previous(LaserJunctionBlock.ConnectionType type) {
        return switch (type) {
            case NONE -> LaserJunctionBlock.ConnectionType.OUTPUT;
            case INPUT -> LaserJunctionBlock.ConnectionType.NONE;
            case OUTPUT -> LaserJunctionBlock.ConnectionType.INPUT;
        };
    }

    public static InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new OpenBeamFacesPayload(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
