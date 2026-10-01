package com.breakinblocks.nautec.capabilities.fluid;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.items.AirBottleItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DivingSuitAirHandler implements ResourceHandler<FluidResource> {
    private final ItemAccess itemAccess;

    public DivingSuitAirHandler(ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
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
        return itemAccess.getResource().is(NTItems.DIVING_CHESTPLATE.get());
    }

    private int storedSeconds() {
        return isChestplate() ? itemAccess.getResource().getOrDefault(NTDataComponents.OXYGEN.get(), 0) : 0;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());
        if (storedSeconds() <= 0) {
            return FluidResource.EMPTY;
        }
        List<FluidResource> fluids = oxygenFluids();
        return fluids.isEmpty() ? FluidResource.EMPTY : fluids.getFirst();
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return getResource(index).isEmpty() ? 0 : (long) itemAccess.getAmount() * storedSeconds();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty() || isValid(index, resource)) {
            return (long) itemAccess.getAmount() * AirBottleItem.TANK_SECONDS;
        }
        return 0;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return NTConfig.divingSuitAcceptsOxygenFluid && isChestplate() && resource.is(NTTags.Fluids.OXYGEN);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        int count = itemAccess.getAmount();
        if (resource.isEmpty() || amount <= 0 || count == 0 || !isValid(index, resource)) {
            return 0;
        }

        int current = storedSeconds();
        int insertedPerItem = Math.min(amount / count, AirBottleItem.TANK_SECONDS - current);
        if (insertedPerItem <= 0) {
            return 0;
        }

        ItemResource filled = itemAccess.getResource().with(NTDataComponents.OXYGEN.get(), current + insertedPerItem);
        return insertedPerItem * itemAccess.exchange(filled, count, transaction);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
