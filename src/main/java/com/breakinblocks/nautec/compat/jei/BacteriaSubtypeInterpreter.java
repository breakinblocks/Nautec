package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentBacteriaStorage;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BacteriaSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    public static final BacteriaSubtypeInterpreter INSTANCE = new BacteriaSubtypeInterpreter();

    private BacteriaSubtypeInterpreter() {
    }

    @Override
    public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
        ComponentBacteriaStorage storage = stack.get(NTDataComponents.BACTERIA);
        if (storage == null) {
            return null;
        }
        BacteriaInstance instance = storage.bacteriaInstance();
        if (instance.isEmpty()) {
            return null;
        }
        String strain = instance.getBacteria().identifier().toString();
        return context == UidContext.Ingredient && instance.isAnalyzed() ? strain + ";analyzed" : strain;
    }
}
