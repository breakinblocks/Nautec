package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.items.SeaEyeTarget;
import com.breakinblocks.nautec.content.structures.ResearchOutpostPiece;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public final class ResearchOutpostTests {
    private ResearchOutpostTests() {
    }

    private static void clear(ServerLevel level, BoundingBox box) {
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY() + 3, box.maxZ())) {
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
    }

    private static void check(NTGameTestHelper helper, Rotation rotation) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(0, 40, 0));
        ResearchOutpostPiece piece = new ResearchOutpostPiece(origin, rotation, 12345L);
        try {
            piece.buildAll(level, RandomSource.create(1));
            helper.assertTrue(level.getBlockState(piece.world(15, 3, 15)).is(Blocks.WATER), rotation + ": the hub is flooded");
            helper.assertTrue(level.getBlockState(piece.world(15, 1, 20)).is(NTBlocks.PRESSURE_HATCH.get()), rotation + ": a Pressure Hatch guards the hub");
            helper.assertTrue(level.getBlockState(piece.world(15, 2, 20)).is(NTBlocks.PRESSURE_HATCH.get()), rotation + ": the hatch is two blocks tall");
            BlockEntity lectern = level.getBlockEntity(piece.world(15, 1, 13));
            helper.assertTrue(lectern instanceof LecternBlockEntity l && l.getBook().has(DataComponents.WRITTEN_BOOK_CONTENT),
                    rotation + ": a research log sits on the lectern");
            for (BlockPos chest : new BlockPos[]{piece.world(19, 1, 11), piece.world(17, 1, 1), piece.world(1, 1, 15), piece.world(29, 1, 15)}) {
                BlockEntity entity = level.getBlockEntity(chest);
                helper.assertTrue(entity instanceof RandomizableContainer container && NTLootTables.RESEARCH_OUTPOST.equals(container.getLootTable()),
                        rotation + ": outpost loot at " + chest);
            }
            BlockEntity crate = level.getBlockEntity(piece.world(11, 1, 11));
            helper.assertTrue(crate instanceof RandomizableContainer container && NTLootTables.CRATE.equals(container.getLootTable()),
                    rotation + ": a salvage crate in the hub");
            helper.assertTrue(level.getBlockState(piece.world(15, 2, 8)).is(Blocks.WATER), rotation + ": the lab corridor is open");
            helper.assertTrue(level.getBlockState(piece.world(22, 2, 15)).is(Blocks.WATER), rotation + ": the storage corridor is open");
            helper.assertTrue(level.getBlockState(piece.world(8, 2, 15)).is(Blocks.WATER), rotation + ": the power corridor is open");
            helper.assertFalse(level.getBlockState(piece.world(15, -1, 15)).isAir(), rotation + ": a foundation holds it up");
        } finally {
            clear(level, piece.getBoundingBox());
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("worldgen/research_outpost_builds_every_room", 40, helper -> {
            check(helper, Rotation.NONE);
            check(helper, Rotation.CLOCKWISE_90);
            helper.succeed();
        });

        r.add("worldgen/research_outpost_is_registered_and_findable", 20, helper -> {
            Registry<Structure> structures = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
            ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, Nautec.rl("research_outpost"));
            Holder<Structure> outpost = structures.getHolder(key).orElse(null);
            helper.assertTrue(outpost != null, "the outpost structure is registered");
            helper.assertTrue(outpost.is(SeaEyeTarget.RESEARCH_OUTPOSTS.structures()), "the Eye of the Sea can point to outposts");
            Registry<StructureSet> sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
            helper.assertTrue(sets.getHolder(ResourceKey.create(Registries.STRUCTURE_SET, Nautec.rl("research_outposts"))).isPresent(), "outposts have a structure set");
            ItemStack log = ResearchOutpostPiece.researchLog(2);
            helper.assertValueEqual(log.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size(), ResearchOutpostPiece.LOG_PAGES, "a log has its pages");
            helper.succeed();
        });
    }
}
