package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.resonance.ResonanceEndpoint;
import com.breakinblocks.nautec.content.resonance.ResonanceGrid;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonancePylonBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ResonanceNetworkTests {
    private ResonanceNetworkTests() {
    }

    private static final class Endpoint implements ResonanceEndpoint {
        private final ResourceKey<Level> dimension;
        private final boolean interdimensional;
        private final boolean sending;
        private final int throughput;
        private int energy;
        private final int capacity;
        private int moved;

        private Endpoint(ResourceKey<Level> dimension, boolean interdimensional, boolean sending, int energy, int capacity, int throughput) {
            this.dimension = dimension;
            this.interdimensional = interdimensional;
            this.sending = sending;
            this.energy = energy;
            this.capacity = capacity;
            this.throughput = throughput;
        }

        @Override
        public ResourceKey<Level> dimension() {
            return dimension;
        }

        @Override
        public boolean interdimensional() {
            return interdimensional;
        }

        @Override
        public boolean sending() {
            return sending;
        }

        @Override
        public int sendable() {
            return Math.min(energy, throughput - moved);
        }

        @Override
        public int receivable() {
            return Math.min(capacity - energy, throughput - moved);
        }

        @Override
        public void send(int amount) {
            energy -= amount;
            moved += amount;
        }

        @Override
        public void receive(int amount) {
            energy += amount;
            moved += amount;
        }
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static int expected(int amount, double loss) {
        return (int) Math.floor(amount * (1.0 - loss));
    }

    public static void register(NTTestRegistrar r) {
        r.add("resonance/same_dimension_transfer_loses_its_share", 20, helper -> {
            Endpoint sender = new Endpoint(Level.OVERWORLD, false, true, 10_000, 100_000, 20_000);
            Endpoint receiver = new Endpoint(Level.OVERWORLD, false, false, 0, 100_000, 20_000);
            ResonanceGrid.transfer(List.of(sender, receiver), 0);
            helper.assertValueEqual(sender.energy, 0, "sender energy");
            helper.assertValueEqual(receiver.energy, expected(10_000, NTConfig.resonanceSameDimensionLoss), "receiver energy after loss");
            helper.succeed();
        });

        r.add("resonance/throughput_caps_each_pylon", 20, helper -> {
            Endpoint sender = new Endpoint(Level.OVERWORLD, false, true, 100_000, 100_000, 20_000);
            Endpoint receiver = new Endpoint(Level.OVERWORLD, false, false, 0, 1_000_000, 1_000_000);
            ResonanceGrid.transfer(List.of(sender, receiver), 0);
            helper.assertValueEqual(sender.energy, 80_000, "a sender gives at most its throughput per tick");
            helper.succeed();
        });

        r.add("resonance/other_dimensions_need_two_abyssal_pylons", 20, helper -> {
            Endpoint basicSender = new Endpoint(Level.NETHER, false, true, 10_000, 100_000, 50_000);
            Endpoint abyssalReceiver = new Endpoint(Level.OVERWORLD, true, false, 0, 100_000, 50_000);
            ResonanceGrid.transfer(List.of(basicSender, abyssalReceiver), 0);
            helper.assertValueEqual(abyssalReceiver.energy, 0, "a basic pylon in another dimension sends nothing");

            Endpoint abyssalSender = new Endpoint(Level.NETHER, true, true, 10_000, 100_000, 50_000);
            ResonanceGrid.transfer(List.of(abyssalSender, abyssalReceiver), 0);
            helper.assertValueEqual(abyssalReceiver.energy, expected(10_000, NTConfig.resonanceCrossDimensionLoss), "abyssal to abyssal across dimensions");
            helper.succeed();
        });

        r.add("resonance/same_dimension_senders_go_first", 20, helper -> {
            Endpoint far = new Endpoint(Level.NETHER, true, true, 10_000, 100_000, 50_000);
            Endpoint near = new Endpoint(Level.OVERWORLD, true, true, 10_000, 100_000, 50_000);
            Endpoint receiver = new Endpoint(Level.OVERWORLD, true, false, 0, 5_000, 50_000);
            ResonanceGrid.transfer(List.of(far, near, receiver), 0);
            helper.assertValueEqual(far.energy, 10_000, "the cheaper same-dimension sender fills the receiver");
            helper.assertValueEqual(receiver.energy, 5_000, "receiver filled");
            helper.succeed();
        });

        r.add("resonance/access_follows_owner_and_trust", 20, helper -> {
            UUID owner = UUID.randomUUID();
            UUID friend = UUID.randomUUID();
            UUID stranger = UUID.randomUUID();
            ResonanceNetwork network = new ResonanceNetwork(UUID.randomUUID(), "Base", owner, "Owner", Map.of(friend, "Friend"), true);
            helper.assertTrue(network.canAccess(owner), "the owner has access");
            helper.assertTrue(network.canAccess(friend), "a trusted player has access");
            helper.assertFalse(network.canAccess(stranger), "a stranger has no access, even with team access on and no shared team");
            helper.succeed();
        });

        r.add("resonance/pylons_carry_fe_across_a_network", 60, helper -> {
            ServerPlayer owner = player(helper, "GridOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetworks.Result created = networks.create(owner, "Gametest grid " + UUID.randomUUID().toString().substring(0, 6));
            helper.assertTrue(created.success(), "the network is created, " + created.error());
            ResonanceNetwork network = created.network();

            BlockPos sendPos = helper.absolutePos(new BlockPos(1, 1, 1));
            BlockPos receivePos = helper.absolutePos(new BlockPos(6, 1, 6));
            helper.getLevel().setBlock(sendPos, NTBlocks.RESONANCE_PYLON.get().defaultBlockState(), Block.UPDATE_ALL);
            helper.getLevel().setBlock(receivePos, NTBlocks.RESONANCE_PYLON.get().defaultBlockState(), Block.UPDATE_ALL);
            ResonancePylonBlockEntity sender = (ResonancePylonBlockEntity) helper.getLevel().getBlockEntity(sendPos);
            ResonancePylonBlockEntity receiver = (ResonancePylonBlockEntity) helper.getLevel().getBlockEntity(receivePos);
            sender.setNetwork(network);
            receiver.setNetwork(network);
            receiver.setSendMode(false);

            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(sender.getPort().insert(10_000, tx), 10_000, "a sending pylon accepts FE");
                tx.commit();
            }
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(receiver.getPort().insert(10_000, tx), 0, "a receiving pylon refuses FE from cables");
            }

            helper.succeedWhen(() -> {
                helper.assertValueEqual(sender.getEnergyStorage().getAmountAsInt(), 0, "the sender emptied");
                helper.assertValueEqual(receiver.getEnergyStorage().getAmountAsInt(), expected(10_000, NTConfig.resonanceSameDimensionLoss),
                        "the receiver got the FE less the loss");
                networks.delete(owner, network.id());
            });
        });

        r.add("resonance/networks_are_written_to_disk", 20, helper -> {
            ServerPlayer owner = player(helper, "Saver");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Saved grid").network();
            networks.trust(owner, network.id(), UUID.randomUUID(), "Friend");
            helper.assertTrue(networks.isDirty(), "creating a network marks the data for saving");
            helper.getLevel().getServer().overworld().getDataStorage().saveAndJoin();
            helper.assertFalse(networks.isDirty(), "saving writes it out");

            Tag encoded = ResonanceNetworks.CODEC.encodeStart(NbtOps.INSTANCE, networks).getOrThrow();
            ResonanceNetworks decoded = ResonanceNetworks.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
            ResonanceNetwork loaded = decoded.get(network.id());
            helper.assertTrue(loaded != null, "the network comes back after loading");
            helper.assertValueEqual(loaded.name(), "Saved grid", "name");
            helper.assertValueEqual(loaded.owner(), owner.getUUID(), "owner");
            helper.assertValueEqual(loaded.trusted().size(), 1, "trusted players");
            networks.delete(owner, network.id());
            helper.succeed();
        });

        r.add("resonance/strangers_cannot_use_a_network", 20, helper -> {
            ServerPlayer owner = player(helper, "NetOwner");
            ServerPlayer stranger = player(helper, "Stranger");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Private").network();
            helper.assertTrue(ResonanceNetworks.canUse(owner, network), "the owner can use it");
            helper.assertFalse(ResonanceNetworks.canUse(stranger, network), "a stranger cannot");
            helper.assertFalse(networks.trust(stranger, network.id(), stranger.getUUID(), "Sneak").success(), "a stranger cannot trust themselves");
            helper.assertTrue(networks.trust(owner, network.id(), stranger.getUUID(), "Friend").success(), "the owner can trust them");
            helper.assertTrue(ResonanceNetworks.canUse(stranger, network), "now they can use it");
            helper.assertFalse(networks.delete(stranger, network.id()).success(), "a trusted player still cannot delete it");
            networks.delete(owner, network.id());
            helper.succeed();
        });
    }
}
