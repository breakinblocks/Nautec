package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.resonance.ResonancePylonBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ResonancePylonMenu extends NTAbstractContainerMenu<ResonancePylonBlockEntity> {
    private final ContainerData data;

    public ResonancePylonMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ResonancePylonBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ResonancePylonBlockEntity.DATA_COUNT));
    }

    public ResonancePylonMenu(int containerId, @NotNull Inventory inv, @NotNull ResonancePylonBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ResonancePylonMenu(int containerId, Inventory inv, ResonancePylonBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.RESONANCE_PYLON.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private int wide(int index) {
        return ResonancePylonBlockEntity.join(data.get(index), data.get(index + 1));
    }

    public int getEnergy() {
        return wide(ResonancePylonBlockEntity.DATA_ENERGY);
    }

    public int getCapacity() {
        return wide(ResonancePylonBlockEntity.DATA_CAPACITY);
    }

    public int getFlow() {
        return wide(ResonancePylonBlockEntity.DATA_FLOW);
    }

    public boolean isSendMode() {
        return data.get(ResonancePylonBlockEntity.DATA_MODE) == 0;
    }

    public int getChunkState() {
        return data.get(ResonancePylonBlockEntity.DATA_CHUNK);
    }

    public boolean isInterdimensional() {
        return data.get(ResonancePylonBlockEntity.DATA_TIER) == 1;
    }
}
