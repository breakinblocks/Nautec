package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity.Synthesizer;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;

public final class PressureSynthesizerTests {
    private static final BlockPos FORGE = new BlockPos(4, 1, 4);

    private PressureSynthesizerTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("pressure_synthesizer/pressure_rules", 20, helper -> {
            helper.setBlock(FORGE, NTBlocks.PRESSURE_FORGE.get());
            BlockPos pos = helper.absolutePos(FORGE);
            int oldDepth = NTConfig.pressureForgeDepth;
            try {
                NTConfig.pressureForgeDepth = pos.getY();
                helper.assertFalse(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos, Synthesizer.NONE), "Deep enough but dry without a synthesizer");
                helper.assertTrue(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos, Synthesizer.BASIC), "The basic synthesizer replaces the water column");
                helper.assertTrue(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos, Synthesizer.ATLANTEAN), "The Atlantean synthesizer replaces the water column");

                NTConfig.pressureForgeDepth = pos.getY() - 1;
                helper.assertFalse(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos, Synthesizer.BASIC), "The basic synthesizer still needs depth");
                helper.assertTrue(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos, Synthesizer.ATLANTEAN), "The Atlantean synthesizer ignores depth");
            } finally {
                NTConfig.pressureForgeDepth = oldDepth;
            }
            helper.succeed();
        });

        r.add("pressure_synthesizer/atlantean_forges_deep_recipe_anywhere", 40, helper -> {
            helper.setBlock(FORGE, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(FORGE, PressureForgeBlockEntity.class);
            BlockPos origin = helper.absolutePos(FORGE.east());
            forge.setSynthesizer(Synthesizer.ATLANTEAN);
            forge.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get()));
            forge.getFluidTank().setFluid(new FluidStack(NTFluids.ETCHING_ACID.getStillFluid(), NTConfig.pressureForgeAcidUsage));
            for (int tick = 0; tick < 400; tick++) {
                forge.receivePower(1000, Direction.WEST, origin);
                forge.receiveNewPurity(3f, Direction.WEST, origin);
                forge.commonTick();
            }
            helper.assertTrue(forge.getItemStackHandler().getStackInSlot(1).is(NTItems.DEEP_STEEL_PLATING.get()),
                    "An Atlantean forge should press a Y -40 recipe at any height with no water");
            helper.succeed();
        });

        r.add("pressure_synthesizer/fit_swap_remove_and_drop", 40, helper -> {
            helper.setBlock(FORGE, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(FORGE, PressureForgeBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos abs = helper.absolutePos(FORGE);
            BlockHitResult hit = new BlockHitResult(abs.getCenter(), Direction.UP, abs, false);

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.PRESSURE_SYNTHESIZER.get()));
            forge.getBlockState().useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertValueEqual(Synthesizer.BASIC, forge.getSynthesizer(), "fitted synthesizer");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "Fitting should use up the held synthesizer");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.ATLANTEAN_PRESSURE_SYNTHESIZER.get()));
            forge.getBlockState().useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
            helper.assertValueEqual(Synthesizer.ATLANTEAN, forge.getSynthesizer(), "synthesizer after swapping");
            helper.assertTrue(player.getInventory().contains(new ItemStack(NTItems.PRESSURE_SYNTHESIZER.get())), "Swapping should hand back the basic synthesizer");

            player.setShiftKeyDown(true);
            forge.getBlockState().useWithoutItem(helper.getLevel(), player, hit);
            helper.assertValueEqual(Synthesizer.NONE, forge.getSynthesizer(), "synthesizer after removing");
            helper.assertTrue(player.getInventory().contains(new ItemStack(NTItems.ATLANTEAN_PRESSURE_SYNTHESIZER.get())), "Removing should hand back the synthesizer");

            forge.setSynthesizer(Synthesizer.BASIC);
            helper.getLevel().destroyBlock(abs, false);
            helper.runAfterDelay(2, () -> {
                boolean dropped = helper.getEntities(EntityType.ITEM, FORGE, 2.0).stream()
                        .anyMatch(item -> item.getItem().is(NTItems.PRESSURE_SYNTHESIZER.get()));
                helper.assertTrue(dropped, "Breaking the forge should drop its synthesizer");
                helper.succeed();
            });
        });

        r.add("pressure_synthesizer/mixer_makes_synthesizer", 40, helper -> {
            helper.setBlock(FORGE, NTBlocks.MIXER.get());
            MixerBlockEntity mixer = helper.getBlockEntity(FORGE, MixerBlockEntity.class);
            helper.assertTrue(mixer.getFluidTank().getCapacity() >= 8000, "The mixer tank must fit 8 buckets of salt water");
            mixer.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 8000));
            mixer.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get()));
            for (int tick = 0; tick < 1200; tick++) {
                mixer.setPowerPerSide(Direction.EAST, 1000);
                mixer.commonTick();
            }
            helper.assertTrue(mixer.getItemStackHandler().getStackInSlot(MixerBlockEntity.OUTPUT_SLOT).is(NTItems.PRESSURE_SYNTHESIZER.get()),
                    "The mixer should make a Pressure Synthesizer");
            helper.assertValueEqual(0, mixer.getFluidTank().getFluidAmount(), "salt water left after the mix");
            helper.assertTrue(mixer.getItemStackHandler().getStackInSlot(0).isEmpty(), "The crystal should be used up");
            helper.succeed();
        });
    }
}
