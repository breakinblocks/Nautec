package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Consumer;

public final class MixerParallelTests {
    private static final BlockPos MIXER = new BlockPos(4, 2, 4);
    private static final int CYCLE_TICKS = 100;

    private MixerParallelTests() {
    }

    private static MixerBlockEntity placeMixer(GameTestHelper helper, int salt, int water) {
        helper.setBlock(MIXER, NTBlocks.MIXER.get());
        MixerBlockEntity mixer = helper.getBlockEntity(MIXER, MixerBlockEntity.class);
        mixer.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.SALT.get(), salt));
        helper.assertValueEqual(mixer.getFluidTank().fill(new FluidStack(Fluids.WATER, water)), water, "water filled into the input tank");
        return mixer;
    }

    private static void tick(MixerBlockEntity mixer, int power, int ticks) {
        for (int i = 0; i < ticks; i++) {
            mixer.setPowerPerSide(Direction.NORTH, power);
            mixer.commonTick();
        }
    }

    private static int saltWater(MixerBlockEntity mixer) {
        FluidStack output = mixer.getSecondaryFluidTank().getFluid();
        return output.is(NTFluids.SALT_WATER.getStillFluid()) ? output.getAmount() : 0;
    }

    private static void withDefaults(GameTestHelper helper, Consumer<GameTestHelper> body) {
        int power = NTConfig.mixerPower;
        int maxParallel = NTConfig.mixerMaxParallel;
        boolean overclock = NTConfig.beamOverclock;
        double cap = NTConfig.beamOverclockMaxSpeed;
        int inputCapacity = NTConfig.mixerInputCapacity;
        int outputCapacity = NTConfig.mixerOutputCapacity;
        try {
            NTConfig.mixerInputCapacity = 64_000;
            NTConfig.mixerOutputCapacity = 64_000;
            NTConfig.mixerPower = 10;
            NTConfig.mixerMaxParallel = 64;
            NTConfig.beamOverclock = true;
            NTConfig.beamOverclockMaxSpeed = 0;
            body.accept(helper);
        } finally {
            NTConfig.mixerInputCapacity = inputCapacity;
            NTConfig.mixerOutputCapacity = outputCapacity;
            NTConfig.mixerPower = power;
            NTConfig.mixerMaxParallel = maxParallel;
            NTConfig.beamOverclock = overclock;
            NTConfig.beamOverclockMaxSpeed = cap;
        }
        helper.succeed();
    }

    public static void register(NTTestRegistrar r) {
        r.add("mixer_parallel/full_stack_mixes_in_one_cycle", 20, helper -> withDefaults(helper, h -> {
            MixerBlockEntity mixer = placeMixer(h, 64, 64_000);
            tick(mixer, 640, 1);
            h.assertValueEqual(mixer.getParallel(), 64, "640 AP runs 64 crafts at once");
            h.assertValueEqual(mixer.beamSpeed(), 1F, "640 AP has no AP left over for speed");
            tick(mixer, 640, CYCLE_TICKS - 1);
            h.assertValueEqual(saltWater(mixer), 0, "nothing is made before the cycle ends");
            tick(mixer, 640, 1);
            h.assertValueEqual(saltWater(mixer), 64_000, "salt water after one cycle");
            h.assertTrue(mixer.getItemStackHandler().getStackInSlot(0).isEmpty(), "the whole stack of salt is used");
            h.assertValueEqual(mixer.getInputFluidAmount(), 0, "water left in the input tank");
        }));

        r.add("mixer_parallel/batch_is_capped_by_inputs", 20, helper -> withDefaults(helper, h -> {
            MixerBlockEntity mixer = placeMixer(h, 5, 64_000);
            tick(mixer, 640, CYCLE_TICKS + 1);
            h.assertValueEqual(saltWater(mixer), 5_000, "five salt make five buckets");
            h.assertTrue(mixer.getItemStackHandler().getStackInSlot(0).isEmpty(), "all five salt are used");
            h.assertValueEqual(mixer.getInputFluidAmount(), 59_000, "water left in the input tank");
        }));

        r.add("mixer_parallel/minimum_power_crafts_once_per_cycle", 20, helper -> withDefaults(helper, h -> {
            MixerBlockEntity mixer = placeMixer(h, 64, 64_000);
            tick(mixer, NTConfig.mixerPower, 1);
            h.assertValueEqual(mixer.getParallel(), 1, "the minimum beam runs one craft");
            h.assertValueEqual(mixer.beamSpeed(), 1F, "the minimum beam runs at normal speed");
            tick(mixer, NTConfig.mixerPower, CYCLE_TICKS);
            h.assertValueEqual(saltWater(mixer), 1_000, "one bucket after one cycle");
            h.assertValueEqual(mixer.getItemStackHandler().getStackInSlot(0).getCount(), 63, "salt left after one craft");
            h.assertValueEqual(mixer.getInputFluidAmount(), 63_000, "water left after one craft");
        }));
    }
}
