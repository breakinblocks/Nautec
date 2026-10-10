package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BacteriaGraftingCategory extends AbstractRecipeCategory<BacteriaGraftingCategory.GraftingRecipe> {
    public static final ResourceLocation UID = Nautec.rl(GraftingRecipe.NAME);
    public static final RecipeType<GraftingRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, GraftingRecipe.class);

    public BacteriaGraftingCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.bacteria_grafting"),
                helper.createDrawableItemStack(new ItemStack(NTItems.GRAFTING_TOOL.get())),
                132,
                42);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GraftingRecipe recipe, IFocusGroup focuses) {
        int y = 6;

        builder.addInputSlot(0, getHeight() / 2 - 9 + y).addItemStack(NTItems.PETRI_DISH.toStack());

        ItemStack stack = NTItems.PETRI_DISH.toStack();
        IBacteriaStorage bacteriaStorage = NTCapabilities.BacteriaStorage.ITEM.getCapability(stack, null);
        bacteriaStorage.setBacteria(0, BacteriaInstance.withMaxStats(recipe.val.bacteria(), Minecraft.getInstance().level.registryAccess()));
        builder.addOutputSlot(getWidth() - 18, getHeight() / 2 - 9 + y).addItemStack(stack);

        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, getWidth() / 2 - 9, getHeight() / 2 - 18 + y).addItemStack(NTItems.GRAFTING_TOOL.toStack());
        builder.addSlot(RecipeIngredientRole.INPUT, getWidth() / 2 - 9, getHeight() / 2 + y).addItemStack(new ItemStack(recipe.sample()));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, GraftingRecipe recipe, IFocusGroup focuses) {
        builder.addText(Component.translatable("nautec.jei.only_in", recipe.val.biome().location().toString()), getWidth(), Minecraft.getInstance().font.lineHeight)
                .setPosition(0, 0)
                .setColor(0xFF000000 | ChatFormatting.DARK_GRAY.getColor())
                .setShadow(false);
    }

    public record GraftingRecipe(Item sample, BacteriaObtainValue val) {
        public static final String NAME = "grafting";
    }
}
