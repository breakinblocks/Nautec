package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class HeldItemOwner {
    private static @Nullable LivingEntity rendering;

    private HeldItemOwner() {
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        rendering = event.getEntity();
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        rendering = null;
    }

    public static @Nullable LivingEntity of(ItemStack stack, @Nullable ItemDisplayContext context) {
        if (context == null) {
            return null;
        }
        if (context.firstPerson()) {
            Entity camera = Minecraft.getInstance().getCameraEntity();
            return camera instanceof LivingEntity living ? living : Minecraft.getInstance().player;
        }
        if (context != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && context != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                && context != ItemDisplayContext.HEAD) {
            return null;
        }
        LivingEntity entity = rendering;
        if (entity != null && (entity.getMainHandItem() == stack || entity.getOffhandItem() == stack)) {
            return entity;
        }
        return null;
    }
}
