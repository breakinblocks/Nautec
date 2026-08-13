package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookEntityPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class DeepLifeEntry extends BaseNautecEntry {
    public DeepLifeEntry(CategoryProviderBase parent) {
        super(parent, "deep_life", "Life in the Deep", "Six plants and four creatures",
                BookIconModel.create(NTItems.LUMINOUS_MEMBRANE));
    }

    @Override
    protected void generatePages() {
        this.page("plants", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Six Plants");
        this.pageText("""
                Deep Kelp grows in columns like ordinary kelp. Prismarine Frond
                grows on the reef, Vent Tubeworm at the vents, Abyssal Coral on
                the trench floor.
                \\
                \\
                Three give off light: Luminescent Algae at nine, Glow Polyp at
                seven, Vent Tubeworm at three. Glow Polyp creeps across stone
                and spreads like glow lichen.
                \\
                \\
                All six need shears. Anything else drops nothing.
                """);

        this.page("grafting", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Grafting");
        this.pageText("""
                Every one of them takes a bacteria graft. The Aquatic Biology
                chapter covers which bacteria each one carries.
                """);

        this.page("silt_skipper", () -> BookEntityPageModel.create()
                .withEntityId("nautec:silt_skipper")
                .withEntityName("Silt Skipper")
                .withScale(1.6F)
                .withText(this.context().pageText()));
        this.pageText("""
                A schooling fish, three to eight at a time, in all four oceans.
                Passive. Can be picked up in a bucket.
                """);

        this.page("lantern_jelly", () -> BookEntityPageModel.create()
                .withEntityId("nautec:lantern_jelly")
                .withEntityName("Lantern Jelly")
                .withScale(1.2F)
                .withText(this.context().pageText()));
        this.pageText("""
                Drifts through the Bioluminescent Grove, carrying its own light.
                Passive and slow.
                \\
                \\
                Drops Luminous Membrane, used for the Photophore Skin augment.
                """);

        this.page("vent_crawler", () -> BookEntityPageModel.create()
                .withEntityId("nautec:vent_crawler")
                .withEntityName("Vent Crawler")
                .withScale(1.2F)
                .withText(this.context().pageText()));
        this.pageText("""
                Walks the Hydrothermal Vents floor. Flees rather than fights.
                \\
                \\
                Drops Chitin Plate. Four make the Vent Carapace augment.
                """);

        this.page("abyssal_maw", () -> BookEntityPageModel.create()
                .withEntityId("nautec:abyssal_maw")
                .withEntityName("Abyssal Maw")
                .withScale(0.9F)
                .withText(this.context().pageText()));
        this.pageText("""
                Hostile, and hunts on sight in the Abyssal Trench. Twenty four
                health, and hits harder than a Drowned.
                \\
                \\
                Drops the Abyssal Organ, used for the Abyssal Eyes augment.
                """);
    }
}
