package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class BuddingPrismarineEntry extends BaseNautecEntry {
    public BuddingPrismarineEntry(CategoryProviderBase parent) {
        super(parent, "budding_prismarine", "Budding Prismarine", "Crystal shards that grow back",
                BookIconModel.create(NTBlocks.BUDDING_PRISMARINE.get()));
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookSpotlightPageModel.create()
                .withTitle(this.context().pageTitle())
                .withItem(NTBlocks.BUDDING_PRISMARINE.get())
                .withText(this.context().pageText()));
        this.pageTitle("Budding Prismarine");
        this.pageText("""
                A block that grows Prismarine Crystal Shards, making them
                renewable.
                \\
                \\
                It works the way Budding Amethyst does.
                """);

        this.page("growth", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("How It Grows");
        this.pageText("""
                Buds appear on any face and thicken over time: small, then
                medium, then large, and finally a Prismarine Cluster.
                \\
                \\
                A bud broken before it reaches cluster drops nothing without
                Silk Touch, and regrows from the start.
                """);

        this.page("harvest", () -> BookSpotlightPageModel.create()
                .withTitle(this.context().pageTitle())
                .withItem(NTItems.PRISMARINE_CRYSTAL_SHARD)
                .withText(this.context().pageText()));
        this.pageTitle("Harvesting");
        this.pageText("""
                A cluster gives two Prismarine Crystal Shards, raised by
                Fortune. Silk Touch takes the cluster itself.
                \\
                \\
                The block needs Silk Touch to be picked up. Mined with anything
                else it breaks into four to six shards and is gone.
                """);

        this.page("finding", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Finding It");
        this.pageText("""
                It forms rarely in the rock under the Abyssal Trench and the
                Bioluminescent Grove, below the sea floor.
                \\
                \\
                A lucky fishing zone in abyssal water can also turn one up as
                treasure.
                """);
    }
}
