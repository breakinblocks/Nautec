package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.BacterialEnrichmentCatalystBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.NotNull;

public class BacterialEnrichmentCatalystMenu extends NTMachineMenu<BacterialEnrichmentCatalystBlockEntity> {
    public static final int INVENTORY_Y = 158;
    public static final int COLONY_X = 26;
    public static final int COLONY_Y = 104;
    public static final int DISH_Y = 126;
    public static final int DISH_IN_X = 8;
    public static final int DISH_OUT_X = 26;
    public static final int DISH_EMPTY_X = 44;

    private final ContainerData data;

    public BacterialEnrichmentCatalystMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (BacterialEnrichmentCatalystBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(BacterialEnrichmentCatalystBlockEntity.DATA_COUNT));
    }

    public BacterialEnrichmentCatalystMenu(int containerId, @NotNull Inventory inv, @NotNull BacterialEnrichmentCatalystBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private BacterialEnrichmentCatalystMenu(int containerId, Inventory inv, BacterialEnrichmentCatalystBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.BACTERIAL_ENRICHMENT_CATALYST.get(), containerId, inv, blockEntity, INVENTORY_Y);
        this.data = data;
        addDataSlots(data);
        addBacteriaStorageSlot(new SlotBacteriaStorage(blockEntity.getBacteriaStorage(), 0, COLONY_X, COLONY_Y));
        addDishPort(BacterialEnrichmentCatalystBlockEntity.DISH_IN, DISH_IN_X, DISH_Y,
                BacterialEnrichmentCatalystBlockEntity.DISH_OUT, DISH_OUT_X, DISH_Y,
                BacterialEnrichmentCatalystBlockEntity.DISH_EMPTY_OUT, DISH_EMPTY_X, DISH_Y);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!player.level().isClientSide()) {
            blockEntity.pressButton(id);
        }
        return true;
    }

    @Override
    protected int getMergeableSlotCount() {
        return 1;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getStatus() {
        return data.get(BacterialEnrichmentCatalystBlockEntity.DATA_STATUS);
    }

    public int getProgress() {
        return wide(BacterialEnrichmentCatalystBlockEntity.DATA_PROGRESS);
    }

    public int getCycleAp() {
        return wide(BacterialEnrichmentCatalystBlockEntity.DATA_CYCLE_AP);
    }

    public int getPower() {
        return wide(BacterialEnrichmentCatalystBlockEntity.DATA_POWER);
    }

    public float getCrystalPurity() {
        return data.get(BacterialEnrichmentCatalystBlockEntity.DATA_CRYSTAL_PURITY) / 100F;
    }

    public float getSourcePurity() {
        return data.get(BacterialEnrichmentCatalystBlockEntity.DATA_SOURCE_PURITY) / 100F;
    }

    public float getTargetPurity() {
        return data.get(BacterialEnrichmentCatalystBlockEntity.DATA_TARGET_PURITY) / 100F;
    }

    public float getRegressionChance() {
        return data.get(BacterialEnrichmentCatalystBlockEntity.DATA_CHANCE) / 1000F;
    }

    public int getColonyCost() {
        return wide(BacterialEnrichmentCatalystBlockEntity.DATA_COLONY_COST);
    }
}
