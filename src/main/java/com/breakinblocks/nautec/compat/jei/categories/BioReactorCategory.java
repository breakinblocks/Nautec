package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.utils.MathUtils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class BioReactorCategory extends BacteriaCategory<BioReactorCategory.BioReactorRecipe> {
    static final Identifier RIGHT_ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    public static final Identifier UID = Nautec.rl(BioReactorRecipe.NAME);
    public static final IRecipeType<BioReactorRecipe> RECIPE_TYPE =
            IRecipeType.create(UID, BioReactorRecipe.class);

    private static final int DRAWABLE_WIDTH = 96;
    private static final int DRAWABLE_HEIGHT = 34;
    private static final int ARROW_ROW_HEIGHT = 24;

    public BioReactorCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.bio_reactor"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.BIO_REACTOR.get())),
                DRAWABLE_WIDTH,
                DRAWABLE_HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BioReactorRecipe recipe, IFocusGroup focuses) {
        Item item = recipe.resource().resolve();
        if (item != Items.AIR) {
            builder.addOutputSlot(getWidth() - 18, 3).add(item);
        }

        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(NTJeiUtil.maxStatDish(recipe.bacteria()));

        addBacteriaSlot(recipe, 0, 3, recipe.bacteria);
    }

    @Override
    public void draw(BioReactorRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        NTJeiUtil.blitSprite(guiGraphics, RIGHT_ARROW_SPRITE, getWidth() / 2 - 12, ARROW_ROW_HEIGHT / 2 - 5, 24, 10);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, BioReactorRecipe recipe, IFocusGroup focuses) {
        Font font = Minecraft.getInstance().font;
        Bacteria bacteria = BacteriaHelper.getBacteria(Minecraft.getInstance().level.registryAccess(), recipe.bacteria());

        if (!(bacteria.stats() instanceof SimpleBacteriaStats stats)) {
            return;
        }

        Component text = Component.translatable("nautec.jei.production",
                MathUtils.roundToPrecision(stats.productionRate().getMin(), 2),
                MathUtils.roundToPrecision(stats.productionRate().getMax(), 2));

        builder.addText(text, getWidth(), font.lineHeight)
                .setPosition(0, getHeight() - font.lineHeight)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setShadow(false);
    }

    public record BioReactorRecipe(ResourceKey<Bacteria> bacteria, Bacteria.Resource resource) {
        public static final String NAME = "bio_reactor";
    }
}
