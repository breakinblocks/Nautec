package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.Optional;

public final class ConfinedSpawnerTests {
    private static final BlockPos SPAWNER = new BlockPos(4, 1, 4);
    private static final BlockPos SOURCE = new BlockPos(4, 1, 6);

    private ConfinedSpawnerTests() {
    }

    private static ConfinedSpawnerBlockEntity confinedChickens(GameTestHelper helper, int spawnCount, int delay) {
        helper.setBlock(SPAWNER, Blocks.SPAWNER.defaultBlockState());
        SpawnerBlockEntity spawner = helper.getBlockEntity(SPAWNER, SpawnerBlockEntity.class);
        spawner.setEntityId(EntityType.CHICKEN, helper.getLevel().getRandom());
        CompoundTag tag = spawner.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putShort("SpawnCount", (short) spawnCount);
        tag.putShort("MinSpawnDelay", (short) delay);
        tag.putShort("MaxSpawnDelay", (short) delay);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            spawner.loadWithComponents(TagValueInput.create(reporter, helper.getLevel().registryAccess(), tag));
        }
        helper.assertTrue(SpawnerConfinementMatrixItem.confine(helper.getLevel(), helper.absolutePos(SPAWNER)), "matrix should confine a spawner");
        return helper.getBlockEntity(SPAWNER, ConfinedSpawnerBlockEntity.class);
    }

    private static void power(GameTestHelper helper) {
        BacteriaMachineTests.placeShieldedSource(helper, SOURCE, Direction.NORTH);
    }

    private static int count(ConfinedSpawnerBlockEntity spawner, Item item) {
        int total = 0;
        for (int slot = 0; slot < ConfinedSpawnerBlockEntity.SLOTS; slot++) {
            ItemStack stack = spawner.getItemStackHandler().getStackInSlot(slot);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    public static void register(NTTestRegistrar r) {
        r.add("confined_spawner/confine_and_release_round_trip", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 7, 30);
            helper.assertTrue(helper.getBlockState(SPAWNER).is(NTBlocks.CONFINED_SPAWNER.get()), "spawner should become a confined spawner");
            helper.assertValueEqual(7, confined.getSettings().spawnCount(), "spawn count read from the spawner");
            helper.assertValueEqual(30, confined.getSettings().minDelay(), "min delay read from the spawner");

            helper.assertTrue(SpawnerConfinementMatrixItem.release(helper.getLevel(), helper.absolutePos(SPAWNER), confined, null), "release should succeed");
            helper.assertTrue(helper.getBlockState(SPAWNER).is(Blocks.SPAWNER), "release should put the spawner back");
            SpawnerBlockEntity restored = helper.getBlockEntity(SPAWNER, SpawnerBlockEntity.class);
            CompoundTag tag = restored.saveWithoutMetadata(helper.getLevel().registryAccess());
            helper.assertValueEqual(7, (int) tag.getShortOr("SpawnCount", (short) 0), "spawn count survives the round trip");
            helper.assertValueEqual(30, (int) tag.getShortOr("MinSpawnDelay", (short) 0), "min delay survives the round trip");
            helper.assertValueEqual("minecraft:chicken",
                    tag.getCompoundOrEmpty("SpawnData").getCompoundOrEmpty("entity").getStringOr("id", ""), "mob survives the round trip");

            AABB around = new AABB(helper.absolutePos(SPAWNER)).inflate(2);
            boolean matrixReturned = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream()
                    .anyMatch(item -> item.getItem().is(NTItems.SPAWNER_CONFINEMENT_MATRIX.get()));
            helper.assertTrue(matrixReturned, "the matrix should come back when no player takes it");
            helper.succeed();
        });

        r.add("confined_spawner/matrix_ignores_other_blocks", 20, helper -> {
            helper.setBlock(SPAWNER, Blocks.STONE.defaultBlockState());
            helper.assertTrue(!SpawnerConfinementMatrixItem.confine(helper.getLevel(), helper.absolutePos(SPAWNER)), "stone is not a spawner");
            helper.assertTrue(helper.getBlockState(SPAWNER).is(Blocks.STONE), "stone stays stone");
            helper.succeed();
        });

        r.add("confined_spawner/runs_on_a_beam_and_stores_drops", 200, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            power(helper);
            boolean[] seenRunning = new boolean[1];
            helper.onEachTick(() -> {
                if (confined.getStatus() == ConfinedSpawnerBlockEntity.Status.RUNNING) {
                    seenRunning[0] = true;
                }
                helper.assertTrue(confined.getBufferedPower() <= NTConfig.confinedSpawnerPowerBuffer, "buffer never exceeds its capacity");
            });
            helper.succeedWhen(() -> {
                helper.assertTrue(seenRunning[0], "spawner should run on a 100 AP beam");
                helper.assertTrue(count(confined, Items.CHICKEN) >= 8, "two cycles of four chickens should store raw chicken");
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(Chicken.class,
                        new AABB(helper.absolutePos(SPAWNER)).inflate(6)).isEmpty(), "no chicken is ever spawned into the world");
            });
        });

        r.add("confined_spawner/no_power_no_drops", 80, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            helper.runAfterDelay(60, () -> {
                helper.assertValueEqual(ConfinedSpawnerBlockEntity.Status.NO_POWER, confined.getStatus(), "status without a beam");
                helper.assertValueEqual(0, count(confined, Items.CHICKEN), "nothing is made without power");
                helper.assertValueEqual(0, confined.getProgress(), "no progress without power");
                helper.succeed();
            });
        });

        r.add("confined_spawner/blacklist_skips_matching_drops", 200, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.setFilterEntry(0, SpawnerFilterEntry.of(new ItemStack(Items.CHICKEN)));
            power(helper);
            helper.runAfterDelay(120, () -> {
                helper.assertValueEqual(0, count(confined, Items.CHICKEN), "blacklisted raw chicken is never made");
                helper.assertValueEqual(ConfinedSpawnerBlockEntity.Status.RUNNING, confined.getStatus(), "still running");
                helper.succeed();
            });
        });

        r.add("confined_spawner/whitelist_tag_keeps_only_matches", 200, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.toggleWhitelist();
            confined.setFilterEntry(5, new SpawnerFilterEntry(Items.CHICKEN.builtInRegistryHolder(), Optional.of(ItemTags.MEAT)));
            power(helper);
            helper.succeedWhen(() -> {
                helper.assertTrue(count(confined, Items.CHICKEN) >= 8, "whitelisted meat tag lets raw chicken through");
                helper.assertValueEqual(0, count(confined, Items.FEATHER), "feathers are not in the meat tag");
            });
        });

        r.add("confined_spawner/empty_whitelist_stops", 60, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.toggleWhitelist();
            power(helper);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(ConfinedSpawnerBlockEntity.Status.FILTERED, confined.getStatus(), "an empty whitelist allows nothing");
                helper.assertValueEqual(0, confined.getProgress(), "a filtered spawner does not spend power");
                helper.succeed();
            });
        });

        r.add("confined_spawner/full_inventory_pauses", 80, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            for (int slot = 0; slot < ConfinedSpawnerBlockEntity.SLOTS; slot++) {
                confined.getItemStackHandler().setStackInSlot(slot, new ItemStack(Items.STONE, 64));
            }
            power(helper);
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(ConfinedSpawnerBlockEntity.Status.FULL, confined.getStatus(), "status with no free slot");
                helper.assertValueEqual(NTConfig.confinedSpawnerPowerBuffer, confined.getBufferedPower(), "a full spawner keeps its buffer topped up");
                confined.getItemStackHandler().setStackInSlot(10, ItemStack.EMPTY);
            });
            helper.runAfterDelay(45, () -> {
                helper.assertValueEqual(ConfinedSpawnerBlockEntity.Status.RUNNING, confined.getStatus(), "freeing a slot resumes the spawner");
                helper.succeed();
            });
        });

        r.add("confined_spawner/pipes_extract_but_never_insert", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.FEATHER, 5));
            BlockPos abs = helper.absolutePos(SPAWNER);
            ItemResource stone = ItemResource.of(new ItemStack(Items.STONE));
            ItemResource feather = ItemResource.of(new ItemStack(Items.FEATHER));
            for (Direction side : new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, null}) {
                ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, side);
                helper.assertTrue(handler != null, "item handler exposed on " + side);
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(0, handler.insert(1, stone, 16, tx), "no insertion on " + side);
                    helper.assertValueEqual(0, handler.insert(0, feather, 1, tx), "no topping up on " + side);
                }
            }
            ResourceHandler<ItemResource> down = helper.getLevel().getCapability(Capabilities.Item.BLOCK, abs, Direction.DOWN);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(5, down.extract(0, feather, 5, tx), "pipes can pull drops out");
                tx.commit();
            }
            helper.assertValueEqual(0, count(confined, Items.FEATHER), "extraction empties the slot");
            helper.succeed();
        });

        r.add("confined_spawner/filter_and_spawner_survive_reload", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 6, 25);
            confined.toggleWhitelist();
            confined.setFilterEntry(3, SpawnerFilterEntry.of(new ItemStack(Items.FEATHER)));
            confined.setFilterEntry(17, new SpawnerFilterEntry(Items.CHICKEN.builtInRegistryHolder(), Optional.of(ItemTags.MEAT)));
            confined.setBufferedPower(321);
            CompoundTag saved = confined.saveWithFullMetadata(helper.getLevel().registryAccess());
            BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(SPAWNER), confined.getBlockState(), saved, helper.getLevel().registryAccess());
            helper.assertTrue(loaded instanceof ConfinedSpawnerBlockEntity, "reloads as a confined spawner");
            ConfinedSpawnerBlockEntity copy = (ConfinedSpawnerBlockEntity) loaded;
            helper.assertTrue(copy.getFilter().isWhitelist(), "whitelist mode persists");
            helper.assertTrue(copy.getFilter().get(3) != null && copy.getFilter().get(3).matches(new ItemStack(Items.FEATHER)), "item filter persists");
            helper.assertTrue(copy.getFilter().get(17) != null && copy.getFilter().get(17).tag().equals(Optional.of(ItemTags.MEAT)), "tag filter persists");
            helper.assertValueEqual(321, copy.getBufferedPower(), "buffered AP persists");
            helper.assertValueEqual(6, copy.getSettings().spawnCount(), "spawner settings persist");
            helper.assertTrue(copy.getSpawnerTag() != null, "original spawner data persists");
            helper.succeed();
        });
    }
}
