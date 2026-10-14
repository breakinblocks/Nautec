package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.bacteria.BacteriaLineage;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StrainLineageCategory extends BacteriaCategory<BacteriaLineage.Path> {
    static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    public static final ResourceLocation UID = Nautec.rl("strain_lineage");
    public static final RecipeType<BacteriaLineage.Path> RECIPE_TYPE = new RecipeType<>(UID, BacteriaLineage.Path.class);

    public static final int MAX_ROWS = 8;
    private static final int HEADER = 12;
    private static final int ROW = 20;
    private static final int CATALYST_X = 22;
    private static final int ARROW_X = 44;
    private static final int CHANCE_X = 72;
    private static final int OUTPUT_X = 102;
    private static final int WIDTH = OUTPUT_X + 18;
    private static final int HEIGHT = HEADER + ROW * MAX_ROWS;

    public StrainLineageCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.strain_lineage"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.MUTATOR.get())),
                WIDTH,
                HEIGHT);
    }

    private static List<BacteriaMutationRecipe> rows(BacteriaLineage.Path path) {
        List<BacteriaMutationRecipe> steps = path.steps();
        return steps.size() > MAX_ROWS ? steps.subList(steps.size() - MAX_ROWS, steps.size()) : steps;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BacteriaLineage.Path path, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(NTJeiUtil.maxStatDish(path.target()));
        List<BacteriaMutationRecipe> rows = rows(path);
        for (int i = 0; i < rows.size(); i++) {
            BacteriaMutationRecipe step = rows.get(i);
            int y = HEADER + i * ROW;
            addBacteriaSlot(path, 0, y, step.inputBacteria());
            addBacteriaSlot(path, OUTPUT_X, y, step.resultBacteria());
            NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.RENDER_ONLY, CATALYST_X + 1, y + 1).addIngredients(step.catalyst());
        }
    }

    @Override
    public void draw(BacteriaLineage.Path path, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        List<BacteriaMutationRecipe> rows = rows(path);
        for (int i = 0; i < rows.size(); i++) {
            NTJeiUtil.blitSprite(guiGraphics, ARROW_SPRITE, ARROW_X, HEADER + i * ROW + 4, 24, 10);
        }
        super.draw(path, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BacteriaLineage.Path path, IFocusGroup focuses) {
        Font font = Minecraft.getInstance().font;
        builder.addText(Component.translatable("nautec.jei.lineage.header", path.steps().size()), WIDTH, font.lineHeight)
                .setPosition(0, 0)
                .setColor(0xFF808080)
                .setShadow(false);
        List<BacteriaMutationRecipe> rows = rows(path);
        for (int i = 0; i < rows.size(); i++) {
            String chance = Math.round(rows.get(i).chance()) + "%";
            builder.addText(Component.literal(chance), OUTPUT_X - CHANCE_X, font.lineHeight)
                    .setPosition(CHANCE_X, HEADER + i * ROW + 5)
                    .setColor(0xFF808080)
                    .setShadow(false);
        }
    }
}
