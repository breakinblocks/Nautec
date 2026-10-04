package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionStructure;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FusionControllerMenu extends NTAbstractContainerMenu<FusionControllerBlockEntity> {
    private static final double REACH = 16.0;

    private final ContainerData data;

    public FusionControllerMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (FusionControllerBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(FusionControllerBlockEntity.DATA_COUNT));
    }

    public FusionControllerMenu(int containerId, @NotNull Inventory inv, @NotNull FusionControllerBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private FusionControllerMenu(int containerId, Inventory inv, FusionControllerBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.FUSION_CONTROLLER.get(), containerId, inv, blockEntity);
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

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved()
                && player.level() == blockEntity.getLevel()
                && player.distanceToSqr(blockEntity.getBlockPos().getCenter()) <= REACH * REACH;
    }

    private int wide(int index) {
        return FusionControllerBlockEntity.join(data.get(index), data.get(index + 1));
    }

    public FusionControllerBlockEntity.Status getStatus() {
        return FusionControllerBlockEntity.Status.byId(data.get(FusionControllerBlockEntity.DATA_STATUS));
    }

    public FusionStructure.Problem getProblem() {
        return FusionStructure.Problem.byId(data.get(FusionControllerBlockEntity.DATA_PROBLEM));
    }

    public float getHeat() {
        return data.get(FusionControllerBlockEntity.DATA_HEAT) / (float) FusionControllerBlockEntity.HEAT_STEPS;
    }

    public int getOutput() {
        return wide(FusionControllerBlockEntity.DATA_OUTPUT);
    }

    public int getInjected() {
        return wide(FusionControllerBlockEntity.DATA_INJECTED);
    }

    public float getPurity() {
        return data.get(FusionControllerBlockEntity.DATA_PURITY) / 100F;
    }

    public int getCeiling() {
        return wide(FusionControllerBlockEntity.DATA_CEILING);
    }

    public int getFuel() {
        return wide(FusionControllerBlockEntity.DATA_FUEL);
    }

    public int getEnergy() {
        return wide(FusionControllerBlockEntity.DATA_ENERGY);
    }

    public int getRadius() {
        return data.get(FusionControllerBlockEntity.DATA_RADIUS);
    }

    public int getInjectors() {
        return data.get(FusionControllerBlockEntity.DATA_INJECTORS);
    }

    public int getCoils() {
        return data.get(FusionControllerBlockEntity.DATA_COILS);
    }

    public int getSatellites() {
        return data.get(FusionControllerBlockEntity.DATA_SATELLITES);
    }
}
