package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.showcase.ShowcaseBlockGrid;
import com.breakinblocks.nautec.content.showcase.ShowcaseFrame;
import com.breakinblocks.nautec.content.showcase.ShowcaseIronProduction;
import com.breakinblocks.nautec.content.showcase.ShowcaseItemWall;
import com.breakinblocks.nautec.content.showcase.ShowcaseLaserPower;
import com.breakinblocks.nautec.content.showcase.ShowcaseMultiblocks;
import com.breakinblocks.nautec.content.showcase.ShowcaseParts;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShowcaseTests {
    private static final Identifier ARENA = Nautec.rl("empty_19x11x19");

    private ShowcaseTests() {
    }

    private static ShowcaseFrame frame(GameTestHelper helper, int x, int z) {
        return new ShowcaseFrame(helper.absolutePos(new BlockPos(x, 0, z)), Rotation.NONE);
    }

    private static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos absolute, Class<T> type) {
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolute);
        if (!type.isInstance(blockEntity)) {
            throw helper.assertionException("Expected " + type.getSimpleName() + " at " + absolute + " but found " + blockEntity);
        }
        return type.cast(blockEntity);
    }

    private static int batteryPower(GameTestHelper helper, ChargerBlockEntity charger) {
        ItemStack battery = charger.getItemStackHandler().getStackInSlot(0);
        IPowerStorage storage = battery.getCapability(NTCapabilities.PowerStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException("Charger should hold a prismatic battery, held " + battery);
        }
        return storage.getPowerStored();
    }

    private static int countIn(ChestBlockEntity chest, Item item) {
        int count = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static Set<Item> nautecItems() {
        Set<Item> items = new LinkedHashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (Nautec.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                items.add(item);
            }
        }
        return items;
    }

    private static Set<Block> nautecBlockItemBlocks() {
        Set<Block> blocks = new LinkedHashSet<>();
        for (Item item : nautecItems()) {
            if (item instanceof BlockItem blockItem) {
                blocks.add(blockItem.getBlock());
            }
        }
        return blocks;
    }

    public static void register(NTTestRegistrar r) {
        r.add("showcase/laser_power_reaches_consumers", ARENA, 360, 0, helper -> {
            ServerLevel level = helper.getLevel();
            ShowcaseFrame frame = frame(helper, 9, 9);
            ShowcaseParts.prepareArea(level, frame, -5, -6, 5, 6, 7);
            ShowcaseLaserPower.Layout layout = ShowcaseLaserPower.build(level, frame);

            helper.assertTrue(layout.suppliedPower() >= 2 * NTConfig.mixerPower,
                    "Two catalysts should cover the mixer on a split beam, supplied " + layout.suppliedPower());

            int[] charged = new int[1];
            helper.runAfterDelay(80, () -> {
                ChargerBlockEntity charger = blockEntity(helper, layout.charger(), ChargerBlockEntity.class);
                MixerBlockEntity mixer = blockEntity(helper, layout.mixer(), MixerBlockEntity.class);
                helper.assertTrue(charger.getPower() > 0, "Charger should receive catalyst power, had " + charger.getPower());
                helper.assertTrue(mixer.getPower() >= NTConfig.mixerPower,
                        "Mixer should receive at least " + NTConfig.mixerPower + " AP, had " + mixer.getPower());
                charged[0] = batteryPower(helper, charger);
                helper.assertTrue(charged[0] > 0, "Battery should have started charging");
            });

            helper.succeedWhen(() -> {
                ChargerBlockEntity charger = blockEntity(helper, layout.charger(), ChargerBlockEntity.class);
                MixerBlockEntity mixer = blockEntity(helper, layout.mixer(), MixerBlockEntity.class);
                helper.assertTrue(charged[0] > 0 && batteryPower(helper, charger) > charged[0],
                        "Battery should keep charging from real catalyst power");
                ItemStack output = mixer.getItemStackHandler().getStackInSlot(MixerBlockEntity.OUTPUT_SLOT);
                helper.assertTrue(output.is(NTItems.AQUARINE_STEEL_COMPOUND.get()),
                        "Mixer should craft aquarine steel compound from the saltwater charge, output was " + output);
            });
        });

        r.add("showcase/bio_reactor_produces_iron_into_chest", ARENA, 500, 0, helper -> {
            ServerLevel level = helper.getLevel();
            ShowcaseFrame frame = frame(helper, 9, 9);
            ShowcaseParts.prepareArea(level, frame, -9, -6, 9, 3, 8);
            ShowcaseIronProduction.Layout layout = ShowcaseIronProduction.build(level, frame);

            int required = NTConfig.bioReactorPowerBase + NTConfig.bioReactorPowerPerColony * ShowcaseIronProduction.COLONIES;
            helper.assertTrue(layout.reactorPower() >= required,
                    "Catalyst chain should cover the reactor, supplies " + layout.reactorPower() + " of " + required);
            helper.assertTrue(layout.incubatorPower() >= NTConfig.incubatorPowerUsage,
                    "Incubator catalysts should cover the incubator, supply " + layout.incubatorPower());

            helper.runAfterDelay(80, () -> {
                BioReactorBlockEntity reactor = blockEntity(helper, layout.controller(), BioReactorBlockEntity.class);
                helper.assertValueEqual(ShowcaseIronProduction.COLONIES, reactor.getActiveColonies(), "loaded colonies");
                helper.assertValueEqual(required, reactor.getRequiredPower(), "reactor requirement");
                helper.assertTrue(reactor.getPower() >= reactor.getRequiredPower(),
                        "Reactor should run on catalyst power, had " + reactor.getPower() + " of " + reactor.getRequiredPower());
                IncubatorBlockEntity incubator = blockEntity(helper, layout.incubator(), IncubatorBlockEntity.class);
                helper.assertTrue(incubator.getPower() >= NTConfig.incubatorPowerUsage,
                        "Incubator should run on catalyst power, had " + incubator.getPower());
            });

            helper.succeedWhen(() -> {
                ChestBlockEntity chest = blockEntity(helper, layout.outputChest(), ChestBlockEntity.class);
                helper.assertTrue(countIn(chest, Items.IRON_INGOT) > 0, "Output chest should be filling with iron ingots");
            });
        });

        r.add("showcase/block_grid_places_every_block", ARENA, 60, 0, helper -> {
            ServerLevel level = helper.getLevel();
            ShowcaseFrame frame = frame(helper, 1, 1);
            int columns = 9;
            ShowcaseParts.prepareArea(level, frame, -1, -1, ShowcaseBlockGrid.PITCH * (columns - 1) + 1,
                    ShowcaseBlockGrid.PITCH * (ShowcaseBlockGrid.rows(columns) - 1) + 1, 8);
            Map<Block, BlockPos> placed = ShowcaseBlockGrid.build(level, frame, columns);

            Set<Block> expected = nautecBlockItemBlocks();
            helper.assertTrue(!expected.isEmpty(), "Nautec should register block items");
            helper.assertValueEqual(expected, new LinkedHashSet<>(ShowcaseBlockGrid.blocks()), "grid block list");

            helper.runAfterDelay(20, () -> {
                for (Block block : expected) {
                    BlockPos pos = placed.get(block);
                    helper.assertTrue(pos != null, "Grid did not place " + BuiltInRegistries.BLOCK.getKey(block));
                    helper.assertTrue(level.getBlockState(pos).is(block),
                            BuiltInRegistries.BLOCK.getKey(block) + " did not survive in the grid at " + pos);
                }
                helper.succeed();
            });
        });

        r.add("showcase/item_wall_frames_every_item", ARENA, 40, 0, helper -> {
            ServerLevel level = helper.getLevel();
            ShowcaseFrame frame = frame(helper, 1, 4);
            int height = ShowcaseItemWall.rows(17) + 2;
            ShowcaseParts.prepareArea(level, frame, -1, -2, 17, 2, height);
            List<ItemFrame> frames = ShowcaseItemWall.build(level, frame, 17);

            Set<Item> expected = nautecItems();
            helper.assertValueEqual(expected.size(), frames.size(), "frames spawned");

            helper.runAfterDelay(5, () -> {
                AABB area = frame.aabb(-1, 0, -2, 17, height, 2);
                Set<Item> framed = new HashSet<>();
                int alive = 0;
                for (ItemFrame itemFrame : level.getEntitiesOfClass(ItemFrame.class, area)) {
                    framed.add(itemFrame.getItem().getItem());
                    alive++;
                }
                helper.assertValueEqual(expected.size(), alive, "frames still hanging");
                for (Item item : expected) {
                    helper.assertTrue(framed.contains(item), "No frame shows " + BuiltInRegistries.ITEM.getKey(item));
                }
                helper.succeed();
            });
        });

        r.add("showcase/multiblocks_form", ARENA, 40, 0, helper -> {
            ServerLevel level = helper.getLevel();
            ShowcaseFrame frame = frame(helper, 9, 9);
            ShowcaseParts.prepareArea(level, frame, -8, -3, 8, 3, 3);
            List<BlockPos> controllers = ShowcaseMultiblocks.build(level, frame);

            helper.runAfterDelay(2, () -> {
                helper.assertTrue(NTMultiblocks.AUGMENTATION_STATION.get().isFormed(level, controllers.get(0)),
                        "Augmentation station should be formed");
                helper.assertTrue(NTMultiblocks.DRAIN.get().isFormed(level, controllers.get(1)),
                        "Deep sea drain should be formed");
                helper.succeed();
            });
        });
    }
}
