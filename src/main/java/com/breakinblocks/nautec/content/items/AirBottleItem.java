package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.fluid.DivingSuitAirHandler;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
                fillHelmetTank(player, SECONDS_PER_BOTTLE);
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

    public static void fillHelmetTank(Player player, int amount) {
        if (!NTConfig.airBottlesFillOxygenHelmets) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || helmet.is(NTItems.DIVING_HELMET)) {
            return;
        }
        ResourceHandler<FluidResource> tank = ItemAccess.forPlayerSlot(player, EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE))
                .getCapability(Capabilities.Fluid.ITEM);
        if (tank == null) {
            return;
        }
        for (FluidResource oxygen : DivingSuitAirHandler.oxygenFluids()) {
            try (Transaction tx = Transaction.openRoot()) {
                if (tank.insert(oxygen, amount, tx) > 0) {
                    tx.commit();
                    return;
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Tooltips.trans(tooltipComponents, "nautec.air_bottle.fill", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents,"nautec.air_bottle.craft_msg", ChatFormatting.GRAY);
        Tooltips.trans(tooltipComponents,"nautec.edible",ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }
}
