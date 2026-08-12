package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.aquatic_biology;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTItems;

public class BacteriaStatsEntry extends BaseNautecEntry {
    public BacteriaStatsEntry(CategoryProviderBase parent) {
        super(parent, "bacteria_stats", "Bacteria Stats", "Statistics raaaaaaawr", BookIconModel.create(NTItems.PETRI_DISH));
    }

    @Override
    protected void generatePages() {
        page("bacteria_stats", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle()))
                .withText(context.pageText());
        pageTitle("Bacteria Stats");
        pageText("""
                After you have analyzed your bacteria, you will notice
                that pressing shift while hovering over the item will
                display information about the bacteria's stats. Stats
                can vary between bacteria, but most of the time there
                are the following stats:
                \
                - Growth Rate, multiplies how much size an Incubator cycle adds
                \
                - Mutation Resistance, lowers the Mutator's success chance, and softens the loss on a failed attempt
                \
                - Production Rate, multiplies how fast the Bio Reactor makes resources
                \
                - Lifespan, the ticks a colony can work in the Bio Reactor before it starts dying off
                \
                Analyzed dishes also show Vitality, which is how
                much of the Lifespan is left. It drops while the
                colony works and returns to 100% whenever the
                colony is fed in an Incubator. At 0% the colony
                is Senescent and loses part of itself with every
                resource it produces.
                """);
    }
}
