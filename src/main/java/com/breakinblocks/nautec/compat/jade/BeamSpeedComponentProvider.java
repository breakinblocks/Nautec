package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

public enum BeamSpeedComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof LaserBlockEntity laser) || laser.getRequiredPower() <= 0) {
            return;
        }
        float speed = laser.beamSpeed();
        if (speed <= 0F) {
            tooltip.add(Component.translatable("nautec.jade.beam_speed.low", laser.getPower(), laser.getRequiredPower()).withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.translatable("nautec.jade.beam_speed", String.format(Locale.ROOT, "%.2f", speed), laser.getPower(), laser.getRequiredPower())
                .withStyle(speed > 1.005F ? ChatFormatting.AQUA : ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid() {
        return Nautec.rl("beam_speed");
    }
}
