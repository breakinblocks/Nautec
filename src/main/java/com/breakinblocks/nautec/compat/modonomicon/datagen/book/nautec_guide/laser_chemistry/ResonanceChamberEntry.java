package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class ResonanceChamberEntry extends BaseNautecEntry {
    public ResonanceChamberEntry(CategoryProviderBase parent) {
        super(parent, "resonance_chamber", "Resonance Chamber", "Hold a beam until it sings",
                BookIconModel.create(NTBlocks.RESONANCE_CHAMBER));
    }

    @Override
    protected void generatePages() {
        page("resonance_chamber", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.RESONANCE_CHAMBER)
                .withText(context.pageText()));
        pageTitle("Resonance Chamber");
        pageText("""
                Every other machine spends a beam the moment it
                arrives. The Chamber hoards it. Point a beam at it
                and the charge inside climbs, and climbs, and does
                not stop climbing on its own.
                Drop an item in first and it will pull the charge
                back down at the right moment and make something
                of it. Leave it empty and it will not.
                """);

        page("ceiling", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("The Ceiling");
        pageText("""
                How much charge the Chamber can hold before it
                comes apart depends on how clean the beam feeding
                it is. A filthy beam gives a low ceiling. A beam
                straight off a Prismarine Crystal gives a ceiling
                several times higher.
                Near the top of that range the Chamber goes
                critical, and that is the only moment it will
                craft. Go past it and the charge lets go all at
                once.
                """);

        page("venting", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Venting");
        pageText("""
                A Chamber that reaches critical with nothing to
                make, or with its output already full, keeps
                charging until it vents. Everything nearby takes
                the hit, including you, and the Chamber sits
                cracked and useless for a while afterwards.
                It does not break, and it does not take the room
                with it. But do not stand next to one you have
                left running empty.
                """);

        page("shards", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Resonant Shards");
        pageText("""
                What the Chamber is really for. Feed it a
                Prismarine Crystal Shard on the cleanest beam you
                can build and it folds it into a Resonant Shard.
                Nothing below the top purity band will do it, so
                this is the first thing in the mod that genuinely
                needs a good beam rather than merely a strong one.
                Keep them. They are the seed of far larger things.
                """);

        page("resonance_chamber_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Resonance Chamber Recipe")
                .withRecipeId1("nautec:resonance_chamber"));
    }
}
