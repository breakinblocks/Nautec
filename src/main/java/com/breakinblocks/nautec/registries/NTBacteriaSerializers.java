package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaSerializer;
import com.breakinblocks.nautec.content.bacteria.EmptyBacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteria;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTBacteriaSerializers {
    public static final DeferredRegister<BacteriaSerializer<?>> SERIALIZERS = DeferredRegister.create(NTRegistries.BACTERIA_SERIALIZER, Nautec.MODID);

    static {
        SERIALIZERS.register("empty", () -> EmptyBacteria.SERIALIZER);
        SERIALIZERS.register("simple", () -> SimpleBacteria.Serializer.INSTANCE);
    }
}
