package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class DishPortTests {
    private static final BlockPos MACHINE = new BlockPos(4, 1, 4);

    private DishPortTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("dish_port/incubator_loads_and_unloads_at_cap", 60, helper -> {
            helper.setBlock(MACHINE, NTBlocks.INCUBATOR.get());
            IncubatorBlockEntity incubator = helper.getBlockEntity(MACHINE, IncubatorBlockEntity.class);
            BacteriaInstance colony = colony(helper, NTBacterias.LITHOPHILES, 100);
            incubator.getItemStackHandler().setStackInSlot(IncubatorBlockEntity.DISH_IN, dish(colony));

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(100L, incubator.getBacteriaStorage().getBacteria(0).getSize(), "colony loaded from the dish");
                ItemStack emptied = incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_OUT);
                helper.assertTrue(emptied.is(NTItems.PETRI_DISH.get()) && contents(emptied).isEmpty(), "The emptied dish should come out");

                incubator.getItemStackHandler().setStackInSlot(IncubatorBlockEntity.DISH_OUT, ItemStack.EMPTY);
                incubator.getItemStackHandler().setStackInSlot(IncubatorBlockEntity.DISH_IN, new ItemStack(NTItems.PETRI_DISH.get()));
            });
            helper.runAfterDelay(24, () -> {
                helper.assertFalse(incubator.getBacteriaStorage().getBacteria(0).isEmpty(), "A growing colony should stay put");
                helper.assertTrue(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_OUT).isEmpty(), "Nothing should come out yet");

                BacteriaInstance grown = incubator.getBacteriaStorage().getBacteria(0).copyWithSize(NTConfig.bacteriaColonySizeCap);
                incubator.getBacteriaStorage().setBacteria(0, grown);
            });
            helper.runAfterDelay(36, () -> {
                helper.assertTrue(incubator.getBacteriaStorage().getBacteria(0).isEmpty(), "A colony at the cap should be taken out");
                BacteriaInstance out = contents(incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTConfig.bacteriaColonySizeCap, out.getSize(), "size of the unloaded colony");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_unloads_only_the_result", 40, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 50));
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_IN, new ItemStack(NTItems.PETRI_DISH.get()));

            helper.runAfterDelay(12, () -> {
                helper.assertFalse(mutator.getBacteriaStorage().getBacteria(0).isEmpty(), "The input colony should never be pulled out");
                helper.assertTrue(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT).isEmpty(), "No result yet");
                mutator.getBacteriaStorage().setBacteria(1, colony(helper, NTBacterias.CALCIOPHILES, 30));
            });
            helper.runAfterDelay(24, () -> {
                helper.assertTrue(mutator.getBacteriaStorage().getBacteria(1).isEmpty(), "The result should be taken out");
                BacteriaInstance out = contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_OUT));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "unloaded strain");
                helper.succeed();
            });
        });

        r.add("dish_port/mutator_waits_when_input_is_taken", 30, helper -> {
            helper.setBlock(MACHINE, NTBlocks.MUTATOR.get());
            MutatorBlockEntity mutator = helper.getBlockEntity(MACHINE, MutatorBlockEntity.class);
            mutator.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 50));
            mutator.getItemStackHandler().setStackInSlot(MutatorBlockEntity.DISH_IN, dish(colony(helper, NTBacterias.HALOTROPHS, 40)));

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.LITHOPHILES, mutator.getBacteriaStorage().getBacteria(0).getBacteria(), "input strain untouched");
                helper.assertFalse(contents(mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.DISH_IN)).isEmpty(),
                        "A dish that cannot load should wait in the port");
                helper.succeed();
            });
        });

        r.add("dish_port/reactor_automation_round_trip", 60, helper -> {
            helper.setBlock(MACHINE, NTBlocks.BIO_REACTOR.get());
            BioReactorBlockEntity reactor = helper.getBlockEntity(MACHINE, BioReactorBlockEntity.class);
            reactor.getBacteriaStorage().setBacteria(0, colony(helper, NTBacterias.LITHOPHILES, 80));
            reactor.setVitality(0, 500);
            ResourceHandler<ItemResource> side = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(MACHINE), Direction.NORTH);
            helper.assertTrue(side != null, "The reactor should expose items to automation");

            ItemStack loaded = dish(colony(helper, NTBacterias.CALCIOPHILES, 60));
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(1, side.insert(reactor.dishInSlot(), ItemResource.of(loaded), 1, tx), "dish pushed into the port");
                tx.commit();
            }

            helper.runAfterDelay(12, () -> {
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, reactor.getBacteriaStorage().getBacteria(1).getBacteria(),
                        "The colony should load into the first empty slot");
                ItemStack empty = reactor.getItemStackHandler().getStackInSlot(reactor.dishOutSlot());
                helper.assertTrue(empty.is(NTItems.PETRI_DISH.get()) && contents(empty).isEmpty(), "The emptied dish should be in the output");
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(1, side.extract(reactor.dishOutSlot(), ItemResource.of(empty), 1, tx), "empty dish pulled out");
                    tx.commit();
                }
                try (Transaction tx = Transaction.openRoot()) {
                    side.insert(reactor.dishInSlot(), ItemResource.of(new ItemStack(NTItems.PETRI_DISH.get())), 1, tx);
                    tx.commit();
                }
            });
            helper.runAfterDelay(24, () -> {
                helper.assertFalse(reactor.getBacteriaStorage().getBacteria(0).isEmpty(), "The well fed colony should stay");
                helper.assertTrue(reactor.getBacteriaStorage().getBacteria(1).isEmpty(), "The unfed colony should be the one taken out");
                BacteriaInstance out = contents(reactor.getItemStackHandler().getStackInSlot(reactor.dishOutSlot()));
                helper.assertValueEqual(NTBacterias.CALCIOPHILES, out.getBacteria(), "unloaded strain");
                helper.succeed();
            });
        });
    }

    private static BacteriaInstance colony(GameTestHelper helper, ResourceKey<Bacteria> key, long size) {
        return BacteriaInstance.roll(key, helper.getLevel().registryAccess()).copyWithSize(size);
    }

    private static ItemStack dish(BacteriaInstance colony) {
        ItemStack stack = new ItemStack(NTItems.PETRI_DISH.get());
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        storage.setBacteria(0, colony);
        return stack;
    }

    private static BacteriaInstance contents(ItemStack stack) {
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        return storage == null ? BacteriaInstance.EMPTY : storage.getBacteria(0);
    }
}
