package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionPortBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionStructure;
import com.breakinblocks.nautec.content.blockentities.fusion.LaserInjectorBlockEntity;
import com.breakinblocks.nautec.content.blocks.fusion.FusionControllerBlock;
import com.breakinblocks.nautec.content.blocks.fusion.LaserInjectorBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class FusionPlantTests {
    private static final BlockPos CORE = new BlockPos(4, 5, 4);
    private static final int RADIUS = 2;
    private static final BlockPos WIDE_CORE = new BlockPos(9, 5, 9);
    private static final Identifier WIDE_ARENA = Nautec.rl("empty_19x11x19");
    private static int lowIgnitionHolders;
    private static long savedIgnition;

    private FusionPlantTests() {
    }

    private static BlockPos controllerPos(GameTestHelper helper) {
        return controllerPos(helper, RADIUS);
    }

    private static BlockPos controllerPos(GameTestHelper helper, int radius) {
        return controllerPos(helper, CORE, radius);
    }

    private static BlockPos controllerPos(GameTestHelper helper, BlockPos corePos, int radius) {
        return helper.absolutePos(corePos).offset(0, 1, -radius);
    }

    private static BlockPos injectorPos(GameTestHelper helper) {
        return injectorPos(helper, RADIUS);
    }

    private static BlockPos injectorPos(GameTestHelper helper, int radius) {
        return injectorPos(helper, CORE, radius);
    }

    private static BlockPos injectorPos(GameTestHelper helper, BlockPos corePos, int radius) {
        return helper.absolutePos(corePos).offset(-radius, 0, 0);
    }

    private static void lowerIgnition() {
        if (lowIgnitionHolders++ == 0) {
            savedIgnition = NTConfig.fusionIgnitionEnergy;
            NTConfig.fusionIgnitionEnergy = 4_000;
        }
    }

    private static void restoreIgnition() {
        if (--lowIgnitionHolders == 0) {
            NTConfig.fusionIgnitionEnergy = savedIgnition;
        }
    }

    private static FusionControllerBlockEntity build(GameTestHelper helper, boolean cultivated) {
        return build(helper, cultivated, RADIUS);
    }

    private static FusionControllerBlockEntity build(GameTestHelper helper, boolean cultivated, int radius) {
        return build(helper, cultivated, CORE, radius);
    }

    private static FusionControllerBlockEntity build(GameTestHelper helper, boolean cultivated, BlockPos corePos, int radius) {
        ServerLevel level = helper.getLevel();
        BlockPos core = helper.absolutePos(corePos);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -FusionStructure.FLOOR_DROP; y <= FusionStructure.CEILING_RISE; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int faces = (Math.abs(x) == radius ? 1 : 0)
                            + (y == -FusionStructure.FLOOR_DROP || y == FusionStructure.CEILING_RISE ? 1 : 0)
                            + (Math.abs(z) == radius ? 1 : 0);
                    BlockState state = faces >= 2 ? NTBlocks.FUSION_CASING.get().defaultBlockState()
                            : faces == 1 ? NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState();
                    level.setBlock(core.offset(x, y, z), state, Block.UPDATE_ALL);
                }
            }
        }
        PrismarineCrystalBlock.build(level, core, cultivated);
        level.setBlock(core.above(FusionStructure.CEILING_RISE), NTBlocks.FUSION_COLLECTOR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(core.below(FusionStructure.FLOOR_DROP), NTBlocks.FUSION_COLLECTOR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(injectorPos(helper, corePos, radius), NTBlocks.LASER_INJECTOR.get().defaultBlockState()
                .setValue(LaserInjectorBlock.FACING, Direction.EAST), Block.UPDATE_ALL);
        level.setBlock(controllerPos(helper, corePos, radius), NTBlocks.FUSION_CONTROLLER.get().defaultBlockState()
                .setValue(FusionControllerBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        FusionControllerBlockEntity controller = (FusionControllerBlockEntity) level.getBlockEntity(controllerPos(helper, corePos, radius));
        controller.rescan(level);
        return controller;
    }

    private static void feedInjector(GameTestHelper helper, int power, float purity) {
        feedInjector(helper, power, purity, RADIUS);
    }

    private static void feedInjector(GameTestHelper helper, int power, float purity, int radius) {
        feedInjector(helper, power, purity, CORE, radius);
    }

    private static void feedInjector(GameTestHelper helper, int power, float purity, BlockPos corePos, int radius) {
        helper.onEachTick(() -> {
            if (helper.getLevel().getBlockEntity(injectorPos(helper, corePos, radius)) instanceof LaserInjectorBlockEntity injector) {
                BlockPos origin = injectorPos(helper, corePos, radius).west(3);
                injector.receivePower(power, Direction.EAST, origin);
                injector.receiveNewPurity(purity, Direction.EAST, origin);
            }
        });
    }

    public static void register(NTTestRegistrar r) {
        r.add("fusion/formed_chamber_sets_its_ceiling", 20, helper -> {
            FusionControllerBlockEntity controller = build(helper, true);
            FusionStructure structure = controller.getStructure();
            helper.assertTrue(structure.formed(), "a complete chamber forms, got " + structure.problem() + " at " + structure.problemPos());
            helper.assertValueEqual(structure.radius(), RADIUS, "chamber radius");
            helper.assertValueEqual(structure.injectors().size(), 1, "injector count");
            helper.assertValueEqual(structure.ceiling(), FusionStructure.chamberContainment(RADIUS), "ceiling with no coils");

            BlockPos wall = helper.absolutePos(CORE).offset(RADIUS, 1, 0);
            helper.getLevel().setBlock(wall, NTBlocks.CONTAINMENT_COIL.get().defaultBlockState(), Block.UPDATE_ALL);
            controller.rescan(helper.getLevel());
            helper.assertValueEqual(controller.getStructure().coils(), 1, "coil count");
            helper.assertValueEqual(controller.getStructure().ceiling(),
                    FusionStructure.chamberContainment(RADIUS) + NTConfig.fusionCoilContainment, "a coil raises the ceiling");
            helper.succeed();
        });

        r.add("fusion/wild_crystal_does_not_form", 20, helper -> {
            FusionControllerBlockEntity controller = build(helper, false);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.WILD_CRYSTAL, "problem");
            helper.succeed();
        });

        r.add("fusion/broken_shell_is_reported", 20, helper -> {
            FusionControllerBlockEntity controller = build(helper, true);
            ServerLevel level = helper.getLevel();
            BlockPos inside = helper.absolutePos(CORE).offset(1, 0, 1);
            level.setBlock(inside, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            controller.rescan(level);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.INTERIOR, "a block inside");
            helper.assertValueEqual(controller.getStructure().problemPos(), inside, "the blocking position");
            level.setBlock(inside, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
            controller.rescan(level);
            helper.assertTrue(controller.getStructure().formed(), "water inside is allowed");

            BlockPos edge = helper.absolutePos(CORE).offset(RADIUS, 0, RADIUS);
            level.setBlock(edge, NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get().defaultBlockState(), Block.UPDATE_ALL);
            controller.rescan(level);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.FRAME, "glass on an edge");
            level.setBlock(edge, NTBlocks.FUSION_CASING.get().defaultBlockState(), Block.UPDATE_ALL);

            level.setBlock(injectorPos(helper), NTBlocks.LASER_INJECTOR.get().defaultBlockState()
                    .setValue(LaserInjectorBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
            controller.rescan(level);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.INJECTOR, "an injector facing away");
            helper.succeed();
        });

        r.add("fusion/ignites_then_makes_fe_from_salt_water", 300, helper -> {
            lowerIgnition();
            FusionControllerBlockEntity controller = build(helper, true);
            BlockPos converterPos = controllerPos(helper).north();
            helper.getLevel().setBlock(converterPos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState(), Block.UPDATE_ALL);
            controller.getFuelTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 10_000));
            helper.assertValueEqual(controller.getHeat(), 0L, "a new plant starts cold");
            feedInjector(helper, 1_000, 3.0F);
            int expected = Math.min(1_000 * NTConfig.fusionFePerAp, controller.getStructure().ceiling());
            helper.succeedWhen(() -> {
                helper.assertTrue(controller.getStatus().running(), "the plant runs, status " + controller.getStatus());
                helper.assertValueEqual(controller.getOutput(), expected, "output at purity 3.0");
                helper.assertTrue(controller.getFuelTank().getFluidAmount() < 10_000, "it burns Salt Water");
                EnergyConverterBlockEntity converter = (EnergyConverterBlockEntity) helper.getLevel().getBlockEntity(converterPos);
                helper.assertTrue(converter.getFeBuffer().getAmountAsInt() > 0, "it pushes FE into a neighbour");
                restoreIgnition();
            });
        });

        r.add("fusion/no_fuel_keeps_it_cold", 60, helper -> {
            FusionControllerBlockEntity controller = build(helper, true);
            feedInjector(helper, 1_000, 3.0F);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(controller.getStatus(), FusionControllerBlockEntity.Status.NO_FUEL, "status");
                helper.assertValueEqual(controller.getHeat(), 0L, "heat");
                helper.succeed();
            });
        });

        r.add("fusion/low_purity_beams_do_not_count", 60, helper -> {
            FusionControllerBlockEntity controller = build(helper, true);
            controller.getFuelTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 1_000));
            feedInjector(helper, 1_000, 1.0F);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(controller.getStatus(), FusionControllerBlockEntity.Status.LOW_PURITY, "status");
                helper.assertValueEqual(controller.getHeat(), 0L, "heat");
                helper.succeed();
            });
        });

        r.add("fusion/port_takes_salt_water_and_gives_fe", 20, helper -> {
            FusionControllerBlockEntity controller = build(helper, true);
            ServerLevel level = helper.getLevel();
            BlockPos portPos = helper.absolutePos(CORE).offset(RADIUS, -1, 1);
            level.setBlock(portPos, NTBlocks.FUSION_PORT.get().defaultBlockState(), Block.UPDATE_ALL);
            controller.rescan(level);
            helper.assertTrue(controller.getStructure().ports().contains(portPos), "the port is part of the plant");
            FusionPortBlockEntity port = (FusionPortBlockEntity) level.getBlockEntity(portPos);
            helper.assertTrue(port.getController() == controller, "the port links to its controller");

            ResourceHandler<FluidResource> fuel = level.getCapability(Capabilities.Fluid.BLOCK, portPos, Direction.EAST);
            helper.assertTrue(fuel != null, "the port exposes a fluid handler");
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = fuel.insert(FluidResource.of(NTFluids.SALT_WATER.getStillFluid()), 500, tx);
                helper.assertValueEqual(inserted, 500, "salt water inserted");
                tx.commit();
            }
            helper.assertValueEqual(controller.getFuelTank().getFluidAmount(), 500, "the controller holds it");
            try (Transaction tx = Transaction.openRoot()) {
                int water = fuel.insert(FluidResource.of(Fluids.WATER), 500, tx);
                helper.assertValueEqual(water, 0, "plain water is refused");
            }
            EnergyHandler energy = level.getCapability(Capabilities.Energy.BLOCK, portPos, Direction.EAST);
            helper.assertTrue(energy != null, "the port exposes an energy handler");
            helper.succeed();
        });

        r.add("fusion/satellite_crystals_raise_ceiling_and_yield", WIDE_ARENA, 20, 0, helper -> {
            int radius = FusionStructure.MIN_SATELLITE_RADIUS;
            int corner = FusionStructure.satelliteOffset(radius);
            FusionControllerBlockEntity controller = build(helper, true, WIDE_CORE, radius);
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(WIDE_CORE);
            helper.assertTrue(controller.getStructure().formed(), "a 7x7 chamber forms, got " + controller.getStructure().problem());
            helper.assertValueEqual(controller.getStructure().satelliteCount(), 0, "satellites before any are placed");
            helper.assertValueEqual(controller.getStructure().fePerAp(), NTConfig.fusionFePerAp, "FE per AP with no satellites");

            PrismarineCrystalBlock.build(level, core.offset(corner, 0, corner), true);
            PrismarineCrystalBlock.build(level, core.offset(-corner, 0, -corner), true);
            controller.rescan(level);
            FusionStructure structure = controller.getStructure();
            helper.assertTrue(structure.formed(), "satellites in the corners keep the chamber formed, got " + structure.problem() + " at " + structure.problemPos());
            helper.assertValueEqual(structure.satelliteCount(), 2, "satellite count");
            helper.assertValueEqual(structure.maxOutput(), NTConfig.fusionMaxOutput + 2 * NTConfig.fusionSatelliteContainment, "maximum output with two satellites");
            helper.assertValueEqual(structure.ceiling(), Math.min(structure.maxOutput(),
                    FusionStructure.chamberContainment(radius) + 2 * NTConfig.fusionSatelliteContainment), "ceiling with two satellites");
            helper.assertValueEqual(structure.fePerAp(), NTConfig.fusionFePerAp + 2 * NTConfig.fusionSatelliteFePerAp, "FE per AP with two satellites");

            BlockPos wild = core.offset(corner, 0, -corner);
            PrismarineCrystalBlock.build(level, wild, false);
            controller.rescan(level);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.WILD_CRYSTAL, "a wild satellite");
            helper.assertValueEqual(controller.getStructure().problemPos(), wild, "the position of the wild satellite");
            for (int i = -3; i <= 2; i++) {
                level.setBlock(wild.above(i), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }

            BlockPos offCorner = core.offset(corner - 1, 0, corner);
            PrismarineCrystalBlock.build(level, offCorner, true);
            controller.rescan(level);
            helper.assertValueEqual(controller.getStructure().problem(), FusionStructure.Problem.INTERIOR, "a crystal outside the corners");
            helper.succeed();
        });

        r.add("fusion/satellites_raise_output_per_ap", WIDE_ARENA, 300, 0, helper -> {
            lowerIgnition();
            int radius = FusionStructure.MIN_SATELLITE_RADIUS;
            int corner = FusionStructure.satelliteOffset(radius);
            FusionControllerBlockEntity controller = build(helper, true, WIDE_CORE, radius);
            BlockPos core = helper.absolutePos(WIDE_CORE);
            for (int dx = -1; dx <= 1; dx += 2) {
                for (int dz = -1; dz <= 1; dz += 2) {
                    PrismarineCrystalBlock.build(helper.getLevel(), core.offset(dx * corner, 0, dz * corner), true);
                }
            }
            controller.rescan(helper.getLevel());
            helper.assertValueEqual(controller.getStructure().satelliteCount(), FusionStructure.MAX_SATELLITES, "all four satellites");
            controller.getFuelTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 10_000));
            feedInjector(helper, 1_000, 3.0F, WIDE_CORE, radius);
            int expected = Math.min(1_000 * controller.getStructure().fePerAp(), controller.getStructure().ceiling());
            helper.succeedWhen(() -> {
                helper.assertTrue(controller.getStatus().running(), "the plant runs, status " + controller.getStatus());
                helper.assertValueEqual(controller.getOutput(), expected, "output with four satellites at purity 3.0");
                restoreIgnition();
            });
        });
    }
}
