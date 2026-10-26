package com.breakinblocks.nautec.compat.guideme;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
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
import com.breakinblocks.nautec.content.recipes.display.CountedIngredientDisplay;
import com.breakinblocks.nautec.content.recipes.display.MachineRecipeDisplay;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.utils.Utils;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.DefaultStyles;
import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytSlot;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import guideme.render.GuiAssets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class NautecRecipeMappings implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(AquaticCatalystChannelingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(CombustionAdditiveRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(ItemTransformationRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(ItemEtchingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(MixingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(AugmentationRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(BacteriaMutationRecipe.TYPE, NautecRecipeMappings::create);
        mappings.add(BacteriaIncubationRecipe.TYPE, NautecRecipeMappings::create);
        mappings.add(ResonanceCraftingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(PressureForgingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.add(LaserCraftingRecipe.Type.INSTANCE, NautecRecipeMappings::create);
        mappings.addStreamFactory(LaserEnchantingRecipe.Type.INSTANCE, NautecRecipeMappings::enchanting);
        mappings.add(ColonyFeedingRecipe.TYPE, NautecRecipeMappings::create);
    }

    public static <I extends RecipeInput, R extends Recipe<I>> LytBlock create(RecipeHolder<R> holder) {
        Recipe<?> recipe = holder.value();
        MachineRecipeDisplay display = MachineRecipeDisplay.of(recipe);
        List<SlotDisplay> inputs = new ArrayList<>(display.inputs());
        List<SlotDisplay> outputs = new ArrayList<>(display.outputs());
        List<Component> notes = new ArrayList<>();
        switch (recipe) {
            case MixingRecipe r -> duration(notes, r.duration());
            case ItemEtchingRecipe r -> duration(notes, r.duration());
            case ItemTransformationRecipe r -> {
                duration(notes, r.duration());
                purity(notes, r.purity());
                notes.add(text("power", r.power()));
            }
            case LaserCraftingRecipe r -> {
                duration(notes, r.duration());
                purity(notes, r.purity());
                notes.add(text("power", r.power()));
            }
            case PressureForgingRecipe r -> {
                duration(notes, r.duration());
                purity(notes, r.purity());
                notes.add(text("depth", r.minDepth()));
            }
            case ResonanceCraftingRecipe r -> {
                purity(notes, r.purity());
                notes.add(Component.translatable("nautec.jei.resonance_critical"));
            }
            case AquaticCatalystChannelingRecipe r -> {
                duration(notes, r.duration());
                purity(notes, r.purity());
                notes.add(text("power_total", r.powerAmount()));
            }
            case CombustionAdditiveRecipe r -> {
                duration(notes, r.duration());
                notes.add(text("additive", number(r.outputMultiplier()), number(r.fuelMultiplier())));
            }
            case AugmentationRecipe r -> {
                notes.add(text("augment", Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, r.resultAugment())));
                notes.add(Component.literal(r.desc()));
            }
            case BacteriaMutationRecipe r -> {
                inputs.addFirst(dish(r.inputBacteria()));
                outputs.add(dish(r.resultBacteria()));
                notes.add(text("mutation_chance", number(r.chance())));
            }
            case BacteriaIncubationRecipe r -> {
                inputs.addFirst(dish(r.bacteria()));
                outputs.add(dish(r.bacteria()));
                notes.add(text("growth", r.growth().toString(), number(r.consumeChance() * 100)));
            }
            case ColonyFeedingRecipe r -> {
                r.bacteria().flatMap(BacteriaSelector::key).ifPresent(key -> {
                    inputs.addFirst(dish(key));
                    outputs.add(dish(key));
                });
                if (r.bacteria().isEmpty()) notes.add(Component.translatable("nautec.jei.colony_feeding.any"));
                else if (r.bacteria().get().tag()) notes.add(Component.translatable("nautec.jei.colony_feeding.tag", r.bacteria().get().asString()));
                notes.add(Component.translatable("nautec.jei.colony_feeding.vitality", r.vitalityTicks(), number(r.vitalityTicks() / 20f)));
            }
            default -> {
            }
        }
        return box(holder, display, inputs, outputs, notes);
    }

    private static Stream<? extends LytBlock> enchanting(RecipeHolder<LaserEnchantingRecipe> holder) {
        LaserEnchantingRecipe recipe = holder.value();
        var registries = Minecraft.getInstance().level.registryAccess();
        return recipe.enchantment(registries).stream().flatMap(enchantment -> IntStream.rangeClosed(1, EnchantingRules.maxLevel(enchantment))
                .mapToObj(level -> {
                    List<SlotDisplay> inputs = List.of(new CountedIngredientDisplay(recipe.input().ingredient().display(), recipe.reagentCount(level)),
                            new FluidStackSlotDisplay(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), EnchantingRules.experienceMb(enchantment, level, recipe.costMultiplier()))));
                    ItemStack book = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level));
                    List<SlotDisplay> outputs = List.of(new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(book)));
                    List<Component> notes = List.of(text("power", EnchantingRules.sustainedPower(enchantment, level, recipe.costMultiplier())),
                            text("power_total", EnchantingRules.totalPower(enchantment, level, recipe.costMultiplier())),
                            Component.translatable("nautec.jei.laser_enchanting.stable", number(NTConfig.abyssalEnchanterStablePurity)),
                            text("enchant_target"));
                    return box(holder, MachineRecipeDisplay.of(recipe), inputs, outputs, notes);
                }));
    }

    private static <I extends RecipeInput, R extends Recipe<I>> LytBlock box(RecipeHolder<R> holder, MachineRecipeDisplay display,
                                                                            List<SlotDisplay> inputs, List<SlotDisplay> outputs, List<Component> notes) {
        ItemStack station = display.craftingStation().resolveForFirstStack(Platform.getSlotDisplayContext());
        LytHBox body = new LytHBox();
        body.setGap(5);
        body.setWrap(false);
        body.setAlignItems(AlignItems.CENTER);
        body.append(slots(inputs));
        if (!outputs.isEmpty()) {
            body.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(24, 17)));
            body.append(slots(outputs));
        }
        var builder = LytStandardRecipeBox.builder().icon(station).title(station.getHoverName().getString()).customBody(body);
        for (Component note : notes) {
            LytParagraph paragraph = LytParagraph.of(note.getString());
            paragraph.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
            builder.addBottom(paragraph);
        }
        return builder.build(holder);
    }

    private static LytBlock slots(List<SlotDisplay> displays) {
        LytVBox grid = new LytVBox();
        grid.setGap(3);
        LytHBox row = null;
        for (int i = 0; i < displays.size(); i++) {
            if (i % 3 == 0) {
                row = new LytHBox();
                row.setGap(3);
                row.setWrap(false);
                grid.append(row);
            }
            SlotDisplay display = displays.get(i);
            List<FluidStack> fluids = display.resolve(Platform.getSlotDisplayContext(), FluidStackContentsFactory.INSTANCE).filter(fluid -> !fluid.isEmpty()).toList();
            if (fluids.isEmpty()) {
                row.append(new LytSlot(display));
            } else {
                LytVBox fluid = new LytVBox();
                fluid.setGap(1);
                fluid.append(new GuideFluidSlot(fluids));
                LytParagraph amount = LytParagraph.of(text("fluid_amount", fluids.getFirst().getAmount()).getString());
                amount.setStyle(DefaultStyles.CRAFTING_RECIPE_TYPE);
                fluid.append(amount);
                row.append(fluid);
            }
        }
        return grid;
    }

    private static SlotDisplay dish(ResourceKey<Bacteria> bacteria) {
        ItemStack stack = BacteriaHelper.getMaxStatDish(bacteria, Minecraft.getInstance().level.registryAccess());
        return new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    private static Component text(String key, Object... args) {
        return Component.translatable("nautec.guide.recipe." + key, args);
    }

    private static void duration(List<Component> notes, int ticks) {
        notes.add(text("duration", number(ticks / 20f)));
    }

    private static void purity(List<Component> notes, float purity) {
        notes.add(Component.translatable("nautec.jei.purity", number(purity)));
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
