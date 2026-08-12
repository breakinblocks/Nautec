package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;

public class SubmarineDockEntry extends BaseNautecEntry {
    public SubmarineDockEntry(CategoryProviderBase parent) {
        super(parent, "submarine_dock", "Sea Scout Dock", "Somewhere to leave it",
                BookIconModel.create(NTBlocks.SUBMARINE_DOCK));
    }

    @Override
    protected void generatePages() {
        page("submarine_dock", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.SUBMARINE_DOCK)
                .withText(context.pageText()));
        pageTitle("Sea Scout Dock");
        pageText("""
                A powered pad. Park a Sea Scout on it and it fills
                the hull's cells without you having to pick the
                thing up and carry it to a Charger.
                It also clamps an empty hull in place, so what you
                left on the pad is still on the pad when you come
                back rather than halfway across the bay.
                Anyone sitting inside keeps breathing on the
                dock's power rather than the hull's.
                """);

        page("submarine_dock_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Sea Scout Dock Recipe")
                .withRecipeId1("nautec:submarine_dock"));
    }
}
