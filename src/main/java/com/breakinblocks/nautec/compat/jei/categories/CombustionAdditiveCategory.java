package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.recipes.CombustionAdditiveRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CombustionAdditiveCategory extends AbstractRecipeCategory<CombustionAdditiveRecipe> {
    public static final Identifier UID = Nautec.rl(CombustionAdditiveRecipe.NAME);
    public static final IRecipeType<CombustionAdditiveRecipe> RECIPE_TYPE = IRecipeType.create(UID, CombustionAdditiveRecipe.class);
    private static final int WIDTH = 150;

    public CombustionAdditiveCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.combustion_additive"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.COMBUSTION_DYNAMO.get())),
                WIDTH,
                22 + 4 * Minecraft.getInstance().font.lineHeight);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CombustionAdditiveRecipe recipe, IFocusGroup focuses) {
        NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, WIDTH / 2 - 9, 0).add(recipe.ingredient());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, CombustionAdditiveRecipe recipe, IFocusGroup focuses) {
        int line = Minecraft.getInstance().font.lineHeight;
        Component[] text = {
                Component.translatable("nautec.jei.combustion.output_multiplier", format(recipe.outputMultiplier())),
                Component.translatable("nautec.jei.combustion.output", Math.round(CombustionDynamoBlockEntity.baseRate() * recipe.outputMultiplier())),
                Component.translatable("nautec.jei.combustion.fuel_multiplier", format(recipe.fuelMultiplier())),
                Component.translatable("nautec.jei.combustion.duration", recipe.duration() / 20),
        };
        for (int i = 0; i < text.length; i++) {
            builder.addText(text[i], getWidth(), line)
                    .setPosition(0, 22 + i * line)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFFFFFFFF)
                    .setShadow(true);
        }
    }

    private static String format(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.valueOf(value);
    }
}
