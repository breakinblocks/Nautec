package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.conditions.SkyblockOptionCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NTConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Nautec.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<SkyblockOptionCondition>>
            SKYBLOCK_OPTION = CONDITION_CODECS.register("skyblock_option", () -> SkyblockOptionCondition.CODEC);

    private NTConditions() {
    }
}
