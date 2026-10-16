package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CombustionAdditiveEmiRecipe extends NTEmiRecipe<CombustionAdditiveRecipe> {
    private static final int WIDTH = 150;

    private final EmiIngredient additive;
    private final Component[] text;

    public CombustionAdditiveEmiRecipe(ResourceLocation id, CombustionAdditiveRecipe recipe) {
        super(NTEmiPlugin.COMBUSTION_ADDITIVE, id, recipe, WIDTH, 22 + 4 * EmiCanvas.lineHeight());
        this.additive = EmiIngredient.of(recipe.ingredient());
        inputs.add(additive);
        this.text = new Component[]{
                Component.translatable("nautec.jei.combustion.output_multiplier", format(recipe.outputMultiplier())),
                Component.translatable("nautec.jei.combustion.output", Math.round(CombustionDynamoBlockEntity.baseRate() * recipe.outputMultiplier())),
                Component.translatable("nautec.jei.combustion.fuel_multiplier", format(recipe.fuelMultiplier())),
                Component.translatable("nautec.jei.combustion.duration", recipe.duration() / 20),
        };
        for (Component line : text) {
            fit(0, line);
        }
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.slot(additive, width / 2 - 9, 0);
        int line = EmiCanvas.lineHeight();
        for (int i = 0; i < text.length; i++) {
            canvas.text(text[i], 0, 22 + i * line, width, EmiCanvas.Align.CENTER, EmiCanvas.WHITE, true);
        }
    }

    private static String format(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.valueOf(value);
    }
}
