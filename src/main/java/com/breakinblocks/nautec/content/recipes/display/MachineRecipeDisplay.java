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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;

import java.util.ArrayList;
import java.util.List;

public record MachineRecipeDisplay(List<SlotDisplay> inputs, List<SlotDisplay> outputs,
                                   SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<MachineRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            SlotDisplay.CODEC.listOf().fieldOf("inputs").forGetter(MachineRecipeDisplay::inputs),
            SlotDisplay.CODEC.listOf().fieldOf("outputs").forGetter(MachineRecipeDisplay::outputs),
            SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(MachineRecipeDisplay::craftingStation)
    ).apply(builder, MachineRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MachineRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), MachineRecipeDisplay::inputs,
            SlotDisplay.STREAM_CODEC.apply(ByteBufCodecs.list()), MachineRecipeDisplay::outputs,
            SlotDisplay.STREAM_CODEC, MachineRecipeDisplay::craftingStation, MachineRecipeDisplay::new);
    public static final Type<MachineRecipeDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);

    public MachineRecipeDisplay {
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
    }

    @Override
    public Type<MachineRecipeDisplay> type() {
        return TYPE;
    }

    @Override
    public SlotDisplay result() {
        // Bucket items are discovery aliases for fluid results, as in the recipe viewer.
        List<SlotDisplay> searchable = new ArrayList<>(outputs);
        for (SlotDisplay output : outputs) {
            if (output instanceof FluidStackSlotDisplay fluid) {
                ItemLike bucket = fluid.stack().getFluid().getBucket();
                if (!bucket.asItem().getDefaultInstance().isEmpty()) {
                    searchable.add(item(bucket));
                }
            }
        }
        return new SlotDisplay.Composite(searchable);
    }

    public static SlotDisplay counted(IngredientWithCount ingredient) {
        SlotDisplay display = ingredient.ingredient().display();
        return ingredient.count() == 1 ? display : new CountedIngredientDisplay(display, ingredient.count());
    }

    public static SlotDisplay item(ItemLike item) {
        return new SlotDisplay.ItemSlotDisplay(item.asItem());
    }

    private static SlotDisplay stack(ItemStackTemplate stack) {
        return new SlotDisplay.ItemStackSlotDisplay(stack);
    }

    private static MachineRecipeDisplay single(ItemLike station, SlotDisplay input, ItemStackTemplate result) {
        return new MachineRecipeDisplay(List.of(input), List.of(stack(result)), item(station));
    }

    public static MachineRecipeDisplay of(Recipe<?> recipe) {
        return switch (recipe) {
            case MixingRecipe r -> {
                List<SlotDisplay> inputs = new ArrayList<>(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList());
                if (!r.fluidIngredient().isEmpty()) inputs.add(new FluidStackSlotDisplay(r.fluidIngredient()));
                List<SlotDisplay> outputs = new ArrayList<>();
                r.resultTemplate().ifPresent(result -> outputs.add(stack(result)));
                if (!r.fluidResult().isEmpty()) outputs.add(new FluidStackSlotDisplay(r.fluidResult()));
                yield new MachineRecipeDisplay(inputs, outputs, item(NTBlocks.MIXER.get()));
            }
            case LaserCraftingRecipe r -> {
                List<SlotDisplay> inputs = new ArrayList<>(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList());
                r.fluidIngredients().forEach(ingredient -> inputs.add(new SlotDisplay.Composite(ingredient.ingredient().fluids().stream()
                        .<SlotDisplay>map(fluid -> new FluidStackSlotDisplay(new FluidStack(fluid, ingredient.amount()))).toList())));
                List<SlotDisplay> outputs = new ArrayList<>(r.resultTemplates().stream().map(MachineRecipeDisplay::stack).toList());
                r.fluidResults().forEach(fluid -> outputs.add(new FluidStackSlotDisplay(fluid)));
                yield new MachineRecipeDisplay(inputs, outputs, item(NTBlocks.LASER_CRAFTING_MATRIX.get()));
            }
            case ItemTransformationRecipe r -> single(NTBlocks.AQUATIC_CATALYST.get(), counted(r.ingredient()), r.resultTemplate());
            case ItemEtchingRecipe r -> single(NTFluids.ETCHING_ACID.getBucket(), counted(r.ingredient()), r.resultTemplate());
            case PressureForgingRecipe r -> single(NTBlocks.PRESSURE_FORGE.get(), r.ingredient().display(), r.resultTemplate());
            case ResonanceCraftingRecipe r -> single(NTBlocks.RESONANCE_CHAMBER.get(), r.ingredient().display(), r.resultTemplate());
            case AquaticCatalystChannelingRecipe r -> new MachineRecipeDisplay(List.of(r.ingredient().display()), List.of(), item(NTBlocks.AQUATIC_CATALYST.get()));
            case CombustionAdditiveRecipe r -> new MachineRecipeDisplay(List.of(r.ingredient().display()), List.of(), item(NTBlocks.COMBUSTION_DYNAMO.get()));
            case AugmentationRecipe r -> new MachineRecipeDisplay(r.ingredients().stream().map(MachineRecipeDisplay::counted).toList(), List.of(), item(NTBlocks.AUGMENTATION_STATION.get()));
            case BacteriaIncubationRecipe r -> new MachineRecipeDisplay(List.of(r.nutrient().display()), List.of(), item(NTBlocks.INCUBATOR.get()));
            case BacteriaMutationRecipe r -> new MachineRecipeDisplay(List.of(r.catalyst().display()), List.of(), item(NTBlocks.MUTATOR.get()));
            case ColonyFeedingRecipe r -> new MachineRecipeDisplay(List.of(counted(r.ingredient())), List.of(), item(NTBlocks.BIO_REACTOR.get()));
            case LaserEnchantingRecipe r -> new MachineRecipeDisplay(List.of(counted(r.input())), List.of(), item(NTBlocks.ABYSSAL_ENCHANTER.get()));
            default -> throw new IllegalArgumentException("Unsupported machine recipe: " + recipe.getClass().getName());
        };
    }
}
