package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class UnderwaterMiningSpeed extends Augment {
    public UnderwaterMiningSpeed(AugmentSlot augmentSlot) {
        super(NTAugments.UNDERWATER_MINING_SPEED_AUGMENT.get(), augmentSlot);
    }

    @Override
    public void onAdded(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.SUBMERGED_MINING_SPEED);
        attribute.setBaseValue(1.0f);
    }

    @Override
    public void onRemoved(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.SUBMERGED_MINING_SPEED);
        attribute.setBaseValue(0.1f);
    }
}
