package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.utils.Utils;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AugmentationEmiRecipe extends NTEmiRecipe<AugmentationRecipe> {
    private final List<EmiIngredient> items = new ArrayList<>();
    private final EmiIngredient augment;

    public AugmentationEmiRecipe(ResourceLocation id, AugmentationRecipe recipe) {
        super(NTEmiPlugin.AUGMENTATION, id, recipe, 80, 52);
        for (IngredientWithCount ingredient : recipe.ingredients()) {
            items.add(NTEmiStacks.of(ingredient));
        }
        List<EmiStack> parts = new ArrayList<>();
        for (ItemStack part : recipe.displayParts()) {
            parts.add(EmiStack.of(part));
        }
        this.augment = EmiIngredient.of(parts);
        inputs.addAll(items);
        catalysts.add(augment);
        fit(0, Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, recipe.resultAugment()));
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.text(Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, recipe.resultAugment()), 0, 0, width, EmiCanvas.Align.CENTER, EmiCanvas.WHITE, true);
        canvas.slot(augment, width / 2 - 9, 12);
        int start = width / 2 - items.size() * 10;
        for (int i = 0; i < items.size(); i++) {
            canvas.slot(items.get(i), start + i * 20 + 1, 32);
        }
    }
}
