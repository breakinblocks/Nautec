package com.breakinblocks.nautec.registries;

import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import com.breakinblocks.nautec.content.structures.ResearchOutpostPiece;
import com.breakinblocks.nautec.content.structures.ResearchOutpost;
import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.structures.DeepslateCrystalGeode;
import com.breakinblocks.nautec.content.structures.Ruins1;
import com.breakinblocks.nautec.content.structures.StoneCrystalGeode;
import com.breakinblocks.nautec.content.structures.UnderwaterGateway;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NTStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Nautec.MODID);

    public static DeferredHolder<StructureType<?>, StructureType<Ruins1>> RUINS_1 = STRUCTURES.register("ruins_1", () -> explicitStructureTypeTyping(Ruins1.CODEC));
    public static DeferredHolder<StructureType<?>, StructureType<StoneCrystalGeode>> STONE_CRYSTAL_GEODE = STRUCTURES.register("stone_crystal_geode", () -> explicitStructureTypeTyping(StoneCrystalGeode.CODEC));
    public static DeferredHolder<StructureType<?>, StructureType<DeepslateCrystalGeode>> DEEPSLATE_CRYSTAL_GEODE = STRUCTURES.register("deepslate_crystal_geode", () -> explicitStructureTypeTyping(DeepslateCrystalGeode.CODEC));

    public static DeferredHolder<StructureType<?>, StructureType<UnderwaterGateway>> UNDERWATER_GATEWAY = STRUCTURES.register("underwater_gateway", () -> explicitStructureTypeTyping(UnderwaterGateway.CODEC));

    public static DeferredHolder<StructureType<?>, StructureType<ResearchOutpost>> RESEARCH_OUTPOST = STRUCTURES.register("research_outpost", () -> explicitStructureTypeTyping(ResearchOutpost.CODEC));

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Nautec.MODID);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> RESEARCH_OUTPOST_PIECE = STRUCTURE_PIECES.register("research_outpost",
            () -> (StructurePieceType.ContextlessType) ResearchOutpostPiece::new);

    private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> structureCodec) {
        return () -> structureCodec;
    }
}
