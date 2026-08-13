package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class CratesEtchingEntry extends BaseNautecEntry {
    public CratesEtchingEntry(CategoryProviderBase parent) {
        super(parent, "etching", "Crate & Item Etching", "Feels like brand new!", BookIconModel.create(NTBlocks.RUSTY_CRATE));
    }

    @Override
    protected void generatePages() {
        this.page("etching", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Etching");
        this.pageText("""
                Etching strips the rust off anything that has been down there long enough to grow some.
                \\
                Drop a rusty crate or item into a pool of Etching Acid and the corrosion lifts away, leaving what was underneath it intact. The metal survives. It is better metal than we make.
                \\
                The crates are locked because somebody locked them. The mechanisms have seized solid since, so the only way in now is a crowbar.
                """);
        this.page("etching_acid_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Etching Acid Recipe")
                .withRecipeId1("nautec:etching_acid_crafting"));
    }
}
