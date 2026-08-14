package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SubmarineHullEntry extends BaseNautecEntry {
    public SubmarineHullEntry(CategoryProviderBase parent) {
        super(parent, "submarine_hull", "Hull and Repair", "Taking a hit", BookIconModel.create(NTItems.SUBMARINE));
    }

    @Override
    protected void generatePages() {
        this.page("submarine_hull", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Hull and Repair");
        this.pageText("""
                The submersible has its own health, its own armour, and it is what the deep attacks. Anything hunting you switches to the hull the moment you climb in.
                \\
                Plating soaks most of a hit, and a parked hull slowly welds itself back together. Nothing else heals it: potions and healing effects slide straight off.
                \\
                Break it and it does not explode into scrap. It ejects the crew, unharmed, and drops itself as a breached hull that will not launch again until you repair it on an anvil with diamonds.
                """);
    }
}
