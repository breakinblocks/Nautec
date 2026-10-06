package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.ResonantCisternMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;

public class ResonantCisternBlockEntity extends ResonantStorageBlockEntity {
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction>[] neighbours = new BlockCapabilityCache[6];
    private FluidStack clientFluid = FluidStack.EMPTY;
    private int clientCapacity = CisternStore.capacityFor(0);

    public ResonantCisternBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.RESONANT_CISTERN.get(), pos, state);
    }

    @Override
    protected ResonantStore createStore(ResonantStorage storage, ResonantChannel channel) {
        return storage.cistern(channel);
    }

    public @Nullable CisternStore cistern() {
        return (CisternStore) store();
    }

    public @Nullable ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        if (side != null && !face(side).exposed()) {
            return null;
        }
        return cistern();
    }

    public FluidStack fluid() {
        CisternStore cistern = cistern();
        return cistern != null ? cistern.fluid() : clientFluid;
    }

    public int capacity() {
        CisternStore cistern = cistern();
        return cistern != null ? cistern.capacity() : clientCapacity;
    }

    @Override
    protected void transfer(ServerLevel level, Direction face, FaceMode mode) {
        CisternStore cistern = cistern();
        if (cistern == null) {
            return;
        }
        BlockCapabilityCache<ResourceHandler<FluidResource>, @Nullable Direction> cache = neighbours[face.ordinal()];
        if (cache == null) {
            cache = BlockCapabilityCache.create(Capabilities.Fluid.BLOCK, level, worldPosition.relative(face), face.getOpposite());
            neighbours[face.ordinal()] = cache;
        }
        ResourceHandler<FluidResource> neighbour = cache.getCapability();
        if (neighbour == null) {
            return;
        }
        BlockEntity other = level.getBlockEntity(worldPosition.relative(face));
        if (other != null && sameChannel(other)) {
            return;
        }
        int amount = NTConfig.resonantFluidPerTransfer;
        if (mode == FaceMode.PUSH) {
            ResourceHandlerUtil.move(cistern, neighbour, resource -> true, amount, null);
        } else {
            ResourceHandlerUtil.move(neighbour, cistern, resource -> true, amount, null);
        }
    }

    @Override
    protected void writeClient(CompoundTag tag, HolderLookup.Provider registries) {
        tag.store("client_fluid", FluidStack.OPTIONAL_CODEC, registries.createSerializationContext(NbtOps.INSTANCE), fluid());
        tag.putInt("client_capacity", capacity());
    }

    @Override
    protected void readClient(ValueInput input) {
        clientFluid = input.read("client_fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY);
        clientCapacity = input.getIntOr("client_capacity", CisternStore.capacityFor(0));
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResonantCisternMenu(containerId, inventory, this);
    }
}
