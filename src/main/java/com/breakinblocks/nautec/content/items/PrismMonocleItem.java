package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.api.items.ICurioItem;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.content.items.tiers.NTArmorMaterials;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.utils.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import top.theillusivec4.curios.api.SlotContext;

public class PrismMonocleItem extends Item implements IPowerItem, ICurioItem {
    public PrismMonocleItem(Properties properties) {
        super(properties
                .attributes(NTArmorMaterials.PRISMARINE.createAttributes(ArmorType.HELMET))
                .enchantable(NTArmorMaterials.PRISMARINE.enchantmentValue())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(NTArmorMaterials.PRISMARINE.equipSound())
                        .setAsset(NTArmorMaterials.PRISMARINE.assetId())
                        .build())
                .repairable(NTArmorMaterials.PRISMARINE.repairIngredient())
                .component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(100)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemUtils.POWER_BAR_COLOR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ItemUtils.powerForDurabilityBar(stack);
    }

    @Override
    public int getMaxInput() {
        return ItemUtils.ITEM_POWER_INPUT;
    }

    @Override
    public int getMaxOutput() {
        return 0;
    }

    @Override
    public void curioTick(ItemStack itemStack, SlotContext slotContext) {
    }
}
