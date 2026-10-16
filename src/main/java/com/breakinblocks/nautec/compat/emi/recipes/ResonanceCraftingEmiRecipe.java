package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResonanceCraftingEmiRecipe extends NTEmiRecipe<ResonanceCraftingRecipe> {
    private final EmiIngredient input;
    private final EmiStack output;

    public ResonanceCraftingEmiRecipe(ResourceLocation id, ResonanceCraftingRecipe recipe) {
        super(NTEmiPlugin.RESONANCE_CRAFTING, id, recipe, 150, 46);
        this.input = EmiIngredient.of(recipe.ingredient());
        this.output = EmiStack.of(recipe.result());
        inputs.add(input);
        outputs.add(output);
        fit(0, Component.translatable("nautec.jei.resonance_critical"));
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.sprite(PressureForgingEmiRecipe.ARROW_SPRITE, 28, 0, 24, 16);
        canvas.slot(input, 0, 0);
        canvas.output(output, 64, 0);
        canvas.text(Component.translatable("nautec.jei.purity_value", recipe.purity()), 0, 22, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.resonance_critical"), 0, 34, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
    }
}
