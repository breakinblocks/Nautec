package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AquaticCatalystComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof AquaticCatalystBlockEntity blockEntity) {
            if (blockEntity.isActive()) {
                iTooltip.add(Component.translatable("nautec.jade.status.active"));
               if (blockEntity.getCurrentRecipe() != null) {
                    iTooltip.add(Component.translatable("nautec.jade.processing",
                            blockEntity.getItemStackHandler().getStackInSlot(0).getCount(),
                            blockEntity.getProcessingItem().getHoverName()));
                    iTooltip.add(Component.translatable("nautec.jade.remaining_duration", blockEntity.getRemainingDuration()));
                    iTooltip.add(Component.translatable("nautec.jade.transferring", blockEntity.getPowerToTransfer()));
                }
            } else {
                iTooltip.add(Component.translatable("nautec.jade.status.inactive"));
            }
        }
    }


    @Override
    public Identifier getUid() {
        return Nautec.rl("aquatic_catalyst");
    }
}
