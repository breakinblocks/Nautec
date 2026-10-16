package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class NTEmiStacks {
    private NTEmiStacks() {
    }

    public static EmiIngredient of(IngredientWithCount ingredient) {
        return EmiIngredient.of(ingredient.ingredient(), ingredient.count());
    }

    public static EmiStack bucket(Fluid fluid) {
        ItemStack bucket = new ItemStack(fluid.getBucket());
        return bucket.isEmpty() ? EmiStack.EMPTY : EmiStack.of(bucket);
    }

    public static EmiStack dish(ResourceKey<Bacteria> bacteria) {
        return EmiStack.of(BacteriaHelper.getMaxStatDish(bacteria, Minecraft.getInstance().level.registryAccess()));
    }

    public static @Nullable ResourceLocation strain(EmiStack stack) {
        ComponentBacteriaStorage storage = stack.get(NTDataComponents.BACTERIA.get());
        if (storage == null) {
            return null;
        }
        BacteriaInstance instance = storage.bacteriaInstance();
        return instance.isEmpty() ? null : instance.getBacteria().location();
    }

    public static ItemStack item(EmiIngredient ingredient) {
        for (EmiStack stack : ingredient.getEmiStacks()) {
            ItemStack item = stack.getItemStack();
            if (!item.isEmpty()) {
                return item.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    public static FluidStack fluid(EmiIngredient ingredient) {
        for (EmiStack stack : ingredient.getEmiStacks()) {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            if (fluid != null) {
                FluidStack result = new FluidStack(fluid, (int) Math.max(1000, Math.min(Integer.MAX_VALUE, stack.getAmount())));
                result.applyComponents(stack.getComponentChanges());
                return result;
            }
        }
        return FluidStack.EMPTY;
    }
}
