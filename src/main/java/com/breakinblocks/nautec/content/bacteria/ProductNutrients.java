package com.breakinblocks.nautec.content.bacteria;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.data.NTDataMaps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ProductNutrients {
    public static final int BLOCK_SIZE = 9;
    private static final String COMMON = "c";
    private static final Set<String> MATERIAL_FAMILIES = Set.of("ingots", "gems", "dusts");

    private static @Nullable RecipeManager cachedMap;
    private static Derived cached = Derived.EMPTY;

    private ProductNutrients() {
    }

    public record Derived(List<BacteriaIncubationRecipe> incubation, List<ColonyFeedingRecipe> feeding, List<BacteriaMutationRecipe> mutation) {
        public static final Derived EMPTY = new Derived(List.of(), List.of(), List.of());
    }

    public static Derived get(ServerLevel level) {
        RecipeManager map = level.getServer().getRecipeManager();
        if (map != cachedMap) {
            cached = derive(map, level.registryAccess());
            cachedMap = map;
        }
        return cached;
    }

    public static Derived derive(RecipeManager map, HolderLookup.Provider registries) {
        List<BacteriaIncubationRecipe> incubation = new ArrayList<>();
        List<ColonyFeedingRecipe> feeding = new ArrayList<>();
        List<BacteriaMutationRecipe> mutation = new ArrayList<>();
        Optional<? extends HolderLookup.RegistryLookup<Bacteria>> lookup = registries.lookup(NTRegistries.BACTERIA_KEY);
        if (lookup.isEmpty()) {
            return Derived.EMPTY;
        }
        List<Holder.Reference<Bacteria>> strains = lookup.get().listElements()
                .sorted(Comparator.comparing(holder -> holder.key().location().toString()))
                .toList();
        for (Holder.Reference<Bacteria> strain : strains) {
            ResourceKey<Bacteria> key = strain.key();
            Ingredient product = product(strain.value().resource());
            if (product == null) {
                continue;
            }
            Ingredient block = storageBlock(strain.value().resource());

            if (NTConfig.mutatorRefineChance > 0) {
                Ingredient catalyst = block != null ? block : product;
                boolean taken = false;
                for (RecipeHolder<BacteriaMutationRecipe> holder : map.getAllRecipesFor(BacteriaMutationRecipe.TYPE)) {
                    if (holder.value().inputBacteria().equals(key) && overlaps(List.of(holder.value().catalyst()), catalyst)) {
                        taken = true;
                        break;
                    }
                }
                if (!taken) {
                    mutation.add(new BacteriaMutationRecipe(key, key, catalyst, (float) NTConfig.mutatorRefineChance));
                }
            }
            if (!NTConfig.bacteriaProductNutrients) {
                continue;
            }

            BacteriaIncubationRecipe base = null;
            List<Ingredient> eaten = new ArrayList<>();
            for (RecipeHolder<BacteriaIncubationRecipe> holder : map.getAllRecipesFor(BacteriaIncubationRecipe.TYPE)) {
                BacteriaIncubationRecipe recipe = holder.value();
                if (recipe.bacteria().equals(key)) {
                    eaten.add(recipe.nutrient());
                    if (base == null || recipe.consumeChance() < base.consumeChance()) {
                        base = recipe;
                    }
                }
            }
            if (base != null) {
                float productChance = (float) Math.min(1.0, base.consumeChance() * NTConfig.bacteriaProductConsumeMultiplier);
                if (!overlaps(eaten, product)) {
                    incubation.add(new BacteriaIncubationRecipe(key, product, base.growth(), productChance));
                }
                if (block != null && !overlaps(eaten, block)) {
                    incubation.add(new BacteriaIncubationRecipe(key, block, base.growth(), productChance / BLOCK_SIZE));
                }
            }

            int ticks = 0;
            List<Ingredient> fed = new ArrayList<>();
            for (RecipeHolder<ColonyFeedingRecipe> holder : map.getAllRecipesFor(ColonyFeedingRecipe.TYPE)) {
                ColonyFeedingRecipe recipe = holder.value();
                if (recipe.bacteria().isPresent() && BacteriaSelector.matches(recipe.bacteria(), key, registries)) {
                    fed.add(recipe.ingredient().ingredient());
                    ticks = Math.max(ticks, recipe.vitalityTicks() / Math.max(1, recipe.ingredient().count()));
                }
            }
            if (ticks > 0) {
                if (!overlaps(fed, product)) {
                    feeding.add(new ColonyFeedingRecipe(Optional.of(BacteriaSelector.of(key)), new IngredientWithCount(product, 1), ticks));
                }
                if (block != null && !overlaps(fed, block)) {
                    feeding.add(new ColonyFeedingRecipe(Optional.of(BacteriaSelector.of(key)), new IngredientWithCount(block, 1), ticks * BLOCK_SIZE));
                }
            }
        }
        return new Derived(List.copyOf(incubation), List.copyOf(feeding), List.copyOf(mutation));
    }

    private static boolean overlaps(List<Ingredient> existing, Ingredient candidate) {
        for (ItemStack candidateStack : candidate.getItems()) {
            ItemStack stack = new ItemStack(candidateStack.getItem());
            for (Ingredient ingredient : existing) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long biomass(Bacteria strain, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        long perItem = Math.max(1, Math.round(NTConfig.replicatorBiomassPerItem / Math.max(0.01, strain.productionMultiplier())));
        Ingredient product = product(strain.resource());
        if (product != null && product.test(stack)) {
            return perItem;
        }
        Ingredient block = storageBlock(strain.resource());
        if (block != null && block.test(stack)) {
            return perItem * BLOCK_SIZE;
        }
        return 0;
    }

    public static @Nullable Ingredient product(Bacteria.Resource resource) {
        if (resource instanceof Bacteria.Resource.ItemTagResource(TagKey<Item> tag)) {
            return tagIngredient(tag);
        }
        Item item = resource.resolve();
        return item == null || item == Items.AIR ? null : Ingredient.of(item);
    }

    public static @Nullable Ingredient storageBlock(Bacteria.Resource resource) {
        Item item = resource.resolve();
        if (item == null || item == Items.AIR) {
            return null;
        }
        Item mapped = item.builtInRegistryHolder().getData(NTDataMaps.STORAGE_BLOCKS);
        if (mapped != null && mapped != Items.AIR && mapped != item) {
            return Ingredient.of(mapped);
        }
        List<TagKey<Item>> materials = new ArrayList<>();
        if (resource instanceof Bacteria.Resource.ItemTagResource(TagKey<Item> tag)) {
            materials.add(tag);
        }
        item.builtInRegistryHolder().tags().forEach(materials::add);
        materials.sort(Comparator.comparing(tag -> tag.location().toString()));
        for (TagKey<Item> tag : materials) {
            ResourceLocation id = tag.location();
            int slash = id.getPath().indexOf('/');
            if (!id.getNamespace().equals(COMMON) || slash < 0 || !MATERIAL_FAMILIES.contains(id.getPath().substring(0, slash))) {
                continue;
            }
            TagKey<Item> blocks = TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(COMMON, "storage_blocks/" + id.getPath().substring(slash + 1)));
            Ingredient ingredient = tagIngredient(blocks);
            if (ingredient != null) {
                return ingredient;
            }
        }
        return null;
    }

    private static @Nullable Ingredient tagIngredient(TagKey<Item> tag) {
        Optional<HolderSet.Named<Item>> items = BuiltInRegistries.ITEM.getTag(tag);
        return items.isPresent() && items.get().size() > 0 ? Ingredient.of(tag) : null;
    }
}
