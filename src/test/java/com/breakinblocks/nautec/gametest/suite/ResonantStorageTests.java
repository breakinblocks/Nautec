package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.menus.ResonantStorageMenu;
import com.breakinblocks.nautec.content.menus.ResonantVaultMenu;
import com.breakinblocks.nautec.content.resonantstorage.ChannelAccess;
import com.breakinblocks.nautec.content.resonantstorage.CisternStore;
import com.breakinblocks.nautec.content.resonantstorage.FaceMode;
import com.breakinblocks.nautec.content.resonantstorage.ResonantAccess;
import com.breakinblocks.nautec.content.resonantstorage.ResonantChannel;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantLink;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorage;
import com.breakinblocks.nautec.content.resonantstorage.ResonantVaultBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.VaultStore;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ResonantStorageTests {
    private static final int Y = 1;

    private ResonantStorageTests() {
    }

    private static ResonantVaultBlockEntity vault(NTGameTestHelper helper, int x, int z, ResonantChannel channel) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, Y, z));
        helper.getLevel().setBlock(pos, NTBlocks.RESONANT_VAULT.get().defaultBlockState(), Block.UPDATE_ALL);
        ResonantVaultBlockEntity vault = (ResonantVaultBlockEntity) helper.getLevel().getBlockEntity(pos);
        vault.link(new ResonantLink(channel, "tester"));
        return vault;
    }

    private static ResonantCisternBlockEntity cistern(NTGameTestHelper helper, int x, int z, ResonantChannel channel) {
        BlockPos pos = helper.absolutePos(new BlockPos(x, Y, z));
        helper.getLevel().setBlock(pos, NTBlocks.RESONANT_CISTERN.get().defaultBlockState(), Block.UPDATE_ALL);
        ResonantCisternBlockEntity cistern = (ResonantCisternBlockEntity) helper.getLevel().getBlockEntity(pos);
        cistern.link(new ResonantLink(channel, "tester"));
        return cistern;
    }

    private static ResonantChannel fresh() {
        return ResonantChannel.privateTo(UUID.randomUUID());
    }

    private static int insert(@Nullable ResourceHandler<ItemResource> handler, ItemStack stack) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static int fill(@Nullable ResourceHandler<FluidResource> handler, FluidStack stack) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(FluidResource.of(stack), stack.getAmount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static int count(ResourceHandler<ItemResource> handler, ItemStack kind) {
        int total = 0;
        for (int i = 0; i < handler.size(); i++) {
            if (handler.getResource(i).is(kind.getItem())) {
                total += handler.getAmountAsInt(i);
            }
        }
        return total;
    }

    private static ServerPlayer serverPlayer(ServerLevel level, UUID id) {
        return new ServerPlayer(level.getServer(), level, new GameProfile(id, "resonant_" + id.toString().substring(0, 6)), ClientInformation.createDefault());
    }

    public static void register(NTTestRegistrar r) {
        r.add("resonant_storage/vaults_on_one_channel_share_items", 5, helper -> {
            ResonantChannel channel = fresh();
            ResonantVaultBlockEntity first = vault(helper, 2, 2, channel);
            ResonantVaultBlockEntity second = vault(helper, 6, 6, channel);
            insert(first.itemHandler(null), new ItemStack(Items.DIAMOND, 7));
            helper.assertValueEqual(7, count(second.itemHandler(null), new ItemStack(Items.DIAMOND)), "diamonds seen through the second vault");
            helper.succeed();
        });

        r.add("resonant_storage/different_addresses_are_separate", 5, helper -> {
            ResonantChannel channel = fresh();
            ResonantVaultBlockEntity first = vault(helper, 2, 2, channel);
            ResonantVaultBlockEntity second = vault(helper, 6, 6, channel.withAddress(channel.address().withSlot(0, DyeColor.PURPLE)));
            insert(first.itemHandler(null), new ItemStack(Items.EMERALD, 3));
            helper.assertValueEqual(0, count(second.itemHandler(null), new ItemStack(Items.EMERALD)), "emeralds leaked onto another address");
            helper.succeed();
        });

        r.add("resonant_storage/private_channels_only_open_for_the_owner", 5, helper -> {
            Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
            Player stranger = helper.makeMockPlayer(GameType.SURVIVAL);
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, ResonantChannel.privateTo(owner.getUUID()));
            helper.assertTrue(vault.canUse(owner), "the owner could not use their private vault");
            helper.assertFalse(vault.canUse(stranger), "a stranger could use a private vault");
            vault.link(new ResonantLink(ResonantChannel.PUBLIC_DEFAULT.withAddress(GatewayAddress.uniform(DyeColor.LIME)), ""));
            helper.assertTrue(vault.canUse(stranger), "a stranger could not use a public vault");
            helper.assertFalse(ResonantAccess.canUse(stranger, new ResonantChannel(ChannelAccess.TEAM, UUID.randomUUID(), GatewayAddress.DEFAULT)),
                    "a stranger could use a team channel they are not in");
            helper.succeed();
        });

        r.add("resonant_storage/pipes_follow_a_rekeyed_vault", 5, helper -> {
            ResonantChannel first = fresh();
            ResonantChannel second = fresh();
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, first);
            insert(vault.itemHandler(null), new ItemStack(Items.IRON_INGOT, 5));
            BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> cache = BlockCapabilityCache.create(
                    TransferCapabilities.Item.BLOCK, helper.getLevel(), vault.getBlockPos(), Direction.UP);
            helper.assertValueEqual(5, count(cache.getCapability(), new ItemStack(Items.IRON_INGOT)), "pipe view before the change");
            vault.link(new ResonantLink(second, "tester"));
            helper.assertValueEqual(0, count(cache.getCapability(), new ItemStack(Items.IRON_INGOT)), "a pipe kept the old channel after a rekey");
            helper.succeed();
        });

        r.add("resonant_storage/closed_sides_hide_the_capability", 5, helper -> {
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, fresh());
            vault.setFace(Direction.EAST, FaceMode.CLOSED);
            helper.assertTrue(vault.itemHandler(Direction.EAST) == null, "a closed side still exposed the vault");
            helper.assertTrue(vault.itemHandler(Direction.WEST) != null, "an open side lost the vault");
            helper.succeed();
        });

        r.add("resonant_storage/expansions_grow_a_vault_and_lock_while_used", 5, helper -> {
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, fresh());
            VaultStore store = vault.vault();
            helper.assertValueEqual(VaultStore.PAGE, store.view().size(), "base capacity");
            insert(store.upgradeSlot(), new ItemStack(NTItems.RESONANT_EXPANSION.get(), 2));
            helper.assertValueEqual(VaultStore.PAGE * 3, store.view().size(), "capacity with two expansions");
            store.setStackAt(VaultStore.PAGE * 2 + 3, new ItemStack(Items.GOLD_INGOT, 4));
            helper.assertValueEqual(0, store.upgradeSlot().removable(), "an expansion could come out while its page held items");
            try (Transaction transaction = Transaction.openRoot()) {
                int taken = store.upgradeSlot().extract(0, ItemResource.of(new ItemStack(NTItems.RESONANT_EXPANSION.get())), 2, transaction);
                helper.assertValueEqual(0, taken, "expansions extracted while needed");
            }
            store.setStackAt(VaultStore.PAGE * 2 + 3, ItemStack.EMPTY);
            helper.assertValueEqual(2, store.upgradeSlot().removable(), "expansions free once the page is empty");
            helper.succeed();
        });

        r.add("resonant_storage/cistern_capacity_follows_expansions", 5, helper -> {
            ResonantChannel channel = fresh();
            ResonantCisternBlockEntity first = cistern(helper, 2, 4, channel);
            ResonantCisternBlockEntity second = cistern(helper, 6, 4, channel);
            int tier = CisternStore.capacityFor(0);
            helper.assertValueEqual(tier, fill(first.fluidHandler(null), new FluidStack(Fluids.WATER, tier * 2)), "base fill");
            CisternStore store = second.cistern();
            insert(store.upgradeSlot(), new ItemStack(NTItems.RESONANT_EXPANSION.get(), 1));
            helper.assertValueEqual(tier, fill(second.fluidHandler(null), new FluidStack(Fluids.WATER, tier * 2)), "fill after one expansion");
            helper.assertValueEqual(tier * 2, first.fluid().getAmount(), "first cistern sees the shared tank");
            helper.assertValueEqual(0, store.upgradeSlot().removable(), "the expansion came out while the tank needed it");
            helper.succeed();
        });

        r.add("resonant_storage/push_side_fills_a_neighbour", 40, helper -> {
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, fresh());
            BlockPos chestPos = vault.getBlockPos().east();
            helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            insert(vault.itemHandler(null), new ItemStack(Items.COBBLESTONE, 20));
            vault.setFace(Direction.EAST, FaceMode.PUSH);
            helper.succeedWhen(() -> {
                ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
                int moved = 0;
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    moved += chest.getItem(i).is(Items.COBBLESTONE) ? chest.getItem(i).getCount() : 0;
                }
                helper.assertTrue(moved == 20, "pushed " + moved + " of 20 cobblestone");
            });
        });

        r.add("resonant_storage/pull_side_empties_a_neighbour", 40, helper -> {
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, fresh());
            BlockPos chestPos = vault.getBlockPos().west();
            helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
            ((ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos)).setItem(0, new ItemStack(Items.REDSTONE, 12));
            vault.setFace(Direction.WEST, FaceMode.PULL);
            helper.succeedWhen(() -> helper.assertTrue(count(vault.itemHandler(null), new ItemStack(Items.REDSTONE)) == 12, "redstone not pulled yet"));
        });

        r.add("resonant_storage/storage_saves_and_loads", 5, helper -> {
            ResonantStorage storage = new ResonantStorage();
            ResonantChannel channel = fresh();
            VaultStore vault = storage.vault(channel);
            insert(vault.upgradeSlot(), new ItemStack(NTItems.RESONANT_EXPANSION.get(), 1));
            vault.setStackAt(VaultStore.PAGE + 1, new ItemStack(Items.DIAMOND, 9));
            storage.cistern(channel).setFluid(new FluidStack(Fluids.LAVA, 4321));
            storage.vault(fresh());
            DynamicOps<Tag> ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            Tag saved = ResonantStorage.CODEC.encodeStart(ops, storage).getOrThrow();
            ResonantStorage loaded = ResonantStorage.CODEC.parse(ops, saved).getOrThrow();
            VaultStore back = loaded.vault(channel);
            helper.assertValueEqual(1, back.upgrades(), "expansions after a reload");
            helper.assertTrue(back.stackAt(VaultStore.PAGE + 1).is(Items.DIAMOND) && back.stackAt(VaultStore.PAGE + 1).getCount() == 9, "items after a reload");
            helper.assertValueEqual(4321, loaded.cistern(channel).fluid().getAmount(), "fluid after a reload");
            helper.succeed();
        });

        r.add("resonant_storage/drops_keep_the_channel", 5, helper -> {
            ResonantChannel channel = fresh().withAddress(GatewayAddress.uniform(DyeColor.MAGENTA));
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, channel);
            List<ItemStack> drops = Block.getDrops(vault.getBlockState(), helper.getLevel(), vault.getBlockPos(), vault);
            ResonantLink link = drops.isEmpty() ? null : drops.getFirst().get(NTDataComponents.RESONANT_LINK.get());
            helper.assertTrue(link != null && link.channel().equals(channel), "the dropped vault lost its channel, got " + link);
            helper.succeed();
        });

        r.add("resonant_storage/comparator_reads_the_channel", 10, helper -> {
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, fresh());
            helper.assertValueEqual(0, vault.comparatorSignal(), "signal when empty");
            for (int i = 0; i < VaultStore.PAGE; i++) {
                insert(vault.itemHandler(null), new ItemStack(Items.STONE, 64));
            }
            helper.assertValueEqual(15, vault.comparatorSignal(), "signal when full");
            helper.succeed();
        });

        r.add("resonant_storage/menu_buttons_recode_for_the_owner", 10, helper -> {
            ServerLevel level = helper.getLevel();
            ServerPlayer owner = serverPlayer(level, UUID.randomUUID());
            ResonantVaultBlockEntity vault = vault(helper, 4, 4, ResonantChannel.privateTo(owner.getUUID()));
            owner.setPos(vault.getBlockPos().getX() + 0.5, vault.getBlockPos().getY() + 1, vault.getBlockPos().getZ() + 0.5);
            ResonantVaultMenu menu = new ResonantVaultMenu(1, owner.getInventory(), vault);
            GatewayAddress wanted = GatewayAddress.uniform(DyeColor.BLUE);
            menu.clickMenuButton(owner, ResonantStorageMenu.ADDRESS_BUTTON + wanted.pack());
            helper.assertTrue(vault.channel().address().equals(wanted), "the address button did not recode, got " + vault.channel().address());
            helper.assertTrue(vault.channel().owner().equals(owner.getUUID()), "recoding changed the owner");
            ServerPlayer stranger = serverPlayer(level, UUID.randomUUID());
            stranger.setPos(owner.getX(), owner.getY(), owner.getZ());
            helper.assertFalse(menu.stillValid(stranger), "a stranger could keep a private vault menu open");
            menu.removed(owner);
            helper.succeed();
        });
    }
}
