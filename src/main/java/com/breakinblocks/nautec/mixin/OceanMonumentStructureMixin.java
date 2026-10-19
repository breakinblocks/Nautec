package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.worldgen.MonumentLowering;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OceanMonumentStructure.class)
public abstract class OceanMonumentStructureMixin {
    @WrapMethod(method = "generatePieces")
    private static void nautec$lowerPerDimension(StructurePiecesBuilder builder, Structure.GenerationContext context, Operation<Void> original) {
        MonumentLowering.with(MonumentLowering.lowersIn(context.randomState()), () -> original.call(builder, context));
    }

    @WrapMethod(method = "regeneratePiecesAfterLoad")
    private static PiecesContainer nautec$keepSavedHeight(ChunkPos chunkPos, long seed, PiecesContainer pieces, Operation<PiecesContainer> original) {
        return MonumentLowering.with(MonumentLowering.wasLowered(pieces), () -> original.call(chunkPos, seed, pieces));
    }
}
