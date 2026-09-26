package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
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

public class PressureForgingRecipeCategory extends AbstractRecipeCategory<PressureForgingRecipe> {
    static final Identifier BURN_PROGRESS_SPRITE = Nautec.rl("container/furnace/empty_arrow");
    public static final Identifier UID = Nautec.rl("pressure_forging");
    public static final IRecipeType<PressureForgingRecipe> RECIPE_TYPE =
            IRecipeType.create(UID, PressureForgingRecipe.class);

    public PressureForgingRecipeCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.pressure_forging"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.PRESSURE_FORGE.get())),
                180,
                80);
    }

    @Override
    public void draw(PressureForgingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        NTJeiUtil.blitSprite(guiGraphics, BURN_PROGRESS_SPRITE, 28, 0, 24, 16);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, PressureForgingRecipe recipe, IFocusGroup focuses) {
        int fontSize = Minecraft.getInstance().font.lineHeight;
        builder.addText(Component.translatable("nautec.jei.purity_value", recipe.purity()), getWidth(), fontSize)
                .setPosition(0, 22).setColor(0xFF808080).setShadow(false);
        builder.addText(Component.translatable("nautec.jei.pressure_depth", Math.min(recipe.minDepth(), NTConfig.pressureForgeDepth)), getWidth(), fontSize)
                .setPosition(0, 34).setColor(0xFF808080).setShadow(false);
        builder.addText(Component.translatable("nautec.jei.pressure_requirements", NTConfig.pressureForgeWaterColumn, NTConfig.pressureForgePowerUsage), getWidth(), fontSize)
                .setPosition(0, 58).setColor(0xFF808080).setShadow(false);
        builder.addText(Component.translatable("nautec.jei.seconds", recipe.duration() / 20f), getWidth(), fontSize)
                .setPosition(0, 46).setColor(0xFF808080).setShadow(false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressureForgingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 0, 0).add(recipe.ingredient());
        if (NTConfig.pressureForgeAcidUsage > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 100, 0)
                    .add(NTFluids.ETCHING_ACID.getStillFluid(), NTConfig.pressureForgeAcidUsage)
                    .setFluidRenderer(NTConfig.pressureForgeAcidUsage, true, 16, 16);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 64, 0).add(recipe.result());
    }
}
