package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalFrameBlock;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlock;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import com.breakinblocks.nautec.content.extradimensional.FramePart;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlock;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.content.resonance.SatelliteArrayBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ExtradimensionalStorageTests {
    private static final BlockPos CORE = new BlockPos(4, 1, 4);

    private ExtradimensionalStorageTests() {
    }

    private static ExtradimensionalStorageBlockEntity core(GameTestHelper helper) {
        helper.setBlock(CORE, NTBlocks.EXTRADIMENSIONAL_STORAGE.get());
        return helper.getBlockEntity(CORE, ExtradimensionalStorageBlockEntity.class);
    }

    private static Block frame(int tier) {
        return switch (tier) {
            case 2 -> NTBlocks.RIFT_CONTAINMENT_FRAME.get();
            case 3 -> NTBlocks.VOID_CONTAINMENT_FRAME.get();
            case 4 -> NTBlocks.ABYSSAL_CONTAINMENT_FRAME.get();
            default -> NTBlocks.EVENT_HORIZON_CONTAINMENT_FRAME.get();
        };
    }

    private static void ring(GameTestHelper helper, int ring, Block block) {
        for (int dx = -ring; dx <= ring; dx++) {
            for (int dz = -ring; dz <= ring; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == ring) {
                    helper.setBlock(CORE.offset(dx, 0, dz), block);
                }
            }
        }
    }

    private static @Nullable EnergyHandler energy(GameTestHelper helper, BlockPos relative, Direction side) {
        return helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(relative), side);
    }

    private static FramePart part(GameTestHelper helper, BlockPos relative) {
        BlockState state = helper.getBlockState(relative);
        return state.getBlock() instanceof ExtradimensionalFrameBlock ? state.getValue(ExtradimensionalFrameBlock.PART) : FramePart.LOOSE;
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    public static void register(NTTestRegistrar r) {
        r.add("extradimensional/single_block_is_tier_one_with_side_ports", 20, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            helper.assertValueEqual(storage.tier(), 1, "a lone core is tier 1");
            helper.assertValueEqual(storage.capacity(), NTConfig.extradimensionalCapacity[0], "tier 1 capacity");
            EnergyHandler north = energy(helper, CORE, Direction.NORTH);
            helper.assertTrue(north != null, "a tier 1 core is its own port on the north side");
            helper.assertTrue(energy(helper, CORE, Direction.UP) == null, "no port on top");
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(north.insert(1_000, tx), 1_000, "an input port takes FE");
                helper.assertValueEqual(north.extract(1_000, tx), 0, "an input port gives none back");
                tx.commit();
            }
            helper.assertValueEqual(storage.getStored(), 1_000L, "stored after a committed insert");
            try (Transaction tx = Transaction.openRoot()) {
                north.insert(5_000, tx);
            }
            helper.assertValueEqual(storage.getStored(), 1_000L, "an aborted insert leaves nothing behind");
            storage.setMode(Direction.NORTH, ExtradimensionalStorageBlockEntity.PortMode.OUTPUT);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(north.insert(1_000, tx), 0, "an output port takes nothing");
                helper.assertValueEqual(north.extract(400, tx), 400, "an output port gives FE");
                tx.commit();
            }
            helper.assertValueEqual(storage.getStored(), 600L, "stored after extracting");
            helper.succeed();
        });

        r.add("extradimensional/a_port_moves_no_more_than_its_rate_per_tick", 20, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            int rate = storage.rate();
            EnergyHandler east = energy(helper, CORE, Direction.EAST);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(east.insert(rate, tx), rate, "the first insert this tick fills the port's budget");
                helper.assertValueEqual(east.insert(rate, tx), 0, "the port is spent until the next tick");
                tx.commit();
            }
            EnergyHandler west = energy(helper, CORE, Direction.WEST);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(west.insert(10, tx), 10, "each port has its own budget");
                tx.commit();
            }
            helper.succeed();
        });

        r.add("extradimensional/rings_form_each_tier_up_to_nine_by_nine", 200, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            int[] built = {1};
            helper.onEachTick(() -> {
                if (storage.tier() == built[0] && built[0] < ExtradimensionalStorageBlockEntity.MAX_TIER) {
                    built[0]++;
                    ring(helper, built[0] - 1, frame(built[0]));
                }
            });
            helper.succeedWhen(() -> {
                helper.assertValueEqual(storage.tier(), ExtradimensionalStorageBlockEntity.MAX_TIER, "five tiers once four rings are built");
                helper.assertValueEqual(storage.capacity(), NTConfig.extradimensionalCapacity[4], "tier 5 capacity");
                helper.assertValueEqual(helper.getBlockState(CORE).getValue(ExtradimensionalStorageBlock.TIER), 5, "the core block shows the tier");
                helper.assertValueEqual(part(helper, new BlockPos(4, 1, 0)), FramePart.PORT, "the middle of the north edge is a port");
                helper.assertValueEqual(helper.getBlockState(new BlockPos(4, 1, 0)).getValue(ExtradimensionalFrameBlock.FACING), Direction.NORTH,
                        "the north port faces north");
                helper.assertValueEqual(part(helper, new BlockPos(0, 1, 0)), FramePart.CORNER, "the outer corner");
                helper.assertValueEqual(part(helper, new BlockPos(2, 1, 0)), FramePart.EDGE, "an outer edge");
                helper.assertValueEqual(part(helper, new BlockPos(4, 1, 3)), FramePart.INNER_EDGE, "an inner ring block");
                helper.assertTrue(energy(helper, new BlockPos(4, 1, 0), Direction.NORTH) != null, "the outer port takes FE");
                helper.assertTrue(energy(helper, new BlockPos(4, 1, 0), Direction.SOUTH) == null, "only on its outer face");
                helper.assertTrue(energy(helper, new BlockPos(4, 1, 3), Direction.NORTH) == null, "inner rings are not ports");
                helper.assertTrue(energy(helper, CORE, Direction.NORTH) == null, "the core is no longer a port");
            });
        });

        r.add("extradimensional/breaking_an_inner_ring_drops_the_tier", 200, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            ring(helper, 1, frame(2));
            ring(helper, 2, frame(3));
            boolean[] broken = {false};
            helper.onEachTick(() -> {
                if (!broken[0] && storage.tier() == 3) {
                    broken[0] = true;
                    helper.setBlock(new BlockPos(4, 1, 3), Blocks.AIR);
                }
            });
            helper.succeedWhen(() -> {
                helper.assertTrue(broken[0], "waiting for tier 3");
                helper.assertValueEqual(storage.tier(), 1, "a gap in the first ring drops the storage back to tier 1");
                helper.assertValueEqual(part(helper, new BlockPos(4, 1, 2)), FramePart.LOOSE, "the outer ring comes apart");
                helper.assertValueEqual(part(helper, new BlockPos(3, 1, 3)), FramePart.LOOSE, "the inner ring comes apart");
                helper.assertTrue(energy(helper, CORE, Direction.NORTH) != null, "the core is a port again");
            });
        });

        r.add("extradimensional/the_wrong_frame_does_not_form", 60, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            ring(helper, 1, frame(3));
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(storage.tier(), 1, "a first ring of Void frames is not a tier 2 ring");
                helper.assertValueEqual(part(helper, new BlockPos(4, 1, 3)), FramePart.LOOSE, "and does not form");
                helper.succeed();
            });
        });

        r.add("extradimensional/breaking_the_core_keeps_its_charge", 20, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            storage.setStored(123_456_789L);
            ServerLevel level = helper.getLevel();
            BlockPos pos = helper.absolutePos(CORE);
            List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, storage);
            helper.assertValueEqual(drops.size(), 1, "the core drops itself");
            helper.assertValueEqual(drops.get(0).get(NTDataComponents.STORED_FE.get()), 123_456_789L, "with its FE");
            helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);
            level.setBlock(helper.absolutePos(new BlockPos(1, 1, 1)), NTBlocks.EXTRADIMENSIONAL_STORAGE.get().defaultBlockState(), Block.UPDATE_ALL);
            ExtradimensionalStorageBlockEntity placed = helper.getBlockEntity(new BlockPos(1, 1, 1), ExtradimensionalStorageBlockEntity.class);
            placed.applyComponentsFromItemStack(drops.get(0));
            helper.assertValueEqual(placed.getStored(), 123_456_789L, "placing it back restores the FE");
            helper.succeed();
        });

        r.add("extradimensional/an_output_port_pushes_into_its_neighbour", 60, helper -> {
            ExtradimensionalStorageBlockEntity storage = core(helper);
            BlockPos converterPos = CORE.east();
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get());
            storage.setStored(10_000);
            storage.setMode(Direction.EAST, ExtradimensionalStorageBlockEntity.PortMode.OUTPUT);
            helper.succeedWhen(() -> {
                EnergyHandler converter = energy(helper, converterPos, Direction.WEST);
                helper.assertTrue(converter != null && converter.getAmountAsLong() > 0, "the converter receives FE");
                helper.assertValueEqual(storage.getStored() + converter.getAmountAsLong(), 10_000L, "no FE is made or lost");
            });
        });

        r.add("extradimensional/network_stores_surplus_and_covers_demand", 200, helper -> {
            ServerPlayer owner = player(helper, "Extradimensional");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Extradimensional").network();
            BlockPos uplinkPos = helper.absolutePos(new BlockPos(1, 1, 1));
            helper.getLevel().setBlock(uplinkPos, NTBlocks.UPLINK_ARRAY.get().defaultBlockState(), Block.UPDATE_ALL);
            for (int dy = 2; dy < 24; dy++) {
                if (helper.getLevel().getBlockState(uplinkPos.above(dy)).is(Blocks.BARRIER)) {
                    helper.getLevel().setBlock(uplinkPos.above(dy), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            SatelliteArrayBlockEntity uplink = (SatelliteArrayBlockEntity) helper.getLevel().getBlockEntity(uplinkPos);
            uplink.setNetwork(network);
            uplink.launch();
            ExtradimensionalStorageBlockEntity storage = core(helper);
            storage.setNetwork(network);
            BlockPos inputPos = new BlockPos(7, 1, 1);
            helper.setBlock(inputPos, NTBlocks.RESONANCE_NODE.get().defaultBlockState().setValue(ResonanceNodeBlock.FACING, Direction.UP));
            ResonanceNodeBlockEntity input = helper.getBlockEntity(inputPos, ResonanceNodeBlockEntity.class);
            input.setNetwork(network);
            input.giveFe(50_000);
            ResonanceNodeBlockEntity[] output = new ResonanceNodeBlockEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertValueEqual(storage.getStored(), 50_000L, "the storage takes the FE no output node wants"))
                    .thenExecute(() -> {
                        BlockPos outputPos = new BlockPos(7, 1, 7);
                        helper.setBlock(outputPos, NTBlocks.RESONANCE_NODE.get().defaultBlockState().setValue(ResonanceNodeBlock.FACING, Direction.UP));
                        output[0] = helper.getBlockEntity(outputPos, ResonanceNodeBlockEntity.class);
                        output[0].setNetwork(network);
                        output[0].setOutput(true);
                    })
                    .thenWaitUntil(() -> helper.assertTrue(output[0].getEnergy().getAmountAsInt() > 0, "the output node is fed from the storage"))
                    .thenExecute(() -> {
                        long delivered = output[0].getEnergy().getAmountAsInt();
                        long drawn = 50_000L - storage.getStored();
                        helper.assertTrue(drawn >= delivered, "the storage paid at least what was delivered, " + drawn + " for " + delivered);
                        networks.delete(owner, network.id());
                    })
                    .thenSucceed();
        });
    }
}
