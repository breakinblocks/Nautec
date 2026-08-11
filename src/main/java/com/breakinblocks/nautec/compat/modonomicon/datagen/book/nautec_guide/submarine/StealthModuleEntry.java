package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class StealthModuleEntry extends BaseNautecEntry {
    public StealthModuleEntry(CategoryProviderBase parent) {
        super(parent, "stealth_module", "Stealth Module", "Running silent", BookIconModel.create(NTItems.STEALTH_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("stealth_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Stealth Module");
        this.pageText("""
                Masks the hull for two minutes. Anything hunting you loses interest immediately and nothing new will lock on while it runs.
                \\
                The masking costs you some speed, so it is an escape, not a chase.
                """);
        this.page("stealth_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Stealth Module")
                .withRecipeId1("nautec:stealth_module"));
    }
}
