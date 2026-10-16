package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class EasInfusionEmiRecipe extends NTEmiRecipe<EasInfusionEmiRecipe.Infusion> {
    private static final int INFUSION_TICKS = 150;
    private static final int EAS_AMOUNT = 1000;

    private final EmiStack input;
    private final EmiStack eas;
    private final EmiStack output;

    public EasInfusionEmiRecipe(Infusion infusion) {
        super(NTEmiPlugin.EAS_INFUSION, id(infusion), infusion, 104, 40);
        this.input = EmiStack.of(infusion.input());
        this.eas = EmiStack.of(NTFluids.EAS.getStillFluid(), EAS_AMOUNT);
        this.output = EmiStack.of(infusion.output());
        inputs.add(input);
        inputs.add(eas);
        outputs.add(output);
    }

    private static ResourceLocation id(Infusion infusion) {
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(infusion.input().getItem());
        return ResourceLocation.fromNamespaceAndPath("nautec", "/eas_infusion/" + item.getNamespace() + "/" + item.getPath());
    }

    public static List<EasInfusionEmiRecipe> all() {
        List<EasInfusionEmiRecipe> recipes = new ArrayList<>();
        for (ItemLike item : List.<ItemLike>of(NTItems.AQUARINE_PICKAXE, NTItems.AQUARINE_AXE, NTItems.AQUARINE_SHOVEL, NTItems.AQUARINE_HOE,
                NTItems.AQUARINE_SWORD)) {
            ItemStack input = new ItemStack(item);
            ItemStack output = input.copy();
            NTDataComponentsUtils.setInfusedStatus(output, true);
            recipes.add(new EasInfusionEmiRecipe(new Infusion(input, output)));
        }
        return recipes;
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        canvas.sprite(PressureForgingEmiRecipe.ARROW_SPRITE, 50, 0, 24, 16);
        canvas.slot(input, 0, 0);
        canvas.tank(eas, 24, 0, 16, 16, EAS_AMOUNT);
        canvas.output(output, 88, 0);
        canvas.text(Component.translatable("nautec.jei.eas_infusion.hint", (float) INFUSION_TICKS / 20), 0, 20, layoutWidth(),
                EmiCanvas.lineHeight() * 2, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
    }

    public record Infusion(ItemStack input, ItemStack output) {
    }
}
