package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.client.sonar.NautecSonarManager;
import com.breakinblocks.nautec.client.sound.SubmarineSoundHandler;
import com.breakinblocks.nautec.client.teleport.ArrivalWaves;
import com.breakinblocks.nautec.client.teleport.TeleportFxManager;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
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
        NautecSonarManager.begin(ping.entityId(), center, ping.range(), ping.hostileRange(), ping.highlightTicks());
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
                Vec3 portal = portalAhead(fx);
                if (level.getEntity(fx.entityId()) instanceof SubmarineEntity submarine) {
                    submarine.setPortalTarget(portal);
                }
                TeleportFxManager.beginCharge(fx.entityId(), portal, forward(fx), fx.ticks());
            }
            case TeleportFxPayload.STAGE_ARRIVE -> {
                SubmarineSoundHandler.play(center, NTSounds.SUBMARINE_TELEPORT_WHOOSH.get(), 1F, 1F);
                spawnSwirl(level, center);
                Vec3 exit = SubmarineEntity.exitPortalCenter(new Vec3(fx.x(), fx.y(), fx.z()), fx.yaw(), fx.pitch());
                TeleportFxManager.beginArrival(fx.entityId(), exit, forward(fx));
                ArrivalWaves.begin(exit);
            }
            case TeleportFxPayload.STAGE_ABORT -> {
                if (TeleportFxManager.trackedEntity() == fx.entityId()) TeleportFxManager.abort();
            }
        }
    }

    private static Vec3 portalAhead(TeleportFxPayload fx) {
        return SubmarineEntity.portalCenter(new Vec3(fx.x(), fx.y(), fx.z()), fx.yaw(), fx.pitch());
    }

    private static Vec3 forward(TeleportFxPayload fx) {
        return Vec3.directionFromRotation(fx.pitch(), fx.yaw());
    }

    private static void spawnSwirl(ClientLevel level, Vec3 center) {
        for (int i = 0; i < SWIRL_PARTICLES; i++) {
            level.addParticle(NTParticles.TELEPORT_SWIRL.get(), center.x, center.y, center.z, 0D, 0D, 0D);
        }
    }
}
