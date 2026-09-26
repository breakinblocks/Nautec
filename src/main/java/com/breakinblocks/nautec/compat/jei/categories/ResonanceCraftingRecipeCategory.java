package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.ResonanceCraftingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ResonanceCraftingRecipeCategory extends AbstractRecipeCategory<ResonanceCraftingRecipe> {
    static final Identifier BURN_PROGRESS_SPRITE = Nautec.rl("container/furnace/empty_arrow");
    public static final Identifier UID = Nautec.rl("resonance_crafting");
    public static final IRecipeType<ResonanceCraftingRecipe> RECIPE_TYPE =
            IRecipeType.create(UID, ResonanceCraftingRecipe.class);

    public ResonanceCraftingRecipeCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.resonance_crafting"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.RESONANCE_CHAMBER.get())),
                150,
                65);
    }

    @Override
    public void draw(ResonanceCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        NTJeiUtil.blitSprite(guiGraphics, BURN_PROGRESS_SPRITE, 28, 0, 24, 16);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ResonanceCraftingRecipe recipe, IFocusGroup focuses) {
        int fontSize = Minecraft.getInstance().font.lineHeight;
        builder.addText(Component.translatable("nautec.jei.purity_value", recipe.purity()), getWidth(), fontSize)
                .setPosition(0, 22).setColor(0xFF808080).setShadow(false);
        builder.addText(Component.translatable("nautec.jei.resonance_critical"), getWidth(), fontSize)
                .setPosition(0, 34).setColor(0xFF808080).setShadow(false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ResonanceCraftingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 0).add(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 64, 0).add(recipe.result());
    }
}
