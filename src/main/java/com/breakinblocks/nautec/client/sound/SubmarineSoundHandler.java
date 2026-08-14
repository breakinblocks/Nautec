package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineSoundHandler {
    private static final float LOW_POWER_FRACTION = 0.1F;
    private static final int BEEP_GAP = 5;

    private static final Map<Integer, HullState> TRACKED = new HashMap<>();

    private SubmarineSoundHandler() {
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() || !(event.getEntity() instanceof SubmarineEntity submarine)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(new SubmarineEngineSound(submarine));
        minecraft.getSoundManager().play(new SubmarineAmbientSound(submarine));
        TRACKED.put(submarine.getId(), new HullState(submarine));
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            TRACKED.remove(event.getEntity().getId());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            TRACKED.clear();
            return;
        }

        TRACKED.values().removeIf(state -> state.submarine.isRemoved());
        for (HullState state : TRACKED.values()) {
            state.tick();
        }
    }

    public static void play(SubmarineEntity submarine, SoundEvent sound, float volume, float pitch) {
        play(submarine.position(), sound, volume, pitch);
    }

    public static void play(Vec3 at, SoundEvent sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(
                new SimpleSoundInstance(sound, SoundSource.PLAYERS, volume, pitch,
                        Minecraft.getInstance().level == null
                                ? RandomSource.create()
                                : Minecraft.getInstance().level.getRandom(),
                        at.x, at.y, at.z));
    }

    private static final class HullState {
        private final SubmarineEntity submarine;
        private boolean deployed;
        private boolean laserActive;
        private boolean lowPower;
        private float health;
        private int beepIn = -1;

        private HullState(SubmarineEntity submarine) {
            this.submarine = submarine;
            this.deployed = submarine.isDeployed();
            this.laserActive = submarine.isLaserActive();
            this.health = submarine.getHealth();
            this.lowPower = powerFraction() < LOW_POWER_FRACTION;
        }

        private float powerFraction() {
            int capacity = this.submarine.getPowerStorage().getPowerCapacity();
            return capacity <= 0 ? 0F : (float) this.submarine.getPowerStored() / capacity;
        }

        private void tick() {
            boolean deployedNow = this.submarine.isDeployed();
            if (deployedNow != this.deployed) {
                play(this.submarine, deployedNow ? NTSounds.SUBMARINE_DEPLOY.get() : NTSounds.SUBMARINE_STOW.get(), 0.8F, 1F);
                this.deployed = deployedNow;
            }

            boolean laserNow = this.submarine.isLaserActive();
            if (laserNow && !this.laserActive) {
                Minecraft.getInstance().getSoundManager().play(new SubmarineLaserSound(this.submarine));
            }
            this.laserActive = laserNow;

            if (laserNow) {
                spawnLaserSparks();
            }

            float healthNow = this.submarine.getHealth();
            if (healthNow < this.health - 0.01F) {
                play(this.submarine, NTSounds.SUBMARINE_HULL_DAMAGE.get(), 1F, 0.9F + this.submarine.getRandom().nextFloat() * 0.2F);
            }
            this.health = healthNow;

            boolean lowNow = powerFraction() < LOW_POWER_FRACTION;
            boolean aboard = Minecraft.getInstance().player != null
                    && Minecraft.getInstance().player.getVehicle() == this.submarine;
            if (lowNow && !this.lowPower && aboard) {
                play(this.submarine, NTSounds.SUBMARINE_LOW_POWER.get(), 0.8F, 1F);
                this.beepIn = BEEP_GAP;
            }
            this.lowPower = lowNow;

            if (this.beepIn > 0 && --this.beepIn == 0) {
                play(this.submarine, NTSounds.SUBMARINE_LOW_POWER.get(), 0.8F, 1F);
                this.beepIn = -1;
            }
        }

        private void spawnLaserSparks() {
            if (this.submarine.getRandom().nextInt(2) != 0) {
                return;
            }

            Vec3 forward = this.submarine.getForward();
            Vec3 right = new Vec3(0D, 1D, 0D).cross(forward).normalize();
            for (int beam = 0; beam < 2; beam++) {
                float length = this.submarine.getLaserLength(beam == 0);
                if (length <= 0F) {
                    continue;
                }

                Vec3 muzzle = this.submarine.position()
                        .add(forward.scale(2D))
                        .add(right.scale(beam == 0 ? -1D : 1D))
                        .add(0D, 0.3D, 0D);
                Vec3 impact = muzzle.add(forward.scale(length));
                this.submarine.level().addParticle(NTParticles.LASER_SPARK.get(), impact.x, impact.y, impact.z,
                        -forward.x * 0.08D, 0.02D, -forward.z * 0.08D);
            }
        }
    }
}
