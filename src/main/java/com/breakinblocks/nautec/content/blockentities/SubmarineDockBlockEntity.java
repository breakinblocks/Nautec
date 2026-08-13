package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.utils.ParticleUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubmarineDockBlockEntity extends LaserBlockEntity {
    private boolean holding;

    public SubmarineDockBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.SUBMARINE_DOCK.get(), blockPos, blockState);
    }

    public AABB dockingArea() {
        return new AABB(worldPosition.above()).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 2.0, 0.0);
    }

    public boolean isDocking() {
        return !docked().isEmpty();
    }

    private List<SubmarineEntity> docked() {
        return level == null ? List.of() : level.getEntitiesOfClass(SubmarineEntity.class, dockingArea());
    }

    @Override
    public void commonTick() {
        super.commonTick();

        List<SubmarineEntity> submarines = getPower() < NTConfig.dockPowerUsage ? List.of() : docked();
        boolean holdingNow = !submarines.isEmpty();

        if (holdingNow != this.holding) {
            MachineSounds.play(level, worldPosition,
                    holdingNow ? NTSounds.DOCK_CLAMP : NTSounds.DOCK_RELEASE, 0.7f, holdingNow ? 0.8f : 0.9f);
            this.holding = holdingNow;
        }

        if (!holdingNow) {
            return;
        }

        for (SubmarineEntity submarine : submarines) {
            hold(submarine);
            charge(submarine);
            breathe(submarine);
        }

        if (level.isClientSide()) {
            ParticleUtils.spawnParticlesAroundBlock(getBlockPos(), getLevel(), ParticleTypes.ELECTRIC_SPARK);
        }
    }

    private static void hold(SubmarineEntity submarine) {
        if (submarine.getControllingPassenger() != null) {
            return;
        }
        submarine.setDeltaMovement(Vec3.ZERO);
        submarine.hurtMarked = true;
    }

    private static void charge(SubmarineEntity submarine) {
        IPowerStorage storage = submarine.getPowerStorage();
        if (storage.getPowerStored() < storage.getPowerCapacity()) {
            storage.tryFillPower(NTConfig.dockChargeRate, false);
        }
    }

    private static void breathe(SubmarineEntity submarine) {
        for (Entity passenger : submarine.getPassengers()) {
            if (passenger instanceof LivingEntity living) {
                living.setAirSupply(living.getMaxAirSupply());
            }
        }
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.values());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }
}
