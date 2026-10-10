package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class NTJeiUtil {
    public static final ResourceLocation SINGLE_SLOT_SPRITE = Nautec.rl("container/furnace/empty_slot");
    public static final int SLOT_SIZE = 18;

    private NTJeiUtil() {
    }

    public static IRecipeSlotBuilder addFramedSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y) {
        return builder.addSlot(role, x, y).setBackground(sprite(SINGLE_SLOT_SPRITE, SLOT_SIZE, SLOT_SIZE), -1, -1);
    }

    public static ItemStack maxStatDish(ResourceKey<Bacteria> bacteria) {
        return BacteriaHelper.getMaxStatDish(bacteria, Minecraft.getInstance().level.registryAccess());
    }

    public static IDrawable sprite(ResourceLocation sprite, int width, int height) {
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
            public void draw(GuiGraphics guiGraphics, int x, int y) {
                NTGui.blitSprite(guiGraphics, sprite, x, y, width, height);
            }
        };
    }

    public static void blitSprite(GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height) {
        NTGui.blitSprite(guiGraphics, sprite, x, y, width, height);
    }

    public static void addIngredientWithCount(IRecipeSlotBuilder slot, IngredientWithCount ingredient) {
        if (ingredient.count() > 1) {
            ItemStack[] items = ingredient.ingredient().getItems();
            List<ItemStack> stacks = new ArrayList<>(items.length);
            for (ItemStack item : items) {
                stacks.add(item.copyWithCount(ingredient.count()));
            }
            slot.addItemStacks(stacks);
        } else {
            slot.addIngredients(ingredient.ingredient());
        }
    }
}
