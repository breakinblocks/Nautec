package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
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
                W and S open and close the throttle, Space rises and C dives. There is no rudder key: the hull simply follows where you look, and it will refuse to turn into rock rather than clip through it.
                \\
                Hold use to unlock your head from the hull and look around without steering. Let go and your view snaps back to where the nose is pointing.
                \\
                Sneak always dismounts, so it can never be a control. On the surface the thrusters barely bite; the submersible is built for the deep.
                """);

        this.page("submarine_hud", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("The Readout");
        this.pageText("""
                Taking the seat puts a panel on your screen: the cell on top, the hull below it, and the control scheme listed underneath so you are not hunting through this book mid-dive.
                \\
                It stands in for the vanilla vehicle hearts, which eighty hull points would otherwise smear across four rows of your screen.
                \\
                Ctrl and H picks the panel up so you can drop it somewhere that suits you. A passenger in the back keeps their ordinary hotbar and sees none of it.
                """);
    }
}
