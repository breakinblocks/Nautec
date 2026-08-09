package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.entity.player.Player;

public class CreativeFlightAugment extends Augment {
    public CreativeFlightAugment(AugmentSlot augmentSlot) {
        super(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void onAdded(Player player) {
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }


    @Override
    public void onRemoved(Player player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }
}
