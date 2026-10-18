package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.OsmosisFilterBlockEntity;
import com.breakinblocks.nautec.content.blocks.OsmosisFilterBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Consumer;

public final class OsmosisFilterTests {
    private static final BlockPos FILTER = new BlockPos(4, 2, 4);

    private OsmosisFilterTests() {
    }

    private static OsmosisFilterBlockEntity placeFilter(GameTestHelper helper) {
        helper.setBlock(FILTER, NTBlocks.OSMOSIS_FILTER.get());
        return helper.getBlockEntity(FILTER, OsmosisFilterBlockEntity.class);
    }

    private static void tick(OsmosisFilterBlockEntity filter, int power, int ticks) {
        for (int i = 0; i < ticks; i++) {
            if (power > 0) {
                filter.setPowerPerSide(Direction.NORTH, power);
            }
            filter.commonTick();
        }
    }

    private static boolean active(GameTestHelper helper) {
        return helper.getBlockState(FILTER).getValue(OsmosisFilterBlock.ACTIVE);
    }

    private static void withDefaults(GameTestHelper helper, Consumer<GameTestHelper> body) {
        int base = NTConfig.osmosisFilterBaseRate;
        int step = NTConfig.osmosisFilterApStep;
        int max = NTConfig.osmosisFilterMaxRate;
        int capacity = NTConfig.osmosisFilterCapacity;
        try {
            NTConfig.osmosisFilterBaseRate = 1_000;
            NTConfig.osmosisFilterApStep = 40;
            NTConfig.osmosisFilterMaxRate = 1_024_000;
            NTConfig.osmosisFilterCapacity = 16_000;
            body.accept(helper);
        } finally {
            NTConfig.osmosisFilterBaseRate = base;
            NTConfig.osmosisFilterApStep = step;
            NTConfig.osmosisFilterMaxRate = max;
            NTConfig.osmosisFilterCapacity = capacity;
        }
        helper.succeed();
    }

    public static void register(NTTestRegistrar r) {
        r.add("osmosis_filter/rate_doubles_every_step", 20, helper -> withDefaults(helper, h -> {
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(0), 1_000.0, "rate with no beam");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(40), 2_000.0, "rate at 40 AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(80), 4_000.0, "rate at 80 AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(160), 16_000.0, "rate at 160 AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(320), 256_000.0, "rate at 320 AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(400), 1_024_000.0, "rate at 400 AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(480), 1_024_000.0, "rate past the cap");
            h.assertValueEqual(OsmosisFilterBlockEntity.ratePerSecond(Integer.MAX_VALUE), 1_024_000.0, "rate on a huge beam");
            h.assertValueEqual(OsmosisFilterBlockEntity.fillLimit(1_000.0), 16_000, "fill limit at low AP");
            h.assertValueEqual(OsmosisFilterBlockEntity.fillLimit(1_024_000.0), 1_024_000, "fill limit at the cap");
        }));

        r.add("osmosis_filter/passive_makes_a_bucket_per_second", 20, helper -> withDefaults(helper, h -> {
            OsmosisFilterBlockEntity filter = placeFilter(h);
            h.assertValueEqual(filter.getRequiredPower(), 0, "the filter needs no beam");
            h.assertValueEqual(filter.getWaterAmount(), 0, "water before the first tick");
            h.assertFalse(active(h), "idle before the first tick");
            tick(filter, 0, 1);
            h.assertValueEqual(filter.getWaterAmount(), 50, "water after one tick");
            h.assertTrue(active(h), "active while filling");
            tick(filter, 0, 19);
            h.assertValueEqual(filter.getWaterAmount(), 1_000, "water after 20 ticks with no beam");
            h.assertTrue(filter.getWater().is(Fluids.WATER), "the tank holds water");
            tick(filter, 0, 20);
            h.assertValueEqual(filter.getWaterAmount(), 2_000, "water after 40 ticks with no beam");
        }));

        r.add("osmosis_filter/beam_of_80_ap_quadruples_output", 20, helper -> withDefaults(helper, h -> {
            OsmosisFilterBlockEntity filter = placeFilter(h);
            h.assertValueEqual(filter.getWaterAmount(), 0, "water before the beam");
            tick(filter, 80, 20);
            h.assertValueEqual(filter.getWaterAmount(), 4_000, "water after 20 ticks at 80 AP");
            h.assertValueEqual(filter.getRate(), 4_000.0, "rate at 80 AP");
            tick(filter, 80, 20);
            h.assertValueEqual(filter.getWaterAmount(), 8_000, "water after 40 ticks at 80 AP");
        }));

