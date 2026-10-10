package com.breakinblocks.nautec.content.recipes;


import com.breakinblocks.nautec.content.recipes.utils.SimpleRecipeSerializer;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.inputs.LaserCraftingRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.content.recipes.utils.RecipeUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.breakinblocks.nautec.utils.codec.StreamCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.utils.templates.FluidStackTemplate;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record LaserCraftingRecipe(List<IngredientWithCount> ingredients, List<SizedFluidIngredient> fluidIngredients,
                                  List<ItemStackTemplate> resultTemplates, List<FluidStackTemplate> fluidResultTemplates,
                                  int power, float purity, int duration) implements Recipe<LaserCraftingRecipeInput> {
    public static final String NAME = "laser_crafting";
    public static final int MAX_ITEM_INPUTS = 3;
    public static final int MAX_FLUID_INPUTS = 2;
    public static final int MAX_ITEM_OUTPUTS = 3;
    public static final int MAX_FLUID_OUTPUTS = 2;

    public LaserCraftingRecipe {
        ingredients = List.copyOf(ingredients);
        fluidIngredients = List.copyOf(fluidIngredients);
        resultTemplates = List.copyOf(resultTemplates);
        fluidResultTemplates = List.copyOf(fluidResultTemplates);
    }

    public List<ItemStack> results() {
        return resultTemplates.stream().map(ItemStackTemplate::create).toList();
    }

    public List<FluidStack> fluidResults() {
        return fluidResultTemplates.stream().map(FluidStackTemplate::create).toList();
    }

    @Override
    public boolean matches(@NotNull LaserCraftingRecipeInput input, @NotNull Level level) {
        return purity <= input.purity()
                && RecipeUtils.consumptionPlan(input.items(), ingredients) != null
                && fluidPlan(input.fluids()) != null;
    }

    public int @Nullable [] fluidPlan(List<FluidStack> fluids) {
        int[] tanks = new int[fluidIngredients.size()];
        return assignFluid(fluids, tanks, new boolean[fluids.size()], 0) ? tanks : null;
    }

    private boolean assignFluid(List<FluidStack> fluids, int[] tanks, boolean[] used, int index) {
        if (index == fluidIngredients.size()) return true;
        SizedFluidIngredient wanted = fluidIngredients.get(index);
        for (int tank = 0; tank < fluids.size(); tank++) {
            FluidStack stack = fluids.get(tank);
            if (!used[tank] && !stack.isEmpty() && wanted.ingredient().test(stack) && stack.getAmount() >= wanted.amount()) {
                used[tank] = true;
                tanks[index] = tank;
                if (assignFluid(fluids, tanks, used, index + 1)) return true;
                used[tank] = false;
            }
        }
        return false;
    }

    public static Optional<RecipeHolder<LaserCraftingRecipe>> findBest(ServerLevel level, LaserCraftingRecipeInput input) {
        RecipeHolder<LaserCraftingRecipe> best = null;
        for (RecipeHolder<LaserCraftingRecipe> holder : level.getServer().getRecipeManager().getAllRecipesFor(Type.INSTANCE)) {
            LaserCraftingRecipe recipe = holder.value();
            if (recipe.matches(input, level) && (best == null || recipe.purity() > best.value().purity())) {
                best = holder;
            }
        }
        return Optional.ofNullable(best);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull LaserCraftingRecipeInput input, HolderLookup.@NotNull Provider registries) {
        return resultTemplates.isEmpty() ? ItemStack.EMPTY : resultTemplates.getFirst().create();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@Nullable Provider registries) {
        return assemble(new LaserCraftingRecipeInput(List.of(), List.of(), 0), registries);
    }

    @Override
    public @NotNull String getGroup() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<LaserCraftingRecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<LaserCraftingRecipeInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return RecipeUtils.listToNonNullList(RecipeUtils.iWCToIngredientsSaveCount(ingredients));
    }

    private static DataResult<LaserCraftingRecipe> validate(LaserCraftingRecipe recipe) {
        if (recipe.ingredients().isEmpty() && recipe.fluidIngredients().isEmpty()) {
            return DataResult.error(() -> "Laser crafting recipe needs at least one item or fluid input");
        }
        if (recipe.resultTemplates().isEmpty() && recipe.fluidResultTemplates().isEmpty()) {
            return DataResult.error(() -> "Laser crafting recipe needs at least one item or fluid result");
        }
        return DataResult.success(recipe);
    }

    public static class Serializer {
        private static final MapCodec<LaserCraftingRecipe> MAP_CODEC = RecordCodecBuilder.<LaserCraftingRecipe>mapCodec(builder -> builder.group(
                IngredientWithCount.CODEC.listOf(0, MAX_ITEM_INPUTS).optionalFieldOf("ingredients", List.of()).forGetter(LaserCraftingRecipe::ingredients),
                SizedFluidIngredient.NESTED_CODEC.listOf(0, MAX_FLUID_INPUTS).optionalFieldOf("fluid_ingredients", List.of()).forGetter(LaserCraftingRecipe::fluidIngredients),
                ItemStackTemplate.CODEC.listOf(0, MAX_ITEM_OUTPUTS).optionalFieldOf("results", List.of()).forGetter(LaserCraftingRecipe::resultTemplates),
                FluidStackTemplate.CODEC.listOf(0, MAX_FLUID_OUTPUTS).optionalFieldOf("fluid_results", List.of()).forGetter(LaserCraftingRecipe::fluidResultTemplates),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("power").forGetter(LaserCraftingRecipe::power),
                Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("purity", 0F).forGetter(LaserCraftingRecipe::purity),
                ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(LaserCraftingRecipe::duration)
        ).apply(builder, LaserCraftingRecipe::new)).validate(LaserCraftingRecipe::validate);
        private static final StreamCodec<RegistryFriendlyByteBuf, LaserCraftingRecipe> STREAM_CODEC = StreamCodecs.composite(
                IngredientWithCount.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ITEM_INPUTS)),
                LaserCraftingRecipe::ingredients,
                SizedFluidIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_FLUID_INPUTS)),
                LaserCraftingRecipe::fluidIngredients,
                ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ITEM_OUTPUTS)),
                LaserCraftingRecipe::resultTemplates,
                FluidStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_FLUID_OUTPUTS)),
                LaserCraftingRecipe::fluidResultTemplates,
                ByteBufCodecs.VAR_INT,
                LaserCraftingRecipe::power,
                ByteBufCodecs.FLOAT,
                LaserCraftingRecipe::purity,
                ByteBufCodecs.VAR_INT,
                LaserCraftingRecipe::duration,
                LaserCraftingRecipe::new
        );
        public static final RecipeSerializer<LaserCraftingRecipe> INSTANCE = new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

        private Serializer() {
        }
    }

    public static class Type {
        public static final RecipeType<LaserCraftingRecipe> INSTANCE = RecipeType.simple(Nautec.rl(NAME));

        private Type() {
        }
    }
}
