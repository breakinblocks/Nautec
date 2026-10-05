package com.breakinblocks.nautec.api.blockentities;

import com.breakinblocks.nautec.Nautec;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = Nautec.MODID)
public final class BlockEntitySyncQueue {
    private static final Map<Level, Set<ContainerBlockEntity>> PENDING = new IdentityHashMap<>();

    private BlockEntitySyncQueue() {
    }

    static void schedule(ServerLevel level, ContainerBlockEntity blockEntity) {
        PENDING.computeIfAbsent(level, key -> new ReferenceLinkedOpenHashSet<>()).add(blockEntity);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Set<ContainerBlockEntity> pending = PENDING.get(level);
        if (pending == null || pending.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        Iterator<ContainerBlockEntity> iterator = pending.iterator();
        while (iterator.hasNext()) {
            ContainerBlockEntity blockEntity = iterator.next();
            if (blockEntity.isRemoved() || blockEntity.getLevel() != level) {
                iterator.remove();
                blockEntity.cancelScheduledSync();
            } else if (blockEntity.readyToSync(now)) {
                iterator.remove();
                blockEntity.flushSync();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        PENDING.remove(event.getLevel());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
