package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteriaStats;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.utils.MathUtils;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Locale;

public class BioReactorEmiRecipe extends NTEmiRecipe<BioReactorEmiRecipe.Production> {
    private static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    private static final int WIDTH = 160;
    private static final int HEIGHT = 54;
    private static final int ARROW_ROW_HEIGHT = 24;

    private final EmiStack product;
    private final List<Component> text;

    public BioReactorEmiRecipe(Production production) {
        super(NTEmiPlugin.BIO_REACTOR, id(production), production, WIDTH, HEIGHT);
        Item item = production.resource().resolve();
        this.product = item == Items.AIR ? EmiStack.EMPTY : EmiStack.of(item);
        inputs.add(NTEmiStacks.dish(production.bacteria()));
        if (!product.isEmpty()) {
            outputs.add(product);
        }
        this.text = text(production.bacteria());
        for (Component line : text) {
            fit(0, line);
        }
    }

    private static ResourceLocation id(Production production) {
        ResourceLocation key = production.bacteria().location();
        return ResourceLocation.fromNamespaceAndPath("nautec", "/bio_reactor/" + key.getNamespace() + "/" + key.getPath());
    }

    private static List<Component> text(ResourceKey<Bacteria> key) {
        Bacteria bacteria = BacteriaHelper.findBacteria(Minecraft.getInstance().level.registryAccess(), key);
        if (bacteria == null || !(bacteria.stats() instanceof SimpleBacteriaStats stats)) {
            return List.of();
        }
        boolean graftable = false;
        for (BacteriaObtainValue value : BuiltInRegistries.BLOCK.getDataMap(NTDataMaps.BACTERIA_OBTAINING).values()) {
            if (value.bacteria().equals(key)) {
                graftable = true;
                break;
            }
        }
        double perMinute = NTConfig.bioReactorBaseSpeed * bacteria.productionMultiplier() * 12D;
        double cap = Math.max(1D, NTConfig.bacteriaColonySizeCap);
        Component rate = graftable
                ? Component.translatable("nautec.jei.items_per_minute.fresh",
                        perMinute(perMinute * stats.productionRate().getMin() * (0.5D + 0.5D * Math.min(cap, bacteria.initialSize().getMin()) / cap)),
                        perMinute(perMinute * stats.productionRate().getMax() * (0.5D + 0.5D * Math.min(cap, bacteria.initialSize().getMax()) / cap)))
                : Component.translatable("nautec.jei.items_per_minute.rate_one", perMinute(perMinute * 0.5D), perMinute(perMinute));
        Component production = graftable
                ? Component.translatable("nautec.jei.production",
                        MathUtils.roundToPrecision(stats.productionRate().getMin(), 2),
                        MathUtils.roundToPrecision(stats.productionRate().getMax(), 2))
                : Component.translatable("nautec.jei.production_inherited");
        Component multiplier = Component.translatable("nautec.jei.production_multiplier", BacteriaHelper.formatMultiplier(bacteria.productionMultiplier()));
        return List.of(rate, production, multiplier);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int width = layoutWidth();
        canvas.sprite(ARROW_SPRITE, width / 2 - 12, ARROW_ROW_HEIGHT / 2 - 5, 24, 10);
        canvas.bacteria(recipe.bacteria(), 0, 3);
        if (!product.isEmpty()) {
            canvas.output(product, width - 18, 3);
        }
        int line = EmiCanvas.lineHeight();
        int[] offsets = {line * 3 + 2, line * 2 + 1, line};
        for (int i = 0; i < text.size(); i++) {
            canvas.text(text.get(i), 0, HEIGHT - offsets[i], width, EmiCanvas.Align.CENTER, EmiCanvas.GRAY, false);
        }
    }

    private static String perMinute(double value) {
        return value >= 10D ? String.valueOf(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    public record Production(ResourceKey<Bacteria> bacteria, Bacteria.Resource resource) {
    }
}
