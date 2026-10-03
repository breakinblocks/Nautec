package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.ColonyReplicatorBlockEntity;
import com.breakinblocks.nautec.content.resonance.ResonancePylonBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.NotNull;

public class ColonyReplicatorMenu extends NTMachineMenu<ColonyReplicatorBlockEntity> {
    public static final int TEMPLATE_X = 26;
    public static final int TEMPLATE_Y = 18;
    public static final int PARTNER_X = 26;
    public static final int PARTNER_Y = 40;
    public static final int FODDER_X = 64;
    public static final int FODDER_Y = 40;
    public static final int RESULT_X = 134;
    public static final int RESULT_Y = 27;

    private final ContainerData data;

    public ColonyReplicatorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (ColonyReplicatorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ColonyReplicatorBlockEntity.DATA_COUNT));
    }

    public ColonyReplicatorMenu(int containerId, @NotNull Inventory inv, @NotNull ColonyReplicatorBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private ColonyReplicatorMenu(int containerId, Inventory inv, ColonyReplicatorBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.COLONY_REPLICATOR.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.TEMPLATE, TEMPLATE_X, TEMPLATE_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.PARTNER, PARTNER_X, PARTNER_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.FODDER, FODDER_X, FODDER_Y));
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), ColonyReplicatorBlockEntity.RESULT, RESULT_X, RESULT_Y));
        addDishPort(ColonyReplicatorBlockEntity.DISH_IN, 24, 61, ColonyReplicatorBlockEntity.DISH_OUT, 46, 61);
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }

    public int getProgress() {
        return data.get(ColonyReplicatorBlockEntity.DATA_PROGRESS);
    }

    public int getDuration() {
        return Math.max(1, data.get(ColonyReplicatorBlockEntity.DATA_DURATION));
    }

    public int getStatus() {
        return data.get(ColonyReplicatorBlockEntity.DATA_STATUS);
    }

    public boolean isSplice() {
        return data.get(ColonyReplicatorBlockEntity.DATA_SPLICE) == 1;
    }

    public int getBiomass() {
        return ResonancePylonBlockEntity.join(data.get(ColonyReplicatorBlockEntity.DATA_BIOMASS), data.get(ColonyReplicatorBlockEntity.DATA_BIOMASS + 1));
    }
}
