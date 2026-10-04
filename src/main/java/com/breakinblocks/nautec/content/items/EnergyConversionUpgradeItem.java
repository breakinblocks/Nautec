package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class EnergyConversionUpgradeItem extends Item {
    public static final int MAX_PER_SLOT = 8;

    private final Tier tier;

    public EnergyConversionUpgradeItem(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.translatable("nautec.energy_conversion_upgrade.effect", tier.ap()).withStyle(ChatFormatting.AQUA));
        tooltipComponents.accept(Component.translatable("nautec.energy_conversion_upgrade.limit", MAX_PER_SLOT).withStyle(ChatFormatting.GRAY));
    }

    public enum Tier {
        BASIC,
        ADVANCED,
        ULTIMATE;

        public int ap() {
            return switch (this) {
                case BASIC -> NTConfig.energyConversionUpgradeAp;
                case ADVANCED -> NTConfig.advancedEnergyConversionUpgradeAp;
                case ULTIMATE -> NTConfig.ultimateEnergyConversionUpgradeAp;
            };
        }
    }
}
