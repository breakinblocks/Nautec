package com.breakinblocks.nautec.client.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.client.renderer.augments.helper.GuardianEyeRenderHelper;
import com.breakinblocks.nautec.content.augments.GuardianEyeAugment;
import com.breakinblocks.nautec.client.renderer.augments.helper.AugmentSlotsRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.Map;
import com.breakinblocks.nautec.client.AugmentClientHelper;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class AugmentClientEvents {

    @SubscribeEvent
    public static void renderPlayerPart(RenderPlayerEvent.Pre<?> event) {
        AugmentSlotsRenderer.render(event);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post<?> event) {
        Map<AugmentSlot, Augment> augments = AugmentClientHelper.forState(event.getRenderState());
        for (Augment augment : augments.values()) {
            if (augment != null && augment instanceof GuardianEyeAugment eyeAugment && eyeAugment.getTargetEntity() != null) {
                GuardianEyeRenderHelper.render(eyeAugment.getPlayer(), eyeAugment, event.getPartialTick(), event.getPoseStack(), event.getSubmitNodeCollector());
                return;
            }
        }
    }

}
