package com.breakinblocks.nautec.content.items.blocks;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class CrystalCradleItem extends BlockItem {
    public CrystalCradleItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        if (Boolean.TRUE.equals(stack.get(NTDataComponents.CRADLE_SEEDED))) {
            long growth = stack.getOrDefault(NTDataComponents.CRADLE_GROWTH, 0L);
            long target = Math.max(1, NTConfig.crystalGrowthPower);
            NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
            tooltipComponents.accept(Component.translatable("nautec.crystal_cradle.tooltip.seeded",
                    String.format(Locale.ROOT, "%.1f", growth * 100.0 / target), format.format(growth), format.format(target))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltipComponents.accept(Component.translatable("nautec.crystal_cradle.tooltip.empty").withStyle(ChatFormatting.GRAY));
        }
    }
}
