package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class SubmarineAbilityBarState {
    private static final int SLOTS = SubmarineEntity.MODULE_SLOTS;

    private static int trackedEntity = -1;
    private static int selected;
    private static final long[] startedAt = new long[SLOTS];
    private static final int[] cooldown = new int[SLOTS];
    private static final int[] active = new int[SLOTS];

    private SubmarineAbilityBarState() {
    }

    public static void follow(int entityId) {
        if (trackedEntity != entityId) {
            clear();
            trackedEntity = entityId;
        }
    }

    public static void clear() {
        trackedEntity = -1;
        selected = 0;
        for (int slot = 0; slot < SLOTS; slot++) {
            startedAt[slot] = 0L;
            cooldown[slot] = 0;
            active[slot] = 0;
        }
    }

    public static int selected() {
        return selected;
    }

    public static void select(int slot) {
        selected = Mth.clamp(slot, 0, SLOTS - 1);
    }

    public static void step(int delta) {
        selected = Math.floorMod(selected + delta, SLOTS);
    }

    public static void onCooldown(int entityId, int slot, int cooldownTicks, int activeTicks) {
        if (slot < 0 || slot >= SLOTS) {
            return;
        }

        follow(entityId);
        startedAt[slot] = gameTime();
        cooldown[slot] = cooldownTicks;
        active[slot] = activeTicks;
    }

    public static float cooldownProgress(int slot) {
        if (cooldown[slot] <= 0) {
            return 0F;
        }

        long elapsed = gameTime() - startedAt[slot];
        if (elapsed >= cooldown[slot]) {
            return 0F;
        }

        return 1F - (float) elapsed / cooldown[slot];
    }

    public static boolean isActive(int slot) {
        return active[slot] > 0 && gameTime() - startedAt[slot] < active[slot];
    }

    private static long gameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}
