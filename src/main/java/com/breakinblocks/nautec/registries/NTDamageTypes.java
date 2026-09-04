package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class NTDamageTypes {
    public static final ResourceKey<DamageType> PARTICLE_BEAM = ResourceKey.create(Registries.DAMAGE_TYPE, Nautec.rl("particle_beam"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(PARTICLE_BEAM, new DamageType("nautec.particle_beam", 0.1F));
    }

    private NTDamageTypes() {
    }
}
