package com.breakinblocks.nautec.content.items.tools;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class WrenchLinkEvents {
    private WrenchLinkEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof AquarineWrenchItem && AquarineWrenchItem.mode(event.getItemStack()) == WrenchMode.LINK) {
            event.setUseBlock(TriState.FALSE);
        }
    }
}
