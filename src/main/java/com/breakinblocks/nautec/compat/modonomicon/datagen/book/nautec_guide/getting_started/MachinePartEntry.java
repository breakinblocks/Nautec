package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTItems;

public class MachinePartEntry extends BaseNautecEntry {
    public MachinePartEntry(CategoryProviderBase parent) {
        super(parent, "machine_part", "Ancient Machine Parts", "All over the place", BookIconModel.create(NTItems.ANCIENT_VALVE));
    }

    @Override
    protected void generatePages() {
        this.page("machine_part", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Machine Parts");
        this.pageText("""
                The ocean floor is littered with
                gears, valves, coils and chips that
                look like refuse and are not.
                \\
                \\
                Most machinery here needs them, and
                no recipe I have found will make one
                from raw material. They can only be
                cleaned and repaired.
                \\
                \\
                Every one of them was manufactured by
                somebody. We are working through what
                is left of their stock.
                """);

        this.page("machine_part_examples", () -> BookSpotlightPageModel.create()
                .withTitle(this.context().pageTitle())
                .withItem(NTItems.BURNT_COIL));
    }
}
