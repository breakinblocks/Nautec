package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class PressureForgeEntry extends BaseNautecEntry {
    public PressureForgeEntry(CategoryProviderBase parent) {
        super(parent, "pressure_forge", "Abyssal Pressure Forge", "Depth is the tool",
                BookIconModel.create(NTBlocks.PRESSURE_FORGE));
    }

    @Override
    protected void generatePages() {
        page("pressure_forge", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.PRESSURE_FORGE)
                .withText(context.pageText()));
        pageTitle("Abyssal Pressure Forge");
        pageText("""
                Every other machine you have built works anywhere
                you can get a beam to. This one does not. It runs
                at depth, under a real column of water, and
                nowhere else.
                Take away the water and it stops, however deep it
                is. Dig a dry shaft to bedrock and you have built
                nothing. The sea has to be sitting on top of it.
                """);

        page("requirements", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("What It Wants");
        pageText("""
                Four things at once, and it is fussy about all of
                them.
                Depth, measured where the Forge sits. Water above
                it, unbroken. A beam, and a clean one, because the
                better recipes ask for purity a Fuel Cell or a
                Crystal has to supply. And Etching Acid, which it
                drinks steadily.
                The deepest recipes want to be far below sea
                level, which is the point: the Abyssal Trench is
                where this machine belongs.
                """);

        page("outputs", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("What It Makes");
        pageText("""
                Flawless Prismarine Crystals, pressed out of
                Resonant Shards. The Forge is the only thing that
                makes them.
                Deep Steel Plating, which the sea presses out of
                Aquarine Steel far below. It is what patches a
                breached submersible hull on an anvil, so the
                thing that takes you into the deep is repaired
                with something only the deep can make.
                """);

        page("pressure_forge_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Abyssal Pressure Forge Recipe")
                .withRecipeId1("nautec:pressure_forge"));
    }
}
