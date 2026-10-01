package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.api.items.ICurioItem;
import com.breakinblocks.nautec.content.items.tiers.NTArmorMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import top.theillusivec4.curios.api.SlotContext;

public class PrismMonocleItem extends Item implements ICurioItem {
    public PrismMonocleItem(Properties properties) {
        super(properties
                .attributes(NTArmorMaterials.PRISMARINE.createAttributes(ArmorType.HELMET))
                .enchantable(NTArmorMaterials.PRISMARINE.enchantmentValue())
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                        .setEquipSound(NTArmorMaterials.PRISMARINE.equipSound())
                        .setAsset(NTArmorMaterials.PRISMARINE.assetId())
                        .build())
                .repairable(NTArmorMaterials.PRISMARINE.repairIngredient()));
    }

    @Override
    public void curioTick(ItemStack itemStack, SlotContext slotContext) {
    }
}
