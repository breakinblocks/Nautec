package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.ResonanceRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.SidedCapUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResonanceChamberBlockEntity extends LaserBlockEntity {
    private float charge;
    private int ventCooldown;

    public ResonanceChamberBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.RESONANCE_CHAMBER.get(), blockPos, blockState);
        addItemHandler(2);
    }

    public float getCharge() {
        return charge;
    }

    public int getVentCooldown() {
        return ventCooldown;
    }

    public boolean isVenting() {
        return ventCooldown > 0;
    }

    public float getStabilityCeiling() {
        return stabilityCeiling(getPurity());
    }

    public static float stabilityCeiling(float purity) {
        return (float) (NTConfig.resonanceBaseCeiling * (1.0 + Math.max(0f, purity)));
    }

    public float getChargeFraction() {
        float ceiling = getStabilityCeiling();
        return ceiling <= 0 ? 0 : charge / ceiling;
    }

    public boolean isCritical() {
        float fraction = getChargeFraction();
        return fraction >= NTConfig.resonanceCriticalLow && fraction <= NTConfig.resonanceCriticalHigh;
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (ventCooldown > 0) {
            ventCooldown--;
            this.charge = 0;
            return;
        }

        if (getPower() < NTConfig.resonancePowerUsage) {
            this.charge = Math.max(0f, this.charge - (float) NTConfig.resonanceBaseCeiling * 0.01f);
            return;
        }

        this.charge += getPower();

        if (level.isClientSide()) {
            return;
        }

        if (isCritical() && tryCraft()) {
            this.charge = 0;
            return;
        }

        if (getChargeFraction() > NTConfig.resonanceCriticalHigh) {
            vent();
        }
    }

    private boolean tryCraft() {
        ItemStack input = getItemStackHandler().getStackInSlot(0);
        if (input.isEmpty()) {
            return false;
        }

        ResonanceCraftingRecipe recipe = findRecipe(input);
        if (recipe == null) {
            return false;
        }

        ItemStack result = recipe.result();
        ItemStack remainder = getItemStackHandler().insertItem(1, result, true);
        if (!remainder.isEmpty()) {
            return false;
        }

        getItemStackHandler().extractItem(0, 1, false);
        getItemStackHandler().insertItem(1, result, false);
        return true;
    }

    private @Nullable ResonanceCraftingRecipe findRecipe(ItemStack input) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.recipeAccess()
                .getRecipeFor(ResonanceCraftingRecipe.Type.INSTANCE, new ResonanceRecipeInput(input, getPurity()), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private void vent() {
        this.charge = 0;
        this.ventCooldown = NTConfig.resonanceVentCooldown;

        if (level.isClientSide()) {
            return;
        }

        double radius = NTConfig.resonanceVentRadius;
        AABB box = new AABB(worldPosition).inflate(radius);
        List<LivingEntity> caught = level.getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity entity : caught) {
            entity.hurt(level.damageSources().magic(), (float) NTConfig.resonanceVentDamage);
        }
        update();
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return SidedCapUtils.allInsert(0);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.charge = in.getFloatOr("charge", 0f);
        this.ventCooldown = in.getIntOr("vent_cooldown", 0);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putFloat("charge", this.charge);
        out.putInt("vent_cooldown", this.ventCooldown);
    }
}
