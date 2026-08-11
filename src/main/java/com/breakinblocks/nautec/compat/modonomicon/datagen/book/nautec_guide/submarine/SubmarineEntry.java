package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SubmarineEntry extends BaseNautecEntry {
    public SubmarineEntry(CategoryProviderBase parent) {
        super(parent, "submarine", "Abyssal Submersible", "A hull of your own", BookIconModel.create(NTItems.SUBMARINE));
    }

    @Override
    protected void generatePages() {
        this.page("submarine", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Abyssal Submersible");
        this.pageText("""
                The trenches do not care how good your diving suit is. Past a certain depth you stop swimming and start needing a hull.
                \\
                The Abyssal Submersible is that hull: two seats, a prismatic power cell, and enough plating to argue with whatever lives down there.
                \\
                Place it in water, right-click to climb in, and sneak to get back out. Sneak-right-click an empty one to pick it up again, charge and all.
                """);
        this.page("submarine_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Abyssal Submersible")
                .withRecipeId1("nautec:submarine"));
    }
}
