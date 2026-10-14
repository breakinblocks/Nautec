package com.breakinblocks.nautec.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.distributor.NetworkStorage;
import com.breakinblocks.nautec.content.distributor.NetworkStorages;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class MENetworkStorage implements NetworkStorage {
    private final IStorageService storage;
    private final IActionSource source;

    private MENetworkStorage(IStorageService storage, IActionSource source) {
        this.storage = storage;
        this.source = source;
    }

    static @Nullable NetworkStorage find(ServerLevel level, BlockPos pos, Direction side) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        InterfaceLogicHost host = null;
        if (blockEntity instanceof InterfaceLogicHost blockHost) {
            host = blockHost;
        } else if (blockEntity instanceof IPartHost partHost && partHost.getPart(side) instanceof InterfaceLogicHost partInterface) {
            host = partInterface;
        }
        if (host == null) {
            return null;
        }
        InterfaceLogic logic = host.getInterfaceLogic();
        IGridNode node = logic.getActionableNode();
        if (node == null || !node.isActive()) {
            return null;
        }
        return new MENetworkStorage(node.getGrid().getStorageService(), IActionSource.ofMachine(logic));
    }

    @Override
    public int fillItems(ResourceHandler<ItemResource> destination, ItemStack template, Predicate<ItemResource> filter, int need, Consumer<ItemStack> spill) {
        if (template.isEmpty()) {
            return 0;
        }
        List<AEItemKey> keys = new ArrayList<>();
        LongArrayList amounts = new LongArrayList();
        for (Object2LongMap.Entry<AEKey> entry : storage.getCachedInventory().findFuzzy(AEItemKey.of(template.getItem()), FuzzyMode.IGNORE_ALL)) {
            if (entry.getKey() instanceof AEItemKey key && entry.getLongValue() > 0) {
                keys.add(key);
                amounts.add(entry.getLongValue());
            }
        }
        MEStorage inventory = storage.getInventory();
        int moved = 0;
        for (int i = 0; i < keys.size() && need > 0; i++) {
            AEItemKey key = keys.get(i);
            ItemResource resource = ItemResource.of(key.toStack());
            if (!filter.test(resource)) {
                continue;
            }
            int step = NetworkStorages.fill(destination, resource, amounts.getLong(i), need,
                    amount -> inventory.extract(key, amount, Actionable.MODULATE, source),
                    amount -> inventory.insert(key, amount, Actionable.MODULATE, source),
                    rest -> spill(key, rest, spill));
            need -= step;
            moved += step;
        }
        return moved;
    }

    @Override
    public int fillFluid(ResourceHandler<FluidResource> destination, Fluid fluid, int need) {
        List<AEFluidKey> keys = new ArrayList<>();
        LongArrayList amounts = new LongArrayList();
        for (Object2LongMap.Entry<AEKey> entry : storage.getCachedInventory().findFuzzy(AEFluidKey.of(fluid), FuzzyMode.IGNORE_ALL)) {
            if (entry.getKey() instanceof AEFluidKey key && key.getFluid() == fluid && entry.getLongValue() > 0) {
                keys.add(key);
                amounts.add(entry.getLongValue());
            }
        }
        MEStorage inventory = storage.getInventory();
        int moved = 0;
        for (int i = 0; i < keys.size() && need > 0; i++) {
            AEFluidKey key = keys.get(i);
            int step = NetworkStorages.fill(destination, FluidResource.of(key.toStack(1)), amounts.getLong(i), need,
                    amount -> inventory.extract(key, amount, Actionable.MODULATE, source),
                    amount -> inventory.insert(key, amount, Actionable.MODULATE, source),
                    rest -> Nautec.LOGGER.warn("An ME network would not take back {} mB of {} that a Distributor could not place", rest, key.getId()));
            need -= step;
            moved += step;
        }
        return moved;
    }

    private static void spill(AEItemKey key, long amount, Consumer<ItemStack> spill) {
        int maxStack = Math.max(1, key.getReadOnlyStack().getMaxStackSize());
        long left = amount;
        while (left > 0) {
            int count = (int) Math.min(left, maxStack);
            spill.accept(key.toStack(count));
            left -= count;
        }
    }
}
