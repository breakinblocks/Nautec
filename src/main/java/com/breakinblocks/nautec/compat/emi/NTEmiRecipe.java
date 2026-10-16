package com.breakinblocks.nautec.compat.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public abstract class NTEmiRecipe<R> extends BasicEmiRecipe {
    protected final R recipe;

    protected NTEmiRecipe(EmiRecipeCategory category, @Nullable ResourceLocation id, R recipe, int width, int height) {
        super(category, id, EmiCanvas.size(width), EmiCanvas.size(height));
        this.recipe = recipe;
    }

    public R recipe() {
        return recipe;
    }

    protected void fit(int x, Component text) {
        width = Math.max(width, EmiCanvas.size(x + Minecraft.getInstance().font.width(text)));
    }

    protected int layoutWidth() {
        return width - 2 * EmiCanvas.PAD;
    }

    protected int layoutHeight() {
        return height - 2 * EmiCanvas.PAD;
    }

    @Override
    public final void addWidgets(WidgetHolder widgets) {
        addWidgets(new EmiCanvas(widgets, this));
    }

    protected abstract void addWidgets(EmiCanvas canvas);
}
