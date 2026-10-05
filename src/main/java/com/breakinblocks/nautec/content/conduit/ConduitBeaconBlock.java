package com.breakinblocks.nautec.content.conduit;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class ConduitBeaconBlock extends LaserBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public ConduitBeaconBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ACTIVE));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE) || random.nextInt(3) != 0) {
            return;
        }
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 3;
        double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 3;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3;
        level.addParticle(ParticleTypes.NAUTILUS, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                x - pos.getX() - 0.5, y - pos.getY() - 0.5, z - pos.getZ() - 0.5);
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof ConduitBeaconBlockEntity beacon)) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(beacon.statusKey()).withStyle(beacon.isRunning() ? ChatFormatting.AQUA : ChatFormatting.RED));
        lines.add(Component.translatable("nautec.conduit_beacon.frame", beacon.getFrameSize(), beacon.effectRange())
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.conduit_beacon.buffer", beacon.getBuffer(), NTConfig.conduitBeaconBuffer)
                .withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.CONDUIT_BEACON.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ConduitBeaconBlock::new);
    }
}
