package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTItems;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BacteriaGraftingEmiRecipe extends NTEmiRecipe<BacteriaGraftingEmiRecipe.Grafting> {
    private static final int WIDTH = 132;
    private static final int HEIGHT = 42;
    private static final int Y = 6;

    private final EmiStack dish;
    private final EmiStack sample;
    private final EmiStack result;
    private final Component biome;

    public BacteriaGraftingEmiRecipe(Grafting grafting) {
        super(NTEmiPlugin.BACTERIA_GRAFTING, id(grafting), grafting, WIDTH, HEIGHT);
        this.dish = EmiStack.of(NTItems.PETRI_DISH.get());
        this.sample = EmiStack.of(grafting.sample());
        ItemStack stack = NTItems.PETRI_DISH.toStack();
        IBacteriaStorage storage = NTCapabilities.BacteriaStorage.ITEM.getCapability(stack, null);
        if (storage != null) {
            storage.setBacteria(0, BacteriaInstance.withMaxStats(grafting.value().bacteria(), Minecraft.getInstance().level.registryAccess()));
        }
        this.result = EmiStack.of(stack);
        inputs.add(dish);
        inputs.add(sample);
        catalysts.add(EmiStack.of(NTItems.GRAFTING_TOOL.get()));
        outputs.add(result);
        this.biome = Component.translatable("nautec.jei.only_in", grafting.value().biome().location().toString());
        fit(0, biome);
    }

    private static ResourceLocation id(Grafting grafting) {
        ResourceLocation sample = BuiltInRegistries.ITEM.getKey(grafting.sample());
        ResourceLocation bacteria = grafting.value().bacteria().location();
        return ResourceLocation.fromNamespaceAndPath("nautec", "/grafting/" + sample.getNamespace() + "/" + sample.getPath() + "/"
                + bacteria.getNamespace() + "/" + bacteria.getPath());
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.text(biome, 0, 0, width, EmiCanvas.Align.LEFT, 0xFF000000 | ChatFormatting.DARK_GRAY.getColor(), false);
        canvas.slot(dish, 0, HEIGHT / 2 - 9 + Y);
        canvas.output(result, width - 18, HEIGHT / 2 - 9 + Y);
        canvas.slot(EmiStack.of(NTItems.GRAFTING_TOOL.get()), width / 2 - 9, HEIGHT / 2 - 18 + Y);
        canvas.slot(sample, width / 2 - 9, HEIGHT / 2 + Y);
    }

    public record Grafting(Item sample, BacteriaObtainValue value) {
    }
}
