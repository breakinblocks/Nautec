package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaSelector;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.compat.emi.NTEmiStacks;
import com.breakinblocks.nautec.content.recipes.ColonyFeedingRecipe;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public class ColonyFeedingEmiRecipe extends NTEmiRecipe<ColonyFeedingRecipe> {
    private static final ResourceLocation ARROW_SPRITE = Nautec.rl("container/bio_reactor/progress_arrow_off");
    private static final int WIDTH = 116;
    private static final int HEIGHT = 44;
    private static final int NUTRIENT_X = 7;
    private static final int ARROW_X = 32;
    private static final int COLONY_X = 62;
    private static final int ROW_Y = 3;

    private final EmiIngredient nutrient;
    private final Optional<ResourceKey<Bacteria>> strain;
    private final Component vitality;

    public ColonyFeedingEmiRecipe(@Nullable ResourceLocation id, ColonyFeedingRecipe recipe) {
        super(NTEmiPlugin.COLONY_FEEDING, id, recipe, WIDTH, HEIGHT);
        this.nutrient = NTEmiStacks.of(recipe.ingredient());
        this.strain = recipe.bacteria().flatMap(BacteriaSelector::key)
                .filter(key -> BacteriaHelper.findBacteria(Minecraft.getInstance().level.registryAccess(), key) != null);
        inputs.add(nutrient);
        strain.ifPresent(key -> inputs.add(NTEmiStacks.dish(key)));
        String seconds = String.format(Locale.ROOT, "%.1f", recipe.vitalityTicks() / 20f);
        this.vitality = Component.translatable("nautec.jei.colony_feeding.vitality", recipe.vitalityTicks(), seconds);
        fit(0, vitality);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        int line = EmiCanvas.lineHeight();
        canvas.sprite(ARROW_SPRITE, ARROW_X, ROW_Y + 4, 24, 10);
        canvas.slot(nutrient, NUTRIENT_X + 1, ROW_Y + 1);
        if (strain.isPresent()) {
            canvas.bacteria(strain.get(), COLONY_X, ROW_Y);
        } else {
            Component target = recipe.bacteria()
                    .map(selector -> selector.tag()
                            ? Component.translatable("nautec.jei.colony_feeding.tag", "#" + selector.id())
                            : Component.translatable("nautec.jei.colony_feeding.strain", selector.id().toString()))
                    .orElse(Component.translatable("nautec.jei.colony_feeding.any"));
            canvas.text(target, COLONY_X - 4, ROW_Y + 1, layoutWidth() - COLONY_X + 4, line * 2, EmiCanvas.Align.LEFT, EmiCanvas.GRAY, false);
        }
        canvas.text(vitality, 0, HEIGHT - line, layoutWidth(), EmiCanvas.Align.CENTER, EmiCanvas.GRAY, false);
    }
}
