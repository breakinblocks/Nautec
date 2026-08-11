package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SonarModuleEntry extends BaseNautecEntry {
    public SonarModuleEntry(CategoryProviderBase parent) {
        super(parent, "sonar_module", "Sonar Module", "Seeing through rock", BookIconModel.create(NTItems.SONAR_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("sonar_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Sonar Module");
        this.pageText("""
                One ping lights up every ore vein around you, straight through solid rock, and outlines anything hostile nearby in red.
                \\
                The crew get night vision with it, which matters more than it sounds at the depths where this is useful.
                \\
                The pulse takes a moment to sweep outward, so the far edges of its range appear last.
                """);
        this.page("sonar_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Sonar Module")
                .withRecipeId1("nautec:sonar_module"));
    }
}
