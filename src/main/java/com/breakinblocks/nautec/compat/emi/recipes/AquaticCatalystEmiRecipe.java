package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class AquaticCatalystEmiRecipe extends NTEmiRecipe<AquaticCatalystChannelingRecipe> {
    private final EmiIngredient input;
    private final Component[] text;

    public AquaticCatalystEmiRecipe(ResourceLocation id, AquaticCatalystChannelingRecipe recipe) {
        super(NTEmiPlugin.AQUATIC_CATALYST_CHANNELING, id, recipe, 136, 24 + 5 * EmiCanvas.lineHeight());
        this.input = EmiIngredient.of(recipe.ingredient());
        inputs.add(input);
        int perTick = recipe.powerAmount() / Math.max(1, recipe.duration());
        this.text = new Component[]{
                Component.translatable("nautec.jei.power_per_tick", perTick),
                Component.translatable("nautec.jei.total_power", recipe.powerAmount()),
                Component.translatable("nautec.jei.duration_ticks", recipe.duration()),
                Component.translatable("nautec.jei.purity", recipe.purity()),
                Component.translatable("nautec.jei.advanced_catalyst", perTick * NTConfig.advancedCatalystFuelMultiplier,
                        String.format(Locale.ROOT, "%.2f", recipe.purity() * NTConfig.advancedCatalystPurityMultiplier)),
        };
        for (Component line : text) {
            fit(0, line);
        }
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        int line = EmiCanvas.lineHeight();
        canvas.slot(input, width / 2 - 8, 0);
        for (int i = 0; i < text.length; i++) {
            canvas.text(text[i], 0, 18 + i * line, width, EmiCanvas.Align.CENTER, EmiCanvas.WHITE, true);
        }
    }
}
