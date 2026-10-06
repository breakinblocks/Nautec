package com.breakinblocks.nautec.compat.starcatcher;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.wdiscute.starcatcher.bobentity.FishingBobEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

final class StarcatcherBobEvents {
    private StarcatcherBobEvents() {
    }

    static void onEntityTick(EntityTickEvent.Post event) {
        if (!NTConfig.luckyZonesEnabled
                || !(event.getEntity() instanceof FishingBobEntity bob)
                || !(bob.level() instanceof ServerLevel level)
                || !bob.isInWater()) {
            return;
        }
        if (LuckyZoneIndex.get(level).zoneAt(level, bob.blockPosition()) == null) {
            return;
        }

        bob.setPos(bob.xo, bob.getY(), bob.zo);
        bob.setDeltaMovement(0.0, bob.getDeltaMovement().y, 0.0);
        if (NTConfig.luckyZoneBiteSpeed > 1) {
            bob.ticksInFluid += NTConfig.luckyZoneBiteSpeed - 1;
        }
    }
}
