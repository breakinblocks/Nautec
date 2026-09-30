package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.SidedCapUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AquaticCatalystBlockEntity extends LaserBlockEntity {
    private RecipeHolder<AquaticCatalystChannelingRecipe> currentRecipe;
    private RecipeHolder<AquaticCatalystChannelingRecipe> nextRecipe;
    private int duration;
    private Identifier currentRecipeId;
    private Identifier nextRecipeId;
    private int syncedTransfer;

    public AquaticCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.AQUATIC_CATALYST.get(), blockPos, blockState);
        addItemHandler(1, (slot, stack) -> getRecipeForCache(stack) != null);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (level.isClientSide()) {
            transmitPower(isActive() ? syncedTransfer : 0);
            return;
        }

        if (currentRecipe == null) {
            startNextRecipe();
        }

        if (currentRecipe != null) {
            int distance = getLaserDistances().getInt(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
            if (distance > 0) {
                int amount = currentRecipe.value().powerAmount() / currentRecipe.value().duration();
                transmitPower(amount);
                setPurity(currentRecipe.value().purity());
                duration++;
            }
            if (duration >= currentRecipe.value().duration()) {
                duration = 0;
                currentRecipe = null;
                startNextRecipe();
                if (currentRecipe == null) {
                    setPurity(0);
                }
            }
        }

        if (getBlockState().getValue(AquaticCatalystBlock.ACTIVE) != isActive()) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AquaticCatalystBlock.ACTIVE, isActive()));
        }
    }

    private void startNextRecipe() {
        if (nextRecipe == null && !getItemStackHandler().getStackInSlot(0).isEmpty()) {
            nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
        }
        if (nextRecipe != null) {
            currentRecipe = nextRecipe;
            getItemStackHandler().extractItem(0, 1, false);
        }
    }

    public boolean isActive() {
        if (level != null && level.isClientSide()) {
            return getBlockState().getValue(AquaticCatalystBlock.ACTIVE);
        }
        return currentRecipe != null;
    }

    public RecipeHolder<AquaticCatalystChannelingRecipe> getCurrentRecipe() {
        return currentRecipe;
    }

    public int getRemainingDuration() {
        return currentRecipe != null ? currentRecipe.value().duration() - duration : 0;
    }

    public ItemStack getProcessingItem() {
        return getItemStackHandler().getStackInSlot(0);
    }

    public int getDuration() {
        return duration;
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);

        this.nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
        setStage();
    }

    @Override
    protected void onLaserDistancesChanged(Direction direction, int prevDistance) {
        super.onLaserDistancesChanged(direction, prevDistance);

        this.nextRecipe = getRecipeForCache(getItemStackHandler().getStackInSlot(0));
    }

    public void setStage() {
        float i = (float) getItemStackHandler().getStackInSlot(0).getCount() / getItemStackHandler().getSlotLimit(0);
        int stage = Mth.ceil(i * 8);
        level.setBlockAndUpdate(worldPosition, getBlockState()
                .setValue(AquaticCatalystBlock.STAGE, stage));
    }

    public RecipeHolder<AquaticCatalystChannelingRecipe> getRecipeForCache(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.recipeAccess()
                .getRecipeFor(AquaticCatalystChannelingRecipe.Type.INSTANCE, new SingleRecipeInput(stack), level)
                .orElse(null);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return Set.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        if (isActive()) {
            return Set.of(getBlockState().getValue(BlockStateProperties.FACING).getOpposite());
        }
        return Collections.emptySet();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return SidedCapUtils.allInsert(0);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);

        this.duration = in.getIntOr("duration", 0);
        this.currentRecipeId = in.getString("current_recipe").map(Identifier::parse).orElse(null);
        this.nextRecipeId = in.getString("next_recipe").map(Identifier::parse).orElse(null);
        this.syncedTransfer = in.getIntOr("transfer", 0);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        this.currentRecipe = loadRecipe(currentRecipeId);
        this.nextRecipe = loadRecipe(nextRecipeId);
    }

    private RecipeHolder<AquaticCatalystChannelingRecipe> loadRecipe(Identifier location) {
        if (location == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return (RecipeHolder<AquaticCatalystChannelingRecipe>) serverLevel.recipeAccess()
                .byKey(ResourceKey.create(Registries.RECIPE, location))
                .orElse(null);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);

        out.putInt("duration", duration);
        if (currentRecipe != null) {
            out.putString("current_recipe", currentRecipe.id().identifier().toString());
            out.putInt("transfer", currentRecipe.value().powerAmount() / currentRecipe.value().duration());
        }
        if (nextRecipe != null) out.putString("next_recipe", nextRecipe.id().identifier().toString());
    }
}
