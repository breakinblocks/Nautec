package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.registries.NTAugments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class DolphinFinAugment extends Augment {
    private static final int DURATION = 100;
    private static final int REFRESH_BELOW = 40;

    public DolphinFinAugment(AugmentSlot augmentSlot) {
        super(NTAugments.DOLPHIN_FIN.get(), augmentSlot);
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isUnderWater()) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.DOLPHINS_GRACE);
        if (current == null || current.getDuration() < REFRESH_BELOW) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, DURATION, 1));
        }
    }
}
