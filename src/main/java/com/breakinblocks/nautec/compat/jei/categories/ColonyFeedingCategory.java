package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class ColonyFeedingCategory extends BacteriaCategory<ColonyFeedingRecipe> {
    static final ResourceLocation RIGHT_ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    public static final ResourceLocation UID = Nautec.rl(ColonyFeedingRecipe.NAME);
    public static final RecipeType<ColonyFeedingRecipe> RECIPE_TYPE = new RecipeType<>(UID, ColonyFeedingRecipe.class);

    private static final int DRAWABLE_WIDTH = 116;
    private static final int DRAWABLE_HEIGHT = 44;
    private static final int NUTRIENT_X = 7;
    private static final int ARROW_X = 32;
    private static final int COLONY_X = 62;
    private static final int ROW_Y = 3;

    public ColonyFeedingCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.colony_feeding"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.BIO_REACTOR.get())),
                DRAWABLE_WIDTH,
                DRAWABLE_HEIGHT);
    }

    private static Optional<ResourceKey<Bacteria>> knownStrain(ColonyFeedingRecipe recipe) {
        return recipe.bacteria().flatMap(BacteriaSelector::key)
                .filter(key -> BacteriaHelper.findBacteria(Minecraft.getInstance().level.registryAccess(), key) != null);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ColonyFeedingRecipe recipe, IFocusGroup focuses) {
        NTJeiUtil.addIngredientWithCount(NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, NUTRIENT_X + 1, ROW_Y + 1), recipe.ingredient());

        Optional<ResourceKey<Bacteria>> strain = knownStrain(recipe);
        if (strain.isPresent()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(NTJeiUtil.maxStatDish(strain.get()));
            addBacteriaSlot(recipe, COLONY_X, ROW_Y, strain.get());
        }
    }

    @Override
    public void draw(@NotNull ColonyFeedingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        NTJeiUtil.blitSprite(guiGraphics, RIGHT_ARROW_SPRITE, ARROW_X, ROW_Y + 4, 24, 10);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ColonyFeedingRecipe recipe, IFocusGroup focuses) {
        Font font = Minecraft.getInstance().font;

        if (knownStrain(recipe).isEmpty()) {
            Component target = recipe.bacteria()
                    .map(selector -> selector.tag()
                            ? Component.translatable("nautec.jei.colony_feeding.tag", "#" + selector.id())
                            : Component.translatable("nautec.jei.colony_feeding.strain", selector.id().toString()))
                    .orElse(Component.translatable("nautec.jei.colony_feeding.any"));
            builder.addText(target, DRAWABLE_WIDTH - COLONY_X + 4, font.lineHeight * 2)
                    .setPosition(COLONY_X - 4, ROW_Y + 1)
                    .setColor(0xFF808080)
                    .setShadow(false);
        }

        String seconds = String.format(Locale.ROOT, "%.1f", recipe.vitalityTicks() / 20f);
        Component vitality = Component.translatable("nautec.jei.colony_feeding.vitality", recipe.vitalityTicks(), seconds);
        builder.addText(vitality, getWidth(), font.lineHeight)
                .setPosition(0, getHeight() - font.lineHeight)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setShadow(false);
    }
}
