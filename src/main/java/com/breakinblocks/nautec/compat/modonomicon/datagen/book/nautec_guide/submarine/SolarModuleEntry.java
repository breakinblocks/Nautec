package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SolarModuleEntry extends BaseNautecEntry {
    public SolarModuleEntry(CategoryProviderBase parent) {
        super(parent, "solar_module", "Solar Module", "Free power, slowly", BookIconModel.create(NTItems.SOLAR_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("solar_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Solar Module");
        this.pageText("""
                Trickles the cell full again whenever the hull sits in open, sunlit water. It works through the water column, so you do not have to surface, but anything solid overhead stops it.
                \\
                It keeps collecting while the submersible is parked and empty, which makes it the difference between a vehicle and a base you can leave somewhere.
                """);
        this.page("solar_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Solar Module")
                .withRecipeId1("nautec:solar_module"));
    }
}
