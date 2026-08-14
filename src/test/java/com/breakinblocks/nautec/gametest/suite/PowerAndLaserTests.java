package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.content.blocks.LongDistanceLaserBlock;
import com.breakinblocks.nautec.content.blocks.OpticsBlock;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

public final class PowerAndLaserTests {
    private static final BlockPos SOURCE_POS = new BlockPos(2, 1, 4);

    private PowerAndLaserTests() {
    }

    private static void placeShieldedSource(GameTestHelper helper, BlockPos pos, Direction... openDirections) {
        helper.setBlock(pos, NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
        Set<Direction> open = Set.of(openDirections);
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || open.contains(direction)) {
                continue;
            }
            helper.setBlock(pos.relative(direction, 2), Blocks.STONE.defaultBlockState());
        }
    }

    private static void assertPurityNear(GameTestHelper helper, float expected, float actual, String what) {
        if (Math.abs(expected - actual) > 1.0e-3f) {
            helper.fail(what + ": expected " + expected + " but was " + actual);
        }
    }

    private static MixerBlockEntity mixer(GameTestHelper helper, BlockPos pos) {
        MixerBlockEntity mixer = helper.getBlockEntity(pos, MixerBlockEntity.class);
        if (mixer == null) {
            throw helper.assertionException("Expected MixerBlockEntity at " + pos);
        }
        return mixer;
    }

    private static IPowerStorage batteryStorage(GameTestHelper helper, ItemStack stack) {
        IPowerStorage storage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException("Prismatic battery did not expose item power capability");
        }
        return storage;
    }

    public static void register(NTTestRegistrar r) {
        r.add("power/item_fill_respects_max_input_and_capacity", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(0, storage.getPowerStored(), "initial stored power");
            helper.assertValueEqual(10000, storage.getPowerCapacity(), "battery capacity");
            helper.assertValueEqual(128, storage.getMaxInput(), "battery max input");

            helper.assertValueEqual(128, storage.tryFillPower(500, false), "fill clamped to max input");
            helper.assertValueEqual(128, storage.getPowerStored(), "stored after clamped fill");

            helper.assertValueEqual(0, storage.tryFillPower(0, false), "fill of zero");
            helper.assertValueEqual(0, storage.tryFillPower(-5, false), "fill of negative");

            storage.setPowerStored(9990);
            helper.assertValueEqual(10, storage.tryFillPower(500, false), "fill clamped to remaining capacity");
            helper.assertValueEqual(10000, storage.getPowerStored(), "stored at capacity");
            helper.assertValueEqual(0, storage.tryFillPower(1, false), "fill when full");

            helper.succeed();
        });

        r.add("power/item_drain_respects_max_output", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);
            helper.assertValueEqual(100, storage.getMaxOutput(), "battery max output");

            storage.setPowerStored(500);
            helper.assertValueEqual(40, storage.tryDrainPower(40, false), "partial drain");
            helper.assertValueEqual(460, storage.getPowerStored(), "stored after partial drain");

            helper.assertValueEqual(100, storage.tryDrainPower(500, false), "drain clamped to max output");
            helper.assertValueEqual(360, storage.getPowerStored(), "stored after clamped drain");

            storage.setPowerStored(30);
            helper.assertValueEqual(30, storage.tryDrainPower(500, false), "drain clamped to stored power");
            helper.assertValueEqual(0, storage.getPowerStored(), "stored after emptying");
            helper.assertValueEqual(0, storage.tryDrainPower(5, false), "drain when empty");

            helper.succeed();
        });

        r.add("power/simulate_does_not_mutate", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(128, storage.tryFillPower(500, true), "simulated fill result");
            helper.assertValueEqual(0, storage.getPowerStored(), "stored unchanged after simulated fill");

            storage.setPowerStored(250);
            helper.assertValueEqual(100, storage.tryDrainPower(500, true), "simulated drain result");
            helper.assertValueEqual(250, storage.getPowerStored(), "stored unchanged after simulated drain");

            helper.succeed();
        });

        r.add("power/purity_get_set_persists", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            IPowerStorage storage = batteryStorage(helper, stack);

            helper.assertValueEqual(0.0f, storage.getPurity(), "initial purity");
            storage.setPurity(0.5f);
            helper.assertValueEqual(0.5f, storage.getPurity(), "purity after set");

            storage.setPowerStored(64);
            helper.assertValueEqual(0.5f, storage.getPurity(), "purity preserved after power change");
            helper.assertValueEqual(64, storage.getPowerStored(), "stored preserved after purity set");

            IPowerStorage freshWrapper = batteryStorage(helper, stack);
            helper.assertValueEqual(0.5f, freshWrapper.getPurity(), "purity persisted in data component");

            helper.succeed();
        });

        r.add("power/item_capability_exposure", 20, helper -> {
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            helper.assertTrue(battery.getCapability(NTCapabilities.PowerStorage.ITEM) != null,
                    "Prismatic battery should expose the item power capability");

            ItemStack stick = new ItemStack(Items.STICK);
            helper.assertTrue(stick.getCapability(NTCapabilities.PowerStorage.ITEM) == null,
                    "Plain stick should not expose the item power capability");

            helper.succeed();
        });

        r.add("laser/source_transmits_power_to_mixer", 100, helper -> {
            BlockPos mixerPos = new BlockPos(5, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(3, source.getLaserDistances().getInt(Direction.EAST), "laser distance to mixer");
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer received power");
                helper.succeed();
            });
        });

        r.add("laser/beam_blocked_by_obstruction", 140, helper -> {
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            BlockPos obstructionPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(50, () -> {
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer powered before obstruction");
                helper.setBlock(obstructionPos, Blocks.STONE.defaultBlockState());
            });

            helper.runAfterDelay(110, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(0, source.getLaserDistances().getInt(Direction.EAST), "laser distance after obstruction");
                helper.assertValueEqual(0, mixer(helper, mixerPos).getPower(), "mixer power after obstruction");
                helper.succeed();
            });
        });

        r.add("laser/relay_passes_power_through", 100, helper -> {
            BlockPos relayPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through relay");
                helper.succeed();
            });
        });

        r.add("laser/relay_facing_gates_input", 100, helper -> {
            BlockPos relayPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.NORTH));
            helper.setBlock(new BlockPos(4, 1, 2), Blocks.STONE.defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
                if (source == null) {
                    helper.fail("Expected CreativePowerSourceBlockEntity");
                    return;
                }
                helper.assertValueEqual(0, source.getLaserDistances().getInt(Direction.EAST), "no connection into wrongly-faced relay");
                helper.assertValueEqual(0, mixer(helper, mixerPos).getPower(), "mixer unpowered behind wrongly-faced relay");
                helper.succeed();
            });
        });

        r.add("laser/mirror_turns_beam_and_dims_purity", 120, helper -> {
            BlockPos mirrorPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(4, 1, 6);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(mirrorPos, NTBlocks.PRISMATIC_MIRROR.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.SOUTH));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(2.0f);

            helper.runAfterDelay(80, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(100, mixer.getPower(), "power around the corner through a mirror");
                assertPurityNear(helper, 2.0f * (float) NTConfig.mirrorPurityFactor, mixer.getPurity(),
                        "purity after one mirror");
                helper.succeed();
            });
        });

        r.add("laser/splitter_divides_power_between_branches", 120, helper -> {
            BlockPos splitterPos = new BlockPos(4, 1, 4);
            BlockPos northMixer = new BlockPos(4, 1, 2);
            BlockPos southMixer = new BlockPos(4, 1, 6);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(splitterPos, NTBlocks.BEAM_SPLITTER.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(northMixer, NTBlocks.MIXER.get().defaultBlockState());
            helper.setBlock(southMixer, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(2.0f);

            helper.runAfterDelay(80, () -> {
                MixerBlockEntity north = mixer(helper, northMixer);
                MixerBlockEntity south = mixer(helper, southMixer);
                helper.assertValueEqual(50, north.getPower(), "north branch power");
                helper.assertValueEqual(50, south.getPower(), "south branch power");
                assertPurityNear(helper, 2.0f * (float) NTConfig.splitterPurityFactor, north.getPurity(),
                        "purity on a split branch");
                helper.succeed();
            });
        });

        r.add("laser/lens_raises_purity", 120, helper -> {
            BlockPos lensPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(lensPos, NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(1.0f);

            helper.runAfterDelay(80, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(100, mixer.getPower(), "power straight through a lens");
                assertPurityNear(helper, 1.0f + (float) NTConfig.lensPurityBonus, mixer.getPurity(),
                        "purity after a focusing lens");
                helper.succeed();
            });
        });

        r.add("laser/purity_clears_when_source_is_removed", 160, helper -> {
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(4, 1, 4), NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                    .setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            if (source == null) {
                helper.fail("Expected CreativePowerSourceBlockEntity");
                return;
            }
            source.setPurity(2.0f);

            helper.runAfterDelay(60, () -> {
                assertPurityNear(helper, 2.0f, mixer(helper, mixerPos).getPurity(), "purity while the source runs");
                helper.setBlock(SOURCE_POS, Blocks.AIR.defaultBlockState());
            });

            helper.runAfterDelay(140, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(0, mixer.getPower(), "power after the source is gone");
                assertPurityNear(helper, 0f, mixer.getPurity(),
                        "purity should fall to zero once the source is removed, not stick at the old value");
                helper.succeed();
            });
        });

        r.add("resonance/ceiling_scales_with_purity", 20, helper -> {
            float base = (float) NTConfig.resonanceBaseCeiling;
            assertPurityNear(helper, base, ResonanceChamberBlockEntity.stabilityCeiling(0f), "ceiling at zero purity");
            assertPurityNear(helper, base * 3f, ResonanceChamberBlockEntity.stabilityCeiling(2f), "ceiling at purity 2");
            assertPurityNear(helper, base, ResonanceChamberBlockEntity.stabilityCeiling(-1f), "negative purity clamps to base");
            helper.succeed();
        });

        r.add("resonance/crafts_in_the_critical_band", 400, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(3.0f);
            chamber.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));

            helper.succeedWhen(() -> {
                ItemStack result = chamber.getItemStackHandler().getStackInSlot(1);
                helper.assertTrue(result.is(NTItems.RESONANT_SHARD.get()),
                        "Chamber should have produced a Resonant Shard, slot held " + result);
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(0).isEmpty(),
                        "The crystal shard should have been consumed");
                helper.assertFalse(chamber.isVenting(), "A successful craft should not vent");
            });
        });

        r.add("resonance/low_purity_will_not_craft_tier_three", 300, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(1.0f);
            chamber.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get()));

            helper.runAfterDelay(280, () -> {
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(1).isEmpty(),
                        "A purity 1 beam should never make a Resonant Shard");
                helper.assertTrue(chamber.getItemStackHandler().getStackInSlot(0).is(NTItems.PRISMARINE_CRYSTAL_SHARD.get()),
                        "The crystal shard should still be sitting there uncrafted");
                helper.succeed();
            });
        });

        r.add("resonance/vent_resets_charge_and_locks_out", 300, helper -> {
            BlockPos chamberPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(chamberPos, NTBlocks.RESONANCE_CHAMBER.get().defaultBlockState());

            CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE_POS, CreativePowerSourceBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(chamberPos, ResonanceChamberBlockEntity.class);
            if (source == null || chamber == null) {
                helper.fail("Expected a power source and a resonance chamber");
                return;
            }
            source.setPurity(0f);

            helper.runAfterDelay(260, () -> {
                helper.assertTrue(chamber.isVenting(), "An empty chamber left charging should vent");
                helper.assertValueEqual(0.0f, chamber.getCharge(), "charge after venting");
                helper.succeed();
            });
        });

        r.add("laser/junction_routes_power", 100, helper -> {
            BlockPos junctionPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(junctionPos, NTBlocks.LASER_JUNCTION.get().defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            LaserJunctionBlockEntity junction = helper.getBlockEntity(junctionPos, LaserJunctionBlockEntity.class);
            if (junction == null) {
                helper.fail("Expected LaserJunctionBlockEntity");
                return;
            }
            junction.getLaserInputs().add(Direction.WEST);
            junction.getLaserOutputs().add(Direction.EAST);

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, junction.getPower(), "junction received power");
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through junction");
                helper.succeed();
            });
        });

        r.add("laser/long_distance_laser_passes_power", 100, helper -> {
            BlockPos ldlPos = new BlockPos(4, 1, 4);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(ldlPos, NTBlocks.LONG_DISTANCE_LASER.get().defaultBlockState()
                    .setValue(LongDistanceLaserBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(100, mixer(helper, mixerPos).getPower(), "mixer power through long distance laser");
                helper.succeed();
            });
        });

        registerEnergyBridge(r);
    }

    private static final int FE_BUFFER = 100_000;
    private static final int FE_PER_TICK = 100;

    private static void registerEnergyBridge(NTTestRegistrar r) {
        r.add("power/creative_energy_source_exposes_energy_capability", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.setBlock(pos, NTBlocks.CREATIVE_ENERGY_SOURCE.get().defaultBlockState());

            EnergyHandler handler = energyAt(helper, pos);
            if (handler == null) {
                helper.fail("Creative Energy Source should expose the energy capability");
                return;
            }

            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(500, handler.extract(500, tx), "creative source extraction");
                helper.assertValueEqual(0, handler.insert(500, tx), "creative source rejects insertion");
                tx.commit();
            }
            helper.succeed();
        });

        r.add("power/energy_converter_accepts_fe_and_converts_it", 120, helper -> {
            BlockPos converterPos = new BlockPos(2, 1, 4);
            BlockPos mixerPos = new BlockPos(5, 1, 4);
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState());
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());

            EnergyHandler handler = energyAt(helper, converterPos);
            if (handler == null) {
                helper.fail("Energy Converter should expose the energy capability");
                return;
            }

            int accepted;
            try (Transaction tx = Transaction.openRoot()) {
                accepted = handler.insert(FE_BUFFER, tx);
                helper.assertValueEqual(0, handler.extract(100, tx), "converter refuses to give FE back");
                tx.commit();
            }
            helper.assertValueEqual(FE_BUFFER, accepted, "FE accepted by converter");
            helper.assertValueEqual(FE_BUFFER, handler.getAmountAsInt(), "FE buffered by converter");

            helper.runAfterDelay(60, () -> {
                helper.assertTrue(handler.getAmountAsInt() < FE_BUFFER, "converter should drain its FE buffer");
                helper.assertValueEqual(FE_PER_TICK, mixer(helper, mixerPos).getPower(), "converted power reaching the mixer");
                helper.succeed();
            });
        });
    }

    private static EnergyHandler energyAt(GameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return helper.getLevel().getCapability(Capabilities.Energy.BLOCK, absolute, null);
    }
}
