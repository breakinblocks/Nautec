package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class IntroductionEntry extends BaseNautecEntry {
    public IntroductionEntry(CategoryProviderBase parent) {
        super(parent, "introduction", "Introduction", "Introducing... NAUTEC", BookIconModel.create(NTBlocks.PRISMARINE_SAND.get()));
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Introduction");
        this.pageText("""
                None of this was mine to begin with.
                \\
                \\
                The sea floor is full of working machinery.
                Arches of dark prismarine with a device still
                seated in them. Crates with the locks corroded
                shut. Components that have outlasted the frames
                they were bolted into.
                \\
                \\
                Somebody built all of it, and then stopped.
                """);

        this.page("their_work", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Their Work");
        this.pageText("""
                Three habits show up everywhere they built.
                \\
                \\
                They moved power as light through crystal, not
                as current along a cable. They worked at depths
                that would kill you outright. And they thought
                nothing of cutting a body open and fitting it
                for the water.
                \\
                \\
                Everything in this book follows from copying
                those three.
                """);

        this.page("starting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Where to Start");
        this.pageText("""
                On a beach, with prismarine. Gather both kinds
                and build an Aquatic Catalyst.
                \\
                \\
                It is a poor imitation of how they made a beam.
                It still works.
                """);
    }
}
