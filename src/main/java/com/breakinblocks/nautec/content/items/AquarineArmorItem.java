package com.breakinblocks.nautec.content.items;


import java.util.List;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.registries.NTArmorMaterials;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AquarineArmorItem extends ArmorItem implements IPowerItem {
    public AquarineArmorItem(ArmorItem.Type type, Properties properties) {
        super(NTArmorMaterials.AQUARINE_STEEL, type, properties
                .durability(100)
                .component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(512)));
    }

    private static final ResourceLocation LEGACY_ARMOR_ID = Nautec.rl("armor");
    private static final ResourceLocation LEGACY_TOUGHNESS_ID = Nautec.rl("toughness");
    private static final double ARMOR_BONUS = 10;
    private static final double TOUGHNESS_BONUS = 5;

    public static ResourceLocation armorModifierId(EquipmentSlot slot) {
        return Nautec.rl("armor_" + slot.getName());
    }

    public static ResourceLocation toughnessModifierId(EquipmentSlot slot) {
        return Nautec.rl("toughness_" + slot.getName());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }
        IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        boolean hasEnergy = powerStorage != null && powerStorage.getPowerStored() > 0;
        EquipmentSlot slotType = this.getType().getSlot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slotType);
        ItemAttributeModifiers current = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers base = current.modifiers().isEmpty() ? this.getDefaultAttributeModifiers() : current;
        ItemAttributeModifiers attributes = new ItemAttributeModifiers(base.modifiers().stream()
                .filter(entry -> !entry.modifier().id().equals(LEGACY_ARMOR_ID) && !entry.modifier().id().equals(LEGACY_TOUGHNESS_ID))
                .toList(), base.showInTooltip());
        attributes = attributes.withModifierAdded(Attributes.ARMOR,
                new AttributeModifier(armorModifierId(slotType), hasEnergy ? ARMOR_BONUS : 0, AttributeModifier.Operation.ADD_VALUE), group);
        attributes = attributes.withModifierAdded(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(toughnessModifierId(slotType), hasEnergy ? TOUGHNESS_BONUS : 0, AttributeModifier.Operation.ADD_VALUE), group);
        if (!attributes.equals(current)) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes);
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        if (stack.getItem() instanceof IPowerItem poweredTool) {
            IPowerStorage energyStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
            if (energyStorage == null) return amount;
            double reductionFactor = 0;
            if (entity != null) {
                HolderLookup.RegistryLookup<Enchantment> registrylookup = entity.level().getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                int unbreakingLevel = stack.getEnchantmentLevel(registrylookup.getOrThrow(Enchantments.UNBREAKING));
                reductionFactor = Math.min(4.0, unbreakingLevel * 0.1);
            }
            int finalEnergyCost = (int) Math.max(0, amount - (amount * reductionFactor));
            energyStorage.tryDrainPower(finalEnergyCost, false);
            return 0;
        }
        return amount;
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
        return 100;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        Tooltips.trans(tooltipComponents, "nautec.armor.ability.desc", ChatFormatting.DARK_PURPLE);
        Tooltips.transInsert(tooltipComponents, "nautec.armor.power", powerStorage.getPowerStored() + "/" + powerStorage.getPowerCapacity() , ChatFormatting.DARK_AQUA);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
