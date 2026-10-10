package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.FishingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public final class DistributorTests {
    private static final BlockPos DISTRIBUTOR = new BlockPos(4, 1, 4);
    private static final BlockPos CHEST = new BlockPos(4, 1, 5);

    private DistributorTests() {
    }

    private static DistributorBlockEntity distributor(NTGameTestHelper helper) {
        helper.setBlock(DISTRIBUTOR, NTBlocks.DISTRIBUTOR.get());
        helper.setBlock(CHEST, Blocks.CHEST.defaultBlockState());
        return helper.getBlockEntity(DISTRIBUTOR, DistributorBlockEntity.class);
    }

    private static Container chest(NTGameTestHelper helper) {
        return helper.getBlockEntity(CHEST, ChestBlockEntity.class);
    }

    private static int count(Container container, Item item) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).is(item)) {
                total += container.getItem(i).getCount();
            }
        }
        return total;
    }

    private static void link(NTGameTestHelper helper, DistributorBlockEntity distributor, BlockPos relative, Direction face) {
        helper.assertValueEqual(distributor.toggle(helper.absolutePos(relative), face), DistributorBlockEntity.LinkResult.LINKED, "link " + relative);
    }

    public static void register(NTTestRegistrar r) {
        r.add("distributor/collects_outputs_into_a_neighbour", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos forgePos = new BlockPos(1, 1, 1);
            helper.setBlock(forgePos, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(forgePos, PressureForgeBlockEntity.class);
            forge.getItemStackHandler().setStackInSlot(1, new ItemStack(NTItems.DEEP_STEEL_PLATING.get(), 5));
            forge.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 3));
            link(helper, distributor, forgePos, Direction.UP);
            distributor.cycle(helper.getLevel());
            helper.assertValueEqual(count(chest(helper), NTItems.DEEP_STEEL_PLATING.get()), 5, "outputs collected into the chest");
            helper.assertValueEqual(forge.getItemStackHandler().getStackInSlot(0).getCount(), 3, "inputs are never collected");
            helper.succeed();
        });

        r.add("distributor/keeps_ghost_inputs_stocked", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos forgePos = new BlockPos(1, 1, 1);
            helper.setBlock(forgePos, NTBlocks.PRESSURE_FORGE.get());
            PressureForgeBlockEntity forge = helper.getBlockEntity(forgePos, PressureForgeBlockEntity.class);
            helper.assertTrue(forge.setGhost(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get())), "an input slot takes a ghost");
            chest(helper).setItem(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get(), 40));
            chest(helper).setItem(1, new ItemStack(Items.DIRT, 10));
            link(helper, distributor, forgePos, Direction.NORTH);
            distributor.cycle(helper.getLevel());
            helper.assertValueEqual(forge.getItemStackHandler().getStackInSlot(0).getCount(), 40, "the ghost slot fills from the chest");
            helper.assertValueEqual(count(chest(helper), Items.DIRT), 10, "nothing else is taken");
            helper.succeed();
        });

        r.add("distributor/analyzer_ghost_takes_any_dish", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos analyzerPos = new BlockPos(1, 1, 1);
            helper.setBlock(analyzerPos, NTBlocks.BACTERIAL_ANALYZER.get());
            BacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(analyzerPos, BacterialAnalyzerBlockEntity.class);
            BacteriaInstance analyzed = DishPortTests.colony(helper, NTBacterias.LITHOPHILES, 100);
            analyzed.setAnalyzed(true);
            helper.assertTrue(analyzer.setGhost(0, DishPortTests.dish(analyzed)), "an analyzed dish sets a ghost");
            helper.assertTrue(analyzer.setGhost(0, new ItemStack(NTItems.PETRI_DISH.get())), "an empty dish sets a ghost");
            helper.assertFalse(analyzer.setGhost(0, new ItemStack(Items.DIRT)), "a non-dish is refused");
            chest(helper).setItem(0, DishPortTests.dish(analyzed));
            chest(helper).setItem(1, DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.CALCIOPHILES, 100)));
            link(helper, distributor, analyzerPos, Direction.NORTH);
            distributor.cycle(helper.getLevel());
            ItemStack loaded = analyzer.getItemStackHandler().getStackInSlot(0);
            helper.assertTrue(DishPort.colonyOf(loaded).is(NTBacterias.CALCIOPHILES), "the colony that needs analysis is loaded");
            helper.assertFalse(chest(helper).getItem(0).isEmpty(), "the analyzed dish stays in the chest");
            BlockPos incubatorPos = new BlockPos(7, 1, 1);
            helper.setBlock(incubatorPos, NTBlocks.INCUBATOR.get());
            IncubatorBlockEntity incubator = helper.getBlockEntity(incubatorPos, IncubatorBlockEntity.class);
            helper.assertTrue(incubator.setGhost(IncubatorBlockEntity.DISH_IN, DishPortTests.dish(analyzed)), "a colony dish sets a ghost on any dish slot");
            helper.succeed();
        });

        r.add("distributor/feeds_one_machine_from_another", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos fishingPos = new BlockPos(1, 1, 1);
            BlockPos forgePos = new BlockPos(7, 1, 1);
            helper.setBlock(fishingPos, NTBlocks.FISHING_STATION.get());
            helper.setBlock(forgePos, NTBlocks.PRESSURE_FORGE.get());
            FishingStationBlockEntity fishing = helper.getBlockEntity(fishingPos, FishingStationBlockEntity.class);
            PressureForgeBlockEntity forge = helper.getBlockEntity(forgePos, PressureForgeBlockEntity.class);
            fishing.getItemStackHandler().setStackInSlot(3, new ItemStack(Items.COD, 12));
            forge.setGhost(0, new ItemStack(Items.COD));
            link(helper, distributor, fishingPos, Direction.UP);
            link(helper, distributor, forgePos, Direction.UP);
            distributor.cycle(helper.getLevel());
            helper.assertValueEqual(forge.getItemStackHandler().getStackInSlot(0).getCount(), 12, "one machine's output feeds another's ghost input");
            helper.assertValueEqual(count(chest(helper), Items.COD), 0, "nothing was left over to export");
            helper.succeed();
        });

        r.add("distributor/requests_stock_any_block_without_churn", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos barrelPos = new BlockPos(1, 1, 1);
            helper.setBlock(barrelPos, Blocks.BARREL.defaultBlockState());
            chest(helper).setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            link(helper, distributor, barrelPos, Direction.UP);
            DistributorLink link = distributor.link(0);
            link.setItem(0, new ItemStack(Items.COBBLESTONE, 16));
            distributor.cycle(helper.getLevel());
            distributor.cycle(helper.getLevel());
            BarrelBlockEntity barrel = helper.getBlockEntity(barrelPos, BarrelBlockEntity.class);
            helper.assertValueEqual(count(barrel, Items.COBBLESTONE), 16, "the barrel is kept at its requested amount");
            helper.assertValueEqual(count(chest(helper), Items.COBBLESTONE), 48, "the rest stays in the chest");
            barrel.setItem(5, new ItemStack(Items.FLINT, 3));
            distributor.cycle(helper.getLevel());
            helper.assertValueEqual(count(chest(helper), Items.FLINT), 3, "anything else the block holds is collected");
            helper.assertValueEqual(count(barrel, Items.COBBLESTONE), 16, "the requested stock is not collected back out");
            helper.succeed();
        });

        r.add("distributor/links_follow_range_and_toggle", 20, helper -> {
            DistributorBlockEntity distributor = distributor(helper);
            BlockPos origin = helper.absolutePos(DISTRIBUTOR);
            helper.assertValueEqual(distributor.toggle(origin, Direction.UP), DistributorBlockEntity.LinkResult.SELF, "cannot link to itself");
            helper.assertValueEqual(distributor.toggle(origin.east(NTConfig.distributorRange + 5), Direction.UP),
                    DistributorBlockEntity.LinkResult.TOO_FAR, "out of range");
            helper.assertValueEqual(distributor.toggle(origin.east(200), Direction.UP), DistributorBlockEntity.LinkResult.LINKED, "200 blocks away links");
            helper.assertValueEqual(distributor.toggle(origin.east(200), Direction.DOWN), DistributorBlockEntity.LinkResult.UNLINKED, "linking again unlinks");
            helper.assertTrue(distributor.getLinks().isEmpty(), "no links left");
            helper.succeed();
        });

        r.add("distributor/ghost_inputs_lock_the_slot_and_persist", 20, helper -> {
            BlockPos mixerPos = new BlockPos(1, 1, 1);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get());
            MixerBlockEntity mixer = helper.getBlockEntity(mixerPos, MixerBlockEntity.class);
            helper.assertFalse(mixer.setGhost(MixerBlockEntity.OUTPUT_SLOT, new ItemStack(Items.SAND)), "outputs cannot be ghosted");
            helper.assertTrue(mixer.setGhost(0, new ItemStack(Items.SAND)), "inputs can");
            helper.assertFalse(mixer.getItemStackHandler().isItemValid(0, new ItemStack(Items.DIRT)), "the ghost slot refuses other items");
            helper.assertTrue(mixer.getItemStackHandler().isItemValid(0, new ItemStack(Items.SAND)), "and takes its own");
            CompoundTag saved = mixer.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(mixerPos), mixer.getBlockState(), saved, helper.getLevel().registryAccess());
            helper.assertTrue(((ContainerBlockEntity) loaded).getGhost(0).is(Items.SAND), "the ghost survives a reload");
            helper.assertTrue(mixer.setGhost(0, ItemStack.EMPTY), "clearing works");
            helper.assertTrue(mixer.getItemStackHandler().isItemValid(0, new ItemStack(Items.DIRT)), "a cleared slot takes anything again");
            helper.succeed();
        });
    }
}
