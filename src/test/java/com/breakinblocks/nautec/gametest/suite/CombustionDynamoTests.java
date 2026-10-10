package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blocks.generators.CombustionDynamoBlock;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

public final class CombustionDynamoTests {
    private static final BlockPos POS = new BlockPos(4, 2, 4);

    private CombustionDynamoTests() {
    }

    private static CombustionDynamoBlockEntity dynamo(NTGameTestHelper helper) {
        helper.setBlock(POS, NTBlocks.COMBUSTION_DYNAMO.get());
        return helper.getBlockEntity(POS, CombustionDynamoBlockEntity.class);
    }

    private static void fuel(CombustionDynamoBlockEntity dynamo, int oil, int water) {
        dynamo.getFluidTank().fill(new FluidStack(NTFluids.OIL.getStillFluid(), oil));
        dynamo.getSecondaryFluidTank().fill(new FluidStack(Fluids.WATER, water));
    }

    private static void run(CombustionDynamoBlockEntity dynamo, int ticks) {
        for (int i = 0; i < ticks; i++) {
            dynamo.commonTick();
        }
    }

    private static int insert(ResourceHandler<FluidResource> handler, FluidStack stack) {
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(stack), stack.getAmount(), tx);
            tx.commit();
            return inserted;
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("combustion_dynamo/burns_oil_and_water_into_fe", 20, helper -> {
            CombustionDynamoBlockEntity dynamo = dynamo(helper);
            fuel(dynamo, 1000, 1000);
            run(dynamo, 100);
            helper.assertValueEqual(dynamo.getStatus(), CombustionDynamoBlockEntity.Status.RUNNING, "status");
            helper.assertValueEqual(dynamo.getRate(), 320, "base output per tick");
            helper.assertValueEqual(dynamo.getEnergyStorage().getAmountAsInt(), 32_000, "FE after 100 ticks");
            helper.assertValueEqual(dynamo.getFluidTank().getFluidAmount(), 990, "oil left: 1 mB per 10 ticks");
            helper.assertValueEqual(dynamo.getSecondaryFluidTank().getFluidAmount(), 900, "water left: 1 mB per tick");
            helper.assertTrue(helper.getBlockState(POS).getValue(CombustionDynamoBlock.LIT), "it lights up while burning");
            helper.succeed();
        });

        r.add("combustion_dynamo/redstone_boosts_output_and_fuel_use", 20, helper -> {
            CombustionDynamoBlockEntity dynamo = dynamo(helper);
            fuel(dynamo, 1000, 1000);
            dynamo.getItemStackHandler().setStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT, new ItemStack(Items.REDSTONE, 2));
            run(dynamo, 100);
            helper.assertValueEqual(dynamo.getRate(), 480, "boosted output per tick");
            helper.assertValueEqual(dynamo.getEnergyStorage().getAmountAsInt(), 48_000, "FE after 100 boosted ticks");
            helper.assertValueEqual(dynamo.getFluidTank().getFluidAmount(), 989, "oil left: 10% more than unboosted");
            helper.assertValueEqual(dynamo.getSecondaryFluidTank().getFluidAmount(), 890, "water left: 10% more than unboosted");
            helper.assertValueEqual(dynamo.getItemStackHandler().getStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT).getCount(), 1,
                    "one redstone was used");
            helper.assertValueEqual(dynamo.getAdditiveLeft(), 1100, "boost ticks left");
            helper.succeed();
        });

        r.add("combustion_dynamo/one_redstone_lasts_a_minute_of_burning", 20, helper -> {
            CombustionDynamoBlockEntity dynamo = dynamo(helper);
            fuel(dynamo, 8000, 8000);
            dynamo.getItemStackHandler().setStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT, new ItemStack(Items.REDSTONE, 1));
            for (int i = 0; i < 1200; i++) {
                dynamo.commonTick();
                dynamo.getEnergyStorage().set(0);
            }
            helper.assertValueEqual(dynamo.getAdditiveLeft(), 0, "the boost runs out after 1200 burning ticks");
            helper.assertTrue(dynamo.getItemStackHandler().getStackInSlot(CombustionDynamoBlockEntity.ADDITIVE_SLOT).isEmpty(), "the redstone is used up");
            dynamo.commonTick();
            helper.assertValueEqual(dynamo.getRate(), 320, "back to base output");
            helper.succeed();
        });

        r.add("combustion_dynamo/tanks_only_take_their_own_fluid", 20, helper -> {
            CombustionDynamoBlockEntity dynamo = dynamo(helper);
            helper.assertValueEqual(dynamo.getFluidTank().fill(new FluidStack(Fluids.WATER, 1000)), 0, "water refused by the oil tank");
            helper.assertValueEqual(dynamo.getSecondaryFluidTank().fill(new FluidStack(NTFluids.OIL.getStillFluid(), 1000)), 0,
                    "oil refused by the water tank");
            ResourceHandler<FluidResource> side = dynamo.getFluidHandlerOnSide(Direction.UP);
            helper.assertValueEqual(insert(side, new FluidStack(Fluids.WATER, 1000)), 1000, "water piped in from a side");
            helper.assertValueEqual(insert(side, new FluidStack(NTFluids.OIL.getStillFluid(), 1000)), 1000, "oil piped in from a side");
            helper.assertValueEqual(insert(side, new FluidStack(Fluids.LAVA, 1000)), 0, "lava refused");
            helper.assertValueEqual(dynamo.getFluidTank().getFluidAmount(), 1000, "oil landed in the oil tank");
            helper.assertValueEqual(dynamo.getSecondaryFluidTank().getFluidAmount(), 1000, "water landed in the water tank");
            helper.succeed();
        });

        r.add("combustion_dynamo/needs_both_oil_and_water", 20, helper -> {
            CombustionDynamoBlockEntity dynamo = dynamo(helper);
            fuel(dynamo, 1000, 0);
            run(dynamo, 20);
            helper.assertValueEqual(dynamo.getStatus(), CombustionDynamoBlockEntity.Status.NO_WATER, "status without water");
            helper.assertValueEqual(dynamo.getEnergyStorage().getAmountAsInt(), 0, "no FE without water");
            helper.assertValueEqual(dynamo.getFluidTank().getFluidAmount(), 1000, "no oil burned without water");
            dynamo.getFluidTank().drain(1000);
            fuel(dynamo, 0, 1000);
            run(dynamo, 20);
            helper.assertValueEqual(dynamo.getStatus(), CombustionDynamoBlockEntity.Status.NO_OIL, "status without oil");
            helper.assertValueEqual(dynamo.getSecondaryFluidTank().getFluidAmount(), 1000, "no water used without oil");
            helper.succeed();
        });

        r.add("combustion_dynamo/oil_chain_is_complete", 20, helper -> {
            helper.assertTrue(NTFluids.OIL.getStillFluid().defaultFluidState().is(NTTags.Fluids.OIL), "NauTec oil is in c:oil");
            var recipes = helper.getLevel().getRecipeManager();
            for (String id : new String[]{"kelp_slurry_mixing", "oil_from_algal_lipid_mixing", "combustion_additive/redstone"}) {
                helper.assertTrue(recipes.byKey(Nautec.rl(id)).isPresent(), "recipe " + id);
            }
            Bacteria lipophiles = helper.getLevel().registryAccess().registryOrThrow(NTRegistries.BACTERIA_KEY).getOrThrow(NTBacterias.LIPOPHILES);
            helper.assertTrue(lipophiles.resource().resolve() == NTItems.ALGAL_LIPID.get(), "Lipophiles produce Algal Lipid");
            helper.succeed();
        });
    }
}
