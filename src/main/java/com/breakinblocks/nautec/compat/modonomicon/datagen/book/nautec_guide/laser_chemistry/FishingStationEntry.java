package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class FishingStationEntry extends BaseNautecEntry {
    public FishingStationEntry(CategoryProviderBase parent) {
        super(parent, "fishing_station", "Fishing Station", "Automated fishing",
                BookIconModel.create(NTBlocks.FISHING_STATION.get()));
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookSpotlightPageModel.create()
                .withTitle(this.context().pageTitle())
                .withItem(NTBlocks.FISHING_STATION.get())
                .withText(this.context().pageText()));
        this.pageTitle("Fishing Station");
        this.pageText("""
                A net on a frame that fishes on its own, filling its own
                inventory with the catch a rod would give you.
                \\
                \\
                It takes a beam from above or below. Purity does not matter.
                """);

        this.page("water", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Placement");
        this.pageText("""
                It checks the water beneath it, two blocks out in each direction
                and two deep. Every block in that volume has to be water, so a
                pillar, a boat or a stand of kelp will stop it.
                \\
                \\
                It rechecks every five seconds. The net only turns while it is
                working.
                """);

        this.page("output", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Output");
        this.pageText("""
                Ordinary fishing loot, one item every two seconds, into fifteen
                slots. Once those are full it drops the overflow on the floor.
                \\
                \\
                It does not use lucky zones and never plays the minigame.
                """);

        this.page("recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Fishing Station")
                .withRecipeId1("nautec:fishing_station"));
    }
}
