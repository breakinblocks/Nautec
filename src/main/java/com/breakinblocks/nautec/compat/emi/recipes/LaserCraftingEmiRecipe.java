package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.LaserCraftingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import dev.emi.emi.api.neoforge.NeoForgeEmiIngredient;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LaserCraftingEmiRecipe extends NTEmiRecipe<LaserCraftingRecipe> {
    private static final int WIDTH = 152;
    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 52;
    private static final int[] INPUT_TANK_X = {1, 19};
    private static final int INPUT_SLOT_X = 39;
    private static final int OUTPUT_SLOT_X = 97;
    private static final int[] OUTPUT_TANK_X = {117, 135};
    private static final int ARROW_X = 63;
    private static final int ARROW_Y = 18;

    private final List<EmiIngredient> items = new ArrayList<>();
    private final List<EmiStack> results = new ArrayList<>();
    private final List<EmiIngredient> fluids = new ArrayList<>();
    private final List<EmiStack> fluidResults = new ArrayList<>();
    private final int inputCapacity;
    private final int outputCapacity;

    public LaserCraftingEmiRecipe(ResourceLocation id, LaserCraftingRecipe recipe) {
        super(NTEmiPlugin.LASER_CRAFTING, id, recipe, WIDTH, TANK_HEIGHT + 4 + 2 * EmiCanvas.lineHeight());
        for (IngredientWithCount ingredient : recipe.ingredients()) {
            items.add(NTEmiStacks.of(ingredient));
        }
        inputs.addAll(items);
        int inputMax = 0;
        for (SizedFluidIngredient ingredient : recipe.fluidIngredients()) {
            EmiIngredient fluid = NeoForgeEmiIngredient.of(ingredient);
            fluids.add(fluid);
            inputs.add(fluid);
            inputMax = Math.max(inputMax, ingredient.amount());
            for (FluidStack stack : ingredient.ingredient().getStacks()) {
                EmiStack bucket = NTEmiStacks.bucket(stack.getFluid());
                if (!bucket.isEmpty()) {
                    inputs.add(bucket);
                }
            }
        }
        this.inputCapacity = Math.max(1000, inputMax);
        for (ItemStack result : recipe.results()) {
            EmiStack stack = EmiStack.of(result);
            results.add(stack);
            outputs.add(stack);
        }
        int outputMax = 0;
        for (FluidStack result : recipe.fluidResults()) {
            EmiStack stack = NeoForgeEmiStack.of(result);
            fluidResults.add(stack);
            outputs.add(stack);
            outputMax = Math.max(outputMax, result.getAmount());
            EmiStack bucket = NTEmiStacks.bucket(result.getFluid());
            if (!bucket.isEmpty()) {
                outputs.add(bucket);
            }
        }
        this.outputCapacity = Math.max(1000, outputMax);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        for (int x : INPUT_TANK_X) {
            tankFrame(canvas, x);
        }
        for (int x : OUTPUT_TANK_X) {
            tankFrame(canvas, x);
        }
        for (int i = 0; i < LaserCraftingRecipe.MAX_ITEM_INPUTS; i++) {
            canvas.slot(i < items.size() ? items.get(i) : EmiStack.EMPTY, INPUT_SLOT_X, 1 + i * 18);
        }
        for (int i = 0; i < LaserCraftingRecipe.MAX_ITEM_OUTPUTS; i++) {
            if (i < results.size()) {
                canvas.output(results.get(i), OUTPUT_SLOT_X, 1 + i * 18);
            } else {
                canvas.slot(EmiStack.EMPTY, OUTPUT_SLOT_X, 1 + i * 18);
            }
        }
        for (int i = 0; i < fluids.size() && i < INPUT_TANK_X.length; i++) {
            canvas.tank(fluids.get(i), INPUT_TANK_X[i], 1, TANK_WIDTH, TANK_HEIGHT, inputCapacity).drawBack(false);
        }
        for (int i = 0; i < fluidResults.size() && i < OUTPUT_TANK_X.length; i++) {
            canvas.tank(fluidResults.get(i), OUTPUT_TANK_X[i], 1, TANK_WIDTH, TANK_HEIGHT, outputCapacity).drawBack(false).recipeContext(this);
        }
        canvas.arrow(ARROW_X, ARROW_Y, recipe.duration());
        int line = EmiCanvas.lineHeight();
        int y = TANK_HEIGHT + 4;
        canvas.text(Component.translatable("nautec.jei.laser_crafting.power", recipe.power()), 0, y, WIDTH / 2, EmiCanvas.Align.LEFT, EmiCanvas.DARK, false);
        canvas.text(Component.translatable("nautec.jei.purity_value", recipe.purity()), WIDTH / 2, y, WIDTH / 2, EmiCanvas.Align.RIGHT, EmiCanvas.DARK, false);
        canvas.text(Component.translatable("nautec.jei.seconds", String.format(Locale.ROOT, "%.1f", recipe.duration() / 20F)), 0, y + line, WIDTH,
                EmiCanvas.Align.CENTER, EmiCanvas.GRAY, false);
    }

    private static void tankFrame(EmiCanvas canvas, int x) {
        canvas.fill(x - 1, 0, x + TANK_WIDTH + 1, TANK_HEIGHT + 2, 0xFF1E2221);
        canvas.fill(x, 1, x + TANK_WIDTH, TANK_HEIGHT + 1, 0xFF16201F);
    }
}
