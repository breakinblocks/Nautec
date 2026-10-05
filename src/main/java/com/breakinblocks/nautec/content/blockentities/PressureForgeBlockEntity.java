package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.PressureForgingRecipeInput;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.utils.RecipeRevision;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PressureForgeBlockEntity extends LaserBlockEntity {
    private static final int WORK_PERIOD = 40;

    public enum Synthesizer implements StringRepresentable {
        NONE,
        BASIC,
        ATLANTEAN;

        public static final StringRepresentable.EnumCodec<Synthesizer> CODEC = StringRepresentable.fromEnum(Synthesizer::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public ItemStack stack() {
            return switch (this) {
                case BASIC -> new ItemStack(NTItems.PRESSURE_SYNTHESIZER.get());
                case ATLANTEAN -> new ItemStack(NTItems.ATLANTEAN_PRESSURE_SYNTHESIZER.get());
                case NONE -> ItemStack.EMPTY;
            };
        }
    }

    private static final int PRESSURE_CHECK_INTERVAL = 20;

    private int progress;
    private Synthesizer synthesizer = Synthesizer.NONE;
    private final RecipeRevision recipeRevision = new RecipeRevision();
    private boolean recipeDirty = true;
    private float cachedPurity = Float.NaN;
    private @Nullable PressureForgingRecipe cachedRecipe;
    private boolean pressureKnown;
    private boolean pressurised;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0}, new int[]{1});
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[]{0}, new int[0]);

    public PressureForgeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.PRESSURE_FORGE.get(), blockPos, blockState);
        addItemHandler(2);
        addFluidTank(NTConfig.pressureForgeCapacity,
                fluidStack -> fluidStack.getFluid() == NTFluids.ETCHING_ACID.getStillFluid());
    }

    public int getProgress() {
        return progress;
    }

    public Synthesizer getSynthesizer() {
        return synthesizer;
    }

    public ItemStack setSynthesizer(Synthesizer synthesizer) {
        ItemStack previous = this.synthesizer.stack();
        this.synthesizer = synthesizer;
        this.progress = 0;
        this.pressureKnown = false;
        this.recipeDirty = true;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return previous;
    }

    public static boolean hasPressure(Level level, BlockPos pos) {
        return hasPressure(level, pos, Synthesizer.NONE);
    }

    public static boolean hasPressure(Level level, BlockPos pos, Synthesizer synthesizer) {
        if (synthesizer == Synthesizer.ATLANTEAN) {
            return true;
        }
        if (pos.getY() > NTConfig.pressureForgeDepth) {
            return false;
        }
        if (synthesizer == Synthesizer.BASIC) {
            return true;
        }

        int required = NTConfig.pressureForgeWaterColumn;
        for (int i = 1; i <= required; i++) {
            BlockPos above = pos.above(i);
            if (level.isOutsideBuildHeight(above) || (!level.getFluidState(above).is(FluidTags.WATER) || !level.getFluidState(above).isSource())) {
                return false;
            }
        }
        return true;
    }

    public boolean isPressurised() {
        return hasPressure(level, worldPosition, synthesizer);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        ItemStack fitted = synthesizer.stack();
        if (!fitted.isEmpty() && level != null) {
            this.synthesizer = Synthesizer.NONE;
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), fitted);
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        PressureForgingRecipe recipe = currentRecipe(serverLevel);
        if (recipe == null || getPower() < NTConfig.pressureForgePowerUsage
                || getFluidTank().getFluidAmount() < NTConfig.pressureForgeAcidUsage) {
            this.progress = 0;
            return;
        }

        if (this.progress < recipe.duration()) {
            this.progress++;
            MachineSounds.interval(serverLevel, worldPosition, NTSounds.PRESSURE_FORGE_WORK, WORK_PERIOD, 0.5f, 0.7f);
            return;
        }

        this.progress = 0;
        forge(recipe);
    }

    private void forge(PressureForgingRecipe recipe) {
        ItemStack result = recipe.result();
        if (!getItemStackHandler().insertItem(1, result, true).isEmpty()) {
            return;
        }

        getItemStackHandler().extractItem(0, 1, false);
        getItemStackHandler().insertItem(1, result, false);
        getFluidTank().drain(NTConfig.pressureForgeAcidUsage);
        MachineSounds.play(level, worldPosition, NTSounds.PRESSURE_FORGE_COMPLETE, 0.8f, 0.9f);
    }

    private @Nullable PressureForgingRecipe currentRecipe(ServerLevel level) {
        ItemStack input = getItemStackHandler().getStackInSlot(0);
        if (input.isEmpty()) {
            return null;
        }
        if (!pressureKnown || level.getGameTime() % PRESSURE_CHECK_INTERVAL == 0) {
            pressureKnown = true;
            pressurised = isPressurised();
        }
        if (!pressurised) {
            return null;
        }
        float purity = getPurity();
        if (recipeRevision.changed(level) || recipeDirty || purity != cachedPurity) {
            recipeDirty = false;
            cachedPurity = purity;
            cachedRecipe = level.recipeAccess()
                    .getRecipeFor(PressureForgingRecipe.Type.INSTANCE,
                            new PressureForgingRecipeInput(input, purity,
                                    synthesizer == Synthesizer.ATLANTEAN ? Integer.MIN_VALUE : worldPosition.getY()), level)
                    .map(RecipeHolder::value)
                    .orElse(null);
        }
        return cachedRecipe;
    }

    @Override
    protected void onItemsChanged(int slot) {
        super.onItemsChanged(slot);
        if (slot == 0) {
            recipeDirty = true;
        }
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
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
        this.synthesizer = in.read("synthesizer", Synthesizer.CODEC).orElse(Synthesizer.NONE);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", this.progress);
        out.store("synthesizer", Synthesizer.CODEC, this.synthesizer);
    }
}
