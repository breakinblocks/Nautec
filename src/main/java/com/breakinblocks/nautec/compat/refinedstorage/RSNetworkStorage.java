package com.breakinblocks.nautec.compat.refinedstorage;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.distributor.NetworkStorage;
import com.breakinblocks.nautec.content.distributor.NetworkStorages;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class RSNetworkStorage implements NetworkStorage {
    private final StorageNetworkComponent storage;
    private final Actor actor;

    private RSNetworkStorage(StorageNetworkComponent storage, Actor actor) {
        this.storage = storage;
        this.actor = actor;
    }

    static @Nullable NetworkStorage find(ServerLevel level, BlockPos pos, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof InterfaceBlockEntity blockEntity)) {
            return null;
        }
        for (InWorldNetworkNodeContainer container : blockEntity.getContainerProvider().getContainers()) {
            if (container.getNode() instanceof InterfaceNetworkNode node && node.isActive()) {
                Network network = node.getNetwork();
                StorageNetworkComponent storage = network == null ? null : network.getComponent(StorageNetworkComponent.class);
                if (storage != null) {
                    return new RSNetworkStorage(storage, new NetworkNodeActor(node));
                }
            }
        }
        return null;
    }

    private List<ResourceKey> candidates(ResourceKey probe) {
        return new ArrayList<>(storage instanceof FuzzyRootStorage fuzzy ? fuzzy.getFuzzy(probe) : List.of(probe));
    }

    @Override
    public int fillItems(ResourceHandler<ItemResource> destination, ItemStack template, Predicate<ItemResource> filter, int need, Consumer<ItemStack> spill) {
        if (template.isEmpty()) {
            return 0;
        }
        int moved = 0;
        for (ResourceKey key : candidates(RSKeys.item(template.getItem()))) {
            Item item = RSKeys.itemOf(key);
            if (need <= 0) {
                break;
            }
            if (item == null) {
                continue;
            }
            long available = storage.get(key);
            ItemResource resource = ItemResource.of(item, RSKeys.components(key));
            if (available <= 0 || !filter.test(resource)) {
                continue;
            }
            int step = NetworkStorages.fill(destination, resource, available, need,
                    amount -> storage.extract(key, amount, Action.EXECUTE, actor),
                    amount -> storage.insert(key, amount, Action.EXECUTE, actor),
                    rest -> spill(key, rest, spill));
            need -= step;
            moved += step;
        }
        return moved;
    }

    @Override
    public int fillFluid(ResourceHandler<FluidResource> destination, Fluid fluid, int need) {
        int moved = 0;
        for (ResourceKey key : candidates(RSKeys.fluid(fluid))) {
            if (need <= 0) {
                break;
            }
            if (RSKeys.fluidOf(key) != fluid) {
                continue;
            }
            long available = storage.get(key);
            if (available <= 0) {
                continue;
            }
            int step = NetworkStorages.fill(destination, FluidResource.of(fluid, RSKeys.components(key)), available, need,
                    amount -> storage.extract(key, amount, Action.EXECUTE, actor),
                    amount -> storage.insert(key, amount, Action.EXECUTE, actor),
                    rest -> Nautec.LOGGER.warn("A Refined Storage network would not take back {} mB of {} that a Distributor could not place", rest, fluid));
            need -= step;
            moved += step;
        }
        return moved;
    }

    private static void spill(ResourceKey key, long amount, Consumer<ItemStack> spill) {
        int maxStack = Math.max(1, RSKeys.maxStackSize(key));
        long left = amount;
        while (left > 0) {
            int count = (int) Math.min(left, maxStack);
            spill.accept(RSKeys.stack(key, count));
            left -= count;
        }
    }
}
