package com.breakinblocks.nautec.data.conditions;

import com.breakinblocks.nautec.registries.NTConditions;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public record SkyblockOptionCondition(SkyblockOption option) implements ICondition {
    public static final MapCodec<SkyblockOptionCondition> CODEC = SkyblockOption.CODEC.fieldOf("option")
            .xmap(SkyblockOptionCondition::new, SkyblockOptionCondition::option);

    @Override
    public boolean test(IContext context) {
        return option.enabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return NTConditions.SKYBLOCK_OPTION.get();
    }

    @Override
    public String toString() {
        return "skyblock_option(\"" + option.getSerializedName() + "\")";
    }
}
