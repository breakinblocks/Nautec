package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BacteriaMutationEmiRecipe extends NTEmiRecipe<BacteriaMutationRecipe> {
    private static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/mutator/progress_arrow_off");
    private static final int GAP = 5;
    private static final int SLOT_SIZE = 18;
    private static final int ARROW_WIDTH = 62;
    private static final int ARROW_HEIGHT = 14;
    private static final int Y_GAP_BETWEEN_IN_CATA = 29;
    private static final int GAP_BETWEEN_SLOT_ARROW = 3;
    private static final int WIDTH = GAP * 2 + SLOT_SIZE * 2 + ARROW_WIDTH + GAP_BETWEEN_SLOT_ARROW * 2;
    private static final int HEIGHT = GAP * 2 + SLOT_SIZE + Y_GAP_BETWEEN_IN_CATA + 8;

    private final EmiIngredient catalyst;

    public BacteriaMutationEmiRecipe(@Nullable ResourceLocation id, BacteriaMutationRecipe recipe) {
        super(NTEmiPlugin.BACTERIA_MUTATIONS, id, recipe, WIDTH, HEIGHT);
        this.catalyst = EmiIngredient.of(recipe.catalyst());
        inputs.add(NTEmiStacks.dish(recipe.inputBacteria()));
        inputs.add(catalyst);
        outputs.add(NTEmiStacks.dish(recipe.resultBacteria()));
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        Font font = Minecraft.getInstance().font;
        canvas.sprite(ARROW_SPRITE, GAP + SLOT_SIZE + GAP_BETWEEN_SLOT_ARROW, GAP + 8 - 1, ARROW_WIDTH, ARROW_HEIGHT);
        canvas.bacteria(recipe.inputBacteria(), GAP - 1, GAP + 7);
        canvas.bacteria(recipe.resultBacteria(), WIDTH - GAP - SLOT_SIZE, GAP + 7);
        canvas.slot(catalyst, WIDTH / 2 - SLOT_SIZE / 2 + 1, GAP + 8 + Y_GAP_BETWEEN_IN_CATA);
        String chance = Math.round(recipe.chance()) + "%";
        int width = font.width(chance);
        canvas.text(Component.literal(chance), WIDTH - GAP - SLOT_SIZE - (width - 18) / 2, GAP + 8 - 3 - font.lineHeight, width + 2,
                EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
    }
}
