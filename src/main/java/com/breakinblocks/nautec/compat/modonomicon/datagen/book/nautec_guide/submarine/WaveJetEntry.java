package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class WaveJetEntry extends BaseNautecEntry {
    public WaveJetEntry(CategoryProviderBase parent) {
        super(parent, "wave_jet", "Wave Jet", "For when a hull is too much boat",
                BookIconModel.create(NTItems.WAVE_JET));
    }

    @Override
    protected void generatePages() {
        page("wave_jet", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTItems.WAVE_JET)
                .withText(context.pageText()));
        pageTitle("Wave Jet");
        pageText("""
                A Sea Scout is a vessel. You park it, you charge
                it, you climb into it. Some days you want none of
                that and you only want to get somewhere.
                The Wave Jet is a thruster with two grips on it.
                Hold it in your main hand, get under water, and
                hold use. It pulls you along wherever you are
                looking, both hands on the grips, flat out like a
                swimmer.
                """);

        page("using", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Driving It");
        pageText("""
                Steer by looking. There is nothing else to it.
                Point where you want to go and the jet takes you
                there, faster the longer you hold it.
                It only runs under water. Break the surface and it
                cuts out on its own, and so does running the cell
                dry. Neither one strands you mid-stroke, you simply
                stop being pulled and go back to swimming.
                """);

        page("power", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Feeding It");
        pageText("""
                It carries its own cell and drinks steadily while
                the thruster is running. The bar under the item is
                what is left in it.
                Charge it in a Charger like a Battery or any other
                powered tool. It holds far less than a Sea Scout,
                which is the trade you are making: no hull, no
                cargo, no crew, but nothing to park either.
                """);

        page("wave_jet_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Wave Jet Recipe")
                .withRecipeId1("nautec:wave_jet"));
    }
}
