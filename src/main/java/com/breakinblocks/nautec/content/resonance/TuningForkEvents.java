package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class TuningForkEvents {
    private TuningForkEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof TuningForkItem || event.getItemStack().getItem() instanceof ResonanceCharmItem) {
            event.setUseBlock(TriState.FALSE);
        }
    }
}
