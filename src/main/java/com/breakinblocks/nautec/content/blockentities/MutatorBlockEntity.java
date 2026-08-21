package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.menus.MutatorMenu;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

public class MutatorBlockEntity extends LaserBlockEntity implements MenuProvider {
    private BacteriaMutationRecipe recipe;
    private boolean active;
    private int progress;

    public MutatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.MUTATOR.get(), blockPos, blockState);
        addBacteriaStorage(2);
        addItemHandler(1);
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
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ItemStack catalyst = getItemStackHandler().getStackInSlot(0);
        BacteriaInstance inputBacteria = getBacteriaStorage().getBacteria(0);
        BacteriaInstance resultBacteria = getBacteriaStorage().getBacteria(1);
        BacteriaMutationRecipe recipe1 = serverLevel.recipeAccess().getRecipeFor(BacteriaMutationRecipe.TYPE, new BacteriaRecipeInput(inputBacteria, catalyst), level).map(RecipeHolder::value).orElse(null);
        this.recipe = (recipe1 != null && resultBacteria.isEmpty()) ? recipe1 : null;

        if (this.active != (this.recipe != null)) {
            this.active = this.recipe != null;
            update();
        }
    }

    @Override
    public void onBacteriaChanged(int slot) {
        super.onBacteriaChanged(slot);

        checkRecipe();
    }

    public static float computeSuccessChance(BacteriaMutationRecipe recipe, BacteriaInstance input) {
        long cap = Math.max(1, NTConfig.bacteriaColonySizeCap);
        float sizeFactor = 1f - 0.25f * ((float) Math.min(input.getSize(), cap) / cap);
        float resistanceFactor = NTConfig.bacteriaMutationResistanceCap <= 0
                ? 1f
                : 1f - 0.5f * (input.getStats().mutationResistance() / NTConfig.bacteriaMutationResistanceCap);

        return Math.max(0f, (recipe.chance() / 100f) * resistanceFactor * sizeFactor);
    }

    public static long computeFailureShrink(BacteriaInstance input) {
        float resistanceFactor = NTConfig.bacteriaMutationResistanceCap <= 0
                ? 1f
                : 1f - input.getStats().mutationResistance() / NTConfig.bacteriaMutationResistanceCap;
        long shrink = (long) Math.ceil(input.getSize() * NTConfig.mutatorFailureShrink * Math.max(0f, resistanceFactor));

        return Math.max(0, Math.min(shrink, input.getSize() - 1));
    }

    @Override
    public void commonTick() {
        super.commonTick();

        boolean canRun = level.isClientSide() ? this.active : this.recipe != null;

        if (canRun) {
            if (getPower() >= NTConfig.mutatorPowerUsage) {
                if (progress >= NTConfig.mutatorCraftingSpeed) {
                    if (!level.isClientSide()) {
                        mutate();
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

    private void mutate() {
        BacteriaInstance inputBacteria = getBacteriaStorage().getBacteria(0);

        if (level.getRandom().nextFloat() < computeSuccessChance(recipe, inputBacteria)) {
            BacteriaInstance result = buildResult(recipe.resultBacteria(), inputBacteria);
            getBacteriaStorage().extractBacteria(0, inputBacteria.getSize(), false);
            getBacteriaStorage().insertBacteria(1, result, false);
        } else {
            getBacteriaStorage().extractBacteria(0, computeFailureShrink(inputBacteria), false);
        }
    }

    private BacteriaInstance buildResult(ResourceKey<Bacteria> resultBacteria, BacteriaInstance input) {
        Bacteria result = BacteriaHelper.getBacteria(level.registryAccess(), resultBacteria);

        if (input.getStats() instanceof SimpleCollapsedStats simpleStats && result.stats() instanceof SimpleBacteriaStats resultBase) {
            SimpleCollapsedStats drift = simpleStats.rollStats();
            return new BacteriaInstance(
                    resultBacteria,
                    result.rollSize(),
                    new SimpleCollapsedStats(resultBase, drift.growthRate(), drift.mutationResistance(), drift.productionRate(), drift.lifespan(), resultBase.color()),
                    false,
                    0
            );
        }

        return BacteriaInstance.roll(resultBacteria, level.registryAccess());
    }

    public int getProgress() {
        return progress;
    }

    public boolean isActive() {
        return active;
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
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MutatorMenu(containerId, playerInventory, this);
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
