package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import io.netty.buffer.Unpooled;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.content.menus.ConfinedSpawnerMenu;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.common.Tags;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.List;
import java.util.Optional;

public final class ConfinedSpawnerTests {
    private static final BlockPos SPAWNER = new BlockPos(4, 1, 4);
    private static final BlockPos SOURCE = new BlockPos(4, 1, 6);

    private ConfinedSpawnerTests() {
    }

    private static ConfinedSpawnerBlockEntity confinedChickens(NTGameTestHelper helper, int spawnCount, int delay) {
        helper.setBlock(SPAWNER, Blocks.SPAWNER.defaultBlockState());
        SpawnerBlockEntity spawner = helper.getBlockEntity(SPAWNER, SpawnerBlockEntity.class);
        spawner.setEntityId(EntityType.CHICKEN, helper.getLevel().getRandom());
        CompoundTag tag = spawner.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putShort("SpawnCount", (short) spawnCount);
        tag.putShort("MinSpawnDelay", (short) delay);
        tag.putShort("MaxSpawnDelay", (short) delay);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            spawner.loadWithComponents(tag, helper.getLevel().registryAccess());
        }
        helper.assertTrue(SpawnerConfinementMatrixItem.confine(helper.getLevel(), helper.absolutePos(SPAWNER)), "matrix should confine a spawner");
        return helper.getBlockEntity(SPAWNER, ConfinedSpawnerBlockEntity.class);
    }

    private static void power(NTGameTestHelper helper) {
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
        r.add("confined_spawner/experience_algae_is_in_the_experience_tag", 20, helper -> {
            helper.assertTrue(NTFluids.EXPERIENCE_ALGAE.getStillFluid().defaultFluidState().is(Tags.Fluids.EXPERIENCE), "still experience algae is c:experience");
            helper.assertTrue(NTFluids.EXPERIENCE_ALGAE.getFlowingFluid().defaultFluidState().is(Tags.Fluids.EXPERIENCE), "flowing experience algae is c:experience");
            helper.succeed();
        });

        r.add("confined_spawner/confine_and_release_round_trip", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 7, 30);
            helper.assertTrue(helper.getBlockState(SPAWNER).is(NTBlocks.CONFINED_SPAWNER.get()), "spawner should become a confined spawner");
            helper.assertValueEqual(7, confined.getSettings().spawnCount(), "spawn count read from the spawner");
            helper.assertValueEqual(30, confined.getSettings().minDelay(), "min delay read from the spawner");

            helper.assertTrue(SpawnerConfinementMatrixItem.release(helper.getLevel(), helper.absolutePos(SPAWNER), confined, null), "release should succeed");
            helper.assertTrue(helper.getBlockState(SPAWNER).is(Blocks.SPAWNER), "release should put the spawner back");
            SpawnerBlockEntity restored = helper.getBlockEntity(SPAWNER, SpawnerBlockEntity.class);
            CompoundTag tag = restored.saveWithoutMetadata(helper.getLevel().registryAccess());
            helper.assertValueEqual(7, (int) tag.getShort("SpawnCount"), "spawn count survives the round trip");
            helper.assertValueEqual(30, (int) tag.getShort("MinSpawnDelay"), "min delay survives the round trip");
            helper.assertValueEqual("minecraft:chicken",
                    tag.getCompound("SpawnData").getCompound("entity").getString("id"), "mob survives the round trip");

            AABB around = new AABB(helper.absolutePos(SPAWNER)).inflate(2);
            boolean matrixReturned = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream()
                    .anyMatch(item -> item.getItem().is(NTItems.SPAWNER_CONFINEMENT_MATRIX.get()));
            helper.assertTrue(matrixReturned, "the matrix should come back when no player takes it");
            helper.succeed();
        });

        r.add("confined_spawner/breaks_into_itself_and_places_back", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 6, 25);
            confined.toggleWhitelist();
            confined.setFilterEntry(3, SpawnerFilterEntry.of(new ItemStack(Items.FEATHER)));
            confined.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.FEATHER, 5));
            ServerLevel level = helper.getLevel();
            List<ItemStack> drops = Block.getDrops(helper.getBlockState(SPAWNER), level, helper.absolutePos(SPAWNER), confined, null,
                    new ItemStack(Items.IRON_PICKAXE));
            helper.assertValueEqual(1, drops.size(), "drop count without silk touch");
            ItemStack dropped = drops.getFirst();
            helper.assertTrue(dropped.is(NTBlocks.CONFINED_SPAWNER.get().asItem()), "The confined spawner should drop itself");
            helper.assertTrue(dropped.has(DataComponents.BLOCK_ENTITY_DATA), "The dropped spawner should carry its data");

            BlockPos target = new BlockPos(2, 1, 2);
            helper.setBlock(target.below(), Blocks.STONE.defaultBlockState());
            BlockPos below = helper.absolutePos(target.below());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, dropped.copy());
            player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(below.getCenter().add(0, 0.5, 0), Direction.UP, below, false)));

            ConfinedSpawnerBlockEntity placed = helper.getBlockEntity(target, ConfinedSpawnerBlockEntity.class);
            helper.assertValueEqual(6, placed.getSettings().spawnCount(), "spawn count after placing");
            helper.assertTrue(placed.getFilter().isWhitelist(), "filter mode after placing");
            helper.assertTrue(placed.getFilter().get(3) != null && placed.getFilter().get(3).matches(new ItemStack(Items.FEATHER)), "filter entry after placing");
            helper.assertValueEqual(5, count(placed, Items.FEATHER), "stored items travel with the item");
            helper.assertTrue(!SpawnerConfinementMatrixItem.release(level, helper.absolutePos(target), placed, null), "a spawner holding drops cannot be released");
            helper.assertTrue(helper.getBlockState(target).is(NTBlocks.CONFINED_SPAWNER.get()), "a refused release leaves it confined");
            placed.getItemStackHandler().setStackInSlot(0, ItemStack.EMPTY);
            helper.assertTrue(SpawnerConfinementMatrixItem.release(level, helper.absolutePos(target), placed, null), "the emptied spawner can be released");
            SpawnerBlockEntity restored = helper.getBlockEntity(target, SpawnerBlockEntity.class);
            helper.assertValueEqual("minecraft:chicken", restored.saveWithoutMetadata(level.registryAccess())
                    .getCompound("SpawnData").getCompound("entity").getString("id"), "mob after release");
            helper.succeed();
        });

        r.add("confined_spawner/stored_experience_blocks_release_and_survives_mining", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 500));
            helper.assertTrue(!SpawnerConfinementMatrixItem.release(helper.getLevel(), helper.absolutePos(SPAWNER), confined, null),
                    "stored experience blocks the release");
            helper.assertTrue(helper.getBlockState(SPAWNER).is(NTBlocks.CONFINED_SPAWNER.get()), "it stays confined");
            confined.getItemStackHandler().setStackInSlot(1, new ItemStack(Items.FEATHER, 3));
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            helper.getLevel().destroyBlock(helper.absolutePos(SPAWNER), true, player);
            AABB around = new AABB(helper.absolutePos(SPAWNER)).inflate(2);
            List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, around);
            helper.assertValueEqual(1, items.size(), "mining drops one item, not the stored contents");
            ItemStack dropped = items.getFirst().getItem();
            helper.assertTrue(dropped.is(NTBlocks.CONFINED_SPAWNER.get().asItem()), "the one drop is the confined spawner");
            helper.assertTrue(dropped.has(DataComponents.BLOCK_ENTITY_DATA), "it carries its contents");
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

        r.add("confined_spawner/kills_fill_the_experience_tank", 200, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            power(helper);
            helper.succeedWhen(() -> {
                FluidStack stored = confined.getFluidTank().getFluid();
                helper.assertTrue(stored.getAmount() > 0, "simulated kills should make liquid experience");
                helper.assertTrue(stored.is(NTFluids.EXPERIENCE_ALGAE.getStillFluid()), "the default experience fluid is Experience Algae");
                helper.assertValueEqual(0, stored.getAmount() % NTConfig.confinedSpawnerXpRatio, "whole experience points only");
            });
        });

        r.add("confined_spawner/menu_carries_experience_through_the_short_packet", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeBlockPos(confined.getBlockPos());
            ConfinedSpawnerMenu client = new ConfinedSpawnerMenu(1, player.getInventory(), buffer);
            ContainerData data = confined.getData();

            confined.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 50_000));
            for (int i = 0; i < data.getCount(); i++) {
                client.setData(i, (short) data.get(i));
            }
            helper.assertValueEqual(50_000, client.getExperience(), "experience above 32767 after the packet");
            helper.assertValueEqual(confined.getFluidTank().getCapacity(), client.getExperienceCapacity(), "tank capacity after the packet");
            helper.assertTrue(client.getExperienceFluid() == NTFluids.EXPERIENCE_ALGAE.getStillFluid(), "stored fluid after the packet");

            confined.getFluidTank().setFluid(FluidStack.EMPTY);
            for (int i = 0; i < data.getCount(); i++) {
                client.setData(i, (short) data.get(i));
            }
            helper.assertValueEqual(0, client.getExperience(), "empty tank after the packet");
            helper.assertTrue(client.getExperienceFluid() == ConfinedSpawnerBlockEntity.experienceFluid(), "an empty tank names the configured fluid");
            helper.succeed();
        });

        r.add("confined_spawner/experience_fluid_follows_the_config_order", 20, helper -> {
            List<String> old = NTConfig.confinedSpawnerXpFluids;
            try {
                NTConfig.confinedSpawnerXpFluids = List.of("missingmod:experience", "nautec:saltwater", "nautec:experience_algae");
                helper.assertTrue(ConfinedSpawnerBlockEntity.experienceFluid() == NTFluids.SALT_WATER.getStillFluid(), "the first fluid that exists wins");
                NTConfig.confinedSpawnerXpFluids = List.of("missingmod:experience", "not a valid id");
                helper.assertTrue(ConfinedSpawnerBlockEntity.experienceFluid() == NTFluids.EXPERIENCE_ALGAE.getStillFluid(), "Experience Algae is the fallback");
            } finally {
                NTConfig.confinedSpawnerXpFluids = old;
            }
            helper.succeed();
        });

        r.add("confined_spawner/experience_tank_drains_but_never_fills", 40, helper -> {
            ConfinedSpawnerBlockEntity confined = confinedChickens(helper, 4, 20);
            confined.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 500));
            ResourceHandler<FluidResource> side = helper.getLevel().getCapability(TransferCapabilities.Fluid.BLOCK, helper.absolutePos(SPAWNER), Direction.NORTH);
            helper.assertTrue(side != null, "the tank should be reachable by pipes");
            FluidResource algae = FluidResource.of(NTFluids.EXPERIENCE_ALGAE.getStillFluid());
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(0, side.insert(0, algae, 100, tx), "pipes cannot fill it");
                helper.assertValueEqual(300, side.extract(0, algae, 300, tx), "pipes can drain it");
                tx.commit();
            }
            helper.assertValueEqual(200, confined.getFluidTank().getFluidAmount(), "left in the tank");
            helper.succeed();
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
                ResourceHandler<ItemResource> handler = helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, abs, side);
                helper.assertTrue(handler != null, "item handler exposed on " + side);
                try (Transaction tx = Transaction.openRoot()) {
                    helper.assertValueEqual(0, handler.insert(1, stone, 16, tx), "no insertion on " + side);
                    helper.assertValueEqual(0, handler.insert(0, feather, 1, tx), "no topping up on " + side);
                }
            }
            ResourceHandler<ItemResource> down = helper.getLevel().getCapability(TransferCapabilities.Item.BLOCK, abs, Direction.DOWN);
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
