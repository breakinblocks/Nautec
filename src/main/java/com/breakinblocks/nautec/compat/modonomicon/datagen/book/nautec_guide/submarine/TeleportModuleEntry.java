package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class TeleportModuleEntry extends BaseNautecEntry {
    public TeleportModuleEntry(CategoryProviderBase parent) {
        super(parent, "teleport_module", "Teleport Module", "Folding the water", BookIconModel.create(NTItems.TELEPORT_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("teleport_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Teleport Module");
        this.pageText("""
                Sneak-right-click the module while standing in water to bind an anchor. It remembers the dimension as well as the place, so jumps across worlds work.
                \\
                Fired from the pilot's seat, the hull drifts forward into a portal of its own making and arrives at the anchor with its crew still aboard.
                \\
                It will refuse if nothing is bound, if the anchor has been walled in or drained, or if the cell is below a fifth full. It is the single most expensive thing the hull can do.
                """);
        this.page("teleport_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Teleport Module")
                .withRecipeId1("nautec:teleport_module"));
    }
}
