package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.utils.RecipeRevision;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class IncubatorBlockEntity extends LaserBlockEntity implements MenuProvider {
    public static final int DISH_IN = 1;
    public static final int DISH_OUT = 2;
    public static final int DISH_EMPTY_OUT = 3;
    private static final int[] LOAD_SLOTS = {0};

    private final RecipeRevision recipeRevision = new RecipeRevision();
    private BacteriaIncubationRecipe recipe;
    private boolean active;
    private int progress;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{0, DISH_IN}, new int[]{DISH_OUT, DISH_EMPTY_OUT});

    public IncubatorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.INCUBATOR.get(), blockPos, blockState);
        addItemHandler(4, 1, (slot, stack) -> slot == 0 || (slot == DISH_IN && DishPort.isDish(stack)));
        addBacteriaStorage(1);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel server) recipeRevision.changed(server);
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
        this.recipe = serverLevel.recipeAccess().getRecipeFor(BacteriaIncubationRecipe.TYPE, new BacteriaRecipeInput(instance, stack), level).map(RecipeHolder::value).orElse(null);

        if (this.active != (this.recipe != null)) {
            this.active = this.recipe != null;
            update();
        }
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level instanceof ServerLevel server && recipeRevision.changed(server)) {
            progress = 0;
            checkRecipe();
        }

        if (level instanceof ServerLevel server && server.getGameTime() % DishPort.INTERVAL == 0) {
            DishPort.tick(this, DISH_IN, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::finishedColony, slot -> progress = 0);
        }

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

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot != DISH_IN) {
            return true;
        }
        return DishPort.accepts(this, resource, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::finishedColony, this::canIncubate);
    }

    private boolean canIncubate(BacteriaInstance colony) {
        if (colony.getSize() >= NTConfig.bacteriaColonySizeCap && colony.getAge() <= 0) {
            return false;
        }
        if (!(level instanceof ServerLevel server)) {
            return true;
        }
        for (RecipeHolder<BacteriaIncubationRecipe> holder : server.recipeAccess().recipeMap().byType(BacteriaIncubationRecipe.TYPE)) {
            if (colony.is(holder.value().bacteria())) {
                return true;
            }
        }
        return false;
    }

    private int finishedColony() {
        BacteriaInstance colony = getBacteriaStorage().getBacteria(0);
        return !colony.isEmpty() && colony.getSize() >= NTConfig.bacteriaColonySizeCap ? 0 : -1;
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
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
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IncubatorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        getItemStackHandler().ensureSize(4);
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
