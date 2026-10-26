package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.items.MobSamplerItem;
import com.breakinblocks.nautec.content.items.SpawnerConfinementMatrixItem;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidStack;

public final class MobSamplingTests {
    private static final BlockPos POS = new BlockPos(4, 1, 4);

    private MobSamplingTests() {
    }

    private static Mob mob(NTGameTestHelper helper, EntityType<?> type) {
        return (Mob) type.create(helper.getLevel());
    }

    private static ItemStack sample(EntityType<?> type) {
        ItemStack sample = new ItemStack(NTItems.MOB_SAMPLE.get());
        sample.set(NTDataComponents.MOB_SAMPLE.get(), type);
        return sample;
    }

    private static UseOnContext context(NTGameTestHelper helper, Player player) {
        BlockPos pos = helper.absolutePos(POS);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static ConfinedSpawnerBlockEntity confined(NTGameTestHelper helper) {
        helper.setBlock(POS, Blocks.SPAWNER);
        SpawnerBlockEntity original = helper.getBlockEntity(POS, SpawnerBlockEntity.class);
        original.setEntityId(EntityType.CHICKEN, helper.getLevel().getRandom());
        helper.assertTrue(SpawnerConfinementMatrixItem.confine(helper.getLevel(), helper.absolutePos(POS)), "confine setup");
        return helper.getBlockEntity(POS, ConfinedSpawnerBlockEntity.class);
    }

    public static void register(NTTestRegistrar r) {
        r.add("mob_sampling/guardian_sample_is_nonlethal_and_single_use", 40, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            Mob guardian = mob(helper, EntityType.GUARDIAN);
            float health = guardian.getHealth();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.MOB_SAMPLER.get()));
            InteractionResult captured = NTItems.MOB_SAMPLER.get().interactLivingEntity(player.getMainHandItem(), player, guardian, InteractionHand.MAIN_HAND);
            helper.assertTrue(captured == InteractionResult.SUCCESS, "guardian can be sampled");
            helper.assertTrue(guardian.isAlive() && !guardian.isRemoved() && guardian.getHealth() == health, "sampling leaves guardian alive and unharmed");
            helper.assertTrue(player.getMainHandItem().is(NTItems.MOB_SAMPLE.get()), "last sampler is replaced in hand");
            helper.assertTrue(player.getMainHandItem().get(NTDataComponents.MOB_SAMPLE.get()) == EntityType.GUARDIAN, "guardian species stored");
            ConfinedSpawnerBlockEntity spawner = confined(helper);
            helper.assertTrue(player.getMainHandItem().useOn(context(helper, player)) == InteractionResult.SUCCESS, "sample applies");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "applying consumes sample");
            helper.assertValueEqual("minecraft:guardian", spawner.getSettings().displayEntity().getString("id"), "guardian assigned");
            helper.succeed();
        });

        r.add("mob_sampling/stacked_offhand_sampler_intercepts_mob_interaction", 40, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(NTItems.MOB_SAMPLER.get(), 2));
            Mob villager = mob(helper, EntityType.VILLAGER);
            PlayerInteractEvent.EntityInteractSpecific event = new PlayerInteractEvent.EntityInteractSpecific(player, InteractionHand.OFF_HAND, villager, Vec3.ZERO);
            MobSamplerItem.onSampleEntity(event);
            helper.assertTrue(event.isCanceled() && event.getCancellationResult() == InteractionResult.SUCCESS, "sampler intercepts trading interaction");
            helper.assertValueEqual(1, player.getOffhandItem().getCount(), "one sampler consumed");
            helper.assertTrue(player.getMainHandItem().is(Items.STICK), "main hand unchanged");
            boolean found = false;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                found |= player.getInventory().getItem(slot).get(NTDataComponents.MOB_SAMPLE.get()) == EntityType.VILLAGER;
            }
            helper.assertTrue(found, "sample delivered to inventory");
            helper.succeed();
        });

        r.add("mob_sampling/modded_mob_sample_survives_item_save_and_network", 40, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.MOB_SAMPLER.get()));
            NTItems.MOB_SAMPLER.get().interactLivingEntity(player.getMainHandItem(), player, mob(helper, NTEntities.SILT_SKIPPER.get()), InteractionHand.MAIN_HAND);
            ItemStack captured = player.getMainHandItem();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            ItemStack saved = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, captured).getOrThrow()).getOrThrow();
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                ItemStack.STREAM_CODEC.encode(buffer, saved);
                ItemStack received = ItemStack.STREAM_CODEC.decode(buffer);
                helper.assertTrue(received.get(NTDataComponents.MOB_SAMPLE.get()) == NTEntities.SILT_SKIPPER.get(), "modded entity id survives persistence and network");
                player.setItemInHand(InteractionHand.MAIN_HAND, received);
                ConfinedSpawnerBlockEntity spawner = confined(helper);
                helper.assertTrue(received.useOn(context(helper, player)) == InteractionResult.SUCCESS, "modded sample applies");
                helper.assertValueEqual(BuiltInRegistries.ENTITY_TYPE.getKey(NTEntities.SILT_SKIPPER.get()).toString(),
                        spawner.getSettings().displayEntity().getString("id"), "modded mob assigned");
            } finally {
                buffer.release();
            }
            helper.succeed();
        });

        r.add("mob_sampling/capture_tag_blocks_collection_and_stored_samples", 40, helper -> {
            helper.assertTrue(EntityType.WITHER.builtInRegistryHolder().is(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED), "test datapack tags wither");
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.MOB_SAMPLER.get()));
            helper.assertTrue(NTItems.MOB_SAMPLER.get().interactLivingEntity(player.getMainHandItem(), player, mob(helper, EntityType.WITHER), InteractionHand.MAIN_HAND)
                    == InteractionResult.FAIL, "tagged mob cannot be sampled");
            helper.assertValueEqual(1, player.getMainHandItem().getCount(), "rejected capture preserves sampler");
            ConfinedSpawnerBlockEntity spawner = confined(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, sample(EntityType.WITHER));
            helper.assertTrue(player.getMainHandItem().useOn(context(helper, player)) == InteractionResult.FAIL, "previously collected tagged sample rejected");
            helper.assertValueEqual(1, player.getMainHandItem().getCount(), "rejected sample preserved");
            helper.assertTrue(!spawner.applySample(EntityType.WITHER), "block entity also enforces tag");
            helper.assertValueEqual("minecraft:chicken", spawner.getSettings().displayEntity().getString("id"), "spawner unchanged");
            helper.succeed();
        });

        r.add("mob_sampling/ordinary_spawner_missing_sample_and_adventure_rejected", 40, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.setBlock(POS, Blocks.SPAWNER);
            player.setItemInHand(InteractionHand.MAIN_HAND, sample(EntityType.GUARDIAN));
            helper.assertTrue(player.getMainHandItem().useOn(context(helper, player)) == InteractionResult.FAIL, "ordinary spawner rejected");
            helper.assertValueEqual(1, player.getMainHandItem().getCount(), "ordinary spawner does not consume sample");
            ConfinedSpawnerBlockEntity spawner = confined(helper);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.MOB_SAMPLE.get()));
            helper.assertTrue(player.getMainHandItem().useOn(context(helper, player)) == InteractionResult.FAIL, "missing sample rejected");
            player.setItemInHand(InteractionHand.MAIN_HAND, sample(EntityType.ARMOR_STAND));
            helper.assertTrue(player.getMainHandItem().useOn(context(helper, player)) == InteractionResult.FAIL, "non-mob sample rejected");
            player.setItemInHand(InteractionHand.MAIN_HAND, sample(EntityType.GUARDIAN));
            player.getAbilities().mayBuild = false;
            helper.assertTrue(NTItems.MOB_SAMPLE.get().useOn(context(helper, player)) == InteractionResult.FAIL, "adventure retarget rejected");
            helper.assertValueEqual("minecraft:chicken", spawner.getSettings().displayEntity().getString("id"), "failed actions preserve mob");
            helper.succeed();
        });

        r.add("mob_sampling/retarget_updates_cached_loot_and_survives_reload", 40, helper -> {
            ConfinedSpawnerBlockEntity spawner = confined(helper);
            CompoundTag settings = spawner.getSpawnerTag().copy();
            settings.putShort("MinSpawnDelay", (short) 1);
            settings.putShort("MaxSpawnDelay", (short) 1);
            spawner.confine(Blocks.SPAWNER.defaultBlockState(), settings);
            spawner.setBufferedPower(500);
            spawner.commonTick();
            helper.assertTrue(spawner.applySample(EntityType.COW), "cow applied after chicken loot cycle");
            for (int slot = 0; slot < ConfinedSpawnerBlockEntity.SLOTS; slot++) {
                spawner.getItemStackHandler().setStackInSlot(slot, ItemStack.EMPTY);
            }
            spawner.setBufferedPower(500);
            spawner.commonTick();
            boolean beef = false;
            for (int slot = 0; slot < ConfinedSpawnerBlockEntity.SLOTS; slot++) {
                ItemStack item = spawner.getItemStackHandler().getStackInSlot(slot);
                beef |= item.is(Items.BEEF);
                helper.assertTrue(!item.is(Items.FEATHER) && !item.is(Items.CHICKEN), "old species loot cache cleared");
            }
            helper.assertTrue(beef, "new species produces its loot");
            CompoundTag saved = spawner.saveWithoutMetadata(helper.getLevel().registryAccess());
            BlockPos copyPos = new BlockPos(2, 1, 2);
            helper.setBlock(copyPos, NTBlocks.CONFINED_SPAWNER.get());
            ConfinedSpawnerBlockEntity copy = helper.getBlockEntity(copyPos, ConfinedSpawnerBlockEntity.class);
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
                copy.loadWithComponents(saved, helper.getLevel().registryAccess());
            }
            helper.assertValueEqual("minecraft:cow", copy.getSettings().displayEntity().getString("id"), "sampled species persists through reload");
            helper.succeed();
        });

        r.add("mob_sampling/retarget_preserves_upgrades_contents_and_release", 40, helper -> {
            ConfinedSpawnerBlockEntity spawner = confined(helper);
            CompoundTag data = spawner.getSpawnerTag().copy();
            data.putShort("MinSpawnDelay", (short) 12);
            data.putShort("MaxSpawnDelay", (short) 34);
            data.putShort("SpawnCount", (short) 6);
            data.putInt("modded_upgrade", 42);
            spawner.confine(Blocks.SPAWNER.defaultBlockState(), data);
            spawner.setBufferedPower(321);
            spawner.setFilterEntry(2, SpawnerFilterEntry.of(new ItemStack(Items.PRISMARINE_SHARD)));
            spawner.toggleWhitelist();
            spawner.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.FEATHER, 7));
            spawner.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 500));
            spawner.getOrCreateDisplayEntity();
            helper.assertTrue(spawner.applySample(EntityType.GUARDIAN), "guardian applied");
            helper.assertValueEqual(12, spawner.getSettings().minDelay(), "minimum delay kept");
            helper.assertValueEqual(34, spawner.getSettings().maxDelay(), "maximum delay kept");
            helper.assertValueEqual(6, spawner.getSettings().spawnCount(), "spawn count kept");
            helper.assertValueEqual(42, spawner.getSpawnerTag().getInt("modded_upgrade"), "foreign mod data kept");
            helper.assertValueEqual(321, spawner.getBufferedPower(), "power kept");
            helper.assertValueEqual(7, spawner.getItemStackHandler().getStackInSlot(0).getCount(), "drops kept");
            helper.assertValueEqual(500, spawner.getFluidTank().getFluidAmount(), "experience kept");
            helper.assertTrue(spawner.getFilter().isWhitelist() && spawner.getFilter().get(2) != null, "filter kept");
            helper.assertTrue(spawner.getOrCreateDisplayEntity().getType() == EntityType.GUARDIAN, "cached display refreshed");
            for (int i = 0; i < 20; i++) {
                helper.assertValueEqual("minecraft:guardian", spawner.getSettings().pick(helper.getLevel().getRandom()).getEntityToSpawn().getString("id"), "old weighted mobs replaced");
            }
            spawner.getItemStackHandler().setStackInSlot(0, ItemStack.EMPTY);
            spawner.getFluidTank().setFluid(FluidStack.EMPTY);
            helper.assertTrue(SpawnerConfinementMatrixItem.release(helper.getLevel(), helper.absolutePos(POS), spawner, null), "release succeeds");
            CompoundTag released = helper.getBlockEntity(POS, SpawnerBlockEntity.class).saveWithoutMetadata(helper.getLevel().registryAccess());
            helper.assertValueEqual("minecraft:guardian", released.getCompound("SpawnData").getCompound("entity").getString("id"), "new mob survives release");
            helper.assertValueEqual(6, (int) released.getShort("SpawnCount"), "spawn count survives release");
            helper.succeed();
        });
    }
}
