package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.utils.BeamOverclock;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.menus.BacterialAnalyzerMenu;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BacterialAnalyzerBlockEntity extends LaserBlockEntity implements MenuProvider {
    private final BeamOverclock overclock = new BeamOverclock();

    @Override
    public int getRequiredPower() {
        return NTConfig.bacteriaAnalyzerPowerUsage;
    }

    private boolean hasRecipe;
    private int progress;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0}, new int[]{1});

    public BacterialAnalyzerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BACTERIAL_ANALYZER.get(), blockPos, blockState);
        addItemHandler(2, 1, (slot, stack) -> slot == 0 && AdvancedBacterialAnalyzerBlockEntity.needsAnalysis(stack));
    }

    @Override
    public void onLoad() {
        super.onLoad();

        checkRecipe();
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);

        checkRecipe();
    }

    private void checkRecipe() {
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
                    ItemStack result = getItemStackHandler().getStackInSlot(0).copyWithCount(1);
                    IBacteriaStorage storage = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
                    if (storage == null) {
                        this.hasRecipe = false;
                        this.progress = 0;
                        return;
                    }

                    getItemStackHandler().extractItem(0, 1, false);
                    BacteriaInstance analyzed = storage.getBacteria(0);
                    analyzed.setAnalyzed(true);
                    storage.setBacteria(0, analyzed);

                    forceInsertItem(1, result, false);

                    this.progress = 0;
                } else {
                    progress = Math.min(NTConfig.bacteriaAnalyzerCraftingSpeed, progress + overclock.advance(beamSpeed()));
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
    public boolean ghostMatches(int slot, ItemStack ghost, ItemResource resource) {
        return AdvancedBacterialAnalyzerBlockEntity.analysisGhostMatches(ghost, resource);
    }

    @Override
    protected boolean acceptsGhost(int slot, ItemStack stack) {
        return slot == 0 && DishPort.isDish(stack);
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
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
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BacterialAnalyzerMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
    }
}
