package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class TeleportFxManager {
    public static final int OPEN_TICKS = 20;
    public static final int ARRIVE_TICKS = 20;

    private static int chargeTicks;
    private static int arriveTicks;
    private static Vec3 portalPos = Vec3.ZERO;
    private static float portalYaw;
    private static int trackedEntity = -1;

    private TeleportFxManager() {
    }

    public static void beginCharge(int entityId, Vec3 position, float yaw, int ticks) {
        trackedEntity = entityId;
        portalPos = position;
        portalYaw = yaw;
        chargeTicks = ticks;
        arriveTicks = 0;
    }

    public static void beginArrival(Vec3 position, float yaw) {
        portalPos = position;
        portalYaw = yaw;
        chargeTicks = 0;
        arriveTicks = ARRIVE_TICKS;
    }

    public static void abort() {
        chargeTicks = 0;
        arriveTicks = 0;
        trackedEntity = -1;
    }

    public static boolean isCharging() {
        return chargeTicks > 0;
    }

    public static boolean isVisible() {
        return chargeTicks > 0 || arriveTicks > 0;
    }

    public static int trackedEntity() {
        return trackedEntity;
    }

    public static Vec3 portalPos() {
        return portalPos;
    }

    public static float portalYaw() {
        return portalYaw;
    }

    public static float openProgress(float partialTick) {
        if (arriveTicks > 0) {
            return Mth.clamp((arriveTicks - partialTick) / ARRIVE_TICKS, 0F, 1F);
        }

        float elapsed = SubmarineModules.TELEPORT_CHARGE_TICKS - chargeTicks + partialTick;
        return Mth.clamp(elapsed / OPEN_TICKS, 0F, 1F);
    }

    public static float fadeStrength(float partialTick) {
        if (arriveTicks > 0) {
            return Mth.clamp((arriveTicks - partialTick) / ARRIVE_TICKS, 0F, 1F);
        }

        if (chargeTicks <= 0) {
            return 0F;
        }

        float remaining = chargeTicks - partialTick;
        return Mth.clamp((12F - remaining) / 12F, 0F, 1F);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            abort();
            return;
        }

        if (chargeTicks > 0) {
            chargeTicks--;
        } else if (arriveTicks > 0) {
            arriveTicks--;
        }
    }
}
