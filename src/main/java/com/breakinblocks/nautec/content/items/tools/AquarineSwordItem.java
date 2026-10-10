package com.breakinblocks.nautec.content.items.tools;


import java.util.List;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.items.tiers.NTToolMaterials;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.utils.ItemUtils;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.sounds.SoundEvents;

public class AquarineSwordItem extends SwordItem implements IPowerItem {
    public AquarineSwordItem(Properties properties) {
        super(NTToolMaterials.AQUARINE, properties
                .attributes(SwordItem.createAttributes(NTToolMaterials.AQUARINE, 3, -2.4f))
                .component(NTDataComponents.IS_INFUSED,false)
                .component(NTDataComponents.ABILITY_ENABLED,false)
                .component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(1200)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if(powerStorage.getPowerStored() <= 0) {
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        IPowerStorage powerStorage = miningEntity.getItemInHand(InteractionHand.MAIN_HAND).getCapability(NTCapabilities.PowerStorage.ITEM);
        powerStorage.tryDrainPower(1, false);
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    public static final AttributeModifier ENABLED_DAMAGE = new AttributeModifier(Nautec.rl("damage"),0.7,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final AttributeModifier DISABLED_DAMAGE = new AttributeModifier(Nautec.rl("damage"),0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }
        AttributeModifier modifier = DISABLED_DAMAGE;
        if (NTDataComponentsUtils.isAbilityEnabled(stack)) {
            IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
            if (powerStorage.getPowerStored() > 0) {
                modifier = ENABLED_DAMAGE;
            }
        }
        ItemAttributeModifiers current = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers updated = current.withModifierAdded(Attributes.ATTACK_DAMAGE, modifier, EquipmentSlotGroup.MAINHAND);
        if (!updated.equals(current)) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, updated);
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        IPowerStorage powerStorage = attacker.getItemInHand(InteractionHand.MAIN_HAND).getCapability(NTCapabilities.PowerStorage.ITEM);
        if(NTDataComponentsUtils.isAbilityEnabled(stack)){
            powerStorage.tryDrainPower(10, false);
            if (!target.level().isClientSide()) {
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(target.level());
                if (lightningBolt != null) {
                    lightningBolt.setPos(target.getX(), target.getY(), target.getZ());
                    target.level().addFreshEntity(lightningBolt);
                    target.level().playSound(null, target.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 0.3F, 1.0F);
                }
            }
        }else{
            powerStorage.tryDrainPower(1, false);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return NTDataComponentsUtils.isAbilityEnabled(stack) || stack.isEnchanted();
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
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
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        Tooltips.trans(tooltipComponents, "nautec.tool.sword.ability", ChatFormatting.DARK_PURPLE);
        if(!NTDataComponentsUtils.isInfused(stack)){
            Tooltips.trans(tooltipComponents, "nautec.tool.infuse-me", ChatFormatting.DARK_GREEN);
        }else{
            Tooltips.transtrans(tooltipComponents, "nautec.tool.status", NTDataComponentsUtils.isAbilityEnabled(stack) ? "nautec.tool.enabled" : "nautec.tool.disabled", NTDataComponentsUtils.isAbilityEnabled(stack) ? ChatFormatting.GREEN : ChatFormatting.RED);
        }
        Tooltips.transInsert(tooltipComponents, "nautec.tool.power", powerStorage.getPowerStored() + "/" + powerStorage.getPowerCapacity(), ChatFormatting.DARK_AQUA);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
