package com.breakinblocks.nautec.content.blocks;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class PressureForgeBlock extends LaserBlock {
    public PressureForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PressureForgeBlock::new);
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.PRESSURE_FORGE.get();
    }

    @Override
    public List<Component> displayText(Level level, BlockPos blockPos, Player player) {
        if (!(level.getBlockEntity(blockPos) instanceof PressureForgeBlockEntity forge)) {
            return List.of();
        }

        if (!forge.isPressurised()) {
            return List.of(Component.translatable("nautec.monocle.not_pressurised", NTConfig.pressureForgeDepth, NTConfig.pressureForgeWaterColumn)
                    .withStyle(ChatFormatting.RED));
        }

        return List.of(
                Component.translatable("nautec.monocle.pressurised").withStyle(ChatFormatting.AQUA),
                Component.translatable("nautec.monocle.acid", forge.getFluidTank().getFluidAmount()).withStyle(ChatFormatting.WHITE),
                Component.translatable("nautec.monocle.purity", String.format("%.2f", forge.getPurity())).withStyle(ChatFormatting.WHITE)
        );
    }
}
