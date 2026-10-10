package com.breakinblocks.nautec.content.items;



import net.minecraft.world.item.Item;
import java.util.List;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NeptunesTridentItem extends TridentItem {
    public static final float DAMAGE = 18.0F;
    public static final int BUILT_IN_LOYALTY = 3;
    private static final float ATTACK_SPEED = -2.8F;
    private static final int SPIN_ATTACK_TICKS = 20;
    private static final double RIPTIDE_GROUND_LIFT = 1.1999999F;

    public NeptunesTridentItem(Properties properties) {
        super(properties);
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

    private static boolean nextDamageWillBreak(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (nextDamageWillBreak(itemInHand)) {
            return InteractionResultHolder.fail(itemInHand);
        } else if (EnchantmentHelper.getTridentSpinAttackStrength(itemInHand, player) > 0.0F && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemInHand);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemInHand);
        }
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int remainingTime) {
        if (!(entity instanceof Player player) || this.getUseDuration(itemStack, entity) - remainingTime < THROW_THRESHOLD_TIME) {
            return;
        }
        float riptideStrength = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player);
        if (riptideStrength > 0.0F && (!player.isInWaterOrRain() || player.isPassenger())) {
            return;
        }
        if (nextDamageWillBreak(itemStack)) {
            return;
        }

        Holder<SoundEvent> sound = EnchantmentHelper.pickHighestLevel(itemStack, EnchantmentEffectComponents.TRIDENT_SOUND)
                .orElse(SoundEvents.TRIDENT_THROW);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (level instanceof ServerLevel serverLevel) {
            if (itemStack.isDamageableItem() && player instanceof ServerPlayer serverPlayer) {
                itemStack.hurtAndBreak(1, serverLevel, serverPlayer, item -> {
                });
            }
            if (riptideStrength == 0.0F) {
                ItemStack thrownItemStack = itemStack.consumeAndReturn(1, player);
                ThrownNeptunesTrident trident = new ThrownNeptunesTrident(serverLevel, player, thrownItemStack);
                trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, SHOOT_POWER, 1.0F);
                if (player.hasInfiniteMaterials()) {
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                serverLevel.addFreshEntity(trident);
                level.playSound(null, trident, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                return;
            }
        }
        if (riptideStrength <= 0.0F) {
            return;
        }

        player.push(Vec3.directionFromRotation(player.getXRot(), player.getYRot()).scale(riptideStrength));
        player.startAutoSpinAttack(SPIN_ATTACK_TICKS, DAMAGE, itemStack);
        if (player.onGround()) {
            player.move(MoverType.SELF, new Vec3(0.0, RIPTIDE_GROUND_LIFT, 0.0));
        }
        level.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownNeptunesTrident trident = new ThrownNeptunesTrident(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
        trident.pickup = AbstractArrow.Pickup.ALLOWED;
        return trident;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.SWORD_SWEEP || super.canPerformAction(stack, itemAbility);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity owner, int slotId, boolean isSelected) {
        if (level.isClientSide()) {
            return;
        }
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        Tooltips.trans(tooltipComponents, "nautec.neptunes_trident.returns", ChatFormatting.GRAY);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
