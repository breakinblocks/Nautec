package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.DrainPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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
        r.add("drain/wrench_on_any_face_sets_an_outward_port_and_reaims", 200, helper -> {
            buildFormedDrain(helper, false);
            BlockPos abs = helper.absolutePos(PORT_PART);
            DrainPartBlock.setLaserPort(abs, helper.getLevel(), Direction.UP);
            DrainPartBlockEntity part = helper.getBlockEntity(PORT_PART, DrainPartBlockEntity.class);
            helper.assertTrue(part.getLaserInputs().contains(Direction.UP), "a port aimed at the top, as an old world might have");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack wrench = new ItemStack(NTItems.AQUARINE_WRENCH.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            BlockHitResult top = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
            InteractionResult result = helper.getBlockState(PORT_PART).useItemOn(wrench, helper.getLevel(), player, InteractionHand.MAIN_HAND, top);
            helper.assertTrue(result.consumesAction(), "wrenching the top of a port wall is taken");
            helper.assertTrue(part.getLaserInputs().contains(Direction.SOUTH), "the port turns to face outward, away from the centre");
            helper.assertTrue(part.getLaserOutputs().contains(Direction.NORTH), "and passes its beam on to the centre");
            DrainPartBlock.setLaserPort(abs, helper.getLevel(), Direction.UP);
            player.setShiftKeyDown(true);
            InteractionResult sneaking = wrench.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, top));
            helper.assertTrue(sneaking.consumesAction(), "a sneaking wrench click also sets the port");
            helper.assertTrue(part.getLaserInputs().contains(Direction.SOUTH), "the sneaking click aims it outward too");
            BacteriaMachineTests.placeShieldedSource(helper, SOURCE, Direction.NORTH);
            helper.succeedWhen(() -> helper.assertTrue(drain(helper).getPower() > 0, "a beam into the outward port reaches the drain"));
        });

        r.add("drain/pump_rate_scales_with_the_square_root_of_the_beam", 20, helper -> {
            int power = NTConfig.drainPower;
            int base = NTConfig.drainSaltWaterAmount;
            NTConfig.drainPower = 20;
            NTConfig.drainSaltWaterAmount = 500;
            try {
                helper.assertValueEqual(DrainBlockEntity.saltWaterPerSecond(20), 0, "no pumping at the threshold");
                helper.assertValueEqual(DrainBlockEntity.saltWaterPerSecond(21), 512, "just over the threshold");
                helper.assertValueEqual(DrainBlockEntity.saltWaterPerSecond(80), 1000, "four times the beam pumps twice as much");
                helper.assertValueEqual(DrainBlockEntity.saltWaterPerSecond(320), 2000, "sixteen times the beam pumps four times as much");
                helper.assertValueEqual(DrainBlockEntity.saltWaterPerSecond(2_000_000), 158_114, "no upper limit on the beam");
                helper.assertTrue(DrainBlockEntity.saltWaterPerSecond(Integer.MAX_VALUE) > 0, "the largest beam does not overflow");
            } finally {
                NTConfig.drainPower = power;
                NTConfig.drainSaltWaterAmount = base;
            }
            helper.succeed();
        });

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

        r.add("drain/ocean_like_biome_tags_count", 20, helper -> {
            Registry<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
            for (ResourceKey<Biome> ocean : List.of(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.WARM_OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN)) {
                helper.assertTrue(DrainBlockEntity.isOcean(biomes.getOrThrow(ocean)), ocean.identifier() + " should count as an ocean");
            }
            for (ResourceKey<Biome> land : List.of(Biomes.PLAINS, Biomes.RIVER, Biomes.DESERT, Biomes.DEEP_DARK)) {
                helper.assertTrue(!DrainBlockEntity.isOcean(biomes.getOrThrow(land)), land.identifier() + " should not count as an ocean, tags " + biomes.getOrThrow(land).tags().map(tag -> tag.location().toString()).toList());
            }
            helper.succeed();
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
