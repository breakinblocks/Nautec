package com.breakinblocks.nautec.content.items.submarine;

import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SubmarineModuleItem extends Item {
    private final SubmarineModuleType moduleType;

    public SubmarineModuleItem(SubmarineModuleType moduleType, Properties properties) {
        super(properties.stacksTo(1));
        this.moduleType = moduleType;
    }

    public SubmarineModuleType getModuleType() {
        return this.moduleType;
    }

    public static @Nullable SubmarineModuleType typeOf(ItemStack stack) {
        return stack.getItem() instanceof SubmarineModuleItem module ? module.getModuleType() : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Tooltips.trans(tooltipComponents, this.moduleType.translationKey() + ".desc", ChatFormatting.GRAY);

        if (this.moduleType.isPassive()) {
            Tooltips.trans(tooltipComponents, "nautec.submarine.module.passive", ChatFormatting.DARK_AQUA);
            return;
        }

        Tooltips.tt(tooltipComponents, Component.translatable("nautec.submarine.module.cost",
                formatPower(this.moduleType.powerCost())), ChatFormatting.DARK_AQUA);

        int cooldown = this.moduleType.cooldownTicks();
        if (cooldown > 0) {
            Tooltips.tt(tooltipComponents, Component.translatable("nautec.submarine.module.cooldown",
                    String.format("%.0f", cooldown / 20F)), ChatFormatting.DARK_AQUA);
        }
    }

    protected static String formatPower(int power) {
        return String.format("%,d", power);
    }
}
