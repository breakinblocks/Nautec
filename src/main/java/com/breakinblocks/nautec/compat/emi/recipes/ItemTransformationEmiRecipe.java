package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.ItemTransformationRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ItemTransformationEmiRecipe extends NTEmiRecipe<ItemTransformationRecipe> {
    private final EmiIngredient input;
    private final EmiStack output;

    public ItemTransformationEmiRecipe(ResourceLocation id, ItemTransformationRecipe recipe) {
        super(NTEmiPlugin.ITEM_TRANSFORMATION, id, recipe, 80, 40);
        this.input = NTEmiStacks.of(recipe.ingredient());
        this.output = EmiStack.of(recipe.result());
        inputs.add(input);
        outputs.add(output);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.sprite(PressureForgingEmiRecipe.ARROW_SPRITE, 28, 0, 24, 16);
        canvas.slot(input, 0, 0);
        canvas.output(output, 64, 0);
        canvas.text(Component.translatable("nautec.jei.seconds", (float) recipe.duration() / 20), 0, 20, width / 2,
                EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.purity_value", recipe.purity()), 0, 20, width, EmiCanvas.Align.RIGHT, EmiCanvas.GRAY, false);
        if (recipe.power() > 0) {
            canvas.text(Component.translatable("nautec.jei.min_power", recipe.power()), 0, 30, width, EmiCanvas.Align.RIGHT, EmiCanvas.GRAY, false);
        }
    }
}
