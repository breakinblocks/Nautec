package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class ArmorModuleEntry extends BaseNautecEntry {
    public ArmorModuleEntry(CategoryProviderBase parent) {
        super(parent, "armor_module", "Armour Module", "More plating", BookIconModel.create(NTItems.ARMOR_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("armor_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Armour Module");
        this.pageText("""
                Bolts another layer onto the hull: tougher plating against heavy hits, and enough mass that being struck no longer shoves you off course.
                \\
                Passive. Pull it and the hull goes straight back to stock.
                """);
        this.page("armor_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Armour Module")
                .withRecipeId1("nautec:armor_module"));
    }
}
