package com.breakinblocks.nautec.content.blockentities.fusion;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FusionPortBlockEntity extends ContainerBlockEntity {
    private @Nullable BlockPos controllerPos;

    public FusionPortBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.FUSION_PORT.get(), blockPos, blockState);
    }

    public void link(@Nullable BlockPos controller) {
        if (controller == null ? controllerPos == null : controller.equals(controllerPos)) {
            return;
        }
        this.controllerPos = controller == null ? null : controller.immutable();
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    public @Nullable FusionControllerBlockEntity getController() {
        if (controllerPos == null || level == null || !level.isLoaded(controllerPos)) {
            return null;
        }
        if (level.getBlockEntity(controllerPos) instanceof FusionControllerBlockEntity controller && controller.hasPort(worldPosition)) {
            return controller;
        }
        return null;
    }

    public @Nullable EnergyHandler getEnergy() {
        FusionControllerBlockEntity controller = getController();
        return controller == null ? null : controller.getEnergyOutput();
    }

    public @Nullable ResourceHandler<FluidResource> getFuel() {
        FusionControllerBlockEntity controller = getController();
        return controller == null ? null : controller.getFuelInput();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        if (controllerPos != null) {
            out.store("controller", BlockPos.CODEC, controllerPos);
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.controllerPos = in.read("controller", BlockPos.CODEC).orElse(null);
    }
}
