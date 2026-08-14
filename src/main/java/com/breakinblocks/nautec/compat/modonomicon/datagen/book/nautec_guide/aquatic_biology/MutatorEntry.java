package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.aquatic_biology;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class MutatorEntry extends BaseNautecEntry {
    public MutatorEntry(CategoryProviderBase parent) {
        super(parent, "mutator", "Mutator", "Mutating and Radiating", BookIconModel.create(NTBlocks.MUTATOR));
    }

    @Override
    protected void generatePages() {
        page("mutator", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.MUTATOR)
                .withText(context.pageText()));
        pageTitle("Mutator");
        pageText("""
                The Mutator is a machine that requires AP
                to turn one Bacteria Colony into another.
                Mutation works by supplying the Mutator with
                a catalyst item and the bacteria that should
                be modified. The output slot must be empty
                before an attempt can start.
                Each attempt either succeeds or fails. On a
                success the whole input colony is converted and
                the new colony carries its parent's stats forward
                with a small drift, so a well bred colony stays
                good after the jump.
                """);
        this.page("mutator_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Mutator Recipe")
                .withRecipeId1("nautec:mutator"))
                .withText(context.pageText());
        pageText("""
                The chance shown in JEI is the starting point. A
                colony's Mutation Resistance cuts that chance down,
                and so does its size, so keep the colony you mean
                to mutate small and mutate it before you grow it.
                A failed attempt does not destroy the colony. It
                only kills off part of it, and a resistant colony
                loses less. The Mutator then tries again.
                Look at JEI for all recipes.
                """);
    }
}
