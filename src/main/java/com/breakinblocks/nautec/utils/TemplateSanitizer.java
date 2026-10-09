package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class TemplateSanitizer {
    public static final int MAX_TEMPLATE_BYTES = 1024;
    public static final int MAX_DISH_TEMPLATE_BYTES = 8192;

    private TemplateSanitizer() {
    }

    public static ItemStack item(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (DishPort.isDish(stack)) {
            ItemStack dish = new ItemStack(stack.getItem(), stack.getCount());
            ComponentBacteriaStorage colony = stack.get(NTDataComponents.BACTERIA.get());
            if (colony != null) {
                dish.set(NTDataComponents.BACTERIA.get(), colony);
            }
            return dish;
        }
        ItemStack template = stack.copy();
        template.remove(DataComponents.CUSTOM_DATA);
        return template;
    }

    public static ItemStack item(ItemStack stack, HolderLookup.Provider registries) {
        ItemStack template = item(stack);
        if (template.isEmpty() || fits(template, registries)) {
            return template;
        }
        return new ItemStack(template.getItem(), template.getCount());
    }

    public static FluidStack fluid(FluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack.getFluid(), 1);
    }

    public static boolean fits(ItemStack template, HolderLookup.Provider registries) {
        return ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), template)
                .result()
                .map(tag -> tag.sizeInBytes() <= (DishPort.isDish(template) ? MAX_DISH_TEMPLATE_BYTES : MAX_TEMPLATE_BYTES))
                .orElse(false);
    }
}
