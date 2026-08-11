package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class ShieldModuleEntry extends BaseNautecEntry {
    public ShieldModuleEntry(CategoryProviderBase parent) {
        super(parent, "shield_module", "Shield Module", "Power instead of hull", BookIconModel.create(NTItems.SHIELD_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("shield_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Shield Module");
        this.pageText("""
                Passively, it spends power to soak incoming damage. While the cell holds out, hits cost you charge instead of hull. When it runs dry, the hull starts taking them again.
                \\
                Fired deliberately, it discharges: everything around the hull except your own crew is thrown clear, hurt, and left unable to move for a few seconds.
                """);
        this.page("shield_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Shield Module")
                .withRecipeId1("nautec:shield_module"));
    }
}
