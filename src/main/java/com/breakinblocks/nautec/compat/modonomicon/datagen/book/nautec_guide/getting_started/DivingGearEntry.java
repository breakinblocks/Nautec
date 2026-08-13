package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTItems;

public class DivingGearEntry extends BaseNautecEntry {
    public DivingGearEntry(CategoryProviderBase parent) {
        super(parent, "diving_gear", "Diving Suit and Oxygen", "Explore the depths of the ocean", BookIconModel.create(NTItems.DIVING_HELMET.get()));
    }

    @Override
    protected void generatePages() {
        this.page("diving", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Diving Gear");
        this.pageText("""
                A suit, made of polymer, that lets you stay under long enough to work.
                \\
                They needed nothing like it. This is the crude answer to a problem they solved by changing themselves, and until you are ready to do the same it is the only answer you have.
                \\
                The helmet clears your vision. The chestplate holds ten minutes of air.
                """);
        this.page("oxygen_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle2("Filling the tanks")
                .withRecipeId2("nautec:diving_chestplate_oxygen")
                .withTitle1("Brown Polymer")
                .withRecipeId1("nautec:brown_polymer"));
    }
}
