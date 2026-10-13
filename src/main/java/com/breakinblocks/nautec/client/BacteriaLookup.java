package com.breakinblocks.nautec.client;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.bacteria.ProductNutrients;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.BacteriaRecipeInput;
import com.breakinblocks.nautec.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BacteriaLookup {
    public static final int LIST_LIMIT = 4;
    public static final int ROW_LIMIT = 8;

    private static @Nullable RecipeMap cachedMap;
    private static @Nullable RegistryAccess cachedAccess;
    private static List<BacteriaMutationRecipe> mutations = List.of();
    private static List<BacteriaIncubationRecipe> incubation = List.of();
    private static List<ColonyFeedingRecipe> feeding = List.of();

    private BacteriaLookup() {
    }

    private static @Nullable ClientLevel refresh() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        RecipeMap map = ClientRecipes.get();
        RegistryAccess access = level.registryAccess();
        if (map != cachedMap || access != cachedAccess) {
            ProductNutrients.Derived derived = ProductNutrients.derive(map, access);
            List<BacteriaMutationRecipe> allMutations = new ArrayList<>();
            for (RecipeHolder<BacteriaMutationRecipe> holder : map.byType(BacteriaMutationRecipe.TYPE)) {
                allMutations.add(holder.value());
            }
            allMutations.addAll(derived.mutation());
            List<BacteriaIncubationRecipe> allIncubation = new ArrayList<>();
            for (RecipeHolder<BacteriaIncubationRecipe> holder : map.byType(BacteriaIncubationRecipe.TYPE)) {
                allIncubation.add(holder.value());
            }
            allIncubation.addAll(derived.incubation());
            List<ColonyFeedingRecipe> allFeeding = new ArrayList<>();
            for (RecipeHolder<ColonyFeedingRecipe> holder : map.byType(ColonyFeedingRecipe.TYPE)) {
                allFeeding.add(holder.value());
            }
            allFeeding.addAll(derived.feeding());
            mutations = List.copyOf(allMutations);
            incubation = List.copyOf(allIncubation);
            feeding = List.copyOf(allFeeding);
            cachedMap = map;
            cachedAccess = access;
        }
        return level;
    }

    public static List<BacteriaMutationRecipe> mutations() {
        return refresh() == null ? List.of() : mutations;
    }

    public static List<BacteriaMutationRecipe> mutationsFrom(ResourceKey<Bacteria> strain) {
        List<BacteriaMutationRecipe> found = new ArrayList<>();
        for (BacteriaMutationRecipe recipe : mutations()) {
            if (recipe.inputBacteria().equals(strain)) {
                found.add(recipe);
            }
        }
        found.sort(Comparator.comparing(BacteriaMutationRecipe::refines).thenComparing(BacteriaMutationRecipe::chance, Comparator.reverseOrder()));
        return found;
    }

    public static Optional<BacteriaMutationRecipe> mutation(BacteriaInstance colony, ItemStack catalyst) {
        ClientLevel level = refresh();
        if (level == null || colony.isEmpty() || catalyst.isEmpty()) {
            return Optional.empty();
        }
        BacteriaRecipeInput input = new BacteriaRecipeInput(colony, catalyst);
        for (BacteriaMutationRecipe recipe : mutations) {
            if (recipe.matches(input, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public static List<ItemStack> feedingItems(BacteriaInstance colony) {
        ClientLevel level = refresh();
        Map<Item, ItemStack> eats = new LinkedHashMap<>();
        if (level == null || colony.isEmpty()) {
            return List.of();
        }
        for (ColonyFeedingRecipe recipe : feeding) {
            if (recipe.accepts(colony, level)) {
                first(recipe.ingredient().ingredient()).ifPresent(item ->
                        eats.putIfAbsent(item, new ItemStack(item, Math.max(1, recipe.ingredient().count()))));
            }
        }
        return List.copyOf(eats.values());
    }

    public static boolean feeds(BacteriaInstance colony, ItemStack stack) {
        ClientLevel level = refresh();
        if (level == null || colony.isEmpty() || stack.isEmpty()) {
            return false;
        }
        for (ColonyFeedingRecipe recipe : feeding) {
            if (recipe.ingredient().ingredient().test(stack) && stack.getCount() >= recipe.ingredient().count() && recipe.accepts(colony, level)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> incubationItems(ResourceKey<Bacteria> strain) {
        Map<Item, ItemStack> eats = new LinkedHashMap<>();
        if (refresh() == null) {
            return List.of();
        }
        for (BacteriaIncubationRecipe recipe : incubation) {
            if (recipe.bacteria().equals(strain)) {
                first(recipe.nutrient()).ifPresent(item -> eats.putIfAbsent(item, new ItemStack(item)));
            }
        }
        return List.copyOf(eats.values());
    }

    public static Optional<Item> first(Ingredient ingredient) {
        return ingredient.items().findFirst().map(Holder::value);
    }

    public static Component catalystName(BacteriaMutationRecipe recipe) {
        return first(recipe.catalyst()).map(item -> new ItemStack(item).getHoverName()).orElse(Component.literal("?"));
    }

    public static Component strainName(ResourceKey<Bacteria> strain) {
        return Utils.registryTranslation(strain);
    }

    public static String percent(float fraction) {
        float value = fraction * 100f;
        return String.format(Locale.ROOT, value >= 10f || value == Math.round(value) ? "%.0f%%" : "%.1f%%", value);
    }

    public static Component itemList(List<ItemStack> stacks) {
        MutableComponent list = Component.empty();
        int shown = 0;
        for (ItemStack stack : stacks) {
            if (shown == LIST_LIMIT) {
                list.append(Component.translatable("nautec.bacteria.lookup.more", stacks.size() - LIST_LIMIT));
                break;
            }
            if (shown > 0) {
                list.append(", ");
            }
            list.append(stack.getCount() > 1 ? Component.translatable("nautec.configuration_card.stack", stack.getCount(), stack.getHoverName())
                    : stack.getHoverName());
            shown++;
        }
        return list;
    }

    public static List<Component> mutationRows(BacteriaInstance colony, boolean boosted) {
        return mutationRows(colony, boosted, ROW_LIMIT);
    }

    public static List<Component> mutationRows(BacteriaInstance colony, boolean boosted, int limit) {
        List<Component> rows = new ArrayList<>();
        List<BacteriaMutationRecipe> options = mutationsFrom(colony.getBacteria());
        for (BacteriaMutationRecipe recipe : options) {
            if (rows.size() == limit) {
                rows.add(Component.translatable("nautec.bacteria.lookup.more_rows", options.size() - limit).withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            String chance = percent(MutatorBlockEntity.computeSuccessChance(recipe, colony, boosted));
            rows.add(recipe.refines()
                    ? Component.translatable("nautec.bacteria.lookup.refine_row", catalystName(recipe), chance).withStyle(ChatFormatting.GRAY)
                    : Component.translatable("nautec.bacteria.lookup.mutation_row", strainName(recipe.resultBacteria()), catalystName(recipe), chance)
                    .withStyle(ChatFormatting.WHITE));
        }
        return rows;
    }
}
