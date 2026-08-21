package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MixerComponentProvider implements IBlockComponentProvider {
    INSTANCE;


    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getBlockEntity() instanceof MixerBlockEntity blockEntity) {
            FluidStack inputFluid = blockEntity.getInputFluid();
            if (!inputFluid.isEmpty()) {
                iTooltip.add(Component.translatable("nautec.jade.fluid_input", inputFluid.getHoverName(), blockEntity.getInputFluidAmount()));
            }

            FluidStack outputFluid = blockEntity.getOutputFluid();
            if (!outputFluid.isEmpty()) {
                iTooltip.add(Component.translatable("nautec.jade.fluid_output", outputFluid.getHoverName(), blockEntity.getOutputFluidAmount()));
            }

            int duration = blockEntity.getDuration();
            int maxDuration = blockEntity.getMaxDuration();
            if (duration > 0 && maxDuration > 0) {
                iTooltip.add(Component.translatable("nautec.jade.mixing_progress", duration, maxDuration));
            }

            iTooltip.add(Component.translatable("nautec.jade.energy", blockEntity.getPower()));
        }
    }

    @Override
    public Identifier getUid() {
        return Nautec.rl("mixer");
    }
}