        r.add("osmosis_filter/passive_stops_at_capacity", 20, helper -> withDefaults(helper, h -> {
            OsmosisFilterBlockEntity filter = placeFilter(h);
            tick(filter, 0, 319);
            h.assertValueEqual(filter.getWaterAmount(), 15_950, "water one tick before full");
            h.assertTrue(active(h), "active one tick before full");
            tick(filter, 0, 1);
            h.assertValueEqual(filter.getWaterAmount(), 16_000, "water when full");
            h.assertValueEqual(filter.getFillLimit(), 16_000, "fill limit with no beam");
            h.assertFalse(active(h), "idle when full");
            tick(filter, 0, 40);
            h.assertValueEqual(filter.getWaterAmount(), 16_000, "a full filter makes no more water");
            h.assertFalse(active(h), "still idle when full");
        }));

        r.add("osmosis_filter/strong_beam_raises_fill_limit", 20, helper -> withDefaults(helper, h -> {
            OsmosisFilterBlockEntity filter = placeFilter(h);
            tick(filter, 0, 320);
            h.assertValueEqual(filter.getWaterAmount(), 16_000, "full with no beam");
            h.assertFalse(active(h), "idle when full");
            tick(filter, 200, 1);
            h.assertValueEqual(filter.getFillLimit(), 32_000, "fill limit at 200 AP is one second of output");
            h.assertValueEqual(filter.getWaterAmount(), 17_600, "water after one tick at 200 AP");
            h.assertTrue(active(h), "active again under a strong beam");
            tick(filter, 200, 20);
            h.assertValueEqual(filter.getWaterAmount(), 32_000, "water stops at the raised fill limit");
            h.assertFalse(active(h), "idle at the raised fill limit");
            tick(filter, 0, 20);
            h.assertValueEqual(filter.getFillLimit(), 16_000, "fill limit falls back without the beam");
            h.assertValueEqual(filter.getWaterAmount(), 32_000, "losing the beam keeps the stored water");
            h.assertFalse(active(h), "idle above the fill limit");
            tick(filter, 400, 1);
            h.assertValueEqual(filter.getFillLimit(), 1_024_000, "fill limit at 400 AP");
            h.assertValueEqual(filter.getWaterAmount(), 83_200, "water after one tick at 400 AP");
        }));

        r.add("osmosis_filter/capability_extracts_and_refuses_insert", 60, helper -> withDefaults(helper, h -> {
            OsmosisFilterBlockEntity filter = placeFilter(h);
            tick(filter, 0, 100);
            BlockPos abs = h.absolutePos(FILTER);
            FluidResource water = FluidResource.of(Fluids.WATER);
            Direction[] faces = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
            for (Direction face : faces) {
                ResourceHandler<FluidResource> handler = h.getLevel().getCapability(Capabilities.Fluid.BLOCK, abs, face);
                h.assertTrue(handler != null, "fluid handler on " + face);
                int before = filter.getWaterAmount();
                try (Transaction tx = Transaction.openRoot()) {
                    h.assertValueEqual(handler.insert(0, water, 1_000, tx), 0, "insert refused on " + face);
                    h.assertValueEqual(handler.extract(0, water, 500, tx), 500, "extract on " + face);
                    tx.commit();
                }
                h.assertValueEqual(filter.getWaterAmount(), before - 500, "water after extracting on " + face);
            }
            ResourceHandler<FluidResource> unsided = h.getLevel().getCapability(Capabilities.Fluid.BLOCK, abs, null);
            h.assertTrue(unsided != null, "unsided fluid handler");
            int before = filter.getWaterAmount();
            try (Transaction tx = Transaction.openRoot()) {
                h.assertValueEqual(unsided.insert(0, water, 1_000, tx), 0, "insert refused without a side");
                tx.commit();
            }
            h.assertValueEqual(filter.getWaterAmount(), before, "insert left the tank unchanged");
            h.assertValueEqual(filter.getFluidTank().fill(new FluidStack(Fluids.LAVA, 1_000)), 0, "lava refused");
        }));
    }
}
