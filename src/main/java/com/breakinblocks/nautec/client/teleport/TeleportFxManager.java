package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class TeleportFxManager {
    public static final int OPEN_TICKS = 20;
    public static final int ARRIVE_TICKS = 20;

    private static int chargeTicks;
    private static int arriveTicks;
    private static Vec3 portalPos = Vec3.ZERO;
    private static Vec3 portalFacing = new Vec3(0D, 0D, 1D);
    private static int trackedEntity = -1;
    private static @Nullable ClientLevel effectLevel;

    private TeleportFxManager() {
    }

    public static void beginCharge(int entityId, Vec3 position, Vec3 facing, int ticks) {
        effectLevel = Minecraft.getInstance().level;
        trackedEntity = entityId;
        portalPos = position;
        portalFacing = facing;
        chargeTicks = ticks;
        arriveTicks = 0;
    }

    public static void beginArrival(int entityId, Vec3 position, Vec3 facing) {
        effectLevel = Minecraft.getInstance().level;
        trackedEntity = entityId;
        portalPos = position;
        portalFacing = facing;
        chargeTicks = 0;
        arriveTicks = ARRIVE_TICKS;
    }

    public static void abort() {
        chargeTicks = 0;
        arriveTicks = 0;
        trackedEntity = -1;
        effectLevel = null;
    }

    public static boolean isCharging() {
        return chargeTicks > 0;
    }

    public static boolean isVisible() {
        return effectLevel != null && effectLevel == Minecraft.getInstance().level
                && (chargeTicks > 0 || arriveTicks > 0);
    }

    public static int trackedEntity() {
        return trackedEntity;
    }

    public static Vec3 portalPos() {
        return portalPos;
    }

    public static Vec3 portalFacing() {
        return portalFacing;
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
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().level != effectLevel) {
            abort();
            return;
        }

        if (chargeTicks > 0) {
            chargeTicks--;
        } else if (arriveTicks > 0) {
            arriveTicks--;
        }
    }

    /** Tracking clients see the portal, but only occupants get screen-space effects. */
    public static float screenStrength(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isVisible() || minecraft.player == null || minecraft.player.getVehicle() == null
                || minecraft.player.getVehicle().getId() != trackedEntity) return 0F;
        return fadeStrength(partialTick);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        abort();
    }
}
