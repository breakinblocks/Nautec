package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class GatewayEntry extends BaseNautecEntry {
    public GatewayEntry(CategoryProviderBase parent) {
        super(parent, "gateway", "Gateway", "Somewhere else, immediately",
                BookIconModel.create(NTBlocks.GATEWAY));
    }

    @Override
    protected void generatePages() {
        page("gateway", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.GATEWAY)
                .withText(context.pageText()));
        pageTitle("Gateway");
        pageText("""
                A flat prismarine plate that moves whatever stands
                on it to another plate wearing the same address.
                No power, no beam, no upkeep at all. The whole
                cost was paid in Resonant Shards to build it,
                which is why it sits at the end of the crystal
                line rather than the start.
                Build two, give them the same address, and the
                distance between them stops mattering.
                Some already sit on the sea floor, spread thin.
                Those all share one address, so the ones you find
                are a network before you touch them.
                """);

        page("addresses", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Addresses");
        pageText("""
                Every Gateway wears four coloured fins, one per
                corner of its top face. Those four colours are its
                address, and there are eight colours to choose
                from, which is four thousand and ninety six
                addresses in total.
                Right-click a fin with a dye to set that corner.
                Right-click with an empty hand to read the address
                back. Two Gateways with the same four colours are
                a pair; anything else ignores them.
                """);

        page("travel", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Travelling");
        pageText("""
                Stand on one and you arrive at the nearest other
                Gateway with the same address. Nearest, not the
                one you meant, so spread a shared address thin or
                give each pair its own.
                Whatever you are riding comes with you, and so
                does anything riding you. A submersible arrives
                still under you and still sealed.
                Everything that arrives is barred from using
                another Gateway for a few seconds, which is what
                stops a pair throwing you back and forth.
                """);

        page("limits", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Limits");
        pageText("""
                A Gateway only reaches other Gateways in the same
                world. Ones you build in the Nether or the End
                form their own separate networks and cannot see
                the ocean.
                Breaking one keeps its address on the dropped
                block, so you can move a Gateway without having to
                dye it all over again.
                """);

        page("gateway_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Gateway Recipe")
                .withRecipeId1("nautec:gateway"));
    }
}
