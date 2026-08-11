package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class BoosterModuleEntry extends BaseNautecEntry {
    public BoosterModuleEntry(CategoryProviderBase parent) {
        super(parent, "booster_module", "Booster Module", "Ten seconds of speed", BookIconModel.create(NTItems.BOOSTER_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("booster_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Booster Module");
        this.pageText("""
                Dumps the reserve into the thrusters for ten seconds of considerably faster travel, then needs a few seconds to recover.
                \\
                Useful for crossing open trench, and for leaving somewhere quickly.
                """);
        this.page("booster_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Booster Module")
                .withRecipeId1("nautec:booster_module"));
    }
}
