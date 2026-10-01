package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blocks.DecorativePrismarineCrystalPartBlock;
import com.breakinblocks.nautec.content.blocks.OpticsBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalPartBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Set;

public final class LaserFixTests {
    private static final BlockPos SOURCE_POS = new BlockPos(2, 1, 4);

    private LaserFixTests() {
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

    private static CreativePowerSourceBlockEntity source(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, CreativePowerSourceBlockEntity.class);
    }

    private static MixerBlockEntity mixer(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockEntity(pos, MixerBlockEntity.class);
    }

    private static void assertNear(GameTestHelper helper, float expected, float actual, String what) {
        if (Math.abs(expected - actual) > 1.0e-3f) {
            helper.fail(what + ": expected " + expected + " but was " + actual);
        }
    }

    private static ItemEntity floatingItem(GameTestHelper helper, ItemStack stack, double x, double y, double z) {
        Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
        ItemEntity entity = new ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, stack);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static int countItems(GameTestHelper helper, Item item) {
        int total = 0;
        for (ItemEntity entity : helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds())) {
            if (entity.isAlive() && entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }

    private static int batteryPower(GameTestHelper helper, ItemStack stack) {
        IPowerStorage storage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException("Battery did not expose item power capability");
        }
        return storage.getPowerStored();
    }

    private static Player player(GameTestHelper helper, GameType gameType, ItemStack held) {
        Player player = helper.makeMockPlayer(gameType);
        if (gameType == GameType.CREATIVE) {
            player.getAbilities().instabuild = true;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        return player;
    }

    private static void breakAs(GameTestHelper helper, Player player, BlockPos relative) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(relative);
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem().copy();
        BlockState adjusted = state.getBlock().playerWillDestroy(level, pos, state, player);
        boolean canHarvest = !player.preventsBlockDrops() && adjusted.canHarvestBlock(level, pos, player);
        boolean removed = adjusted.onDestroyedByPlayer(level, pos, player, tool, canHarvest, level.getFluidState(pos));
        if (removed) {
            adjusted.getBlock().destroy(level, pos, adjusted);
        }
        if (removed && canHarvest) {
            adjusted.getBlock().playerDestroy(level, player, pos, adjusted, blockEntity, tool);
        }
    }

    private static void placeDecorativeCrystal(GameTestHelper helper, BlockPos bottom) {
        helper.setBlock(bottom, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get().defaultBlockState());
        for (int i = 1; i < 6; i++) {
            helper.setBlock(bottom.above(i), NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL_PART.get().defaultBlockState()
                    .setValue(DecorativePrismarineCrystalPartBlock.INDEX, i));
        }
    }

    private static void assertDecorativeCrystalGone(GameTestHelper helper, BlockPos bottom) {
        for (int i = 0; i < 6; i++) {
            helper.assertBlockPresent(Blocks.AIR, bottom.above(i));
        }
    }

    public static void register(NTTestRegistrar r) {
        registerTransformation(r);
        registerLens(r);
        registerSplitting(r);
        registerCharger(r);
        registerCrystals(r);
    }

