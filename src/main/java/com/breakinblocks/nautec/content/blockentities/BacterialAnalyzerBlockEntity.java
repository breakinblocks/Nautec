package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.bacteria.BacteriaStats;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.items.PetriDishItem;
import com.breakinblocks.nautec.content.menus.BacterialAnalyzerMenu;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BacterialAnalyzerBlockEntity extends LaserBlockEntity implements MenuProvider {
    private boolean hasRecipe;
    private int progress;

    public BacterialAnalyzerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BACTERIAL_ANALYZER.get(), blockPos, blockState);
        addItemHandler(2, 1, (slot, stack) -> (slot == 0 && stack.getItem() instanceof PetriDishItem));
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);

        ItemStack stack = getItemStackHandler().getStackInSlot(0);
        ItemStack resultStack = getItemStackHandler().getStackInSlot(1);
        IBacteriaStorage iBacteriaStorage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (iBacteriaStorage != null) {
            BacteriaInstance bacteria = iBacteriaStorage.getBacteria(0);
            this.hasRecipe = !bacteria.isEmpty()
                    && !bacteria.isAnalyzed()
                    && resultStack.isEmpty();
        } else {
            this.hasRecipe = false;
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (hasRecipe) {
            if (getPower() >= NTConfig.bacteriaAnalyzerPowerUsage) {
                if (progress >= NTConfig.bacteriaAnalyzerCraftingSpeed) {
                    ItemStack extracted = getItemStackHandler().extractItem(0, 1, false);

                    ItemStack result = extracted.copy();
                    IBacteriaStorage storage = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
                    storage.getBacteria(0).setAnalyzed(true);

                    forceInsertItem(1, result, false);

                } else {
                    progress++;
                }
            }
        } else {
            progress = 0;
        }
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.DOWN);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Bacterial Analyzer");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BacterialAnalyzerMenu(containerId, playerInventory, this);
    }
}
