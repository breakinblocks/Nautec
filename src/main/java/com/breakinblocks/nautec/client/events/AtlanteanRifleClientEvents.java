package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.AtlanteanRifleBeamRenderer;
import com.breakinblocks.nautec.client.sound.AtlanteanRifleChargeSound;
import com.breakinblocks.nautec.client.sound.AtlanteanRifleFireSound;
import com.breakinblocks.nautec.content.items.AtlanteanRifleBeam;
import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class AtlanteanRifleClientEvents {
    private static final Map<Integer, AtlanteanRifleChargeSound> CHARGE_SOUNDS = new HashMap<>();
    private static final Map<Integer, AtlanteanRifleFireSound> FIRE_SOUNDS = new HashMap<>();

    private static final int IMPACT_SPARKS_COLD = 2;
    private static final int IMPACT_SPARKS_HOT = 6;
    private static final double IMPACT_SPREAD = 0.12D;
    private static final double IMPACT_LIFT = 0.04D;

    private static final float MUZZLE_SPARK_CHANCE = 0.6F;
    private static final int MUZZLE_SPARKS_MAX = 3;
    private static final double MUZZLE_SPREAD = 0.22D;
    private static final float CRACKLE_CHANCE = 0.14F;

    private static final int MUZZLE_FLOW_COLD = 2;
    private static final int MUZZLE_FLOW_HOT = 4;
    private static final double MUZZLE_FLOW_SPEED = 0.45D;
    private static final double MUZZLE_FLOW_SPREAD = 0.06D;
    private static final float MUZZLE_POP_CHANCE = 0.35F;

    private AtlanteanRifleClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            CHARGE_SOUNDS.clear();
            FIRE_SOUNDS.clear();
            AtlanteanRifleBeamRenderer.forget();
            return;
        }

        CHARGE_SOUNDS.values().removeIf(AbstractTickableSoundInstance::isStopped);
        FIRE_SOUNDS.values().removeIf(AbstractTickableSoundInstance::isStopped);

        for (Player player : level.players()) {
            if (!AtlanteanRifleItem.isUsing(player)) {
                continue;
            }

            if (player.getTicksUsingItem() < AtlanteanRifleChargeSound.DURATION_TICKS) {
                CHARGE_SOUNDS.computeIfAbsent(player.getId(), id -> play(minecraft, new AtlanteanRifleChargeSound(player)));
            } else {
                FIRE_SOUNDS.computeIfAbsent(player.getId(), id -> play(minecraft, new AtlanteanRifleFireSound(player)));
            }

            if (AtlanteanRifleItem.isFiring(player)) {
                float ramp = AtlanteanRifleItem.rampProgress(AtlanteanRifleItem.firingTicks(player, 1.0F));
                spawnImpact(level, player, ramp);
                streamMuzzle(minecraft, level, player, ramp);
                shedSparks(minecraft, level, player, ramp);
            }
        }
    }

    private static <S extends AbstractTickableSoundInstance> S play(Minecraft minecraft, S sound) {
        minecraft.getSoundManager().play(sound);
        return sound;
    }

    private static void spawnImpact(ClientLevel level, Player player, float ramp) {
        double range = NTConfig.rifleRange;
        AtlanteanRifleBeam.Hit hit = AtlanteanRifleBeam.trace(level, player, range, 1.0F);
        if (hit.entity() == null && hit.length() >= range - 0.01D) {
            return;
        }

        RandomSource random = level.getRandom();
        Vec3 end = hit.end();
        int sparks = Math.round(Mth.lerp(ramp, IMPACT_SPARKS_COLD, IMPACT_SPARKS_HOT));
        for (int i = 0; i < sparks; i++) {
            level.addParticle(NTParticles.LASER_SPARK.get(), end.x, end.y, end.z,
                    (random.nextDouble() - 0.5D) * IMPACT_SPREAD * (1D + ramp),
                    IMPACT_LIFT + random.nextDouble() * IMPACT_LIFT * (1D + ramp),
                    (random.nextDouble() - 0.5D) * IMPACT_SPREAD * (1D + ramp));
        }
        if (random.nextFloat() < ramp) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z,
                    (random.nextDouble() - 0.5D) * IMPACT_SPREAD, random.nextDouble() * IMPACT_LIFT, (random.nextDouble() - 0.5D) * IMPACT_SPREAD);
        }
    }

    private static void streamMuzzle(Minecraft minecraft, ClientLevel level, Player player, float ramp) {
        RandomSource random = level.getRandom();
        Vec3 muzzle = AtlanteanRifleBeamRenderer.muzzleWorld(player);
        Vec3 look = player.getLookAngle();
        int count = Math.round(Mth.lerp(ramp, MUZZLE_FLOW_COLD, MUZZLE_FLOW_HOT));
        for (int i = 0; i < count; i++) {
            Vec3 velocity = look.scale(MUZZLE_FLOW_SPEED * (0.6D + random.nextDouble() * 0.8D))
                    .add((random.nextDouble() - 0.5D) * MUZZLE_FLOW_SPREAD,
                            (random.nextDouble() - 0.5D) * MUZZLE_FLOW_SPREAD,
                            (random.nextDouble() - 0.5D) * MUZZLE_FLOW_SPREAD);
            level.addParticle(ParticleTypes.SCULK_SOUL, muzzle.x, muzzle.y, muzzle.z, velocity.x, velocity.y, velocity.z);
        }
        if (random.nextFloat() < MUZZLE_POP_CHANCE) {
            Vec3 velocity = look.scale(MUZZLE_FLOW_SPEED * 0.5D);
            level.addParticle(ParticleTypes.SCULK_CHARGE_POP, muzzle.x, muzzle.y, muzzle.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private static void shedSparks(Minecraft minecraft, ClientLevel level, Player player, float ramp) {
        RandomSource random = level.getRandom();
        if (ramp <= 0F || random.nextFloat() >= ramp * MUZZLE_SPARK_CHANCE) {
            return;
        }

        Vec3 muzzle = AtlanteanRifleBeamRenderer.muzzleWorld(player);
        int sparks = 1 + random.nextInt(Math.max(1, Math.round(ramp * MUZZLE_SPARKS_MAX)));
        for (int i = 0; i < sparks; i++) {
            level.addParticle(random.nextBoolean() ? NTParticles.LASER_SPARK.get() : ParticleTypes.ELECTRIC_SPARK,
                    muzzle.x, muzzle.y, muzzle.z,
                    (random.nextDouble() - 0.5D) * MUZZLE_SPREAD,
                    random.nextDouble() * MUZZLE_SPREAD * 0.5D,
                    (random.nextDouble() - 0.5D) * MUZZLE_SPREAD);
        }

        if (random.nextFloat() < ramp * CRACKLE_CHANCE) {
            level.playLocalSound(muzzle.x, muzzle.y, muzzle.z, NTSounds.ATLANTEAN_RIFLE_SPARK.get(), SoundSource.PLAYERS,
                    0.5F + 0.5F * ramp, 0.9F + random.nextFloat() * 0.4F, false);
        }
    }
}
