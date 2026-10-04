package com.breakinblocks.nautec.compat.apotheosis;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.fml.ModList;

public final class ApothicAttributesCompat {
    public static final String MOD_ID = "apothic_attributes";
    private static final Identifier DRAW_SPEED = Identifier.fromNamespaceAndPath(MOD_ID, "draw_speed");
    private static final Identifier ARROW_VELOCITY = Identifier.fromNamespaceAndPath(MOD_ID, "arrow_velocity");
    private static final Identifier ARROW_DAMAGE = Identifier.fromNamespaceAndPath(MOD_ID, "arrow_damage");
    private static final double MIN_DRAW_SPEED = 0.1D;

    private static Boolean loaded;

    private ApothicAttributesCompat() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }

    public static double drawSpeed(LivingEntity entity) {
        return Math.max(MIN_DRAW_SPEED, value(entity, DRAW_SPEED));
    }

    public static double beamDamageMultiplier(LivingEntity entity) {
        return value(entity, ARROW_VELOCITY) * value(entity, ARROW_DAMAGE);
    }

    private static double value(LivingEntity entity, Identifier id) {
        if (!isLoaded()) {
            return 1.0D;
        }
        return BuiltInRegistries.ATTRIBUTE.get(id)
                .map(entity::getAttribute)
                .map(AttributeInstance::getValue)
                .orElse(1.0D);
    }
}
