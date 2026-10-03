package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.OxygenDiffuserBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.List;

public class OxygenDiffuserBlock extends LaserBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public OxygenDiffuserBlock(Properties properties) {
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }
        boolean underwater = level.getFluidState(pos.above()).is(FluidTags.WATER);
        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            if (underwater) {
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, pos.getY() + 1.05, z, 0.0, 0.05, 0.0);
            } else {
                level.addParticle(ParticleTypes.CLOUD, x, pos.getY() + 1.05, z, 0.0, 0.02, 0.0);
            }
        }
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof OxygenDiffuserBlockEntity diffuser)) {
            return List.of();
        }
        int size = OxygenDiffuserBlockEntity.radius();
        return diffuser.isRunning()
                ? List.of(Component.translatable("nautec.oxygen_diffuser.running", size).withStyle(ChatFormatting.AQUA))
                : List.of(Component.translatable("nautec.oxygen_diffuser.no_power", NTConfig.oxygenDiffuserPower).withStyle(ChatFormatting.RED));
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.OXYGEN_DIFFUSER.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(OxygenDiffuserBlock::new);
    }
}
