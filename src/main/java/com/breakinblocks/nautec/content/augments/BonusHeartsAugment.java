package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class BonusHeartsAugment extends Augment {
    public BonusHeartsAugment(AugmentSlot augmentSlot) {
        super(NTAugments.BONUS_HEART_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void onAdded(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        attribute.setBaseValue(40);
    }

    @Override
    public void onRemoved(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        attribute.setBaseValue(20);
    }
}
