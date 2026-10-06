package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class LaserCraftingRecipeCategory extends AbstractRecipeCategory<LaserCraftingRecipe> {
    public static final Identifier UID = Nautec.rl(LaserCraftingRecipe.NAME);
    public static final IRecipeType<LaserCraftingRecipe> RECIPE_TYPE = IRecipeType.create(UID, LaserCraftingRecipe.class);

    private static final int WIDTH = 152;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 52;
    private static final int[] INPUT_TANK_X = {1, 19};
    private static final int INPUT_SLOT_X = 39;
    private static final int OUTPUT_SLOT_X = 97;
    private static final int[] OUTPUT_TANK_X = {117, 135};
    private static final int ARROW_X = 63;
    private static final int ARROW_Y = 18;

    public LaserCraftingRecipeCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.laser_crafting"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.LASER_CRAFTING_MATRIX.get())),
                WIDTH,
                TANK_HEIGHT + 4 + 2 * Minecraft.getInstance().font.lineHeight);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LaserCraftingRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < LaserCraftingRecipe.MAX_ITEM_INPUTS; i++) {
            IRecipeSlotBuilder slot = NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, INPUT_SLOT_X, 1 + i * 18);
            if (i < recipe.ingredients().size()) {
                NTJeiUtil.addIngredientWithCount(slot, recipe.ingredients().get(i));
            }
        }
        List<ItemStack> results = recipe.results();
        for (int i = 0; i < LaserCraftingRecipe.MAX_ITEM_OUTPUTS; i++) {
            IRecipeSlotBuilder slot = NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.OUTPUT, OUTPUT_SLOT_X, 1 + i * 18);
            if (i < results.size()) {
                slot.add(results.get(i));
            }
        }

        int inputCapacity = Math.max(1000, recipe.fluidIngredients().stream().mapToInt(SizedFluidIngredient::amount).max().orElse(0));
        for (int i = 0; i < recipe.fluidIngredients().size(); i++) {
            SizedFluidIngredient ingredient = recipe.fluidIngredients().get(i);
            IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, INPUT_TANK_X[i], 1)
                    .setFluidRenderer(inputCapacity, false, TANK_WIDTH, TANK_HEIGHT);
            for (Holder<Fluid> fluid : ingredient.ingredient().fluids()) {
                slot.add(fluid.value(), ingredient.amount());
                addBucket(builder, RecipeIngredientRole.INPUT, fluid.value());
            }
        }

        List<FluidStack> fluidResults = recipe.fluidResults();
        int outputCapacity = Math.max(1000, fluidResults.stream().mapToInt(FluidStack::getAmount).max().orElse(0));
        for (int i = 0; i < fluidResults.size(); i++) {
            FluidStack result = fluidResults.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_TANK_X[i], 1)
                    .setFluidRenderer(outputCapacity, false, TANK_WIDTH, TANK_HEIGHT)
                    .add(result.getFluid(), result.getAmount(), result.getComponentsPatch());
            addBucket(builder, RecipeIngredientRole.OUTPUT, result.getFluid());
        }
    }

    private static void addBucket(IRecipeLayoutBuilder builder, RecipeIngredientRole role, Fluid fluid) {
        ItemStack bucket = new ItemStack(fluid.getBucket());
        if (!bucket.isEmpty()) {
            builder.addInvisibleIngredients(role).add(bucket);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, LaserCraftingRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.duration()).setPosition(ARROW_X, ARROW_Y);
        int lineHeight = Minecraft.getInstance().font.lineHeight;
        int y = TANK_HEIGHT + 4;
        builder.addText(Component.translatable("nautec.jei.laser_crafting.power", recipe.power()), WIDTH / 2, lineHeight)
                .setPosition(0, y)
                .setColor(0xFF404040)
                .setShadow(false);
        builder.addText(Component.translatable("nautec.jei.purity_value", recipe.purity()), WIDTH / 2, lineHeight)
                .setPosition(WIDTH / 2, y)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setColor(0xFF404040)
                .setShadow(false);
        builder.addText(Component.translatable("nautec.jei.seconds", String.format(Locale.ROOT, "%.1f", recipe.duration() / 20F)), WIDTH, lineHeight)
                .setPosition(0, y + lineHeight)
                .setTextAlignment(HorizontalAlignment.CENTER)
                .setColor(0xFF808080)
                .setShadow(false);
    }

    @Override
    public void draw(@NotNull LaserCraftingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        for (int x : INPUT_TANK_X) {
            tankFrame(guiGraphics, x, 1);
        }
        for (int x : OUTPUT_TANK_X) {
            tankFrame(guiGraphics, x, 1);
        }
    }

    private static void tankFrame(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + TANK_WIDTH + 1, y + TANK_HEIGHT + 1, 0xFF1E2221);
        guiGraphics.fill(x, y, x + TANK_WIDTH, y + TANK_HEIGHT, 0xFF16201F);
    }
}
