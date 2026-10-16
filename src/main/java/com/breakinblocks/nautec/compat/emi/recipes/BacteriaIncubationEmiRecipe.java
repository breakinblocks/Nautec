package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.BacteriaIncubationRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BacteriaIncubationEmiRecipe extends NTEmiRecipe<BacteriaIncubationRecipe> {
    private static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/incubator/progress_arrow_off");
    private static final int GAP = 5;
    private static final int SLOT_SIZE = 18;
    private static final int ARROW_WIDTH = 79;
    private static final int Y_GAP_BETWEEN_IN_CATA = 29;
    private static final int GAP_BETWEEN_SLOT_ARROW = 3;
    private static final int WIDTH = GAP * 2 + SLOT_SIZE * 2 + ARROW_WIDTH + GAP_BETWEEN_SLOT_ARROW * 2;
    private static final int HEIGHT = GAP * 2 + SLOT_SIZE + Y_GAP_BETWEEN_IN_CATA + 8;

    private final EmiIngredient nutrient;

    public BacteriaIncubationEmiRecipe(@Nullable ResourceLocation id, BacteriaIncubationRecipe recipe) {
        super(NTEmiPlugin.BACTERIA_INCUBATION, id, recipe, WIDTH, HEIGHT);
        this.nutrient = EmiIngredient.of(recipe.nutrient());
        inputs.add(NTEmiStacks.dish(recipe.bacteria()));
        inputs.add(nutrient);
        fit(0, Component.translatable("nautec.jei.growth", recipe.growth().toString()));
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        canvas.sprite(ARROW_SPRITE, GAP + SLOT_SIZE + GAP_BETWEEN_SLOT_ARROW + 17, GAP - 2, 46, 29);
        canvas.bacteria(recipe.bacteria(), WIDTH / 2 - SLOT_SIZE / 2 + 1, GAP + Y_GAP_BETWEEN_IN_CATA - 22);
        canvas.slot(nutrient, WIDTH / 2 - SLOT_SIZE / 2 + 2, GAP + Y_GAP_BETWEEN_IN_CATA);
        canvas.text(Component.literal(Math.round(recipe.consumeChance() * 100) + "%"), WIDTH / 2 + 15, HEIGHT - 26, WIDTH / 2,
                EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.growth", recipe.growth().toString()), 0, HEIGHT - 9, layoutWidth(),
                EmiCanvas.Align.CENTER, EmiCanvas.GRAY, false);
    }
}
