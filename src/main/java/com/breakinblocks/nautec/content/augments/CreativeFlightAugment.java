package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public class CreativeFlightAugment extends Augment {
    private static final Identifier MODIFIER_ID = Nautec.rl("creative_flight_augment");

    public CreativeFlightAugment(AugmentSlot augmentSlot) {
        super(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void onAdded(Player player) {
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute != null) {
            attribute.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void onRemoved(Player player) {
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_ID);
        }
        if (!player.isCreative() && !player.isSpectator()) {
            // Older versions saved the augment's flight grant in vanilla abilities.
            player.getAbilities().mayfly = false;
            if (!player.mayFly()) {
                player.getAbilities().flying = false;
            }
            player.onUpdateAbilities();
        }
    }
}
