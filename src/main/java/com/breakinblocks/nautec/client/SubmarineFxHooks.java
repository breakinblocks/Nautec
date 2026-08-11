package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.client.sonar.NautecSonarManager;
import com.breakinblocks.nautec.client.sound.SubmarineSoundHandler;
import com.breakinblocks.nautec.client.teleport.TeleportFxManager;
import com.breakinblocks.nautec.network.SonarPingPayload;
import com.breakinblocks.nautec.network.TeleportFxPayload;
import com.breakinblocks.nautec.registries.NTParticles;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class SubmarineFxHooks {
    private static final int PING_MOTES = 60;
    private static final int SWIRL_PARTICLES = 40;

    private SubmarineFxHooks() {
    }

    public static void onSonarPing(SonarPingPayload ping) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Vec3 center = new Vec3(ping.x(), ping.y(), ping.z());
        RandomSource random = level.getRandom();
        for (int i = 0; i < PING_MOTES; i++) {
            Vec3 offset = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).scale(4D);
            Vec3 at = center.add(offset);
            level.addParticle(NTParticles.SONAR_MOTE.get(), at.x, at.y, at.z, 0D, 0.01D, 0D);
        }

        SubmarineSoundHandler.play(center, NTSounds.SUBMARINE_SONAR_PING.get(), 1F, 1F);
        NautecSonarManager.begin(center, ping.range(), ping.highlightTicks());
    }

    public static void onTeleportFx(TeleportFxPayload fx) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Vec3 center = new Vec3(fx.x(), fx.y() + 1D, fx.z());
        switch (fx.stage()) {
            case TeleportFxPayload.STAGE_CHARGE -> {
                SubmarineSoundHandler.play(center, NTSounds.SUBMARINE_TELEPORT_CHARGE.get(), 1F, 1F);
                spawnSwirl(level, center);
                TeleportFxManager.beginCharge(fx.entityId(), portalAhead(fx), fx.yaw(), fx.ticks());
            }
            case TeleportFxPayload.STAGE_ARRIVE -> {
                SubmarineSoundHandler.play(center, NTSounds.SUBMARINE_TELEPORT_WHOOSH.get(), 1F, 1F);
                spawnSwirl(level, center);
                TeleportFxManager.beginArrival(portalAhead(fx), fx.yaw());
            }
            default -> TeleportFxManager.abort();
        }
    }

    private static Vec3 portalAhead(TeleportFxPayload fx) {
        float yaw = fx.yaw() * ((float) Math.PI / 180F);
        return new Vec3(fx.x() - Math.sin(yaw) * 6D, fx.y() + 1D, fx.z() + Math.cos(yaw) * 6D);
    }

    private static void spawnSwirl(ClientLevel level, Vec3 center) {
        for (int i = 0; i < SWIRL_PARTICLES; i++) {
            level.addParticle(NTParticles.TELEPORT_SWIRL.get(), center.x, center.y, center.z, 0D, 0D, 0D);
        }
    }
}
