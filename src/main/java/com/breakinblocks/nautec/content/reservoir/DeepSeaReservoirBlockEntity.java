package com.breakinblocks.nautec.content.reservoir;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.content.menus.DeepSeaReservoirMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import com.breakinblocks.nautec.api.blockentities.NTBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeepSeaReservoirBlockEntity extends NTBlockEntity implements MenuProvider {
    private final ReservoirTank tank = new ReservoirTank(this::capacity, this::contentsChanged);
    private boolean syncPending;
    private long lastSync;

    public DeepSeaReservoirBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.DEEP_SEA_RESERVOIR.get(), pos, state);
    }

    public static long capacityFor(int tier) {
        long[] capacities = NTConfig.deepSeaReservoirCapacity;
        int index = Math.clamp(tier, 1, capacities.length) - 1;
        return Math.max(0, capacities[index]) * 1000L;
    }

    public ReservoirTank tank() {
        return tank;
    }

    public int tier() {
        BlockState state = getBlockState();
        return state.hasProperty(DeepSeaReservoirBlock.TIER) ? state.getValue(DeepSeaReservoirBlock.TIER) : 1;
    }

    public long capacity() {
        return capacityFor(tier());
    }

    public long amount() {
        return tank.amount();
    }

    public ReservoirFluid contents() {
        return tank.contents();
    }

    public long excessIfRemoved() {
        int tier = tier();
        return tier <= 1 ? 0 : tank.excessOver(capacityFor(tier - 1));
    }

    public int comparatorSignal() {
        long capacity = capacity();
        long amount = tank.amount();
        if (amount <= 0 || capacity <= 0) {
            return 0;
        }
        return 1 + (int) Math.min(14, amount * 14 / capacity);
    }

    public void upgradeTo(int tier) {
        if (level == null || tier < 1 || tier > ReservoirFluid.MAX_TIER) {
            return;
        }
        BlockState state = getBlockState();
        level.setBlock(worldPosition, state.setValue(DeepSeaReservoirBlock.TIER, tier), Block.UPDATE_ALL);
        setChanged();
        syncNow();
    }

    public long downgrade() {
        int tier = tier();
        if (level == null || tier <= 1) {
            return 0;
        }
        long voided = tank.trimTo(capacityFor(tier - 1));
        upgradeTo(tier - 1);
        return voided;
    }

    private void contentsChanged() {
        setChanged();
        syncPending = true;
    }

    public void serverTick() {
        if (!syncPending || level == null) {
            return;
        }
        long now = level.getGameTime();
        if (now - lastSync >= NTConfig.deepSeaReservoirSyncTicks) {
            syncNow();
        }
    }

    private void syncNow() {
        if (level != null && !level.isClientSide()) {
            syncPending = false;
            lastSync = level.getGameTime();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        ReservoirFluid contents = tank.contents();
        if (!contents.isEmpty()) {
            output.store("fluid", ReservoirFluid.CODEC, contents);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        tank.load(input.read("fluid", ReservoirFluid.CODEC).orElse(ReservoirFluid.EMPTY));
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.@NotNull DataComponentInput components) {
        super.applyImplicitComponents(components);
        ReservoirFluid fluid = components.get(NTDataComponents.RESERVOIR_FLUID.get());
        if (fluid != null) {
            tank.load(fluid);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        super.collectImplicitComponents(components);
        ReservoirFluid contents = tank.contents();
        if (!contents.isEmpty()) {
            components.set(NTDataComponents.RESERVOIR_FLUID.get(), contents);
        }
        if (tier() > 1) {
            components.set(NTDataComponents.RESERVOIR_TIER.get(), tier());
        }
    }

    @Override
    public void removeComponentsFromTag(@NotNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("fluid");
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DeepSeaReservoirMenu(containerId, inventory, this);
    }
}
