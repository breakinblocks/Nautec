package com.breakinblocks.nautec.content.distributor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NetworkStorage {
    int fillItems(ResourceHandler<ItemResource> destination, ItemStack template, Predicate<ItemResource> filter, int need, Consumer<ItemStack> spill);

    int fillFluid(ResourceHandler<FluidResource> destination, Fluid fluid, int need);
}
