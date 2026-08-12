package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.menus.IncubatorMenu;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.RNGUtils;
import com.breakinblocks.nautec.utils.ranges.IntRange;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class IncubatorBlockEntity extends LaserBlockEntity implements MenuProvider {
    private BacteriaIncubationRecipe recipe;
    private boolean active;
    private int progress;

    public IncubatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.INCUBATOR.get(), blockPos, blockState);
        addItemHandler(1, 1);
        addBacteriaStorage(1);
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

    @Override
    public void onBacteriaChanged(int slot) {
        super.onBacteriaChanged(slot);
        checkRecipe();
    }

    private void checkRecipe() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack stack = getItemStackHandler().getStackInSlot(0);
        BacteriaInstance instance = getBacteriaStorage().getBacteria(0);
        this.recipe = instance.getSize() < NTConfig.bacteriaColonySizeCap
                ? serverLevel.recipeAccess().getRecipeFor(BacteriaIncubationRecipe.TYPE, new BacteriaRecipeInput(instance, stack), level).map(RecipeHolder::value).orElse(null)
                : null;

        if (this.active != (this.recipe != null)) {
            this.active = this.recipe != null;
            update();
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();

        boolean canRun = level.isClientSide() ? this.active : this.recipe != null;

        if (canRun) {
            if (getPower() >= NTConfig.incubatorPowerUsage) {
                if (progress >= NTConfig.incubatorCraftingSpeed) {
                    if (!level.isClientSide()) {
                        grow();
                    }

                    progress = 0;
                } else {
                    progress++;
                }
            }
        } else {
            progress = 0;
        }
    }

    private void grow() {
        IntRange growth = recipe.growth();
        if (level.getRandom().nextFloat() < recipe.consumeChance()) {
            getItemStackHandler().extractItem(0, 1, false);
        }

        BacteriaInstance bacteria = getBacteriaStorage().getBacteria(0);
        long rolled = Math.round(RNGUtils.uniformRandInt(growth) * (double) bacteria.getStats().growthRate());
        long headroom = NTConfig.bacteriaColonySizeCap - bacteria.getSize();
        long grown = Math.max(0, Math.min(rolled, headroom));

        bacteria.setSize(bacteria.getSize() + grown);
        bacteria.setAge(0);
        getBacteriaStorage().onBacteriaChanged(0);
    }

    public boolean isActive() {
        return active;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.UP, Direction.DOWN);
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
        return Component.literal("Incubator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IncubatorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
        this.active = in.getBooleanOr("active", false);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
        out.putBoolean("active", this.active);
    }
}
