package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryParentModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.BatteryEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.BuddingPrismarineEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.ChargerEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.ChemistryIntroductionEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.CrystalShardsEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.DrainEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.FishingStationEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.LaserManipulationEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.MixerEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.laser_chemistry.ToolsEntry;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.world.phys.Vec2;

public class LaserChemistryCategory extends CategoryProvider {
    public LaserChemistryCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[0];
    }

    @Override
    protected void generateEntries() {
        BookEntryModel laserManipulationEntry = new LaserManipulationEntry(this)
                .generate(new Vec2(0, -2));
        add(laserManipulationEntry);
        BookEntryModel mixerEntry = new MixerEntry(this)
                .generate(new Vec2(0, 0));
        add(mixerEntry);
        BookEntryModel chargerEntry = new ChargerEntry(this)
                .generate(new Vec2(0, 2));
        add(chargerEntry);
        BookEntryModel chemistryIntroductionEntry = new ChemistryIntroductionEntry(this)
                .generate(new Vec2(2, 0));
        add(chemistryIntroductionEntry.withParent(mixerEntry));
        BookEntryModel toolsEntry = new ToolsEntry(this)
                .generate(new Vec2(4, 0));
        add(toolsEntry.withParent(chemistryIntroductionEntry).withParent(chargerEntry));
        BookEntryModel crystalShardsEntry = new CrystalShardsEntry(this)
                .generate(new Vec2(6, 0));
        add(crystalShardsEntry.withParent(toolsEntry));
        BookEntryModel drainEntry = new DrainEntry(this)
                .generate(new Vec2(4, -2));
        add(drainEntry.withParent(mixerEntry));
        BookEntryModel batteryEntry = new BatteryEntry(this)
                .generate(new Vec2(8, -2));
        add(batteryEntry.withParent(crystalShardsEntry));
        BookEntryModel buddingPrismarineEntry = new BuddingPrismarineEntry(this)
                .generate(new Vec2(8, 0));
        add(buddingPrismarineEntry.withParent(crystalShardsEntry));
        BookEntryModel fishingStationEntry = new FishingStationEntry(this)
                .generate(new Vec2(6, -2));
        add(fishingStationEntry.withParent(drainEntry));
    }

    @Override
    protected String categoryName() {
        return "Laser Chemistry";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL);
    }

    @Override
    public String categoryId() {
        return "laser_chemistry";
    }

    @Override
    protected BookEntryParentModel parent(BookEntryModel parentEntry) {
        return BookEntryParentModel.create(Nautec.rl("getting_started"));
    }
}
