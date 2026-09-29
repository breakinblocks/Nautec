package com.breakinblocks.nautec.client.teleport;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class ArrivalWaves {
    public static final int EXPAND_TICKS = 22;
    public static final int FADE_TICKS = 16;
    private static final float RADIUS = 14.0F;
    private static final int MAX_WAVES = 4;
    private static final List<Wave> waves = new ArrayList<>();
    private static @Nullable ClientLevel waveLevel;

    private ArrivalWaves() {
    }

    public static void begin(Vec3 center) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (level != waveLevel) {
            waves.clear();
        }
        waveLevel = level;
        if (waves.size() == MAX_WAVES) {
            waves.removeFirst();
        }
        waves.add(new Wave(center, RADIUS, level.getRandom().nextFloat() * 100.0F));
    }

    public static List<Wave> waves() {
        return waveLevel != null && waveLevel == Minecraft.getInstance().level ? waves : List.of();
    }

    public static void clear() {
        waves.clear();
        waveLevel = null;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (waveLevel == null) {
            return;
        }
        if (Minecraft.getInstance().level != waveLevel) {
            clear();
            return;
        }
        for (Wave wave : waves) {
            wave.age++;
        }
        waves.removeIf(wave -> wave.age > EXPAND_TICKS + FADE_TICKS);
    }

    public static final class Wave {
        private final Vec3 center;
        private final float radius;
        private final float seed;
        private int age;

        private Wave(Vec3 center, float radius, float seed) {
            this.center = center;
            this.radius = radius;
            this.seed = seed;
        }

        public Vec3 center() {
            return center;
        }

        public float radius() {
            return radius;
        }

        public float seed() {
            return seed;
        }

        public float front(float partialTick) {
            float progress = Mth.clamp((age + partialTick) / EXPAND_TICKS, 0.0F, 1.0F);
            return (1.0F - (1.0F - progress) * (1.0F - progress) * (1.0F - progress)) * radius;
        }

        public float fade(float partialTick) {
            float past = age + partialTick - EXPAND_TICKS;
            return past <= 0.0F ? 1.0F : Mth.clamp(1.0F - past / FADE_TICKS, 0.0F, 1.0F);
        }

        public float seconds(float partialTick) {
            return (age + partialTick) / 20.0F;
        }
    }
}
