package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.content.entities.ThrownNeptunesTrident;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ShockwaveCooldown;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class NeptunesTridentItem extends TridentItem {
    public static final float DAMAGE = 18.0F;
    public static final int BUILT_IN_LOYALTY = 3;
    private static final float ATTACK_SPEED = -2.8F;
    private static final int SPIN_ATTACK_TICKS = 20;
    private static final double RIPTIDE_GROUND_LIFT = 1.1999999F;

    public NeptunesTridentItem(Properties properties) {
        super(properties.enchantable(1));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, DAMAGE, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player) || this.getUseDuration(itemStack, entity) - remainingTime < THROW_THRESHOLD_TIME) {
            return false;
        }
        float riptideStrength = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
        if (riptideStrength > 0.0F && (!player.isInWaterOrRain() || player.isPassenger())) {
            return false;
        }
        if (itemStack.nextDamageWillBreak()) {
            return false;
        }

        Holder<SoundEvent> sound = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                .orElse(SoundEvents.TRIDENT_THROW);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (level instanceof ServerLevel serverLevel) {
            itemStack.hurtWithoutBreaking(1, player);
            if (riptideStrength == 0.0F) {
                ItemStack thrownItemStack = itemStack.consumeAndReturn(1, player);
                ThrownNeptunesTrident trident = Projectile.spawnProjectileFromRotation(
                        ThrownNeptunesTrident::new, serverLevel, thrownItemStack, player, 0.0F, PROJECTILE_SHOOT_POWER, 1.0F);
                if (player.hasInfiniteMaterials()) {
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.playSound(null, trident, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                return true;
            }
        }
        if (riptideStrength <= 0.0F) {
            return false;
        }

        player.push(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(riptideStrength));
        player.startAutoSpinAttack(SPIN_ATTACK_TICKS, DAMAGE, itemStack);
        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0, RIPTIDE_GROUND_LIFT, 0.0));
        }
        level.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownNeptunesTrident trident = new ThrownNeptunesTrident(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        trident.pickup = AbstractArrow.Pickup.ALLOWED;
        return trident;
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.SWORD_SWEEP || super.canPerformAction(stack, itemAbility);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        ShockwaveCooldown cooldown = itemStack.get(NTDataComponents.SHOCKWAVE_COOLDOWN.get());
        if (cooldown != null && cooldown.isReady(level.getGameTime())) {
            itemStack.remove(NTDataComponents.SHOCKWAVE_COOLDOWN.get());
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.LOYALTY) && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Tooltips.trans(tooltipComponents, "nautec.neptunes_trident.returns", ChatFormatting.GRAY);
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
