package com.breakinblocks.nautec.client.shockwave;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class TidalShockwaves {
    public static final int EXPAND_TICKS = 16;
    public static final int FADE_TICKS = 14;
    private static final float EDGE_OVERSHOOT = 1.5F;
    private static final int MAX_WAVES = 8;
    private static final int MAX_RING_PARTICLES = 40;
    private static final float RING_PARTICLES_PER_BLOCK = 6.0F;
    private static final int BURST_PARTICLES = 48;
    private static final int GROUND_SEARCH_UP = 2;
    private static final int GROUND_SEARCH_DOWN = 5;
    private static final List<Wave> waves = new ArrayList<>();
    private static @Nullable ClientLevel waveLevel;

    private TidalShockwaves() {
    }

    public static void begin(Vec3 center, float radius) {
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
        waves.add(new Wave(center, radius + EDGE_OVERSHOOT, level.getRandom().nextFloat() * 100.0F));
        burst(level, center);
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
        ClientLevel level = Minecraft.getInstance().level;
        if (waveLevel == null) {
            return;
        }
        if (level != waveLevel) {
            clear();
            return;
        }
        for (Wave wave : waves) {
            wave.age++;
            if (wave.age <= EXPAND_TICKS) {
                ring(level, wave);
            }
        }
        waves.removeIf(wave -> wave.age > EXPAND_TICKS + FADE_TICKS);
    }

    private static void burst(ClientLevel level, Vec3 center) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < BURST_PARTICLES; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 0.1 + random.nextDouble() * 0.3;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.addParticle(ParticleTypes.SPLASH, center.x + dx * 0.3, center.y + 0.1, center.z + dz * 0.3,
                    dx * speed, 0.0, dz * speed);
            level.addParticle(ParticleTypes.BUBBLE, center.x, center.y + 0.3, center.z,
                    dx * speed, 0.2 + random.nextDouble() * 0.3, dz * speed);
            if (i % 4 == 0) {
                level.addParticle(ParticleTypes.CLOUD, center.x, center.y + 0.2, center.z,
                        dx * speed * 0.5, 0.15 + random.nextDouble() * 0.2, dz * speed * 0.5);
            }
        }
    }

    private static void ring(ClientLevel level, Wave wave) {
        RandomSource random = level.getRandom();
        float front = wave.front(0.0F);
        int count = Math.min(MAX_RING_PARTICLES, Mth.ceil(front * RING_PARTICLES_PER_BLOCK));
        Vec3 center = wave.center();
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            double x = center.x + dx * front;
            double z = center.z + dz * front;
            double y = groundY(level, x, center.y, z);
            level.addParticle(ParticleTypes.SPLASH, x, y + 0.05, z, dx * 0.25, 0.0, dz * 0.25);
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.BUBBLE, x, y + 0.3, z, dx * 0.1, 0.25, dz * 0.1);
            }
        }
    }

    private static double groundY(ClientLevel level, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(x, y, z).mutable().move(Direction.UP, GROUND_SEARCH_UP);
        for (int i = 0; i < GROUND_SEARCH_UP + GROUND_SEARCH_DOWN; i++) {
            BlockPos below = pos.below();
            VoxelShape floor = level.getBlockState(below).getCollisionShape(level, below);
            if (!floor.isEmpty() && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return below.getY() + floor.max(Direction.Axis.Y);
            }
            pos.move(Direction.DOWN);
        }
        return y;
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
            return (1.0F - (1.0F - progress) * (1.0F - progress)) * radius;
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