    private static void registerTransformation(NTTestRegistrar r) {
        r.add("laserfix/transformation_output_scales_with_result_count", 320, helper -> {
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(2.0f);
            floatingItem(helper, new ItemStack(Items.PRISMARINE_CRYSTALS, 3), 4.5, 1.3, 4.5);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(0, countItems(helper, Items.PRISMARINE_CRYSTALS), "prismarine crystals left in the beam");
                helper.assertValueEqual(6, countItems(helper, NTItems.PRISMARINE_CRYSTAL_SHARD.get()),
                        "three crystals through a recipe yielding two each should make six shards");
            });
        });

        r.add("laserfix/transformation_prefers_highest_purity_recipe", 300, helper -> {
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(2.0f);
            floatingItem(helper, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get()), 4.5, 1.3, 4.5);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(0, countItems(helper, NTItems.AQUARINE_STEEL_COMPOUND.get()), "compound left in the beam");
                helper.assertValueEqual(2, countItems(helper, NTItems.AQUARINE_STEEL_INGOT.get()),
                        "a purity 2 beam should run the dense recipe and make two ingots");
            });
        });

        r.add("laserfix/transformation_low_purity_uses_basic_recipe", 300, helper -> {
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(1.0f);
            floatingItem(helper, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get()), 4.5, 1.3, 4.5);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(0, countItems(helper, NTItems.AQUARINE_STEEL_COMPOUND.get()), "compound left in the beam");
                helper.assertValueEqual(1, countItems(helper, NTItems.AQUARINE_STEEL_INGOT.get()),
                        "a purity 1 beam should fall back to the basic recipe");
            });
        });
    }

    private static void registerLens(NTTestRegistrar r) {
        r.add("laserfix/lens_output_beam_transforms_at_boosted_purity", 320, helper -> {
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(4, 1, 4), NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(new BlockPos(7, 1, 4), NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(1.9f);
            floatingItem(helper, new ItemStack(Items.PRISMARINE_CRYSTALS), 6.0, 1.3, 4.5);

            helper.succeedWhen(() -> {
                helper.assertValueEqual(0, countItems(helper, Items.PRISMARINE_CRYSTALS), "crystal left in the lens beam");
                helper.assertValueEqual(2, countItems(helper, NTItems.PRISMARINE_CRYSTAL_SHARD.get()),
                        "the lens output beam should carry purity 2 and run the purity 2 recipe");
            });
        });

        r.add("laserfix/lens_purity_capped_at_two", 140, helper -> {
            BlockPos mixerPos = new BlockPos(8, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(4, 1, 4), NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(1.9f);

            helper.runAfterDelay(100, () -> {
                MixerBlockEntity mixer = mixer(helper, mixerPos);
                helper.assertValueEqual(100, mixer.getPower(), "power through two lenses");
                assertNear(helper, 2.0f, mixer.getPurity(), "purity after two lenses from 1.9");
                helper.succeed();
            });
        });

        r.add("laserfix/lens_passes_high_purity_unchanged", 140, helper -> {
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(new BlockPos(4, 1, 4), NTBlocks.FOCUSING_LENS.get().defaultBlockState()
                    .setValue(OpticsBlock.FACING, Direction.EAST));
            helper.setBlock(mixerPos, NTBlocks.MIXER.get().defaultBlockState());
            source(helper, SOURCE_POS).setPurity(2.5f);

            helper.runAfterDelay(100, () -> {
                assertNear(helper, 2.5f, mixer(helper, mixerPos).getPurity(), "a lens should not change purity already above 2");
                helper.succeed();
            });
        });
    }

    private static void registerSplitting(NTTestRegistrar r) {
        r.add("laserfix/creative_source_splits_power", 120, helper -> {
            BlockPos eastMixer = new BlockPos(5, 1, 4);
            BlockPos southMixer = new BlockPos(2, 1, 7);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST, Direction.SOUTH);
            helper.setBlock(eastMixer, NTBlocks.MIXER.get().defaultBlockState());
            helper.setBlock(southMixer, NTBlocks.MIXER.get().defaultBlockState());

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(50, mixer(helper, eastMixer).getPower(), "east branch power");
                helper.assertValueEqual(50, mixer(helper, southMixer).getPower(), "south branch power");
                helper.succeed();
            });
        });

        r.add("laserfix/junction_splits_power", 120, helper -> {
            BlockPos junctionPos = new BlockPos(4, 1, 4);
            BlockPos eastMixer = new BlockPos(6, 1, 4);
            BlockPos southMixer = new BlockPos(4, 1, 6);
            placeShieldedSource(helper, SOURCE_POS, Direction.EAST);
            helper.setBlock(junctionPos, NTBlocks.LASER_JUNCTION.get().defaultBlockState());
            helper.setBlock(eastMixer, NTBlocks.MIXER.get().defaultBlockState());
            helper.setBlock(southMixer, NTBlocks.MIXER.get().defaultBlockState());

            LaserJunctionBlockEntity junction = helper.getBlockEntity(junctionPos, LaserJunctionBlockEntity.class);
            junction.getLaserInputs().add(Direction.WEST);
            junction.getLaserOutputs().add(Direction.EAST);
            junction.getLaserOutputs().add(Direction.SOUTH);

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(100, junction.getPower(), "junction received power");
                helper.assertValueEqual(50, mixer(helper, eastMixer).getPower(), "east junction output");
                helper.assertValueEqual(50, mixer(helper, southMixer).getPower(), "south junction output");
                helper.succeed();
            });
        });

        r.add("laserfix/energy_converter_splits_power", 120, helper -> {
            BlockPos converterPos = new BlockPos(2, 1, 4);
            BlockPos eastMixer = new BlockPos(5, 1, 4);
            BlockPos southMixer = new BlockPos(2, 1, 7);
            helper.setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState());
            helper.setBlock(eastMixer, NTBlocks.MIXER.get().defaultBlockState());
            helper.setBlock(southMixer, NTBlocks.MIXER.get().defaultBlockState());

            EnergyHandler handler = helper.getLevel().getCapability(Capabilities.Energy.BLOCK, helper.absolutePos(converterPos), null);
            if (handler == null) {
                helper.fail("Energy Converter should expose the energy capability");
                return;
            }
            try (Transaction tx = Transaction.openRoot()) {
                handler.insert(100_000, tx);
                tx.commit();
            }

            helper.runAfterDelay(80, () -> {
                helper.assertValueEqual(50, mixer(helper, eastMixer).getPower(), "east converter output");
                helper.assertValueEqual(50, mixer(helper, southMixer).getPower(), "south converter output");
                helper.succeed();
            });
        });
    }

    private static void registerCharger(NTTestRegistrar r) {
        r.add("laserfix/charger_charges_at_beam_power", 100, helper -> {
            BlockPos chargerPos = new BlockPos(4, 1, 4);
            placeShieldedSource(helper, new BlockPos(4, 4, 4), Direction.DOWN);
            helper.setBlock(chargerPos, NTBlocks.CHARGER.get().defaultBlockState());

            ChargerBlockEntity charger = helper.getBlockEntity(chargerPos, ChargerBlockEntity.class);
            charger.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMATIC_BATTERY.get()));

            int[] baseline = new int[1];
            helper.runAfterDelay(40, () ->
                    baseline[0] = batteryPower(helper, charger.getItemStackHandler().getStackInSlot(0)));

            helper.runAfterDelay(50, () -> {
                helper.assertValueEqual(100, charger.getPower(), "charger beam power");
                int delta = batteryPower(helper, charger.getItemStackHandler().getStackInSlot(0)) - baseline[0];
                helper.assertTrue(delta >= 900 && delta <= 1100,
                        "Battery should charge at the 100 per tick beam power over 10 ticks, delta was " + delta);
                helper.succeed();
            });
        });

        r.add("laserfix/charger_accepts_side_beam", 60, helper -> {
            BlockPos chargerPos = new BlockPos(5, 1, 4);
            placeShieldedSource(helper, new BlockPos(2, 1, 4), Direction.EAST);
            helper.setBlock(chargerPos, NTBlocks.CHARGER.get().defaultBlockState());
            ChargerBlockEntity charger = helper.getBlockEntity(chargerPos, ChargerBlockEntity.class);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(100, charger.getPower(), "charger power from a beam into its side");
                helper.succeed();
            });
        });
    }

    private static void registerCrystals(NTTestRegistrar r) {
        r.add("laserfix/decorative_crystal_needs_clear_space", 40, helper -> {
            BlockPos bottom = new BlockPos(4, 1, 4);
            BlockPos blocker = new BlockPos(4, 4, 4);
            helper.setBlock(blocker, Blocks.STONE.defaultBlockState());
            ItemStack stack = new ItemStack(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.asItem());
            Player player = player(helper, GameType.SURVIVAL, stack);

            helper.placeAt(player, stack, bottom.below(), Direction.UP);
            helper.assertBlockPresent(Blocks.AIR, bottom);
            helper.assertBlockPresent(Blocks.STONE, blocker);
            helper.assertValueEqual(1, player.getMainHandItem().getCount(), "item kept when placement is blocked");

            helper.setBlock(blocker, Blocks.AIR.defaultBlockState());
            helper.placeAt(player, player.getMainHandItem(), bottom.below(), Direction.UP);
            helper.assertBlockPresent(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.get(), bottom);
            for (int i = 1; i < 6; i++) {
                helper.assertBlockPresent(NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL_PART.get(), bottom.above(i));
            }
            helper.assertTrue(player.getMainHandItem().isEmpty(), "item consumed once placement has room");
            helper.succeed();
        });

        r.add("laserfix/decorative_crystal_part_break_drops_once", 40, helper -> {
            BlockPos bottom = new BlockPos(4, 1, 4);
            placeDecorativeCrystal(helper, bottom);
            Player player = player(helper, GameType.SURVIVAL, ItemStack.EMPTY);

            breakAs(helper, player, bottom.above(3));

            assertDecorativeCrystalGone(helper, bottom);
            helper.assertValueEqual(1, countItems(helper, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.asItem()),
                    "breaking an upper part should drop the crystal once");
            helper.succeed();
        });

        r.add("laserfix/decorative_crystal_bottom_break_drops_once", 40, helper -> {
            BlockPos bottom = new BlockPos(4, 1, 4);
            placeDecorativeCrystal(helper, bottom);
            Player player = player(helper, GameType.SURVIVAL, ItemStack.EMPTY);

            breakAs(helper, player, bottom);

            assertDecorativeCrystalGone(helper, bottom);
            helper.assertValueEqual(1, countItems(helper, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.asItem()),
                    "breaking the bottom should drop the crystal once");
            helper.succeed();
        });

        r.add("laserfix/decorative_crystal_creative_break_drops_nothing", 40, helper -> {
            BlockPos bottom = new BlockPos(4, 1, 4);
            placeDecorativeCrystal(helper, bottom);
            Player player = player(helper, GameType.CREATIVE, ItemStack.EMPTY);

            breakAs(helper, player, bottom.above(5));

            assertDecorativeCrystalGone(helper, bottom);
            helper.assertValueEqual(0, countItems(helper, NTBlocks.DECORATIVE_PRISMARINE_CRYSTAL.asItem()),
                    "creative breaking should not drop the crystal");
            helper.succeed();
        });

        r.add("laserfix/prismarine_crystal_space_check_matches_placement", 40, helper -> {
            BlockPos floorClick = new BlockPos(4, 0, 4);
            BlockPos core = new BlockPos(4, 4, 4);
            BlockPos belowCore = core.below();
            BlockPos aboveOldRange = core.above(4);
            ItemStack stack = new ItemStack(NTBlocks.PRISMARINE_CRYSTAL.asItem(), 2);
            Player player = player(helper, GameType.SURVIVAL, stack);

            helper.setBlock(belowCore, Blocks.STONE.defaultBlockState());
            helper.placeAt(player, stack, floorClick, Direction.UP);
            helper.assertBlockPresent(Blocks.STONE, belowCore);
            helper.assertBlockNotPresent(NTBlocks.PRISMARINE_CRYSTAL.get(), core);
            helper.assertValueEqual(2, player.getMainHandItem().getCount(), "item kept when a filled position is blocked");

            helper.setBlock(belowCore, Blocks.AIR.defaultBlockState());
            helper.setBlock(aboveOldRange, Blocks.STONE.defaultBlockState());
            helper.placeAt(player, player.getMainHandItem(), floorClick, Direction.UP);
            helper.assertBlockPresent(NTBlocks.PRISMARINE_CRYSTAL.get(), core);
            BlockPos top = core.above(2);
            for (int i = 0; i < 6; i++) {
                if (i == 2) {
                    continue;
                }
                BlockPos pos = top.below(i);
                helper.assertBlockPresent(NTBlocks.PRISMARINE_CRYSTAL_PART.get(), pos);
                helper.assertValueEqual(i, helper.getBlockState(pos).getValue(PrismarineCrystalPartBlock.INDEX), "index at " + pos);
            }
            helper.assertBlockPresent(Blocks.STONE, aboveOldRange);
            helper.assertValueEqual(1, player.getMainHandItem().getCount(), "item consumed once the crystal is placed");
            helper.succeed();
        });
    }
}
