package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine;

import com.breakinblocks.nautec.compat.modonomicon.datagen.book.BaseNautecEntry;
import com.breakinblocks.nautec.registries.NTItems;
import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookCraftingRecipePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;

public class ImpulseLaserModuleEntry extends BaseNautecEntry {
    public ImpulseLaserModuleEntry(CategoryProviderBase parent) {
        super(parent, "impulse_laser_module", "Impulse Laser", "Twin beams", BookIconModel.create(NTItems.IMPULSE_LASER_MODULE));
    }

    @Override
    protected void generatePages() {
        this.page("impulse_laser_module", () -> BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
        this.pageTitle("Impulse Laser");
        this.pageText("""
                Fires as a toggle rather than a shot. While it is lit, two prismatic beams cut whatever the nose is pointed at, out to sixty-odd blocks.
                \\
                It is expensive: it draws hard on the cell every cycle, and switches itself off the moment the power runs out or the last pilot leaves.
                """);
        this.page("impulse_laser_module_recipe", () -> BookCraftingRecipePageModel.create()
                .withTitle1("Impulse Laser")
                .withRecipeId1("nautec:impulse_laser_module"));
    }
}
