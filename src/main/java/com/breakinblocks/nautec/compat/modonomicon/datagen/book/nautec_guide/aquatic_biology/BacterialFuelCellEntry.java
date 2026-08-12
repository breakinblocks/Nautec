package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.aquatic_biology;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class BacterialFuelCellEntry extends BaseNautecEntry {
    public BacterialFuelCellEntry(CategoryProviderBase parent) {
        super(parent, "bacterial_fuel_cell", "Bacterial Fuel Cell", "Burns a colony for power",
                BookIconModel.create(NTBlocks.BACTERIAL_FUEL_CELL));
    }

    @Override
    protected void generatePages() {
        page("bacterial_fuel_cell", () -> BookSpotlightPageModel.create()
                .withTitle(context.pageTitle())
                .withItem(NTBlocks.BACTERIAL_FUEL_CELL)
                .withText(context.pageText()));
        pageTitle("Bacterial Fuel Cell");
        pageText("""
                The Bacterial Fuel Cell is the other thing you can
                do with a colony. Instead of working it for
                resources, the Fuel Cell eats it and gives you a
                laser in return.
                Right-click it with a Petri Dish to load a colony.
                Right-click again with an empty dish to take back
                whatever is left.
                It only burns while its beam has somewhere to go.
                Point it at nothing and the colony sits there
                untouched.
                """);

        page("power", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Output");
        pageText("""
                Production Rate sets how much power the beam
                carries, and it burns colony at the same pace, so
                a fast colony is not free energy. It is the same
                energy sooner.
                Mutation Resistance sets the purity of the beam.
                A colony at the resistance cap emits at 2.5, just
                short of a Prismarine Crystal, without any of the
                multiblock around it.
                Colony size is simply how long it lasts.
                """);

        page("aging", () -> BookTextPageModel.create()
                .withTitle(context.pageTitle())
                .withText(context.pageText()));
        pageTitle("Old Colonies");
        pageText("""
                A Fuel Cell does not age a colony, it only
                consumes it, so Vitality does not matter here.
                That makes it the right place for a colony that
                has outlived its Lifespan and is no longer worth
                feeding. A worn out colony still burns exactly as
                well as a fresh one.
                The Bio Reactor and the Fuel Cell want the same
                colonies. Deciding which one gets them is the
                whole trade: matter or energy.
                """);

        page("bacterial_fuel_cell_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Bacterial Fuel Cell Recipe")
                .withRecipeId1("nautec:bacterial_fuel_cell"));
    }
}
