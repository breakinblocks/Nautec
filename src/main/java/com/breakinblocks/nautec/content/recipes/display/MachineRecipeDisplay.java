package com.breakinblocks.nautec.content.recipes.display;

import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.utils.templates.ItemStackTemplate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Recipe presentation data for GuideMe on Minecraft 1.21.1. */
public record MachineRecipeDisplay(List<Slot> inputs, List<Slot> outputs, Slot craftingStation) {
    public record Slot(List<ItemStack> items, List<FluidStack> fluids) {
        public static Slot ingredient(Ingredient ingredient, int count) {
            return new Slot(Arrays.stream(ingredient.getItems()).map(stack -> stack.copyWithCount(count)).toList(), List.of());
        }
        public static Slot stack(ItemStack stack) { return new Slot(List.of(stack.copy()), List.of()); }
        public static Slot fluid(FluidStack fluid) { return new Slot(List.of(), List.of(fluid.copy())); }
    }
    public static boolean supports(Recipe<?> recipe) {
        return recipe instanceof MixingRecipe || recipe instanceof LaserCraftingRecipe
                || recipe instanceof ItemTransformationRecipe || recipe instanceof ItemEtchingRecipe
                || recipe instanceof PressureForgingRecipe || recipe instanceof ResonanceCraftingRecipe
                || recipe instanceof AquaticCatalystChannelingRecipe || recipe instanceof CombustionAdditiveRecipe
                || recipe instanceof AugmentationRecipe || recipe instanceof BacteriaIncubationRecipe
                || recipe instanceof BacteriaMutationRecipe || recipe instanceof ColonyFeedingRecipe
                || recipe instanceof LaserEnchantingRecipe;
    }

    public boolean hasResult(Item item) {
        return outputs.stream().anyMatch(slot -> slot.items().stream().anyMatch(stack -> stack.is(item))
                || slot.fluids().stream().anyMatch(fluid -> fluid.getFluid().getBucket() == item));
    }

    public static Slot counted(IngredientWithCount ingredient) {
        Slot display = Slot.ingredient(ingredient.ingredient(), 1);
        return ingredient.count() == 1 ? display : Slot.ingredient(ingredient.ingredient(), ingredient.count());
    }

    public static Slot item(ItemLike item) {
        return Slot.stack(new ItemStack(item));
    }

    private static Slot stack(ItemStackTemplate stack) {
        return Slot.stack(stack.create());
    }

    private static MachineRecipeDisplay single(ItemLike station, Slot input, ItemStackTemplate result) {
        return new MachineRecipeDisplay(List.of(input), List.of(stack(result)), item(station));
    }

    public static MachineRecipeDisplay of(Recipe<?> recipe) {
        return switch (recipe) {
            case MixingRecipe r -> {
                List<Slot> inputs = new ArrayList<>(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList());
                if (!r.fluidIngredient().isEmpty()) inputs.add(Slot.fluid(r.fluidIngredient()));
                List<Slot> outputs = new ArrayList<>();
                r.resultTemplate().ifPresent(result -> outputs.add(stack(result)));
                if (!r.fluidResult().isEmpty()) outputs.add(Slot.fluid(r.fluidResult()));
                yield new MachineRecipeDisplay(inputs, outputs, item(NTBlocks.MIXER.get()));
            }
            case LaserCraftingRecipe r -> {
                List<Slot> inputs = new ArrayList<>(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList());
                r.fluidIngredients().forEach(ingredient -> inputs.add(new Slot(List.of(), List.of(ingredient.getFluids()))));
                List<Slot> outputs = new ArrayList<>(r.resultTemplates().stream().map(MachineRecipeDisplay::stack).toList());
                r.fluidResults().forEach(fluid -> outputs.add(Slot.fluid(fluid)));
                yield new MachineRecipeDisplay(inputs, outputs, item(NTBlocks.LASER_CRAFTING_MATRIX.get()));
            }
            case ItemTransformationRecipe r -> single(NTBlocks.AQUATIC_CATALYST.get(), counted(r.ingredient()), r.resultTemplate());
            case ItemEtchingRecipe r -> single(NTFluids.ETCHING_ACID.getBucket(), counted(r.ingredient()), r.resultTemplate());
            case PressureForgingRecipe r -> single(NTBlocks.PRESSURE_FORGE.get(), Slot.ingredient(r.ingredient(), 1), r.resultTemplate());
            case ResonanceCraftingRecipe r -> single(NTBlocks.RESONANCE_CHAMBER.get(), Slot.ingredient(r.ingredient(), 1), r.resultTemplate());
            case AquaticCatalystChannelingRecipe r -> new MachineRecipeDisplay(List.of(Slot.ingredient(r.ingredient(), 1)), List.of(), item(NTBlocks.AQUATIC_CATALYST.get()));
            case CombustionAdditiveRecipe r -> new MachineRecipeDisplay(List.of(Slot.ingredient(r.ingredient(), 1)), List.of(), item(NTBlocks.COMBUSTION_DYNAMO.get()));
            case AugmentationRecipe r -> new MachineRecipeDisplay(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList(), List.of(), item(NTBlocks.AUGMENTATION_STATION.get()));
            case BacteriaIncubationRecipe r -> new MachineRecipeDisplay(List.of(Slot.ingredient(r.nutrient(), 1)), List.of(), item(NTBlocks.INCUBATOR.get()));
            case BacteriaMutationRecipe r -> new MachineRecipeDisplay(List.of(Slot.ingredient(r.catalyst(), 1)), List.of(), item(NTBlocks.MUTATOR.get()));
            case ColonyFeedingRecipe r -> new MachineRecipeDisplay(List.of(counted(r.ingredient())), List.of(), item(NTBlocks.BIO_REACTOR.get()));
            case LaserEnchantingRecipe r -> new MachineRecipeDisplay(List.of(counted(r.input())), List.of(), item(NTBlocks.ABYSSAL_ENCHANTER.get()));
            default -> throw new IllegalArgumentException("Unsupported machine recipe: " + recipe.getClass().getName());
        };
    }
}
