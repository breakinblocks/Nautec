package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class DeepOceansEntry extends BaseNautecEntry {
    public DeepOceansEntry(CategoryProviderBase parent) {
        super(parent, "deep_oceans", "The Deep Oceans", "Four new stretches of water",
                BookIconModel.create(NTBlocks.LUMINESCENT_ALGAE.get()));
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("The Deep Oceans");
        this.pageText("""
                Four ocean biomes sit alongside the vanilla ones without
                replacing any of them, so they appear in existing worlds at the
                edges of explored ground.
                \\
                \\
                All four count as oceans. Salt water, the Deep Sea Drain,
                geodes, ruins and shipwrecks all work in them.
                """);

        this.page("abyssal_trench", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Abyssal Trench");
        this.pageText("""
                The deepest and coldest. Visibility is about twenty blocks.
                \\
                \\
                Drowned spawn far more heavily here than elsewhere, and the
                Abyssal Maw hunts in it. Abyssal Coral grows on the floor.
                Budding Prismarine forms in the rock below.
                \\
                \\
                The Pressure Forge needs depth and an unbroken column of water
                above it.
                """);

        this.page("bioluminescent_grove", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Bioluminescent Grove");
        this.pageText("""
                Clear teal water with over a hundred blocks of visibility. Glow
                spores drift through it.
                \\
                \\
                Deep Kelp, Luminescent Algae and Glow Polyp all grow here. Glow
                squid and Lantern Jelly live in it. Budding Prismarine forms in
                the rock below.
                """);

        this.page("hydrothermal_vents", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Hydrothermal Vents");
        this.pageText("""
                Warm murky water over basalt and magma, with about fifty blocks
                of visibility and bubbles rising constantly.
                \\
                \\
                Vent Tubeworms grow here. Vent Crawlers walk the floor and drop
                Chitin Plate.
                """);

        this.page("prismarine_reef", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Prismarine Reef");
        this.pageText("""
                The shallowest of the four, bright and clear, at the edge of the
                drop-off.
                \\
                \\
                Coral, tropical fish, dolphins and prismarine outcrops standing
                out of the sea floor. Prismarine Fronds grow here.
                """);
    }
}
