package com.breakinblocks.nautec.registries;

import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public final class NTMobCategories {
    public static final EnumProxy<MobCategory> VENT_FAUNA =
            new EnumProxy<>(MobCategory.class, "nautec:vent_fauna", 6, true, false, 128);

    public static final EnumProxy<MobCategory> GROVE_FAUNA =
            new EnumProxy<>(MobCategory.class, "nautec:grove_fauna", 6, true, false, 128);

    public static MobCategory ventFauna() {
        return VENT_FAUNA.getValue();
    }

    public static MobCategory groveFauna() {
        return GROVE_FAUNA.getValue();
    }

    private NTMobCategories() {
    }
}
