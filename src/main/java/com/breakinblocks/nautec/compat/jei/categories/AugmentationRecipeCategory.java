package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.Utils;
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

import java.util.List;

public class AugmentationRecipeCategory extends AbstractRecipeCategory<AugmentationRecipe> {
    public static final Identifier UID = Nautec.rl("augmentation");
    public static final IRecipeType<AugmentationRecipe> RECIPE_TYPE =
            IRecipeType.create(UID, AugmentationRecipe.class);

    public AugmentationRecipeCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.augmentation_effects"),
                helper.createDrawableItemStack(new ItemStack(NTItems.CLAW_ROBOT_ARM.get())),
                80,
                64);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AugmentationRecipe recipe, IFocusGroup focuses) {
        List<IngredientWithCount> ingredients = recipe.ingredients();
        int width = getWidth() / 2 - (ingredients.size() * 10);

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, getWidth() / 2 - 8 - 1, 12)
                .add(recipe.augmentItem().getDefaultInstance());

        for (int i = 0; i < ingredients.size(); i++) {
            IngredientWithCount ingredient = ingredients.get(i);
            NTJeiUtil.addIngredientWithCount(
                    NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, width + i * 20 + 1, 32), ingredient);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AugmentationRecipe recipe, IFocusGroup focuses) {
        builder.addText(Utils.registryTranslation(NTRegistries.AUGMENT_TYPE, recipe.resultAugment()), getWidth(), Minecraft.getInstance().font.lineHeight)
                .setPosition(0, 0)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFFFFFFFF)
                .setShadow(true);
    }
}
