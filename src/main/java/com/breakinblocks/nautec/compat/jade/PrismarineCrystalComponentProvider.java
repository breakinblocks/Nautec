package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PrismarineCrystalComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        PrismarineCrystalBlockEntity crystal = PrismarineCrystalBlock.findCrystal(accessor.getLevel(), accessor.getPosition());
        if (crystal == null) {
            return;
        }
        if (crystal.isCultivated()) {
            tooltip.add(Component.translatable("nautec.jade.crystal.cultivated").withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("nautec.jade.crystal.natural").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return Nautec.rl("prismarine_crystal");
    }
}
