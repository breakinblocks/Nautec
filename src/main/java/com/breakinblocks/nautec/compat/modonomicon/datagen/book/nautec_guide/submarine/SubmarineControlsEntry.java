package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class SubmarineControlsEntry extends BaseNautecEntry {
    public SubmarineControlsEntry(CategoryProviderBase parent) {
        super(parent, "submarine_controls", "Piloting", "Driving the thing", BookIconModel.create(NTItems.SUBMARINE));
    }

    @Override
    protected void generatePages() {
        this.page("submarine_controls", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Piloting");
        this.pageText("""
                W and S open and close the throttle, A and D work the rudder, Space rises and C dives.
                \\
                Hold use to steer with the mouse: the hull follows where you look, and it will refuse to turn into rock rather than clip through it.
                \\
                Sneak always dismounts, so it can never be a control. On the surface the thrusters barely bite; the submersible is built for the deep.
                """);
    }
}
