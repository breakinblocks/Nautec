package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.blockentities.BacterialAnalyzerBlockEntity;
import com.breakinblocks.nautec.content.menus.BacterialAnalyzerMenu;
import com.breakinblocks.nautec.content.recipes.utils.RecipeUtils;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import java.util.List;
import java.util.Map;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.utils.RecipeRevision;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blockentities.FishingStationBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.menus.IncubatorMenu;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.transaction.Transaction;


public final class ReleaseRegressionTests {
    public static void register(NTTestRegistrar r) {
        r.add("release/bacteria_copy_isolation", 40, helper -> {
            ItemStack original = new ItemStack(NTItems.PETRI_DISH.get());
            BacteriaInstance colony = BacteriaInstance.withMaxStats(NTBacterias.CYANOBACTERIA, helper.getLevel().registryAccess());
            colony.setAnalyzed(false);
            original.getCapability(NTCapabilities.BacteriaStorage.ITEM).setBacteria(0, colony);
            ItemStack copy = original.copy();
            BacteriaInstance analyzed = copy.getCapability(NTCapabilities.BacteriaStorage.ITEM).getBacteria(0);
            analyzed.setAnalyzed(true);
            copy.getCapability(NTCapabilities.BacteriaStorage.ITEM).setBacteria(0, analyzed);
            helper.assertTrue(copy.getCapability(NTCapabilities.BacteriaStorage.ITEM).getBacteria(0).isAnalyzed(), "Copy must accept its own analyzed state");
            helper.assertTrue(!original.getCapability(NTCapabilities.BacteriaStorage.ITEM).getBacteria(0).isAnalyzed(), "Analyzing copied dish also changed the original dish");
            helper.succeed();
        });
        r.add("release/force_insert_notifies", 40, helper -> {
            int[] changed = {0};
            FishingStationBlockEntity be = new FishingStationBlockEntity(BlockPos.ZERO, NTBlocks.FISHING_STATION.get().defaultBlockState()) {
                @Override public void update() { changed[0]++; }
            };
            be.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.COD, 1));
            changed[0] = 0;
            be.forceInsertItem(0, new ItemStack(Items.COD, 1), false);
            helper.assertValueEqual(2, be.getItemStackHandler().getStackInSlot(0).getCount(), "merged count");
            helper.assertTrue(changed[0] > 0, "Merging into existing output never called update/save notification");
            helper.succeed();
        });
        r.add("release/energy_insert_marks_dirty", 40, helper -> {
            int[] changed = {0};
            EnergyConverterBlockEntity be = new EnergyConverterBlockEntity(BlockPos.ZERO, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState()) {
                @Override public void setChanged() { changed[0]++; }
            };
            changed[0] = 0;
            try (Transaction tx = Transaction.openRoot()) {
                be.getFeBuffer().insert(2500, tx);
                tx.commit();
            }
            helper.assertTrue(changed[0] > 0, "Committed FE insertion never marked the block entity dirty");
            helper.succeed();
        });
        r.add("release/submarine_pickup_preserves_cooldown", 40, helper -> {
            SubmarineEntity source = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            SubmarineEntity loaded = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            source.getModules().startCooldown(0, 600, 100);
            loaded.applyStack(source.toStack());
            helper.assertTrue(loaded.getModules().remainingCooldown(0) > 0, "Picking up and redeploying the submarine clears the cooldown");
            helper.succeed();
        });
        r.add("release/incubator_shift_click_reaches_machine", 40, helper -> {
            BlockPos pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, NTBlocks.INCUBATOR.get().defaultBlockState());
            IncubatorBlockEntity machine = helper.getBlockEntity(pos, IncubatorBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getInventory().setItem(10, new ItemStack(Items.SUGAR));
            IncubatorMenu menu = new IncubatorMenu(1, player.getInventory(), machine);
            menu.quickMoveStack(player, 1);
            helper.assertTrue(machine.getItemStackHandler().getStackInSlot(0).is(Items.SUGAR), "Shift click moved sugar within player inventory instead of into incubator");
            helper.succeed();
        });
        r.add("release/fishing_partial_stack_conserves_count", 40, helper -> {
            FishingStationBlockEntity be = new FishingStationBlockEntity(BlockPos.ZERO, NTBlocks.FISHING_STATION.get().defaultBlockState());
            be.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.COD, 63));
            ItemStack remainder = be.storeCatch(new ItemStack(Items.COD, 5));
            int total = remainder.getCount();
            for (int slot = 0; slot < 15; slot++) total += be.getItemStackHandler().getStackInSlot(slot).getCount();
            helper.assertValueEqual(68, total, "Catch must be conserved across partial and empty slots");
            helper.succeed();
        });
        r.add("release/energy_rollback_does_not_notify", 40, helper -> {
            int[] changed = {0};
            EnergyConverterBlockEntity be = new EnergyConverterBlockEntity(BlockPos.ZERO, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState()) {
                @Override public void setChanged() { changed[0]++; }
            };
            changed[0] = 0;
            try (Transaction tx = Transaction.openRoot()) {
                be.getFeBuffer().insert(2500, tx);
            }
            helper.assertValueEqual(0, changed[0], "Rolled back energy must not notify");
            helper.assertValueEqual(0, be.getFeBuffer().getAmountAsInt(), "Rolled back energy must not persist");
            helper.succeed();
        });
        r.add("release/machine_first_menu_transfers_both_directions", 40, helper -> {
            BlockPos pos = new BlockPos(1, 1, 1);
            helper.setBlock(pos, NTBlocks.BACTERIAL_ANALYZER.get().defaultBlockState());
            BacterialAnalyzerBlockEntity machine = helper.getBlockEntity(pos, BacterialAnalyzerBlockEntity.class);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getInventory().setItem(10, DishPortTests.dish(DishPortTests.colony(helper, NTBacterias.LITHOPHILES, 100)));
            BacterialAnalyzerMenu menu = new BacterialAnalyzerMenu(1, player.getInventory(), machine);
            menu.quickMoveStack(player, 3);
            helper.assertTrue(machine.getItemStackHandler().getStackInSlot(0).is(NTItems.PETRI_DISH.get()), "Player to machine transfer");
            menu.quickMoveStack(player, 0);
            helper.assertTrue(machine.getItemStackHandler().getStackInSlot(0).isEmpty(), "Machine to player transfer");
            helper.succeed();
        });
        r.add("release/mixing_plan_backtracks_across_noncontiguous_slots", 40, helper -> {
            List<ItemStack> inputs = List.of(new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.DIRT));
            List<IngredientWithCount> ingredients = List.of(
                    new IngredientWithCount(Ingredient.of(Items.IRON_INGOT, Items.GOLD_INGOT), 1),
                    new IngredientWithCount(Ingredient.of(Items.IRON_INGOT), 1));
            int[] plan = RecipeUtils.consumptionPlan(inputs, ingredients);
            helper.assertTrue(plan != null, "Valid overlapping recipe was rejected");
            helper.assertValueEqual(2, plan[0], "Broad ingredient must consume gold");
            helper.assertValueEqual(0, plan[1], "Specific ingredient must consume iron");
            helper.succeed();
        });
        r.add("release/pressure_requires_source_water", 40, helper -> {
            int oldDepth = NTConfig.pressureForgeDepth;
            int oldColumn = NTConfig.pressureForgeWaterColumn;
            BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
            try {
                NTConfig.pressureForgeDepth = pos.getY();
                NTConfig.pressureForgeWaterColumn = 1;
                helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.LAVA.defaultBlockState());
                helper.assertTrue(!PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos), "Lava qualifies as pressure");
                helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3));
                helper.assertTrue(!PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos), "Flowing water qualifies as pressure");
                helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.WATER.defaultBlockState());
                helper.assertTrue(PressureForgeBlockEntity.hasPressure(helper.getLevel(), pos), "Source water rejected");
            } finally {
                NTConfig.pressureForgeDepth = oldDepth;
                NTConfig.pressureForgeWaterColumn = oldColumn;
            }
            helper.succeed();
        });
        r.add("release/submarine_cooldown_survives_item_codec", 40, helper -> {
            SubmarineEntity source = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            SubmarineEntity loaded = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            source.getModules().startCooldown(0, 600, 100);
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
            Tag saved = ItemStack.CODEC.encodeStart(ops, source.toStack()).getOrThrow();
            loaded.applyStack(ItemStack.CODEC.parse(ops, saved).getOrThrow());
            helper.assertValueEqual(600, loaded.getModules().remainingCooldown(0), "Serialized cooldown");
            helper.succeed();
        });
        r.add("release/recipe_revision_detects_replacement_once", 40, helper -> {
            RecipeRevision revision = new RecipeRevision();
            RecipeMap first = RecipeMap.create(List.of());
            RecipeMap reloaded = RecipeMap.create(List.of());
            helper.assertTrue(!revision.changed(first), "Initial load must preserve saved progress");
            helper.assertTrue(!revision.changed(first), "Unchanged recipes must preserve progress");
            helper.assertTrue(revision.changed(reloaded), "Reload must invalidate cached recipes");
            helper.assertTrue(!revision.changed(reloaded), "Reload must invalidate only once");
            helper.succeed();
        });
        r.add("release/spotlight_ownership_codec_preserves_originals", 40, helper -> {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
            Map<BlockPos, BlockState> originals = Map.of(new BlockPos(-30, -59, 17), Blocks.WATER.defaultBlockState());
            Tag encoded = NTDataAttachments.SPOTLIGHT_CODEC.encodeStart(ops, originals).getOrThrow();
            helper.assertValueEqual(originals, NTDataAttachments.SPOTLIGHT_CODEC.parse(ops, encoded).getOrThrow(), "Saved light ownership");
            helper.succeed();
        });
        r.add("release/submarine_module_entity_state_roundtrip", 40, helper -> {
            SubmarineEntity source = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            SubmarineEntity loaded = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            source.getModules().startCooldown(2, 600, 100);
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
            source.getModules().save(output);
            loaded.getModules().load(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
            helper.assertValueEqual(source.getModules().snapshot(), loaded.getModules().snapshot(), "Module entity state");
            helper.succeed();
        });
        r.add("release/active_duration_keeps_aging_after_cooldown", 40, helper -> {
            SubmarineEntity submarine = NTEntities.SUBMARINE.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
            long started = helper.getLevel().getGameTime();
            submarine.getModules().startCooldown(0, 1, 10);
            helper.runAfterDelay(3, () -> {
                int expected = (int) Math.max(0, 10 - (helper.getLevel().getGameTime() - started));
                helper.assertValueEqual(expected, submarine.getModules().snapshot().active().get(0), "Active time after cooldown expiry");
                helper.succeed();
            });
        });
    }

}
