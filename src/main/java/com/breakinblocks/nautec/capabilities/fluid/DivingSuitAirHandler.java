package com.breakinblocks.nautec.capabilities.fluid;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.items.AirBottleItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;

public class DivingSuitAirHandler implements IFluidHandlerItem {
    private final ItemStack container;

    public DivingSuitAirHandler(ItemStack container) {
        this.container = container;
    }

    public static List<FluidResource> oxygenFluids() {
        List<FluidResource> fluids = new ArrayList<>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(NTTags.Fluids.OXYGEN)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState())) {
                fluids.add(FluidResource.of(fluid));
            }
        }
        return fluids;
    }

    private boolean isChestplate() {
        return container.is(NTItems.DIVING_CHESTPLATE.get());
    }

    private int storedSeconds() {
        return isChestplate() ? container.getOrDefault(NTDataComponents.OXYGEN.get(), 0) : 0;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        int seconds = storedSeconds();
        if (seconds <= 0) {
            return FluidStack.EMPTY;
        }
        List<FluidResource> fluids = oxygenFluids();
        return fluids.isEmpty() ? FluidStack.EMPTY : fluids.getFirst().toStack(seconds * container.getCount());
    }

    @Override
    public int getTankCapacity(int tank) {
        return AirBottleItem.TANK_SECONDS * container.getCount();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return NTConfig.divingSuitAcceptsOxygenFluid && isChestplate() && stack.is(NTTags.Fluids.OXYGEN);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int count = container.getCount();
        if (resource.isEmpty() || count == 0 || !isFluidValid(0, resource)) {
            return 0;
        }
        int current = storedSeconds();
        int insertedPerItem = Math.min(resource.getAmount() / count, AirBottleItem.TANK_SECONDS - current);
        if (insertedPerItem <= 0) {
            return 0;
        }
        if (action.execute()) {
            container.set(NTDataComponents.OXYGEN.get(), current + insertedPerItem);
        }
        return insertedPerItem * count;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
