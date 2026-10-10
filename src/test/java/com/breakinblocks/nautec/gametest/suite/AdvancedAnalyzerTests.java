package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.AdvancedBacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public final class AdvancedAnalyzerTests {
    private static final BlockPos ANALYZER = new BlockPos(4, 1, 4);

    private AdvancedAnalyzerTests() {
    }

    private static ItemStack dish(NTGameTestHelper helper, boolean analyzed) {
        ItemStack stack = new ItemStack(NTItems.PETRI_DISH.get());
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        BacteriaInstance colony = BacteriaInstance.roll(NTBacterias.LITHOPHILES, helper.getLevel().registryAccess());
        colony.setAnalyzed(analyzed);
        storage.setBacteria(0, colony);
        return stack;
    }

    private static boolean analyzed(ItemStack stack) {
        IBacteriaStorage storage = stack.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        return storage != null && storage.getBacteria(0).isAnalyzed();
    }

    private static AdvancedBacterialAnalyzerBlockEntity filled(NTGameTestHelper helper) {
        helper.setBlock(ANALYZER, NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
        AdvancedBacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(ANALYZER, AdvancedBacterialAnalyzerBlockEntity.class);
        for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
            analyzer.getItemStackHandler().setStackInSlot(i, dish(helper, false));
        }
        return analyzer;
    }

    private static void run(NTGameTestHelper helper, AdvancedBacterialAnalyzerBlockEntity analyzer, int power, float purity, int ticks) {
        BlockPos origin = helper.absolutePos(ANALYZER.above());
        for (int tick = 0; tick < ticks; tick++) {
            analyzer.receivePower(power, Direction.UP, origin);
            analyzer.receiveNewPurity(purity, Direction.UP, origin);
            analyzer.commonTick();
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("advanced_analyzer/analyzes_nine_at_once", 20, helper -> {
            AdvancedBacterialAnalyzerBlockEntity analyzer = filled(helper);
            run(helper, analyzer, NTConfig.advancedAnalyzerPowerUsage, 3.0f, NTConfig.advancedAnalyzerCraftingSpeed + 2);
            for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(i).isEmpty(), "Input " + i + " should be done in one cycle");
                ItemStack out = analyzer.getItemStackHandler().getStackInSlot(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + i);
                helper.assertTrue(out.is(NTItems.PETRI_DISH.get()) && analyzed(out), "Output " + i + " should hold an analyzed dish");
            }
            helper.succeed();
        });

        r.add("advanced_analyzer/needs_power_and_purity", 20, helper -> {
            AdvancedBacterialAnalyzerBlockEntity analyzer = filled(helper);
            run(helper, analyzer, NTConfig.advancedAnalyzerPowerUsage - 1, 3.0f, NTConfig.advancedAnalyzerCraftingSpeed + 2);
            helper.assertValueEqual(AdvancedBacterialAnalyzerBlockEntity.STATUS_LOW_POWER, analyzer.getStatus(), "status with a weak beam");
            run(helper, analyzer, NTConfig.advancedAnalyzerPowerUsage, (float) NTConfig.advancedAnalyzerPurity - 0.1f, NTConfig.advancedAnalyzerCraftingSpeed + 2);
            helper.assertValueEqual(AdvancedBacterialAnalyzerBlockEntity.STATUS_LOW_PURITY, analyzer.getStatus(), "status with an impure beam");
            for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
                helper.assertTrue(analyzer.getItemStackHandler().getStackInSlot(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + i).isEmpty(),
                        "Nothing should be analyzed without enough power and purity");
            }
            helper.succeed();
        });

        r.add("advanced_analyzer/waits_for_output_room_and_rejects_finished_dishes", 20, helper -> {
            helper.setBlock(ANALYZER, NTBlocks.ADVANCED_BACTERIAL_ANALYZER.get());
            AdvancedBacterialAnalyzerBlockEntity analyzer = helper.getBlockEntity(ANALYZER, AdvancedBacterialAnalyzerBlockEntity.class);
            var items = analyzer.getItemStackHandler();
            helper.assertFalse(items.isItemValid(0, dish(helper, true)), "An analyzed dish should not go in");
            helper.assertFalse(items.isItemValid(0, new ItemStack(NTItems.PETRI_DISH.get())), "An empty dish should not go in");
            helper.assertTrue(items.isItemValid(0, dish(helper, false)), "An unanalyzed dish fits");
            helper.assertFalse(items.isItemValid(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT, dish(helper, false)), "Nothing goes into the outputs");

            for (int i = 0; i < AdvancedBacterialAnalyzerBlockEntity.DISHES; i++) {
                items.setStackInSlot(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + i, dish(helper, true));
            }
            items.setStackInSlot(0, dish(helper, false));
            run(helper, analyzer, NTConfig.advancedAnalyzerPowerUsage, 3.0f, NTConfig.advancedAnalyzerCraftingSpeed + 2);
            helper.assertValueEqual(AdvancedBacterialAnalyzerBlockEntity.STATUS_OUTPUT_FULL, analyzer.getStatus(), "status with full outputs");
            helper.assertFalse(items.getStackInSlot(0).isEmpty(), "The dish should wait for room");
            items.setStackInSlot(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + 4, ItemStack.EMPTY);
            run(helper, analyzer, NTConfig.advancedAnalyzerPowerUsage, 3.0f, 1);
            helper.assertTrue(items.getStackInSlot(0).isEmpty(), "The dish should move out once a slot frees up");
            helper.assertTrue(analyzed(items.getStackInSlot(AdvancedBacterialAnalyzerBlockEntity.FIRST_OUTPUT + 4)), "into the free slot");
            helper.succeed();
        });
    }
}
