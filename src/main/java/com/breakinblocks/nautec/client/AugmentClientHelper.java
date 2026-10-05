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
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class AugmentClientHelper {
    private static final ContextKey<Map<AugmentSlot, Augment>> AUGMENTS = new ContextKey<>(Nautec.rl("render_augments"));
    private static final Map<Integer, Snapshot> SNAPSHOTS = new HashMap<>();
    private static final long PRUNE_INTERVAL = 200L;
    private static @Nullable Level snapshotLevel;
    private static long prunedAt;

    private record Snapshot(long gameTime, Map<AugmentSlot, Augment> sources, Map<AugmentSlot, Augment> copies) {
        private boolean matches(Map<AugmentSlot, Augment> current) {
            if (current.size() != sources.size()) {
                return false;
            }
            for (Map.Entry<AugmentSlot, Augment> entry : current.entrySet()) {
                if (sources.get(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }
    }

    @SubscribeEvent
    public static void register(RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                state.setRenderData(AUGMENTS, avatar instanceof Player player ? snapshot(player) : Map.of());
            }
        });
    }

    private static Map<AugmentSlot, Augment> snapshot(Player player) {
        Level level = player.level();
        long gameTime = level.getGameTime();
        if (level != snapshotLevel) {
            SNAPSHOTS.clear();
            snapshotLevel = level;
            prunedAt = gameTime;
        } else if (gameTime - prunedAt >= PRUNE_INTERVAL || gameTime < prunedAt) {
            SNAPSHOTS.values().removeIf(snapshot -> snapshot.gameTime != gameTime);
            prunedAt = gameTime;
        }
        Map<AugmentSlot, Augment> sources = AugmentHelper.getAugments(player);
        if (sources.isEmpty()) {
            SNAPSHOTS.remove(player.getId());
            return Map.of();
        }
        Snapshot cached = SNAPSHOTS.get(player.getId());
        if (cached != null && cached.gameTime == gameTime && cached.matches(sources)) {
            return cached.copies;
        }
        Map<AugmentSlot, Augment> identities = new HashMap<>(sources);
        Map<AugmentSlot, Augment> copies = new HashMap<>();
        sources.forEach((slot, source) -> copies.put(slot, source.copyForRender()));
        Snapshot snapshot = new Snapshot(gameTime, identities, Map.copyOf(copies));
        SNAPSHOTS.put(player.getId(), snapshot);
        return snapshot.copies;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SNAPSHOTS.clear();
        snapshotLevel = null;
    }

    public static Map<AugmentSlot, Augment> forState(LivingEntityRenderState state) {
        Map<AugmentSlot, Augment> snapshot = state.getRenderData(AUGMENTS);
        return snapshot == null ? Map.of() : snapshot;
    }
}
