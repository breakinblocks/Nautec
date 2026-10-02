package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class DrainTests {
    private static final BlockPos DRAIN_C = new BlockPos(4, 1, 4);
    private static final BlockPos PORT_PART = DRAIN_C.offset(0, 0, 1);
    private static final BlockPos SOURCE = DRAIN_C.offset(0, 0, 3);
    private static final int OPEN_TICKS = 160;

    private DrainTests() {
    }

    private static void buildFormedDrain(GameTestHelper helper, boolean water) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block block = (dx == 0 && dz == 0) ? NTBlocks.DRAIN.get() : NTBlocks.DRAIN_WALL.get();
                helper.setBlock(DRAIN_C.offset(dx, 0, dz), block);
            }
        }
        helper.assertTrue(MultiblockHelper.form(NTMultiblocks.DRAIN.get(), helper.absolutePos(DRAIN_C), helper.getLevel()), "drain should form");
        if (water) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.setBlock(DRAIN_C.offset(dx, 1, dz), Blocks.WATER);
                }
            }
        }
    }

    private static void beamIntoPort(GameTestHelper helper) {
        DrainPartBlock.setLaserPort(helper.absolutePos(PORT_PART), helper.getLevel(), Direction.SOUTH);
        BacteriaMachineTests.placeShieldedSource(helper, SOURCE, Direction.NORTH);
    }

    private static void feed(GameTestHelper helper, int power) {
        helper.onEachTick(() -> {
            DrainBlockEntity drain = helper.getBlockEntity(DRAIN_C, DrainBlockEntity.class);
            drain.receivePower(power, Direction.NORTH, helper.absolutePos(DRAIN_C));
        });
    }

    private static void sneakUse(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        BlockPos abs = helper.absolutePos(PORT_PART);
        BlockState state = helper.getBlockState(PORT_PART);
        state.useWithoutItem(helper.getLevel(), player, new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
    }

    private static DrainBlockEntity drain(GameTestHelper helper) {
        return helper.getBlockEntity(DRAIN_C, DrainBlockEntity.class);
    }

    public static void register(NTTestRegistrar r) {
        r.add("drain/opens_and_pumps_through_its_laser_port", 400, helper -> {
            helper.setBiome(Biomes.OCEAN);
            buildFormedDrain(helper, true);
            beamIntoPort(helper);
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(drain(helper).hasOperatingPower(), "the beam through the port should power the drain, got " + drain(helper).getPower());
                helper.assertTrue(helper.getBlockState(DRAIN_C).getValue(DrainPartBlock.HAS_POWER), "powered state reaches the blockstate");
                sneakUse(helper);
                helper.assertTrue(helper.getBlockState(DRAIN_C).getValue(DrainPartBlock.OPEN), "a sneaking player opens a powered drain");
            });
            helper.runAfterDelay(20 + OPEN_TICKS, () -> helper.succeedWhen(() -> {
                DrainBlockEntity drain = drain(helper);
                helper.assertValueEqual(DrainBlockEntity.Status.PUMPING, drain.getStatus(), "status once open");
                helper.assertTrue(drain.getFluidTank().getFluidAmount() > 0, "salt water pumped");
                helper.assertTrue(drain.getFluidTank().getFluid().is(NTFluids.SALT_WATER.getStillFluid()), "the fluid is salt water");
            }));
        });

        r.add("drain/refuses_to_open_at_the_threshold", 60, helper -> {
            helper.setBiome(Biomes.OCEAN);
            buildFormedDrain(helper, true);
            feed(helper, NTConfig.drainPower);
            helper.runAfterDelay(10, () -> {
                DrainBlockEntity drain = drain(helper);
                helper.assertValueEqual(DrainBlockEntity.Status.NO_POWER, drain.getStatus(), "status at exactly the threshold");
                helper.assertTrue(!helper.getBlockState(DRAIN_C).getValue(DrainPartBlock.HAS_POWER), "no powered state at the threshold");
                Component refusal = drain.open();
                helper.assertTrue(refusal != null, "opening without enough power explains why");
                helper.assertTrue(!helper.getBlockState(DRAIN_C).getValue(DrainPartBlock.OPEN), "the drain stays shut");
                helper.succeed();
            });
        });

        r.add("drain/one_threshold_for_bubbles_and_pumping", 60, helper -> {
            helper.setBiome(Biomes.OCEAN);
            buildFormedDrain(helper, true);
            feed(helper, NTConfig.drainPower - 2);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(!helper.getBlockState(DRAIN_C).getValue(DrainPartBlock.HAS_POWER), "below the threshold the drain has no powered state");
                helper.assertTrue(!helper.getBlockState(PORT_PART).getValue(DrainPartBlock.HAS_POWER), "and neither do its walls");
                helper.succeed();
            });
        });

        r.add("drain/explains_a_non_ocean_biome", 400, helper -> {
            helper.setBiome(Biomes.PLAINS);
            buildFormedDrain(helper, true);
            feed(helper, 100);
            helper.runAfterDelay(5, () -> {
                Component warning = drain(helper).open();
                helper.assertTrue(warning != null, "opening outside an ocean warns that it will not pump");
            });
            helper.runAfterDelay(5 + OPEN_TICKS, () -> helper.succeedWhen(() -> {
                DrainBlockEntity drain = drain(helper);
                helper.assertValueEqual(DrainBlockEntity.Status.NOT_OCEAN, drain.getStatus(), "status outside an ocean");
                helper.assertValueEqual(0, drain.getFluidTank().getFluidAmount(), "nothing pumped outside an ocean");
            }));
        });

        r.add("drain/explains_missing_water", 400, helper -> {
            helper.setBiome(Biomes.OCEAN);
            buildFormedDrain(helper, false);
            feed(helper, 100);
            helper.runAfterDelay(5, () -> helper.assertTrue(drain(helper).open() != null, "opening without water above warns"));
            helper.runAfterDelay(5 + OPEN_TICKS, () -> helper.succeedWhen(() ->
                    helper.assertValueEqual(DrainBlockEntity.Status.NO_WATER, drain(helper).getStatus(), "status without water")));
        });

        r.add("drain/animation_state_reaches_clients", 60, helper -> {
            helper.setBiome(Biomes.OCEAN);
            buildFormedDrain(helper, true);
            feed(helper, 100);
            helper.runAfterDelay(5, () -> {
                DrainBlockEntity drain = drain(helper);
                helper.assertTrue(drain.open() == null, "an ocean drain with water and power opens with no warning");
                CompoundTag update = drain.getUpdateTag(helper.getLevel().registryAccess());
                helper.assertTrue(update.getCompoundOrEmpty("valve").getIntOr("ticks", 0) > 0, "the valve animation is in the client update");
                helper.assertTrue(drain.isMoving(), "the drain is moving after opening");
                helper.succeed();
            });
        });
    }
}
