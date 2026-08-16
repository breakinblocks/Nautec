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
                Hold it, get under water, and hold use. It pulls
                you along wherever you are looking, both hands on
                the grips, flat out like a swimmer.
                """);

        page("hands", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Both Hands");
        pageText("""
                Two grips means two hands. Carry it in either one
                and the other has to stay empty.
                Put anything in your free hand and the jet is
                handed straight back to your pack. It is not a
                thing you hold alongside a sword.
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

        page("breath", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Holding Your Breath");
        pageText("""
                Riding it holds your breath. Your air stops
                draining for as long as you are thrusting, and
                picks up again the moment you let go.
                It keeps what you have rather than filling you up.
                Set off with half a lungful and you arrive with
                half a lungful. Set off with none and it will not
                save you. For that you want a Diving Suit.
                """);

        page("spotlight", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("The Spotlight");
        pageText("""
                Press F while holding it to light the lamps. They
                throw a cone wherever you look and put a real pool
                of light on whatever you point at, which is worth
                more than the beam itself once you are deep enough
                that nothing else reaches.
                It burns a little power while lit and puts itself
                out when the cell is empty. It works out of the
                water too, if you would rather carry a lamp than a
                torch.
                """);

        page("power", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Feeding It");
        pageText("""
                It carries its own cell and drinks steadily while
                the thruster is running, and a trickle more while
                the lamps are lit. The bar under the item is what
                is left in it.
                Charge it in a Charger like a Battery or any other
                powered tool. It holds far less than a Sea Scout.
                """);

        page("wave_jet_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Wave Jet Recipe")
                .withRecipeId1("nautec:wave_jet"));
    }
}
