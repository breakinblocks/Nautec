package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.BeamOverclock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Map;
import java.util.function.IntSupplier;

public final class BeamOverclockTests {
    private static final BlockPos MIXER = new BlockPos(4, 2, 4);

    private BeamOverclockTests() {
    }

    private static int runMixer(NTGameTestHelper helper, int power, int ticks) {
        helper.setBlock(MIXER, NTBlocks.MIXER.get());
        MixerBlockEntity mixer = helper.getBlockEntity(MIXER, MixerBlockEntity.class);
        mixer.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.SALT.get()));
        mixer.getFluidTank().fill(new FluidStack(Fluids.WATER, 1000));
        BlockPos origin = helper.absolutePos(MIXER).north();
        for (int i = 0; i < ticks; i++) {
            mixer.receivePower(power, Direction.NORTH, origin);
            mixer.commonTick();
        }
        return mixer.getSecondaryFluidTank().getFluid().is(NTFluids.SALT_WATER.getStillFluid()) ? mixer.getSecondaryFluidTank().getFluidAmount() : 0;
    }

    public static void register(NTTestRegistrar r) {
        r.add("beam_overclock/speed_follows_the_square_root_of_the_beam", 20, helper -> {
            boolean enabled = NTConfig.beamOverclock;
            double cap = NTConfig.beamOverclockMaxSpeed;
            try {
                NTConfig.beamOverclock = true;
                NTConfig.beamOverclockMaxSpeed = 0;
                helper.assertValueEqual(BeamOverclock.speed(9, 10), 0F, "a beam under the requirement does not run");
                helper.assertValueEqual(BeamOverclock.speed(10, 10), 1F, "exactly the requirement runs at normal speed");
                helper.assertValueEqual(BeamOverclock.speed(40, 10), 2F, "four times the beam runs twice as fast");
                helper.assertValueEqual(BeamOverclock.speed(1000, 10), 10F, "a hundred times the beam runs ten times as fast");
                NTConfig.beamOverclockMaxSpeed = 3;
                helper.assertValueEqual(BeamOverclock.speed(1000, 10), 3F, "the configured cap holds");
                NTConfig.beamOverclock = false;
                helper.assertValueEqual(BeamOverclock.speed(1000, 10), 1F, "switched off, a stronger beam gives no speed");
                BeamOverclock counter = new BeamOverclock();
                int steps = 0;
                for (int i = 0; i < 10; i++) {
                    steps += counter.advance(1.5F);
                }
                helper.assertValueEqual(steps, 15, "fractional speed adds up exactly over time");
            } finally {
                NTConfig.beamOverclock = enabled;
                NTConfig.beamOverclockMaxSpeed = cap;
            }
            helper.succeed();
        });

        r.add("beam_overclock/every_scaled_machine_reports_its_requirement", 20, helper -> {
            Map<BlockEntityType<?>, IntSupplier> machines = Map.ofEntries(
                    Map.entry(NTBlockEntityTypes.MIXER.get(), () -> NTConfig.mixerPower),
                    Map.entry(NTBlockEntityTypes.MUTATOR.get(), () -> NTConfig.mutatorPowerUsage),
                    Map.entry(NTBlockEntityTypes.INCUBATOR.get(), () -> NTConfig.incubatorPowerUsage),
                    Map.entry(NTBlockEntityTypes.BACTERIAL_ANALYZER.get(), () -> NTConfig.bacteriaAnalyzerPowerUsage),
                    Map.entry(NTBlockEntityTypes.ADVANCED_BACTERIAL_ANALYZER.get(), () -> NTConfig.advancedAnalyzerPowerUsage),
                    Map.entry(NTBlockEntityTypes.PRESSURE_FORGE.get(), () -> NTConfig.pressureForgePowerUsage),
                    Map.entry(NTBlockEntityTypes.COLONY_REPLICATOR.get(), () -> NTConfig.replicatorPowerUsage),
                    Map.entry(NTBlockEntityTypes.GRAFTING_STATION.get(), () -> NTConfig.graftingStationPowerUsage),
                    Map.entry(NTBlockEntityTypes.SUBMARINE_DOCK.get(), () -> NTConfig.dockPowerUsage),
                    Map.entry(NTBlockEntityTypes.AUGMENTATION_STATION_EXTENSION.get(), () -> NTConfig.augmentationStationPower),
                    Map.entry(NTBlockEntityTypes.BIO_REACTOR.get(), () -> NTConfig.bioReactorPowerBase),
                    Map.entry(NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR.get(), () -> NTConfig.industrialBioReactorPowerBase));
            machines.forEach((type, expected) -> {
                var block = type.getValidBlocks().iterator().next();
                var entity = type.create(BlockPos.ZERO, block.defaultBlockState());
                helper.assertTrue(entity instanceof LaserBlockEntity, type + " is a laser machine");
                helper.assertValueEqual(((LaserBlockEntity) entity).getRequiredPower(), expected.getAsInt(), "required power of " + block);
            });
            helper.succeed();
        });

        r.add("beam_overclock/mixer_finishes_twice_as_fast_on_four_times_the_beam", 20, helper -> {
            boolean enabled = NTConfig.beamOverclock;
            double cap = NTConfig.beamOverclockMaxSpeed;
            try {
                NTConfig.beamOverclock = true;
                NTConfig.beamOverclockMaxSpeed = 0;
                helper.assertValueEqual(runMixer(helper, NTConfig.mixerPower, 60), 0, "at its normal beam the 100 tick recipe is not done after 60 ticks");
                helper.assertValueEqual(runMixer(helper, NTConfig.mixerPower * 4, 60), 1000, "on four times the beam it finishes inside 60 ticks");
            } finally {
                NTConfig.beamOverclock = enabled;
                NTConfig.beamOverclockMaxSpeed = cap;
            }
            helper.succeed();
        });
    }
}
