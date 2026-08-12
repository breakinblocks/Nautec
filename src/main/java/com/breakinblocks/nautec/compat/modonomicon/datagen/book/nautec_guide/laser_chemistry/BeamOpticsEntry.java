package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class BeamOpticsEntry extends BaseNautecEntry {
    public BeamOpticsEntry(CategoryProviderBase parent) {
        super(parent, "beam_optics", "Beam Optics", "Bending light, and what it costs",
                BookIconModel.create(NTBlocks.PRISMATIC_MIRROR));
    }

    @Override
    protected void generatePages() {
        page("beam_optics", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.PRISMATIC_MIRROR)
                .withText(context.pageText()));
        pageTitle("Beam Optics");
        pageText("""
                A Prismarine Relay carries a beam in a straight
                line and changes nothing about it. Optics do the
                opposite: they let you route a beam wherever you
                want, and every one of them costs you something.
                What they cost is purity. Every recipe worth
                running has a purity it will not run below, so the
                route you build decides what you can make at the
                far end of it.
                """);

        page("purity_tiers", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Purity Tiers");
        pageText("""
                Think of purity in four bands.
                Tier 0 is any beam at all, however battered.
                Tier 1 is around 1.0, which a single Aquatic
                Catalyst on crystal shards will hold.
                Tier 2 is around 2.0, which wants a Bacterial Fuel
                Cell on a well bred colony, or a Focusing Lens
                propping up something weaker.
                Tier 3 is 3.0 and above, and only a Prismarine
                Crystal gets you there cleanly.
                """);

        page("mirror", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.PRISMATIC_MIRROR)
                .withText(context.pageText()));
        pageTitle("Prismatic Mirror");
        pageText("""
                Takes a beam from any side and throws it back out
                the way it is facing, so you can turn corners
                without a relay run in every direction.
                It keeps nine tenths of the purity it was given.
                One corner is cheap. Six corners is most of a tier.
                """);
        page("mirror_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Prismatic Mirror Recipe")
                .withRecipeId1("nautec:prismatic_mirror"));

        page("splitter", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.BEAM_SPLITTER)
                .withText(context.pageText()));
        pageTitle("Beam Splitter");
        pageText("""
                Takes one beam in and sends it out of every side
                that has somewhere to go. The power is shared
                evenly between the branches, so two ways out means
                half each, and each branch keeps four fifths of
                the purity.
                One strong source feeding a whole workshop, at the
                price of every branch being weaker than the trunk.
                """);
        page("splitter_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Beam Splitter Recipe")
                .withRecipeId1("nautec:beam_splitter"));

        page("lens", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.FOCUSING_LENS)
                .withText(context.pageText()));
        pageTitle("Focusing Lens");
        pageText("""
                The only optic that gives something back. A beam
                passing straight through comes out half a point
                purer than it went in.
                It does nothing to a dead line, so it cannot
                conjure a beam out of nothing. Use it at the end of
                a long route to claw back what the corners took.
                """);
        page("lens_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Focusing Lens Recipe")
                .withRecipeId1("nautec:focusing_lens"));
    }
}
