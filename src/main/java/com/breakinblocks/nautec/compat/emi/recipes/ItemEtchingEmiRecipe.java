package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ItemEtchingEmiRecipe extends NTEmiRecipe<ItemEtchingRecipe> {
    private final EmiIngredient input;
    private final EmiStack output;

    public ItemEtchingEmiRecipe(ResourceLocation id, ItemEtchingRecipe recipe) {
        super(NTEmiPlugin.ITEM_ETCHING, id, recipe, 80, 30);
        this.input = NTEmiStacks.of(recipe.ingredient());
        this.output = EmiStack.of(recipe.result());
        inputs.add(input);
        outputs.add(output);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        canvas.sprite(PressureForgingEmiRecipe.ARROW_SPRITE, 28, 0, 24, 16);
        canvas.slot(input, 0, 0);
        canvas.output(output, 64, 0);
        canvas.text(Component.translatable("nautec.jei.seconds", (float) recipe.duration() / 20), 0, 20, layoutWidth(),
                EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
    }
}
