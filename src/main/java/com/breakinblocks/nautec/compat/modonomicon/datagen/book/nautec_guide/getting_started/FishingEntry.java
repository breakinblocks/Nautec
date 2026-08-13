package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.getting_started;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class FishingEntry extends BaseNautecEntry {
    public FishingEntry(CategoryProviderBase parent) {
        super(parent, "fishing", "Fishing", "Lucky zones and the Prismatic Fishing Rod",
                BookIconModel.create(NTItems.NAUTEC_FISHING_ROD));
    }

    @Override
    protected void generatePages() {
        this.page("intro", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Fishing");
        this.pageText("""
                Two separate systems, and they stack. Lucky zones change where
                you cast. The Prismatic Fishing Rod changes what happens once
                something bites.
                \\
                \\
                Both add to an ordinary catch. Neither replaces it.
                """);

        this.page("zones", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Lucky Zones");
        this.pageText("""
                Patches of water a few blocks across form near you while you are
                on an ocean or a river, marked by glow particles rising out of
                them.
                \\
                \\
                Cast into one and you get an extra roll on that zone's loot on
                top of your normal catch. Any rod works. Catching from a zone
                uses it up, and an untouched zone fades after a few minutes.
                \\
                \\
                A zone only forms where every block around it is open water.
                What it can give you depends on the water: rivers, oceans in
                general, and each of the four Nautec oceans have their own
                tables.
                """);

        this.page("live_catches", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Live Catches");
        this.pageText("""
                A zone can hook a creature rather than an item. Reeling one in
                releases the animal at your bobber.
                \\
                \\
                Which creatures, and in which waters, is set in the loot tables.
                """);

        this.page("rod", () -> BookSpotlightPageModel.create()
                .withTitle(this.context().pageTitle())
                .withItem(NTItems.NAUTEC_FISHING_ROD)
                .withText(this.context().pageText()));
        this.pageTitle("Prismatic Fishing Rod");
        this.pageText("""
                Casts like any rod. When something bites, a bar appears and you
                have about three seconds to act.
                \\
                \\
                Space or the left mouse button is the strike. Failing it, or
                ignoring the bar, gives you the catch you would have had
                anyway.
                """);

        this.page("minigames", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Three Bars");
        this.pageText("""
                Which one appears is random. The bar names itself.
                \\
                \\
                Something is biting. Strike as the marker crosses the green.
                \\
                \\
                It is fighting you. Three marks, struck in order.
                \\
                \\
                It is running with the line. Hold from the start of the green,
                release at the end.
                """);

        this.page("rewards", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Rewards");
        this.pageText("""
                Succeed and a treasure roll is added to the catch. If the catch
                was already a treasure one, you get two treasure rolls and an
                ordinary roll.
                \\
                \\
                Inside a lucky zone both apply.
                """);

        this.page("rod_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Prismatic Fishing Rod")
                .withRecipeId1("nautec:nautec_fishing_rod"));
    }
}
