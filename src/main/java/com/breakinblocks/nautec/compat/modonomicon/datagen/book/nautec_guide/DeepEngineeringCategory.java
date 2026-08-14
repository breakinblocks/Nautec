package com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.ModonomiconProviderBase;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryParentModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.deep_engineering.BeamOpticsEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.deep_engineering.GatewayEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.deep_engineering.PressureForgeEntry;
import com.breakinblocks.nautec.compat.modonomicon.datagen.book.nautec_guide.deep_engineering.ResonanceChamberEntry;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.world.phys.Vec2;

public class DeepEngineeringCategory extends CategoryProvider {
    public DeepEngineeringCategory(ModonomiconProviderBase parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        return new String[0];
    }

    @Override
    protected void generateEntries() {
        BookEntryModel beamOpticsEntry = new BeamOpticsEntry(this).generate(new Vec2(0, 0));
        add(beamOpticsEntry);

        BookEntryModel resonanceChamberEntry = new ResonanceChamberEntry(this).generate(new Vec2(2, 0));
        add(resonanceChamberEntry.withParent(beamOpticsEntry));

        BookEntryModel gatewayEntry = new GatewayEntry(this).generate(new Vec2(4, -2));
        add(gatewayEntry.withParent(resonanceChamberEntry));

        BookEntryModel pressureForgeEntry = new PressureForgeEntry(this).generate(new Vec2(4, 2));
        add(pressureForgeEntry.withParent(resonanceChamberEntry));
    }

    @Override
    protected String categoryName() {
        return "Deep Engineering";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(NTItems.RESONANT_SHARD);
    }

    @Override
    public String categoryId() {
        return "deep_engineering";
    }

    @Override
    protected BookEntryParentModel parent(BookEntryModel parentEntry) {
        return BookEntryParentModel.create(Nautec.rl("laser_chemistry"));
    }
}
