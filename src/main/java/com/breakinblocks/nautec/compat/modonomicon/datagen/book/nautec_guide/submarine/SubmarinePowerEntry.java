package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SubmarinePowerEntry extends BaseNautecEntry {
    public SubmarinePowerEntry(CategoryProviderBase parent) {
        super(parent, "submarine_power", "Power and Air", "Keeping the lights on", BookIconModel.create(NTItems.PRISMATIC_BATTERY));
    }

    @Override
    protected void generatePages() {
        this.page("submarine_power", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Power and Air");
        this.pageText("""
                The hull runs on stored power. Idling costs a trickle, moving costs more, and keeping the cabin sealed costs more again.
                \\
                In exchange, a sealed and powered hull keeps its crew breathing indefinitely. Run the cell flat underwater and you will find out how quickly that stops being true.
                \\
                Charge it like any other Nautec device. The readout on your screen shows the cell on top and the hull below it.
                """);
    }
}
