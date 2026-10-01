package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AirBottleItem extends Item {
    public static final int TANK_SECONDS = 600;
    public static final int SECONDS_PER_BOTTLE = TANK_SECONDS / 5;

    public AirBottleItem(Properties properties) {
        super(properties
                .food(new FoodProperties.Builder().alwaysEdible().build(), Consumables.HONEY_BOTTLE)
                .craftRemainder(Items.GLASS_BOTTLE));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide()) {
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 0));
            if (entity instanceof Player player) {
                player.addItem(new ItemStack(Items.GLASS_BOTTLE));
                refillTank(player.getItemBySlot(EquipmentSlot.CHEST));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    public static void refillTank(ItemStack chestplate) {
        if (!(chestplate.getItem() instanceof DivingSuitArmorItem)) {
            return;
        }
        int currentLevel = NTDataComponentsUtils.getOxygenLevels(chestplate);
        if (currentLevel < TANK_SECONDS) {
            NTDataComponentsUtils.setOxygenLevels(chestplate, Math.min(TANK_SECONDS, currentLevel + SECONDS_PER_BOTTLE));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Tooltips.trans(tooltipComponents, "nautec.air_bottle.fill", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents,"nautec.air_bottle.craft_msg", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents,"nautec.edible",ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
