package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.content.recipes.PressureForgingRecipe;
import com.breakinblocks.nautec.registries.NTFluids;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PressureForgingEmiRecipe extends NTEmiRecipe<PressureForgingRecipe> {
    static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/furnace/empty_arrow");
    private final EmiIngredient input;
    private final EmiStack acid;
    private final EmiStack output;

    public PressureForgingEmiRecipe(ResourceLocation id, PressureForgingRecipe recipe) {
        super(NTEmiPlugin.PRESSURE_FORGING, id, recipe, 180, 80);
        this.input = EmiIngredient.of(recipe.ingredient());
        this.acid = NTConfig.pressureForgeAcidUsage > 0
                ? EmiStack.of(NTFluids.ETCHING_ACID.getStillFluid(), NTConfig.pressureForgeAcidUsage)
                : EmiStack.EMPTY;
        this.output = EmiStack.of(recipe.result());
        inputs.add(input);
        if (!acid.isEmpty()) {
            inputs.add(acid);
        }
        outputs.add(output);
        fit(0, Component.translatable("nautec.jei.pressure_depth", Math.min(recipe.minDepth(), NTConfig.pressureForgeDepth)));
        fit(0, Component.translatable("nautec.jei.pressure_requirements", NTConfig.pressureForgeWaterColumn, NTConfig.pressureForgePowerUsage));
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.sprite(ARROW_SPRITE, 28, 0, 24, 16);
        canvas.slot(input, 0, 0);
        if (!acid.isEmpty()) {
            canvas.tank(acid, 100, 0, 16, 16, NTConfig.pressureForgeAcidUsage);
        }
        canvas.output(output, 64, 0);
        canvas.text(Component.translatable("nautec.jei.purity_value", recipe.purity()), 0, 22, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.pressure_depth", Math.min(recipe.minDepth(), NTConfig.pressureForgeDepth)),
                0, 34, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.seconds", recipe.duration() / 20f), 0, 46, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        canvas.text(Component.translatable("nautec.jei.pressure_requirements", NTConfig.pressureForgeWaterColumn, NTConfig.pressureForgePowerUsage),
                0, 58, width, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
    }
}
