package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class NTJeiUtil {
    public static final Identifier SINGLE_SLOT_SPRITE = Nautec.rl("container/furnace/empty_slot");
    public static final int SLOT_SIZE = 18;

    private NTJeiUtil() {
    }

    public static IRecipeSlotBuilder addFramedSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y) {
        return builder.addSlot(role, x, y).setBackground(sprite(SINGLE_SLOT_SPRITE, SLOT_SIZE, SLOT_SIZE), -1, -1);
    }

    public static ItemStack maxStatDish(ResourceKey<Bacteria> bacteria) {
        return BacteriaHelper.getMaxStatDish(bacteria, Minecraft.getInstance().level.registryAccess());
    }

    public static IDrawable sprite(Identifier sprite, int width, int height) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return width;
            }

            @Override
            public int getHeight() {
                return height;
            }

            @Override
            public void draw(GuiGraphicsExtractor guiGraphics, int x, int y) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
            }
        };
    }

    public static void blitSprite(GuiGraphicsExtractor guiGraphics, Identifier sprite, int x, int y, int width, int height) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    public static void addIngredientWithCount(IRecipeSlotBuilder slot, IngredientWithCount ingredient) {
        if (ingredient.count() > 1) {
            slot.addItemStacks(ingredient.ingredient().items().map(holder -> new ItemStack(holder, ingredient.count())).toList());
        } else {
            slot.add(ingredient.ingredient());
        }
    }
}
