package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class DolphinFinAugment extends Augment {
    public DolphinFinAugment(AugmentSlot augmentSlot) {
        super(NTAugments.DOLPHIN_FIN.get(), augmentSlot);
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        if (event.getEntity().isUnderWater()){
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,20,1));
        }
    }
}
