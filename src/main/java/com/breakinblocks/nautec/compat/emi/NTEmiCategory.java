package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.Nautec;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public class NTEmiCategory extends EmiRecipeCategory {
    private final String translationKey;

    public NTEmiCategory(String path, String nameKey, ItemLike icon) {
        this(path, nameKey, EmiStack.of(icon));
    }

    public NTEmiCategory(String path, String nameKey, EmiStack icon) {
        super(Nautec.rl(path), icon);
        this.translationKey = "nautec.jei.category." + nameKey;
    }

    @Override
    public Component getName() {
        return Component.translatable(translationKey);
    }
}
