package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.tags.NTTags;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class CombustionFuelEmiRecipe extends NTEmiRecipe<CombustionFuelEmiRecipe.Fuel> {
    private static final int WIDTH = 150;
    private static final int AMOUNT = 1000;

    private final EmiIngredient oil;
    private final EmiStack water;
    private final Component[] text;

    public CombustionFuelEmiRecipe() {
        super(NTEmiPlugin.COMBUSTION_DYNAMO, ResourceLocation.fromNamespaceAndPath("nautec", "/combustion_dynamo/fuel"), new Fuel(),
                WIDTH, 22 + 3 * EmiCanvas.lineHeight());
        List<EmiStack> oils = new ArrayList<>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(NTTags.Fluids.OIL)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState())) {
                oils.add(EmiStack.of(fluid, AMOUNT));
            }
        }
        this.oil = EmiIngredient.of(oils);
        this.water = EmiStack.of(Fluids.WATER, AMOUNT);
        inputs.add(oil);
        for (EmiStack stack : oils) {
            Fluid fluid = stack.getKeyOfType(Fluid.class);
            if (fluid != null) {
                EmiStack bucket = NTEmiStacks.bucket(fluid);
                if (!bucket.isEmpty()) {
                    inputs.add(bucket);
                }
            }
        }
        inputs.add(water);
        this.text = new Component[]{
                Component.translatable("nautec.jei.combustion.output", CombustionDynamoBlockEntity.baseRate()),
                Component.translatable("nautec.jei.combustion.oil", 1, NTConfig.combustionDynamoTicksPerOil),
                Component.translatable("nautec.jei.combustion.water", NTConfig.combustionDynamoWaterPerTick),
        };
        for (Component line : text) {
            fit(0, line);
        }
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.tank(oil, width / 2 - 20, 0, 16, 16, AMOUNT);
        canvas.tank(water, width / 2 + 2, 0, 16, 16, AMOUNT);
        int line = EmiCanvas.lineHeight();
        for (int i = 0; i < text.length; i++) {
            canvas.text(text[i], 0, 22 + i * line, width, EmiCanvas.Align.CENTER, EmiCanvas.WHITE, true);
        }
    }

    public record Fuel() {
    }
}
