package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.SubmarineAnvilRepair;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

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
            if (submarine.isStealthed()) {
                event.setCanceled(true);
                return;
            }
            event.setNewAboutToBeSetTarget(submarine);
            return;
        }

        if (target instanceof SubmarineEntity submarine && submarine.isStealthed()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSubmarineDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof SubmarineEntity submarine)) {
            return;
        }

        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR) || !submarine.hasModule(SubmarineModuleType.SHIELD)) {
            return;
        }

        float damage = event.getNewDamage();
        if (damage <= 0F) {
            return;
        }

        float powerPerHealth = NTConfig.submarineShieldPowerPerHeart / 2F;
        float absorbed = Math.min(damage, submarine.getPowerStored() / powerPerHealth);
        if (absorbed <= 0F) {
            return;
        }

        submarine.setPowerStored(submarine.getPowerStored() - Mth.ceil(absorbed * powerPerHealth));
        event.setNewDamage(damage - absorbed);
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
