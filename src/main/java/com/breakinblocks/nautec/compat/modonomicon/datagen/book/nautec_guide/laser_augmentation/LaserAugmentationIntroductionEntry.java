package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_augmentation;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.breakinblocks.nautec.registries.NTItems;

public class LaserAugmentationIntroductionEntry extends BaseNautecEntry {
    public LaserAugmentationIntroductionEntry(CategoryProviderBase parent) {
        super(parent, "laser_augmentation_introduction", "Introduction to Laser Augmentation", "Beginning of the end", BookIconModel.create(NTItems.ELDRITCH_HEART));
    }

    @Override
    protected void generatePages() {
        this.page("laser_augmentation_intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Introduction to Laser Augmentation");
        this.pageText("""
                The Augmentation Station is theirs. I did not work out how to
                cut a limb off and seat a machine in its place; I found a room
                built to do it, and the sockets were already the right shape.
                \\
                \\
                They did not visit the water. They were rebuilt for it. Once
                you understand that, the organs we pull out of the deep stop
                looking like trophies and start looking like spare parts.
                \\
                \\
                Read this chapter carefully. It is the one that is done to you
                rather than by you.
                """);
    }
}
