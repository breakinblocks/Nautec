package com.breakinblocks.nautec.content.distributor;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.sides.SideKind;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.SingleSlotHandler;
import com.breakinblocks.nautec.content.menus.DistributorMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.ItemTemplates;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class DistributorBlockEntity extends ContainerBlockEntity implements MenuProvider {
    public enum LinkResult {
        LINKED,
        UNLINKED,
        TOO_FAR,
        FULL,
        SELF
    }

    private record Endpoint(DistributorLink link, @Nullable ResourceHandler<ItemResource> items, @Nullable ResourceHandler<FluidResource> fluids,
                            List<ItemStack> keptItems, Set<Fluid> keptFluids) {
        boolean keeps(ItemResource resource) {
            for (ItemStack template : keptItems) {
                if (ItemTemplates.matches(template, resource)) {
                    return true;
                }
            }
            return false;
        }
    }

    private record Neighbour(@Nullable ResourceHandler<ItemResource> items, @Nullable ResourceHandler<FluidResource> fluids) {
    }

    private final List<DistributorLink> links = new ArrayList<>();

    public DistributorBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.DISTRIBUTOR.get(), pos, state);
    }

    public List<DistributorLink> getLinks() {
        return links;
    }

    public @Nullable DistributorLink link(int index) {
        return index >= 0 && index < links.size() ? links.get(index) : null;
    }

    public LinkResult toggle(BlockPos pos, Direction face) {
        if (pos.equals(worldPosition)) {
            return LinkResult.SELF;
        }
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).pos().equals(pos)) {
                links.remove(i);
                changed();
                return LinkResult.UNLINKED;
            }
        }
        if (pos.getCenter().distanceTo(worldPosition.getCenter()) > NTConfig.distributorRange) {
            return LinkResult.TOO_FAR;
        }
        if (links.size() >= NTConfig.distributorMaxLinks) {
            return LinkResult.FULL;
        }
        links.add(new DistributorLink(pos, face));
        changed();
        return LinkResult.LINKED;
    }

    public void unlink(int index) {
        if (index >= 0 && index < links.size()) {
            links.remove(index);
            changed();
        }
    }

    public void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level instanceof ServerLevel serverLevel && !links.isEmpty() && serverLevel.getGameTime() % Math.max(1, NTConfig.distributorInterval) == 0) {
            cycle(serverLevel);
        }
    }

    public void cycle(ServerLevel serverLevel) {
        Set<BlockPos> linked = new HashSet<>();
        List<Endpoint> endpoints = new ArrayList<>();
        for (DistributorLink link : links) {
            linked.add(link.pos());
            if (!serverLevel.isLoaded(link.pos())) {
                continue;
            }
            List<ItemStack> keptItems = new ArrayList<>();
            Set<Fluid> keptFluids = new HashSet<>();
            boolean outputsOnly = serverLevel.getBlockEntity(link.pos()) instanceof ContainerBlockEntity machine && machine.hasSideConfig(SideKind.ITEMS);
            for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
                if (!link.item(i).isEmpty() && !outputsOnly) {
                    keptItems.add(link.item(i));
                }
            }
            for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
                if (!link.fluid(i).isEmpty()) {
                    keptFluids.add(link.fluid(i).getFluid());
                }
            }
            if (!outputsOnly && serverLevel.getBlockEntity(link.pos()) instanceof ContainerBlockEntity machine) {
                keptItems.addAll(machine.ghosts().values());
            }
            endpoints.add(new Endpoint(link, serverLevel.getCapability(Capabilities.Item.BLOCK, link.pos(), link.face()),
                    serverLevel.getCapability(Capabilities.Fluid.BLOCK, link.pos(), link.face()), keptItems, keptFluids));
        }
        List<Neighbour> neighbours = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos target = worldPosition.relative(direction);
            if (linked.contains(target) || !serverLevel.isLoaded(target) || serverLevel.getBlockEntity(target) instanceof DistributorBlockEntity) {
                continue;
            }
            neighbours.add(new Neighbour(serverLevel.getCapability(Capabilities.Item.BLOCK, target, direction.getOpposite()),
                    serverLevel.getCapability(Capabilities.Fluid.BLOCK, target, direction.getOpposite())));
        }

        for (Endpoint endpoint : endpoints) {
            supply(serverLevel, endpoint, endpoints, neighbours);
        }
        for (Endpoint endpoint : endpoints) {
            for (Neighbour neighbour : neighbours) {
                move(endpoint.items(), neighbour.items(), resource -> !endpoint.keeps(resource), Integer.MAX_VALUE);
                move(endpoint.fluids(), neighbour.fluids(), resource -> !endpoint.keptFluids().contains(resource.getFluid()), Integer.MAX_VALUE);
            }
        }
    }

    private void supply(ServerLevel serverLevel, Endpoint endpoint, List<Endpoint> endpoints, List<Neighbour> neighbours) {
        DistributorLink link = endpoint.link();
        if (serverLevel.getBlockEntity(link.pos()) instanceof ContainerBlockEntity machine && machine.getItemStackHandler() != null) {
            for (Int2ObjectMap.Entry<ItemStack> ghost : machine.ghosts().int2ObjectEntrySet()) {
                ResourceHandler<ItemResource> slot = new SingleSlotHandler<>(machine.getItemHandler(), ghost.getIntKey());
                ItemStack template = ghost.getValue();
                fillItems(slot, resource -> ItemTemplates.matches(template, resource), Integer.MAX_VALUE, endpoint, endpoints, neighbours);
            }
        }
        if (endpoint.items() != null) {
            for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
                ItemStack template = link.item(i);
                if (template.isEmpty()) {
                    continue;
                }
                Predicate<ItemResource> wanted = resource -> ItemTemplates.matches(template, resource);
                long have = count(endpoint.items(), wanted);
                int need = (int) Math.max(0, link.itemAmount(i) - have);
                if (need > 0) {
                    fillItems(endpoint.items(), wanted, need, endpoint, endpoints, neighbours);
                }
            }
        }
        if (endpoint.fluids() != null) {
            for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
                FluidStack template = link.fluid(i);
                if (template.isEmpty()) {
                    continue;
                }
                Fluid fluid = template.getFluid();
                long have = count(endpoint.fluids(), resource -> resource.getFluid() == fluid);
                int need = (int) Math.max(0, link.fluidAmount(i) - have);
                if (need <= 0) {
                    continue;
                }
                Predicate<FluidResource> filter = resource -> resource.getFluid() == fluid;
                for (Endpoint other : endpoints) {
                    if (other != endpoint && need > 0 && !other.keptFluids().contains(fluid)) {
                        need -= move(other.fluids(), endpoint.fluids(), filter, need);
                    }
                }
                for (Neighbour neighbour : neighbours) {
                    if (need > 0) {
                        need -= move(neighbour.fluids(), endpoint.fluids(), filter, need);
                    }
                }
            }
        }
    }

    private static void fillItems(ResourceHandler<ItemResource> destination, Predicate<ItemResource> filter, int need, Endpoint self, List<Endpoint> endpoints,
                                  List<Neighbour> neighbours) {
        for (Endpoint other : endpoints) {
            if (other != self && need > 0) {
                need -= move(other.items(), destination, resource -> filter.test(resource) && !other.keeps(resource), need);
            }
        }
        for (Neighbour neighbour : neighbours) {
            if (need > 0) {
                need -= move(neighbour.items(), destination, filter, need);
            }
        }
    }

    private static <R extends Resource> long count(ResourceHandler<R> handler, Predicate<R> filter) {
        long total = 0;
        for (int i = 0; i < handler.size(); i++) {
            R resource = handler.getResource(i);
            if (!resource.isEmpty() && filter.test(resource)) {
                total += handler.getAmountAsLong(i);
            }
        }
        return total;
    }

    private static <R extends Resource> int move(@Nullable ResourceHandler<R> from, @Nullable ResourceHandler<R> to, Predicate<R> filter, int amount) {
        if (from == null || to == null || from == to || amount <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int moved = ResourceHandlerUtil.moveStacking(from, to, filter, amount, tx);
            tx.commit();
            return moved;
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DistributorMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        ValueOutput.ValueOutputList list = out.childrenList("links");
        for (DistributorLink link : links) {
            link.save(list.addChild());
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        links.clear();
        for (ValueInput child : in.childrenListOrEmpty("links")) {
            links.add(DistributorLink.load(child));
        }
    }
}
