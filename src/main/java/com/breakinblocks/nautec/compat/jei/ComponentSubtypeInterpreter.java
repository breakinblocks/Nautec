package com.breakinblocks.nautec.compat.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ComponentSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    private final DataComponentType<?>[] types;

    public ComponentSubtypeInterpreter(DataComponentType<?>[] types) {
        this.types = types;
    }

    @Override
    public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
        List<Object> values = new ArrayList<>(types.length);
        boolean any = false;
        for (DataComponentType<?> type : types) {
            Object value = stack.get(type);
            any |= value != null;
            values.add(value);
        }
        return any ? values : null;
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
        Object data = getSubtypeData(stack, context);
        return data == null ? "" : data.toString();
    }
}
