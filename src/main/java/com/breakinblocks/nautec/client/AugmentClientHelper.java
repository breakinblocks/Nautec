package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class AugmentClientHelper {
    private static final ContextKey<Map<AugmentSlot, Augment>> AUGMENTS = new ContextKey<>(Nautec.rl("render_augments"));

    @SubscribeEvent
    public static void register(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                Map<AugmentSlot, Augment> snapshot = new HashMap<>();
                if (avatar instanceof Player player) {
                    AugmentHelper.getAugments(player).forEach((slot, source) -> {
                        snapshot.put(slot, source.copyForRender());
                    });
                }
                state.setRenderData(AUGMENTS, Map.copyOf(snapshot));
            }
        });
    }

    public static Map<AugmentSlot, Augment> forState(LivingEntityRenderState state) {
        Map<AugmentSlot, Augment> snapshot = state.getRenderData(AUGMENTS);
        return snapshot == null ? Map.of() : snapshot;
    }
}
