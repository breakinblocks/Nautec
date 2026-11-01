package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ResonanceChamberBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.conduit.ConduitBeaconBlockEntity;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalFrameBlock;
import com.breakinblocks.nautec.content.extradimensional.FramePart;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;

public final class IssueTrackerTests {
    private static final BlockPos POS = new BlockPos(4, 3, 4);

    private IssueTrackerTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("issues/spawn_eggs_create_their_mobs", 20, helper -> {
            Item[] eggs = {NTItems.SILT_SKIPPER_SPAWN_EGG.get(), NTItems.LANTERN_JELLY_SPAWN_EGG.get(),
                    NTItems.VENT_CRAWLER_SPAWN_EGG.get(), NTItems.ABYSSAL_MAW_SPAWN_EGG.get()};
            EntityType<?>[] types = {NTEntities.SILT_SKIPPER.get(), NTEntities.LANTERN_JELLY.get(),
                    NTEntities.VENT_CRAWLER.get(), NTEntities.ABYSSAL_MAW.get()};
            helper.setBlock(POS, Blocks.STONE);
            BlockPos abs = helper.absolutePos(POS);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            for (int i = 0; i < eggs.length; i++) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(eggs[i]));
                BlockHitResult hit = new BlockHitResult(abs.getCenter().add(0, 0.5, 0), Direction.UP, abs, false);
                player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
                EntityType<?> expected = types[i];
                helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Mob.class, new AABB(abs).inflate(3),
                        mob -> mob.getType() == expected).isEmpty(), "Egg did not spawn " + expected);
                helper.assertTrue(player.getMainHandItem().isEmpty(), "Survival egg is consumed");
            }
            helper.succeed();
        });
        r.add("issues/containment_frames_resist_flowing_water", 80, helper -> {
            var frames = new ExtradimensionalFrameBlock[]{NTBlocks.RIFT_CONTAINMENT_FRAME.get(),
                    NTBlocks.VOID_CONTAINMENT_FRAME.get(), NTBlocks.ABYSSAL_CONTAINMENT_FRAME.get(),
                    NTBlocks.EVENT_HORIZON_CONTAINMENT_FRAME.get()};
            for (int i = 0; i < frames.length; i++) {
                BlockPos pos = new BlockPos(1 + i * 2, 2, 4);
                for (FramePart part : FramePart.values()) {
                    helper.assertTrue(frames[i].defaultBlockState().setValue(ExtradimensionalFrameBlock.PART, part).blocksMotion(),
                            "Water must not replace " + frames[i] + " in state " + part);
                }
                helper.setBlock(pos, frames[i]);
                helper.setBlock(pos.above(), Blocks.WATER);
            }
            helper.runAfterDelay(30, () -> {
                for (int i = 0; i < frames.length; i++) {
                    helper.assertTrue(helper.getBlockState(new BlockPos(1 + i * 2, 2, 4)).is(frames[i]),
                            "Flowing water destroyed a containment frame");
                }
                helper.succeed();
            });
        });
        r.add("issues/drain_empty_tank_ignores_filled_bucket", 20, helper -> {
            helper.setBlock(POS, NTBlocks.DRAIN.get().defaultBlockState().setValue(Multiblock.FORMED, true));
            DrainBlockEntity drain = helper.getBlockEntity(POS, DrainBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            BlockPos abs = helper.absolutePos(POS);
            BlockHitResult hit = new BlockHitResult(abs.getCenter(), Direction.UP, abs, false);
            helper.getBlockState(POS).useWithoutItem(helper.getLevel(), player, hit);
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "Filled bucket remains intact");
            helper.assertTrue(drain.getFluidTank().getFluid().isEmpty(), "Empty drain stays empty");
            drain.getFluidTank().fill(new FluidStack(Fluids.WATER, 2000));
            helper.getBlockState(POS).useWithoutItem(helper.getLevel(), player, hit);
            helper.assertValueEqual(2000, drain.getFluidTank().getFluidAmount(), "A full bucket takes no fluid");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            helper.getBlockState(POS).useWithoutItem(helper.getLevel(), player, hit);
            helper.assertValueEqual(1000, drain.getFluidTank().getFluidAmount(), "An empty bucket takes one bucket");
            helper.succeed();
        });
        r.add("issues/aquarine_glass_passes_beams", 20, helper -> {
            helper.setBlock(POS, NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get());
            BlockPos abs = helper.absolutePos(POS);
            helper.assertFalse(LaserBlockEntity.stopsBeam(helper.getLevel(), abs, helper.getBlockState(POS),
                    abs.west().getCenter(), abs.east().getCenter()), "Aquarine glass blocks the beam");
            helper.succeed();
        });
        r.add("issues/catalyst_transmits_and_syncs_fuel_purity", 80, helper -> {
            helper.setBlock(POS, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
            helper.setBlock(POS.east(2), NTBlocks.RESONANCE_CHAMBER.get());
            AquaticCatalystBlockEntity catalyst = helper.getBlockEntity(POS, AquaticCatalystBlockEntity.class);
            ResonanceChamberBlockEntity chamber = helper.getBlockEntity(POS.east(2), ResonanceChamberBlockEntity.class);
            helper.runAfterDelay(1, () -> catalyst.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 2)));
            helper.runAfterDelay(35, () -> {
                helper.assertTrue(chamber.getPower() > 0, "Catalyst supplies power");
                helper.assertTrue(Math.abs(chamber.getPurity() - 0.8f) < 0.001f, "Receiver gets fuel purity");
                var tag = catalyst.getUpdateTag(helper.getLevel().registryAccess());
                helper.assertTrue(tag.contains("source_purity"), "Client update includes source purity");
                helper.assertTrue(Math.abs(tag.getFloat("source_purity") - 0.8f) < 0.001f, "Synced purity matches fuel");
                helper.succeed();
            });
        });
        r.add("issues/conduit_beacon_syncs_diagnostics", 20, helper -> {
            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) <= 1 && Math.abs(y) <= 1 && Math.abs(z) <= 1) {
                            helper.setBlock(POS.offset(x, y, z), Blocks.WATER);
                        } else if (ConduitBeaconBlockEntity.isFrameSlot(x, y, z)) {
                            helper.setBlock(POS.offset(x, y, z), Blocks.PRISMARINE);
                        }
                    }
                }
            }
            helper.setBlock(POS, NTBlocks.CONDUIT_BEACON.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
            ConduitBeaconBlockEntity beacon = helper.getBlockEntity(POS, ConduitBeaconBlockEntity.class);
            beacon.setPowerPerSide(Direction.UP, 1000);
            beacon.commonTick();
            helper.assertTrue(beacon.isRunning(), "Formed beacon runs");
            var tag = beacon.getUpdateTag(helper.getLevel().registryAccess());
            helper.assertTrue(tag.getBoolean("water") && tag.getBoolean("running"), "Client sees water and running status");
            helper.assertValueEqual(beacon.getFrameSize(), tag.getInt("frame_size"), "Client sees the frame size");
            helper.succeed();
        });
        r.add("issues/combustion_boosters_scale_output_and_consumption", 20, helper -> {
            helper.setBlock(POS, NTBlocks.COMBUSTION_DYNAMO.get());
            CombustionDynamoBlockEntity dynamo = helper.getBlockEntity(POS, CombustionDynamoBlockEntity.class);
            Item[] items = {Items.BLAZE_POWDER, NTItems.FIERY_CRYSTAL.get(), NTItems.CRYSTALLINE_COMBUSTION_BOOSTER.get(), NTItems.STELLAR_COMBUSTION_BOOSTER.get()};
            int[] rates = {640, 960, 1600, 3200};
            int[] durations = {2400, 6000, 12000, 72000};
            int[] waterUsed = {150, 200, 300, 500};
            for (int i = 0; i < items.length; i++) {
                helper.setBlock(POS, Blocks.AIR);
                helper.setBlock(POS, NTBlocks.COMBUSTION_DYNAMO.get());
                dynamo = helper.getBlockEntity(POS, CombustionDynamoBlockEntity.class);
                dynamo.getFluidTank().fill(new FluidStack(NTFluids.OIL.getStillFluid(), 1000));
                dynamo.getSecondaryFluidTank().fill(new FluidStack(Fluids.WATER, 1000));
                dynamo.getItemStackHandler().setStackInSlot(0, new ItemStack(items[i]));
                for (int tick = 0; tick < 100; tick++) {
                    dynamo.getEnergyStorage().set(0);
                    dynamo.commonTick();
                }
                helper.assertValueEqual(rates[i], dynamo.getRate(), "Boosted output");
                helper.assertValueEqual(1000 - waterUsed[i], dynamo.getSecondaryFluidTank().getFluidAmount(), "Scaled water use");
                helper.assertValueEqual(1000 - waterUsed[i] / 10, dynamo.getFluidTank().getFluidAmount(), "Scaled oil use");
                helper.assertValueEqual(durations[i] - 100, dynamo.getAdditiveLeft(), "Remaining boost time");
                dynamo.getSecondaryFluidTank().setFluid(FluidStack.EMPTY);
                dynamo.commonTick();
                helper.assertValueEqual(durations[i] - 100, dynamo.getAdditiveLeft(), "No water pauses the booster");
            }
            helper.succeed();
        });
    }
}
