package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Nautec.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> CRYSTAL_FOUND = TRIGGERS.register("crystal_found", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> GATEWAY_FOUND = TRIGGERS.register("gateway_found", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> GATEWAY_TRAVEL = TRIGGERS.register("gateway_travel", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> SUBMARINE_FLIGHT = TRIGGERS.register("submarine_flight", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> BACTERIA_GRAFTED = TRIGGERS.register("bacteria_grafted", PlayerTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, PlayerTrigger> BACTERIA_MUTATED = TRIGGERS.register("bacteria_mutated", PlayerTrigger::new);

    private NTCriteriaTriggers() {
    }

    public static void triggerNear(PlayerTrigger trigger, ServerLevel level, BlockPos pos, double radius) {
        Vec3 centre = Vec3.atCenterOf(pos);
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && player.position().distanceToSqr(centre) <= radius * radius) {
                trigger.trigger(player);
            }
        }
    }
}
