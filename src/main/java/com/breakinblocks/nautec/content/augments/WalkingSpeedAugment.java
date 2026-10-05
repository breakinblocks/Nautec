package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

public class WalkingSpeedAugment extends Augment {
    private static final Identifier MODIFIER_ID = Nautec.rl("walking_speed_augment");
    private static final float DEFAULT_WALKING_SPEED = 0.1f;
    private static final float LEGACY_WALKING_SPEED = 0.25f;
    private static final double SPEED_BONUS = 1.5;

    public WalkingSpeedAugment(AugmentSlot augmentSlot) {
        super(NTAugments.WALKING_SPEED_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void onAdded(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        clearLegacySpeed(player, attribute);
        attribute.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER_ID, SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public void onRemoved(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        clearLegacySpeed(player, attribute);
        attribute.removeModifier(MODIFIER_ID);
    }

    private static void clearLegacySpeed(Player player, AttributeInstance attribute) {
        Abilities abilities = player.getAbilities();
        if (abilities.getWalkingSpeed() == LEGACY_WALKING_SPEED) {
            abilities.setWalkingSpeed(DEFAULT_WALKING_SPEED);
            attribute.setBaseValue(DEFAULT_WALKING_SPEED);
        }
    }
}
