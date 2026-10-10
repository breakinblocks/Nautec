package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.GraftingStationBlockEntity;
import com.breakinblocks.nautec.content.blocks.GraftingStationBlock;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class GraftingStationTests {
    private static final BlockPos STATION = new BlockPos(4, 1, 4);

    private GraftingStationTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("grafting_station/grafts_a_colony_anywhere", 40, helper -> {
            GraftingStationBlockEntity station = loaded(helper);
            BacteriaObtainValue stone = GraftingStationBlockEntity.sample(new ItemStack(Items.STONE));
            helper.assertTrue(stone != null, "Stone should be a graftable sample");

            run(helper, station, 3.0f, NTConfig.graftingStationDuration + 5);

            ItemStack result = station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.OUTPUT_SLOT);
            helper.assertTrue(result.is(NTItems.PETRI_DISH.get()), "The station should put out a Petri Dish");
            IBacteriaStorage storage = result.getCapability(NTCapabilities.BacteriaStorage.ITEM);
            helper.assertTrue(storage != null, "The result dish should hold bacteria");
            BacteriaInstance colony = storage.getBacteria(0);
            helper.assertFalse(colony.isEmpty(), "Every cycle should graft a colony, with no chance roll");
            helper.assertValueEqual(stone.bacteria(), colony.getBacteria(), "grafted strain");
            helper.assertTrue(station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.DISH_SLOT).isEmpty(), "The empty dish should be used");
            helper.assertTrue(station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT).isEmpty(), "The sample block should be used");
            helper.assertValueEqual(0, station.getFluidTank().getFluidAmount(), "salt water left after one graft");
            helper.succeed();
        });

        r.add("grafting_station/needs_pure_beam", 40, helper -> {
            GraftingStationBlockEntity station = loaded(helper);
            run(helper, station, (float) NTConfig.graftingStationPurity - 0.1f, NTConfig.graftingStationDuration + 5);
            helper.assertValueEqual(GraftingStationBlockEntity.STATUS_LOW_PURITY, station.getStatus(), "status with a weak beam");
            helper.assertTrue(station.getItemStackHandler().getStackInSlot(GraftingStationBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "A beam below the purity floor should never graft");
            helper.assertFalse(station.getBlockState().getValue(GraftingStationBlock.ACTIVE), "The station should not look active while stalled");
            helper.succeed();
        });

        r.add("grafting_station/accepts_only_valid_inputs", 20, helper -> {
            helper.setBlock(STATION, NTBlocks.GRAFTING_STATION.get());
            GraftingStationBlockEntity station = helper.getBlockEntity(STATION, GraftingStationBlockEntity.class);
            var items = station.getItemStackHandler();
            helper.assertFalse(items.isItemValid(GraftingStationBlockEntity.SAMPLE_SLOT, new ItemStack(Items.DIRT)), "Dirt is not graftable");
            helper.assertTrue(items.isItemValid(GraftingStationBlockEntity.SAMPLE_SLOT, new ItemStack(Items.SAND)), "Sand is graftable");
            helper.assertTrue(items.isItemValid(GraftingStationBlockEntity.DISH_SLOT, new ItemStack(NTItems.PETRI_DISH.get())), "An empty dish fits");
            helper.assertFalse(items.isItemValid(GraftingStationBlockEntity.OUTPUT_SLOT, new ItemStack(NTItems.PETRI_DISH.get())), "Nothing goes into the output");
            helper.assertValueEqual(0, station.getFluidTank().fill(new FluidStack(Fluids.WATER, 1000)), "fresh water accepted");
            helper.succeed();
        });
    }

    private static GraftingStationBlockEntity loaded(NTGameTestHelper helper) {
        helper.setBlock(STATION, NTBlocks.GRAFTING_STATION.get());
        GraftingStationBlockEntity station = helper.getBlockEntity(STATION, GraftingStationBlockEntity.class);
        station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.DISH_SLOT, new ItemStack(NTItems.PETRI_DISH.get()));
        station.getItemStackHandler().setStackInSlot(GraftingStationBlockEntity.SAMPLE_SLOT, new ItemStack(Items.STONE));
        station.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), NTConfig.graftingStationSaltWaterUsage));
        return station;
    }

    private static void run(NTGameTestHelper helper, GraftingStationBlockEntity station, float purity, int ticks) {
        BlockPos origin = helper.absolutePos(STATION.above());
        for (int tick = 0; tick < ticks; tick++) {
            station.receivePower(NTConfig.graftingStationPowerUsage, Direction.UP, origin);
            station.receiveNewPurity(purity, Direction.UP, origin);
            station.commonTick();
        }
    }
}
