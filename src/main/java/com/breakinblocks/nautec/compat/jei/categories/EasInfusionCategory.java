package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.stream.Stream;

public class EasInfusionCategory extends AbstractRecipeCategory<EasInfusionCategory.InfusionRecipe> {
    public static final ResourceLocation UID = Nautec.rl("eas_infusion");
    public static final RecipeType<InfusionRecipe> RECIPE_TYPE = new RecipeType<>(UID, InfusionRecipe.class);
    private static final int INFUSION_TICKS = 150;
    private static final int EAS_AMOUNT = 1000;

    public record InfusionRecipe(ItemStack input, ItemStack output) {
    }

    public EasInfusionCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.eas_infusion"),
                helper.createDrawableItemStack(new ItemStack(NTFluids.EAS.getBucket())),
                104,
                40);
    }

    public static List<InfusionRecipe> recipes() {
        return Stream.<ItemLike>of(NTItems.AQUARINE_PICKAXE, NTItems.AQUARINE_AXE, NTItems.AQUARINE_SHOVEL, NTItems.AQUARINE_HOE, NTItems.AQUARINE_SWORD)
                .map(item -> {
                    ItemStack input = new ItemStack(item);
                    ItemStack output = input.copy();
                    NTDataComponentsUtils.setInfusedStatus(output, true);
                    return new InfusionRecipe(input, output);
                })
                .toList();
    }

    @Override
    public void draw(InfusionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        NTJeiUtil.blitSprite(guiGraphics, ItemEtchingRecipeCategory.BURN_PROGRESS_SPRITE, 50, 0, 24, 16);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
        builder.addText(Component.translatable("nautec.jei.eas_infusion.hint", (float) INFUSION_TICKS / 20), getWidth(), Minecraft.getInstance().font.lineHeight * 2)
                .setPosition(0, 20)
                .setColor(0xFF808080)
                .setShadow(false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InfusionRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 0).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 24, 0)
                .addFluidStack(NTFluids.EAS.getStillFluid(), EAS_AMOUNT)
                .setFluidRenderer(EAS_AMOUNT, true, 16, 16);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 88, 0).addItemStack(recipe.output());
    }
}
