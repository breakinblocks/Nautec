package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.client.screen.FluidTankRenderer;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.api.menu.slots.SlotFluidHandler;
import com.breakinblocks.nautec.capabilities.fluid.FluidTank;
import com.breakinblocks.nautec.content.blockentities.OsmosisFilterBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class OsmosisFilterMenu extends NTMachineMenu<OsmosisFilterBlockEntity> {
    public static final int TANK_X = 26;
    public static final int TANK_Y = 18;
    public static final int TANK_WIDTH = 18;
    public static final int TANK_HEIGHT = 52;

    private final ContainerData data;

    public OsmosisFilterMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (OsmosisFilterBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(OsmosisFilterBlockEntity.DATA_COUNT));
    }

    public OsmosisFilterMenu(int containerId, @NotNull Inventory inv, @NotNull OsmosisFilterBlockEntity blockEntity) {
        this(containerId, inv, blockEntity, blockEntity.getData());
    }

    private OsmosisFilterMenu(int containerId, Inventory inv, OsmosisFilterBlockEntity blockEntity, ContainerData data) {
        super(NTMenuTypes.OSMOSIS_FILTER.get(), containerId, inv, blockEntity);
        this.data = data;
        addDataSlots(data);
        addFluidHandlerSlot(new LimitedTankSlot(blockEntity.getFluidTank(), TANK_X, TANK_Y, TANK_WIDTH, TANK_HEIGHT, this::getFillLimit));
    }

    @Override
    protected int getMergeableSlotCount() {
        return 0;
    }

    private int wide(int index) {
        return DataSlots.join(data.get(index), data.get(index + 1));
    }

    public int getRate() {
        return wide(OsmosisFilterBlockEntity.DATA_RATE);
    }

    public int getFillLimit() {
        int limit = wide(OsmosisFilterBlockEntity.DATA_LIMIT);
        return limit > 0 ? limit : Math.max(1, NTConfig.osmosisFilterCapacity);
    }

    public int getPower() {
        return wide(OsmosisFilterBlockEntity.DATA_POWER);
    }

    private static final class LimitedTankSlot extends SlotFluidHandler {
        private final IntSupplier limit;
        private FluidTankRenderer renderer;
        private int rendererLimit;

        private LimitedTankSlot(FluidTank tank, int x, int y, int width, int height, IntSupplier limit) {
            super(tank, 0, x, y, width, height);
            this.limit = limit;
        }

        @Override
        public int getFluidCapacity() {
            return Math.max(1, limit.getAsInt());
        }

        @Override
        public FluidTankRenderer getRenderer() {
            int capacity = getFluidCapacity();
            if (renderer == null || rendererLimit != capacity) {
                renderer = new FluidTankRenderer(capacity, true, getWidth(), getHeight());
                rendererLimit = capacity;
            }
            return renderer;
        }
    }
}
