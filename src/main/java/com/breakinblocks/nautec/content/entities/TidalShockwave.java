package com.breakinblocks.nautec.content.entities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ShockwaveCooldown;
import com.breakinblocks.nautec.network.TidalShockwavePayload;
import com.breakinblocks.nautec.registries.NTDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class TidalShockwave {
    private static final float EDGE_STRENGTH = 0.5F;
    private static final double EFFECT_RANGE = 64.0;

    private TidalShockwave() {
    }

    public static boolean isReady(ItemStack weapon, long gameTime) {
        ShockwaveCooldown cooldown = weapon.get(NTDataComponents.SHOCKWAVE_COOLDOWN.get());
        return cooldown == null || cooldown.isReady(gameTime);
    }

    public static float damage(ServerLevel level, ItemStack weapon) {
        return (float) (NTConfig.tridentShockwaveDamage
                + NTConfig.tridentShockwaveDamagePerSharpness * enchantmentLevel(level, weapon, Enchantments.SHARPNESS));
    }

    public static float knockback(ServerLevel level, ItemStack weapon) {
        return (float) (NTConfig.tridentShockwaveKnockback
                + NTConfig.tridentShockwaveKnockbackPerLevel * enchantmentLevel(level, weapon, Enchantments.KNOCKBACK));
    }

    public static float falloff(double distance, double radius) {
        return radius <= 0.0 ? 1.0F : 1.0F - (1.0F - EDGE_STRENGTH) * (float) Math.min(1.0, distance / radius);
    }

    public static void release(ServerLevel level, Entity source, @Nullable Entity owner, ItemStack weapon, Vec3 center) {
        double radius = NTConfig.tridentShockwaveRadius;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.snapTo(center.x, center.y, center.z);
            level.addFreshEntity(bolt);
        }

        DamageSource damageSource = level.damageSources().source(NTDamageTypes.TIDAL_SHOCKWAVE, source, owner);
        float damage = damage(level, weapon);
        float knockback = knockback(level, weapon);
        AABB area = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, target -> isAffected(target, owner))) {
            double distance = Math.sqrt(target.getBoundingBox().distanceToSqr(center));
            if (distance > radius) {
                continue;
            }
            float strength = falloff(distance, radius);
            target.hurtServer(level, damageSource, damage * strength);
            target.knockback(knockback * strength, center.x - target.getX(), center.z - target.getZ());
            target.hurtMarked = true;
        }

        if (NTConfig.tridentShockwaveCooldown > 0) {
            weapon.set(NTDataComponents.SHOCKWAVE_COOLDOWN.get(),
                    new ShockwaveCooldown(level.getGameTime() + NTConfig.tridentShockwaveCooldown, NTConfig.tridentShockwaveCooldown));
        }

        level.playSound(null, center.x, center.y, center.z, SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 2.0F, 0.6F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 2.0F, 0.5F);
        PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, EFFECT_RANGE,
                new TidalShockwavePayload(center.x, center.y, center.z, (float) radius));
    }

    private static boolean isAffected(LivingEntity target, @Nullable Entity owner) {
        if (!target.isAlive() || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            return false;
        }
        if (owner == null) {
            return true;
        }
        if (target == owner || owner.isAlliedTo(target) || target.isAlliedTo(owner)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable) || ownable.getOwner() != owner;
    }

    private static int enchantmentLevel(ServerLevel level, ItemStack weapon, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, weapon))
                .orElse(0);
    }
}
