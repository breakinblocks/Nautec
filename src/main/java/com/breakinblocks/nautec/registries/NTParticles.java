package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;

public final class NTParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Nautec.MODID);

    public static final Supplier<SimpleParticleType> VENT_BUBBLE = PARTICLE_TYPES.register("vent_bubble",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> GLOW_SPORE = PARTICLE_TYPES.register("glow_spore",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> ABYSSAL_MOTE = PARTICLE_TYPES.register("abyssal_mote",
            () -> new SimpleParticleType(false));

    public static final Supplier<SimpleParticleType> THRUSTER_WAKE = PARTICLE_TYPES.register("thruster_wake",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BOOST_TRAIL = PARTICLE_TYPES.register("boost_trail",
            () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> SONAR_MOTE = PARTICLE_TYPES.register("sonar_mote",
            () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> SHIELD_RING = PARTICLE_TYPES.register("shield_ring",
            () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> TELEPORT_SWIRL = PARTICLE_TYPES.register("teleport_swirl",
            () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> LASER_SPARK = PARTICLE_TYPES.register("laser_spark",
            () -> new SimpleParticleType(false));

    private NTParticles() {
    }
}
