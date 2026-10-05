package com.breakinblocks.nautec.content.dishstorage;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DishStorageBlockEntity extends ContainerBlockEntity implements MenuProvider {
    private final int capacity;

    public DishStorageBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.DISH_STORAGE.get(), pos, state);
        this.capacity = state.getBlock() instanceof DishStorageBlock storage ? storage.getCapacity() : 48;
        addItemHandler(capacity, 1, (slot, stack) -> stack.getItem() instanceof PetriDishItem);
    }

    public int getCapacity() {
        return capacity;
    }

    public int storedCount() {
        int count = 0;
        for (int slot = 0; slot < capacity; slot++) {
            if (!getItemStackHandler().getStackInSlot(slot).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public int comparatorSignal() {
        int stored = storedCount();
        return stored == 0 ? 0 : 1 + stored * 14 / capacity;
    }

    @Override
    public void update() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        return getItemHandler();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drop();
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
        return new DishStorageMenu(containerId, inventory, this);
    }
}
