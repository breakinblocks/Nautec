package com.breakinblocks.nautec.compat.modonomicon.datagen.book;

import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.AquaticBiologyCategory;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.DeepEngineeringCategory;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.GettingStartedCategory;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.LaserAugmentationCategory;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.LaserChemistryCategory;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.SubmarineCategory;

public class NautecGuide extends SingleBookSubProvider {
    public NautecGuide() {
        super("nautec_guide", Nautec.MODID);
    }

    @Override
    protected void registerDefaultMacros() {
    }

    @Override
    protected void generateCategories() {
        add(new GettingStartedCategory(this).generate());
        add(new LaserChemistryCategory(this).generate());
        add(new AquaticBiologyCategory(this).generate());
        add(new DeepEngineeringCategory(this).generate());
        add(new LaserAugmentationCategory(this).generate());
        add(new SubmarineCategory(this).generate());
    }

    @Override
    protected String bookName() {
        return "Nautec Guide";
    }

    @Override
    protected String bookTooltip() {
        return "Notes on machinery somebody else left on the sea floor";
    }
}
