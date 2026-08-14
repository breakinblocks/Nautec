package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class EldritchHeartAugment extends Augment {
    public EldritchHeartAugment(AugmentSlot augmentSlot) {
        super(NTAugments.ELDRITCH_HEART.get(), augmentSlot);
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        if (player.isInWater() && player.tickCount % 20 == 0) {
            player.heal(1.0f);
        }
    }
}
