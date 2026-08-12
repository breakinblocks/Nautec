package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.aquatic_biology;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class IncubatorEntry extends BaseNautecEntry {
    public IncubatorEntry(CategoryProviderBase parent) {
        super(parent, "incubator", "Incubator", "Incubating the colony", BookIconModel.create(NTBlocks.INCUBATOR));
    }

    @Override
    protected void generatePages() {
        page("incubator", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.INCUBATOR)
                .withText(context.pageText()));
        pageTitle("Incubator");
        pageText("""
                The Incubator is a machine that requires AP
                to incubate Bacteria Colonies. Incubation allows
                a colony to grow and increase its size.
                Incubation works by supplying the Incubator with
                a nutrient item and the bacteria that should
                be incubated. Every cycle the colony gains a
                rolled amount from the recipe's growth range,
                multiplied by the colony's growth rate. Each
                cycle also has a chance to consume the nutrient.
                """);
        this.page("incubator_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Incubator Recipe")
                .withRecipeId1("nautec:incubator"))
                .withText(context.pageText());
        pageText("""
                A colony with a high growth rate gains far more
                per cycle than one with a low growth rate. Growth
                stops exactly at the colony size cap, so a colony
                near the cap will never overshoot it.
                Incubating also restores a colony's Vitality to
                full, which is the only way to reverse the aging
                that happens inside the Bio Reactor.
                Look at JEI for all recipes
                """);
    }
}
