package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class DrownedLungAugment extends Augment {
    public DrownedLungAugment(AugmentSlot augmentSlot) {
        super(NTAugments.DROWNED_LUNG.get(), augmentSlot);
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if(player.isUnderWater()){
            player.setAirSupply(player.getMaxAirSupply());
        }
    }
}
