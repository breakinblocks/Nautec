package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryParentModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.submarine.*;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.world.phys.Vec2;

public class SubmarineCategory extends CategoryProvider {
    public SubmarineCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[0];
    }

    @Override
    protected void generateEntries() {
        BookEntryModel submarineEntry = new SubmarineEntry(this).generate(new Vec2(0, 0));
        add(submarineEntry);

        BookEntryModel controlsEntry = new SubmarineControlsEntry(this).generate(new Vec2(2, -2));
        add(controlsEntry.withParent(submarineEntry));

        BookEntryModel powerEntry = new SubmarinePowerEntry(this).generate(new Vec2(2, 0));
        add(powerEntry.withParent(submarineEntry));

        BookEntryModel hullEntry = new SubmarineHullEntry(this).generate(new Vec2(2, 2));
        add(hullEntry.withParent(submarineEntry));

        BookEntryModel modulesEntry = new SubmarineModulesEntry(this).generate(new Vec2(4, 0));
        add(modulesEntry.withParent(powerEntry).withParent(controlsEntry));

        BookEntryModel solarEntry = new SolarModuleEntry(this).generate(new Vec2(6, -3));
        add(solarEntry.withParent(modulesEntry));

        BookEntryModel armorEntry = new ArmorModuleEntry(this).generate(new Vec2(6, -1));
        add(armorEntry.withParent(modulesEntry));

        BookEntryModel boosterEntry = new BoosterModuleEntry(this).generate(new Vec2(6, 1));
        add(boosterEntry.withParent(modulesEntry));

        BookEntryModel stealthEntry = new StealthModuleEntry(this).generate(new Vec2(6, 3));
        add(stealthEntry.withParent(modulesEntry));

        BookEntryModel sonarEntry = new SonarModuleEntry(this).generate(new Vec2(8, -3));
        add(sonarEntry.withParent(modulesEntry));

        BookEntryModel shieldEntry = new ShieldModuleEntry(this).generate(new Vec2(8, -1));
        add(shieldEntry.withParent(modulesEntry));

        BookEntryModel laserEntry = new ImpulseLaserModuleEntry(this).generate(new Vec2(8, 1));
        add(laserEntry.withParent(modulesEntry));

        BookEntryModel teleportEntry = new TeleportModuleEntry(this).generate(new Vec2(8, 3));
        add(teleportEntry.withParent(modulesEntry));
    }

    @Override
    protected String categoryName() {
        return "The Sea Scout";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(NTItems.SUBMARINE);
    }

    @Override
    public String categoryId() {
        return "submarine";
    }

    @Override
    protected BookEntryParentModel parent(BookEntryModel parentEntry) {
        return BookEntryParentModel.create(Nautec.rl("getting_started"));
    }
}
