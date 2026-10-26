package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.CrystalCultivatorBlockEntity;
import com.breakinblocks.nautec.content.blocks.CrystalCultivatorBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.material.Fluids;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

public final class CrystalCultivatorTests {
    private static final BlockPos SOURCE = new BlockPos(4, 3, 4);
    private static final BlockPos DEVICE = SOURCE.above();
    private static final BlockPos CROP = SOURCE.east();

    private static CrystalCultivatorBlockEntity place(NTGameTestHelper helper, Block source) {
        for (Direction face : Direction.values()) helper.setBlock(SOURCE.relative(face), Blocks.AIR);
        helper.setBlock(SOURCE, source);
        helper.setBlock(DEVICE, NTBlocks.CRYSTAL_CULTIVATOR.get());
        return helper.getBlockEntity(DEVICE, CrystalCultivatorBlockEntity.class);
    }

    private static void feed(CrystalCultivatorBlockEntity machine, int power, float purity) {
        machine.setPowerPerSide(machine.facing(), power);
        machine.setPurityPerSide(machine.facing(), purity);
        machine.commonTick();
    }

    private static BlockState cluster(Block block) {
        return block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.EAST);
    }

    private static boolean harvest(NTGameTestHelper helper, CrystalCultivatorBlockEntity machine) {
        return machine.harvest(helper.getLevel(), helper.absolutePos(CROP), Direction.EAST);
    }

    public static void register(NTTestRegistrar r) {
        r.add("cultivator/power_and_fortune_curves", 20, h -> {
            h.assertValueEqual(CrystalCultivatorBlockEntity.growthSpeed(19), 0.0, "below threshold");
            h.assertValueEqual(CrystalCultivatorBlockEntity.growthSpeed(20), 1.0, "base speed");
            h.assertValueEqual(CrystalCultivatorBlockEntity.growthSpeed(100), 10.0, "100 AP speed");
            double first = CrystalCultivatorBlockEntity.growthSpeed(180) - 10;
            double second = CrystalCultivatorBlockEntity.growthSpeed(260) - CrystalCultivatorBlockEntity.growthSpeed(180);
            h.assertTrue(first > second && second > 0 && first < 9, "growth has diminishing returns");
            h.assertValueEqual(CrystalCultivatorBlockEntity.fortune(2.9F), 2, "whole levels");
            h.assertValueEqual(CrystalCultivatorBlockEntity.fortune(6), 6, "six purity");
            h.assertValueEqual(CrystalCultivatorBlockEntity.fortune(9), 8, "diminishing fortune");
            h.assertValueEqual(CrystalCultivatorBlockEntity.fortune(12), 9, "higher purity");
            h.assertValueEqual(CrystalCultivatorBlockEntity.fortune(Float.NaN), 0, "invalid purity is safe");
            h.succeed();
        });
        r.add("cultivator/six_mounting_faces", 20, h -> {
            for (Direction face : Direction.values()) {
                h.setBlock(SOURCE.relative(face), NTBlocks.CRYSTAL_CULTIVATOR.get().defaultBlockState().setValue(CrystalCultivatorBlock.FACING, face));
                var be = h.getBlockEntity(SOURCE.relative(face), CrystalCultivatorBlockEntity.class);
                h.assertTrue(be.sourcePos().equals(h.absolutePos(SOURCE)), "points back at the source on " + face);
                h.assertTrue(be.getLaserInputs().size() == 1 && be.getLaserInputs().contains(face), "only outward receiver on " + face);
                h.assertTrue(be.getLaserOutputs().isEmpty(), "consumes the beam");
            }
            h.succeed();
        });
        r.add("cultivator/harvests_mature_only", 20, h -> {
            var be = place(h, NTBlocks.BUDDING_PRISMARINE.get());
            feed(be, 20, 0);
            h.setBlock(CROP, cluster(NTBlocks.LARGE_PRISMARINE_BUD.get()));
            h.assertFalse(harvest(h, be), "immature bud remains");
            h.setBlock(CROP, cluster(NTBlocks.PRISMARINE_CLUSTER.get()));
            h.assertTrue(harvest(h, be), "mature cluster harvested");
            h.assertTrue(be.storedItems() > 0, "normal loot collected");
            h.assertTrue(h.getBlockState(SOURCE).is(NTBlocks.BUDDING_PRISMARINE.get()), "source preserved");
            h.succeed();
        });
        r.add("cultivator/requires_power_and_supported_source", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            h.setBlock(CROP, cluster(Blocks.AMETHYST_CLUSTER));
            h.assertFalse(harvest(h, be), "no unpowered harvest");
            feed(be, 19, 6);
            h.assertFalse(harvest(h, be), "19 AP insufficient");
            h.setBlock(SOURCE, Blocks.STONE);
            feed(be, 100, 6);
            h.assertFalse(harvest(h, be), "ordinary blocks not supported");
            h.succeed();
        });
        r.add("cultivator/full_buffer_is_atomic", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            feed(be, 20, 0);
            for (int slot = 0; slot < 9; slot++) be.getItemStackHandler().setStackInSlot(slot, new ItemStack(Items.AMETHYST_SHARD, 64));
            be.getItemStackHandler().setStackInSlot(8, new ItemStack(Items.AMETHYST_SHARD, 63));
            h.setBlock(CROP, cluster(Blocks.AMETHYST_CLUSTER));
            h.assertFalse(harvest(h, be), "whole harvest must fit");
            h.assertValueEqual(be.storedItems(), 575, "no partial insertion");
            h.assertTrue(h.getBlockState(CROP).is(Blocks.AMETHYST_CLUSTER), "cluster retained");
            be.getItemStackHandler().setStackInSlot(8, ItemStack.EMPTY);
            h.assertTrue(harvest(h, be), "resumes after extraction");
            h.assertValueEqual(be.storedItems(), 516, "exact four shard harvest");
            h.succeed();
        });
        r.add("cultivator/preserves_water", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            feed(be, 20, 0);
            h.setBlock(CROP, cluster(Blocks.AMETHYST_CLUSTER).setValue(BlockStateProperties.WATERLOGGED, true));
            h.assertTrue(harvest(h, be), "waterlogged cluster harvested");
            h.assertTrue(h.getBlockState(CROP).getFluidState().is(Fluids.WATER), "water retained");
            h.succeed();
        });
        r.add("cultivator/fortune_reaches_loot_table", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            feed(be, 20, 6);
            int total = 0;
            for (int i = 0; i < 60; i++) {
                for (int slot = 0; slot < 9; slot++) be.getItemStackHandler().setStackInSlot(slot, ItemStack.EMPTY);
                h.setBlock(CROP, cluster(Blocks.AMETHYST_CLUSTER));
                h.assertTrue(harvest(h, be), "harvest succeeds");
                total += be.storedItems();
            }
            h.assertTrue(total > 60 * 4, "Fortune VI increases the actual vanilla loot");
            h.succeed();
        });
        r.add("cultivator/native_growth_accelerates", 30, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            int previous = h.getLevel().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            try {
                h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, h.getLevel().getServer());
                for (int i = 0; i < 40000; i++) feed(be, 20, 0);
                for (Direction face : Direction.values()) if (face != Direction.UP) h.assertTrue(h.getBlockState(SOURCE.relative(face)).isAir(), "20 AP adds no growth");
                for (int i = 0; i < 40000; i++) feed(be, 100, 0);
                h.assertTrue(be.storedItems() > 0, "100 AP grows and harvests through normal amethyst stages");
            } finally {
                h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(previous, h.getLevel().getServer());
            }
            h.succeed();
        });
        r.add("cultivator/respects_disabled_random_ticks", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            int previous = h.getLevel().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            try {
                h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0, h.getLevel().getServer());
                for (int i = 0; i < 2000; i++) feed(be, Integer.MAX_VALUE, 0);
                for (Direction face : Direction.values()) if (face != Direction.UP) h.assertTrue(h.getBlockState(SOURCE.relative(face)).isAir(), "no accelerated growth with random ticks disabled");
            } finally {
                h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(previous, h.getLevel().getServer());
            }
            h.succeed();
        });
        r.add("cultivator/attachments_do_not_stack", 20, h -> {
            place(h, Blocks.BUDDING_AMETHYST);
            int owners = 0;
            for (Direction face : Direction.values()) h.setBlock(SOURCE.relative(face), NTBlocks.CRYSTAL_CULTIVATOR.get().defaultBlockState().setValue(CrystalCultivatorBlock.FACING, face));
            for (Direction face : Direction.values()) if (h.getBlockEntity(SOURCE.relative(face), CrystalCultivatorBlockEntity.class).controlsSource()) owners++;
            h.assertValueEqual(owners, 1, "only one attachment controls a source");
            h.succeed();
        });
        r.add("cultivator/buffer_persists_and_can_be_extracted", 20, h -> {
            var be = place(h, Blocks.BUDDING_AMETHYST);
            be.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.AMETHYST_SHARD, 12));
            var saved = be.saveWithoutMetadata(h.getLevel().registryAccess());
            be.getItemStackHandler().setStackInSlot(0, ItemStack.EMPTY);
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
                be.loadWithComponents(saved, h.getLevel().registryAccess());
            }
            h.assertValueEqual(be.storedItems(), 12, "buffer survives reload");
            for (Direction face : Direction.values()) {
                var handler = h.getLevel().getCapability(TransferCapabilities.Item.BLOCK, h.absolutePos(DEVICE), face);
                h.assertTrue(handler != null, "extraction capability on " + face);
                try (Transaction tx = Transaction.openRoot()) {
                    var resource = ItemResource.of(Items.AMETHYST_SHARD);
                    h.assertValueEqual(handler.insert(0, resource, 1, tx), 0, "output only");
                    h.assertValueEqual(handler.extract(0, resource, 1, tx), 1, "pipe extraction");
                    tx.commit();
                }
            }
            h.assertValueEqual(be.storedItems(), 6, "extracted one per face");
            h.succeed();
        });
        r.add("cultivator/installed_mod_clusters", 30, h -> {
            int tested = 0;
            for (String namespace : new String[]{"ae2", "geore"}) {
                Block source = BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(namespace)
                        && b.defaultBlockState().is(NTTags.Blocks.CULTIVATOR_SOURCES)
                        && (!namespace.equals("ae2") || BuiltInRegistries.BLOCK.getKey(b).getPath().equals("flawless_budding_quartz"))).findFirst().orElse(null);
                if (source == null) continue;
                Block crop = BuiltInRegistries.BLOCK.stream().filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(namespace)
                        && b.defaultBlockState().is(NTTags.Blocks.CULTIVATOR_CLUSTERS)).findFirst().orElseThrow();
                var be = place(h, source);
                feed(be, 100, 6);
                h.setBlock(CROP, cluster(crop));
                h.assertTrue(harvest(h, be), "actual " + namespace + " cluster harvests");
                h.assertTrue(be.storedItems() > 0, "actual " + namespace + " drops stored");
                be = place(h, source);
                int previous = h.getLevel().getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
                try {
                    h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(3, h.getLevel().getServer());
                    for (int i = 0; i < 20000; i++) feed(be, 1000, 0);
                } finally {
                    h.getLevel().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(previous, h.getLevel().getServer());
                }
                h.assertTrue(be.storedItems() > 0, "actual " + namespace + " budding growth produces harvests");
                h.assertTrue(h.getBlockState(SOURCE).is(source), "non-decaying source retained");
                tested++;
            }
            if (Boolean.getBoolean("nautec.cultivator.requireCompat")) h.assertValueEqual(tested, 2, "AE2 and GeoOre both exercised");
            h.succeed();
        });
    }
}
