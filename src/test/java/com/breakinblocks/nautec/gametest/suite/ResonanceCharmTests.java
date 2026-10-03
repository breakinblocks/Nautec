package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.resonance.ResonanceBinding;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import com.breakinblocks.nautec.content.resonance.ResonanceNetwork;
import com.breakinblocks.nautec.content.resonance.ResonanceNetworks;
import com.breakinblocks.nautec.content.resonance.ResonancePylonBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

public final class ResonanceCharmTests {
    private ResonanceCharmTests() {
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    private static ResonancePylonBlockEntity sender(GameTestHelper helper, ResonanceNetwork network, int energy) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 4));
        helper.getLevel().setBlock(pos, NTBlocks.RESONANCE_PYLON.get().defaultBlockState(), Block.UPDATE_ALL);
        ResonancePylonBlockEntity pylon = (ResonancePylonBlockEntity) helper.getLevel().getBlockEntity(pos);
        pylon.setNetwork(network);
        pylon.getEnergyStorage().set(energy);
        return pylon;
    }

    private static ItemStack charm(ResonanceNetwork network) {
        ItemStack charm = new ItemStack(NTItems.RESONANCE_CHARM.get());
        charm.set(NTDataComponents.RESONANCE_BINDING.get(), new ResonanceBinding(network.id(), network.name()));
        return charm;
    }

    public static void register(NTTestRegistrar r) {
        r.add("charm/charges_ap_items_from_the_network", 40, helper -> {
            ServerPlayer owner = player(helper, "CharmOwner");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Charm grid").network();
            ResonancePylonBlockEntity pylon = sender(helper, network, 50_000);
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            owner.getInventory().setItem(0, battery);
            ItemStack charm = charm(network);

            helper.runAfterDelay(2, () -> {
                int delivered = ResonanceCharmItem.charge(owner, charm);
                IPowerStorage power = owner.getInventory().getItem(0).getCapability(NTCapabilities.PowerStorage.ITEM);
                helper.assertTrue(delivered > 0, "the charm delivered power");
                helper.assertValueEqual(power.getPowerStored(), delivered, "the battery holds what was delivered");
                int drawn = 50_000 - pylon.getEnergyStorage().getAmountAsInt();
                helper.assertTrue(drawn > delivered, "the pylon gave a little more than arrived, drew " + drawn + " for " + delivered);
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });

        r.add("charm/does_nothing_without_access", 40, helper -> {
            ServerPlayer owner = player(helper, "Owner");
            ServerPlayer thief = player(helper, "Thief");
            ResonanceNetworks networks = ResonanceNetworks.get(helper.getLevel().getServer());
            ResonanceNetwork network = networks.create(owner, "Locked grid").network();
            ResonancePylonBlockEntity pylon = sender(helper, network, 50_000);
            thief.getInventory().setItem(0, new ItemStack(NTItems.PRISMATIC_BATTERY.get()));

            helper.runAfterDelay(2, () -> {
                helper.assertValueEqual(ResonanceCharmItem.charge(thief, charm(network)), 0, "a charm bound by someone without access gives nothing");
                helper.assertValueEqual(pylon.getEnergyStorage().getAmountAsInt(), 50_000, "the pylon kept its power");
                helper.assertValueEqual(ResonanceCharmItem.charge(owner, new ItemStack(NTItems.RESONANCE_CHARM.get())), 0, "an unbound charm gives nothing");
                networks.delete(owner, network.id());
                helper.succeed();
            });
        });
    }
}
