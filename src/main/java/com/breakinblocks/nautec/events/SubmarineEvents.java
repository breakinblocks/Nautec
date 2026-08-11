package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.SubmarineAnvilRepair;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber(modid = Nautec.MODID)
public final class SubmarineEvents {
    private SubmarineEvents() {
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) {
            return;
        }

        if (target.getVehicle() instanceof SubmarineEntity submarine) {
            event.setNewAboutToBeSetTarget(submarine);
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        SubmarineAnvilRepair.Result result = SubmarineAnvilRepair.compute(event.getLeft(), event.getRight());
        if (result == null) {
            return;
        }

        event.setOutput(result.output());
        event.setXpCost(result.xpCost());
        event.setMaterialCost(result.materialCost());
    }
}
