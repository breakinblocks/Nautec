package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ReactorUpgradeItem extends Item {
    private final Type type;

    public ReactorUpgradeItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type getUpgradeType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        if (type == Type.FUSION) {
            tooltipComponents.accept(effect(type).copy().withStyle(ChatFormatting.GOLD));
            for (Type part : Type.BASIC) {
                tooltipComponents.accept(effect(part).copy().withStyle(ChatFormatting.AQUA));
            }
        } else {
            tooltipComponents.accept(effect(type).copy().withStyle(ChatFormatting.AQUA));
        }
        tooltipComponents.accept(Component.translatable("nautec.reactor_upgrade.power", format(type.powerMultiplier()))
                .withStyle(ChatFormatting.GRAY));
    }

    private static Component effect(Type type) {
        return switch (type) {
            case SPEED -> Component.translatable("nautec.reactor_upgrade.speed.effect", percent(NTConfig.reactorSpeedUpgradeBonus));
            case YIELD -> Component.translatable("nautec.reactor_upgrade.yield.effect", NTConfig.reactorYieldUpgradeBonus);
            case EFFICIENCY -> Component.translatable("nautec.reactor_upgrade.efficiency.effect",
                    percent(1 - NTConfig.reactorEfficiencyUpgradeFactor), percent(NTConfig.reactorEfficiencyUpgradeFloor));
            case FUSION -> Component.translatable("nautec.reactor_upgrade.fusion.effect");
        };
    }

    private static String percent(double fraction) {
        return String.valueOf(Math.round(fraction * 100));
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public enum Type {
        SPEED,
        YIELD,
        EFFICIENCY,
        FUSION;

        public static final List<Type> BASIC = List.of(SPEED, YIELD, EFFICIENCY);

        public double powerMultiplier() {
            return switch (this) {
                case SPEED -> NTConfig.reactorSpeedUpgradePowerMultiplier;
                case YIELD -> NTConfig.reactorYieldUpgradePowerMultiplier;
                case EFFICIENCY -> NTConfig.reactorEfficiencyUpgradePowerMultiplier;
                case FUSION -> NTConfig.reactorFusionUpgradePowerMultiplier;
            };
        }
    }
}
