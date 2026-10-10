package com.breakinblocks.nautec.transfer;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.transfer.adapter.ExposedEnergyStorage;
import com.breakinblocks.nautec.transfer.adapter.ExposedFluidHandler;
import com.breakinblocks.nautec.transfer.adapter.ExposedItemHandler;
import com.breakinblocks.nautec.transfer.adapter.WrappedEnergyStorage;
import com.breakinblocks.nautec.transfer.adapter.WrappedFluidHandler;
import com.breakinblocks.nautec.transfer.adapter.WrappedItemHandler;
import com.breakinblocks.nautec.transfer.energy.EnergyHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TransferCapabilities {
    private TransferCapabilities() {
    }

    public static final class Item {
        public static final BlockCapability<ResourceHandler<ItemResource>, @Nullable Direction> BLOCK =
                BlockCapability.createSided(Nautec.rl("item_resource_handler"), ResourceHandler.asClass());

        private Item() {
        }
    }

    public static final class Fluid {
        public static final BlockCapability<ResourceHandler<FluidResource>, @Nullable Direction> BLOCK =
                BlockCapability.createSided(Nautec.rl("fluid_resource_handler"), ResourceHandler.asClass());

        private Fluid() {
        }
    }

    public static final class Energy {
        public static final BlockCapability<EnergyHandler, @Nullable Direction> BLOCK =
                BlockCapability.createSided(Nautec.rl("energy_handler"), EnergyHandler.class);

        private Energy() {
        }
    }

    public static @Nullable ResourceHandler<ItemResource> wrapItems(@Nullable IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof ExposedItemHandler exposed ? exposed.handler() : new WrappedItemHandler(handler);
    }

    public static @Nullable IItemHandler exposeItems(@Nullable ResourceHandler<ItemResource> handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof WrappedItemHandler wrapped ? wrapped.handler() : new ExposedItemHandler(handler);
    }

    public static @Nullable ResourceHandler<FluidResource> wrapFluids(@Nullable IFluidHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof ExposedFluidHandler exposed ? exposed.handler() : new WrappedFluidHandler(handler);
    }

    public static @Nullable IFluidHandler exposeFluids(@Nullable ResourceHandler<FluidResource> handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof WrappedFluidHandler wrapped ? wrapped.handler() : new ExposedFluidHandler(handler);
    }

    public static @Nullable EnergyHandler wrapEnergy(@Nullable IEnergyStorage storage) {
        if (storage == null) {
            return null;
        }
        return storage instanceof ExposedEnergyStorage exposed ? exposed.handler() : new WrappedEnergyStorage(storage);
    }

    public static @Nullable IEnergyStorage exposeEnergy(@Nullable EnergyHandler handler) {
        if (handler == null) {
            return null;
        }
        return handler instanceof WrappedEnergyStorage wrapped ? wrapped.storage() : new ExposedEnergyStorage(handler);
    }

    public static void registerBridges(RegisterCapabilitiesEvent event) {
        List<Block> own = new ArrayList<>();
        List<Block> foreign = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (Nautec.MODID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) {
                own.add(block);
            } else {
                foreign.add(block);
            }
        }
        Block[] ownBlocks = own.toArray(Block[]::new);
        Block[] foreignBlocks = foreign.toArray(Block[]::new);

        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> exposeItems(level.getCapability(Item.BLOCK, pos, state, be, side)), ownBlocks);
        event.registerBlock(Capabilities.FluidHandler.BLOCK,
                (level, pos, state, be, side) -> exposeFluids(level.getCapability(Fluid.BLOCK, pos, state, be, side)), ownBlocks);
        event.registerBlock(Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, be, side) -> exposeEnergy(level.getCapability(Energy.BLOCK, pos, state, be, side)), ownBlocks);

        event.registerBlock(Item.BLOCK,
                (level, pos, state, be, side) -> wrapItems(level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, be, side)), foreignBlocks);
        event.registerBlock(Fluid.BLOCK,
                (level, pos, state, be, side) -> wrapFluids(level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, be, side)), foreignBlocks);
        event.registerBlock(Energy.BLOCK,
                (level, pos, state, be, side) -> wrapEnergy(level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, state, be, side)), foreignBlocks);
    }
}
