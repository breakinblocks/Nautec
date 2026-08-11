package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SubmarineModulesEntry extends BaseNautecEntry {
    public SubmarineModulesEntry(CategoryProviderBase parent) {
        super(parent, "submarine_modules", "Modules", "Nine slots", BookIconModel.create(NTItems.AQUATIC_CHIP));
    }

    @Override
    protected void generatePages() {
        this.page("submarine_modules", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Modules");
        this.pageText("""
                Right-click the hull with a wrench to open its module bay: nine slots, one module each.
                \\
                While you pilot, your hotbar is replaced by those nine slots. Scroll or use the number keys to select, then left-click, or press F, to fire the selected module.
                \\
                Solar and Armour are passive and work from any slot. Everything else costs power and has a cooldown that sweeps across its cell while it recharges.
                \\
                Modules stay in the hull when you pick it up, so a stowed submersible carries its whole loadout with it.
                """);
    }
}
