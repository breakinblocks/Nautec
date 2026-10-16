package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class MixingEmiRecipe extends NTEmiRecipe<MixingRecipe> {
    private static final ResourceLocation DOWN_ARROW_SPRITE = Nautec.rl("container/furnace/down_arrow");
    private static final int MAX_SLOTS = 4;
    private static final int SLOT_SIZE = 18;
    private static final int GAP = 2;

    private final List<EmiIngredient> items = new ArrayList<>();
    private final EmiStack output;
    private final EmiStack fluidInput;
    private final EmiStack fluidOutput;

    public MixingEmiRecipe(ResourceLocation id, MixingRecipe recipe) {
        super(NTEmiPlugin.MIXING, id, recipe, 106, 66);
        for (IngredientWithCount ingredient : recipe.ingredients()) {
            items.add(NTEmiStacks.of(ingredient));
        }
        inputs.addAll(items);
        ItemStack result = recipe.result();
        this.output = result.isEmpty() ? EmiStack.EMPTY : EmiStack.of(result);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        FluidStack fluidIn = recipe.fluidIngredient();
        this.fluidInput = fluidIn.getFluid() == Fluids.EMPTY ? EmiStack.EMPTY : EmiStack.of(fluidIn.getFluid(), fluidIn.getAmount());
        if (!fluidInput.isEmpty()) {
            inputs.add(fluidInput);
            addIfPresent(inputs, NTEmiStacks.bucket(fluidIn.getFluid()));
        }
        FluidStack fluidOut = recipe.fluidResult();
        this.fluidOutput = fluidOut.getFluid() == Fluids.EMPTY ? EmiStack.EMPTY : EmiStack.of(fluidOut.getFluid(), fluidOut.getAmount());
        if (!fluidOutput.isEmpty()) {
            outputs.add(fluidOutput);
            EmiStack bucket = NTEmiStacks.bucket(fluidOut.getFluid());
            if (!bucket.isEmpty()) {
                outputs.add(bucket);
            }
        }
    }

    private static void addIfPresent(List<EmiIngredient> list, EmiStack stack) {
        if (!stack.isEmpty()) {
            list.add(stack);
        }
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        canvas.sprite(DOWN_ARROW_SPRITE, 32, 22, 15, 23);
        canvas.sprite(DOWN_ARROW_SPRITE, 88, 22, 15, 23);
        if (!items.isEmpty()) {
            int shown = Math.min(items.size(), MAX_SLOTS);
            int totalWidth = shown * SLOT_SIZE + (shown - 1) * GAP;
            int startX = (80 - totalWidth) / 2;
            for (int i = 0; i < items.size(); i++) {
                canvas.slot(items.get(i), startX + i * (SLOT_SIZE + GAP), 0);
            }
        }
        if (!output.isEmpty()) {
            canvas.output(output, (80 - SLOT_SIZE) / 2, 50);
        }
        if (!fluidInput.isEmpty()) {
            canvas.tank(fluidInput, 88, 0, 16, 16, fluidInput.getAmount());
        }
        if (!fluidOutput.isEmpty()) {
            canvas.tank(fluidOutput, 88, 48, 16, 16, fluidOutput.getAmount()).recipeContext(this);
        }
    }
}
