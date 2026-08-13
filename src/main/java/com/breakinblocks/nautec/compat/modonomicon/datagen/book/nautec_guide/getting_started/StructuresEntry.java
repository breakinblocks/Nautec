package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class StructuresEntry extends BaseNautecEntry {
    public StructuresEntry(CategoryProviderBase parent) {
        super(parent, "structures", "Structures", "Structures that you will come across on your journey through the oceans", BookIconModel.create(NTBlocks.PRISMARINE_CRYSTAL));
    }

    @Override
    protected void generatePages() {
        this.page("structures", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));

        this.pageTitle("Deep Sea Structures");
        this.pageText("""
                Find an ocean and look for something on the floor that is too regular to be natural.
                \\
                Dark prismarine, cut and laid in an arch, with a device still seated in the middle of it. Nothing grows in that shape on its own.
                \\
                The arch is not what you came for. Below it, within a short radius, there is usually a geode, and in the geode a crystal.
                """);

        this.page("geode", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));

        this.pageTitle("Deep Sea Structures");
        this.pageText("""
                There is salvage scattered around the crystal, and the crystal itself stands inside scaffolding.
                \\
                Somebody put that scaffolding there. They were working on this one when they stopped, and whatever they were part way through, they never came back for it.
                \\
                \\
                Edit: Do NOT break the crystal (yet). It shatters and leaves nothing behind.
                """);
    }
}
