package com.breakinblocks.nautec.events.helper;

import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.utils.RecipeRevision;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class ItemEtching {
    public static final int ACID_CONSUME_CHANCE = 3;

    private static final Map<ItemEntity, Entry> activeEtching = new IdentityHashMap<>();
    private static final RecipeRevision REVISION = new RecipeRevision();

    public static void onEntityLeave(ItemEntity itemEntity) {
        activeEtching.remove(itemEntity);
    }

    public static void processItemEtching(ItemEntity itemEntity, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (REVISION.changed(serverLevel)) {
            activeEtching.clear();
        }
        ItemStack stack = itemEntity.getItem();
        Entry entry = activeEtching.get(itemEntity);
        if (entry == null || !ItemStack.isSameItemSameComponents(entry.key, stack)) {
            Optional<ItemEtchingRecipe> recipe = getEtchingRecipe(stack, serverLevel);
            entry = new Entry(stack.copyWithCount(1), recipe.orElse(null));
            activeEtching.put(itemEntity, entry);
            if (recipe.isPresent()) {
                return;
            }
        }
        if (entry.recipe == null) {
            return;
        }

        int etchingTime = entry.progress;
        if (etchingTime >= entry.recipe.duration()) {
            activeEtching.remove(itemEntity);
            transformItem(itemEntity, entry.recipe, level, level.getRandom().nextInt(ACID_CONSUME_CHANCE) == 0);
            return;
        }

        entry.progress = etchingTime + 1;
        if (etchingTime % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.FLAME, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 20, 0.5, 0.5, 0.5, 0);
        }
    }

    private static Optional<ItemEtchingRecipe> getEtchingRecipe(ItemStack stack, ServerLevel level) {
        return level.getRecipeManager()
                .getRecipeFor(ItemEtchingRecipe.Type.INSTANCE, new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    public static void transformItem(ItemEntity itemEntity, ItemEtchingRecipe recipe, Level level, boolean consumeAcid) {
        ItemStack inputStack = itemEntity.getItem();
        ItemStack resultStack = recipe.getResultItem(level.registryAccess()).copy();
        resultStack.setCount(inputStack.getCount());

        if (inputStack.is(NTBlocks.RUSTY_CRATE.asItem()) && resultStack.is(NTBlocks.CRATE.asItem())) {
            ItemContainerContents value = inputStack.copy().get(DataComponents.CONTAINER);
            resultStack.set(DataComponents.CONTAINER, value);
            SeededContainerLoot value1 = inputStack.copy().get(DataComponents.CONTAINER_LOOT);
            resultStack.set(DataComponents.CONTAINER_LOOT, value1);
        }

        double x = itemEntity.getX();
        double y = itemEntity.getY();
        double z = itemEntity.getZ();
        BlockPos acidPos = itemEntity.blockPosition();
        itemEntity.discard();

        if (consumeAcid && isAcidSource(level.getFluidState(acidPos))) {
            level.setBlock(acidPos, Blocks.AIR.defaultBlockState(), 11);
        }

        level.addFreshEntity(new ItemEntity(level, x, y, z, resultStack));
    }

    private static boolean isAcidSource(FluidState state) {
        return state.isSource() && state.is(NTFluids.ETCHING_ACID.getStillFluid());
    }

    private static final class Entry {
        private final ItemStack key;
        private final ItemEtchingRecipe recipe;
        private int progress;

        private Entry(ItemStack key, ItemEtchingRecipe recipe) {
            this.key = key;
            this.recipe = recipe;
        }
    }
}
