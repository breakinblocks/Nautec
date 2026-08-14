package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.aquatic_biology;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookMultiblockPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.breakinblocks.nautec.registries.NTBlocks;

public class BioReactorEntry extends BaseNautecEntry {
    public BioReactorEntry(CategoryProviderBase parent) {
        super(parent, "bio_reactor", "Bio Reactor", "Doesn't produce power", BookIconModel.create(NTBlocks.BIO_REACTOR));
    }

    @Override
    protected void generatePages() {
        page("bio_reactor", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.BIO_REACTOR)
                .withText(context.pageText()));
        pageTitle("Bio Reactor");
        pageText("""
                The Bio Reactor is a Multiblock Machine that
                requires AP to produce resources from bacteria.
                The Bio Reactor can handle up to 3 bacteria
                colonies at once. It needs 25 AP on its own and
                another 25 AP for every colony it holds, so a
                full reactor asks for 100 AP. Below that number
                nothing runs at all.
                The speed a colony produces at follows its
                production rate and its size. A big colony with a
                high production rate is many times faster than a
                small one with a low rate.
                """);
        this.page("bio_reactor_1", () -> BookMultiblockPageModel.create()
                .withMultiblockId(modLoc("bio_reactor"))
                .withText(context.pageText()));
        this.pageText("""
                Working colonies age. Once a colony has been
                running longer than its Lifespan its Vitality
                reaches zero and it starts losing part of itself
                with every resource it makes, until the slot is
                empty. Put the dish back in an Incubator before
                that happens: feeding a colony resets its age and
                sets Vitality back to full.
                All production recipes can be viewed in JEI.
                """);
    }
}
