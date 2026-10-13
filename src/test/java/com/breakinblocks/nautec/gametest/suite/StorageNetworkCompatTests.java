package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.function.Consumer;

public final class StorageNetworkCompatTests {
    static final BlockPos DISTRIBUTOR = new BlockPos(4, 1, 4);
    static final BlockPos FORGE = new BlockPos(1, 1, 1);
    static final int STOCKED = 40;
    static final int ALREADY_LOADED = 30;

    private StorageNetworkCompatTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("storage_networks/distributor_pulls_from_an_me_interface", 400, ifLoaded("ae2", helper -> AE2TestNetworks.distributorPulls(helper, false)));
        r.add("storage_networks/distributor_pulls_through_an_me_interface_part", 400, ifLoaded("ae2", helper -> AE2TestNetworks.distributorPulls(helper, true)));
        r.add("storage_networks/laser_p2p_tunnel_carries_a_beam", 400, ifLoaded("ae2", helper -> AE2TestNetworks.laserTunnel(helper)));
        r.add("storage_networks/laser_p2p_tunnel_splits_between_outputs", 400, ifLoaded("ae2", helper -> AE2TestNetworks.laserTunnelSplits(helper)));
        r.add("storage_networks/distributor_pulls_from_a_refined_storage_interface", 400,
                ifLoaded("refinedstorage", helper -> RSTestNetworks.distributorPulls(helper)));
    }

    private static Consumer<GameTestHelper> ifLoaded(String modId, Consumer<GameTestHelper> body) {
        return helper -> {
            if (ModList.get().isLoaded(modId)) {
                body.accept(helper);
            } else {
                helper.succeed();
            }
        };
    }

    static DistributorBlockEntity distributor(GameTestHelper helper) {
        helper.setBlock(DISTRIBUTOR, NTBlocks.DISTRIBUTOR.get());
        return helper.getBlockEntity(DISTRIBUTOR, DistributorBlockEntity.class);
    }

    static PressureForgeBlockEntity forgeWanting(GameTestHelper helper, DistributorBlockEntity distributor) {
        helper.setBlock(FORGE, NTBlocks.PRESSURE_FORGE.get());
        PressureForgeBlockEntity forge = helper.getBlockEntity(FORGE, PressureForgeBlockEntity.class);
        helper.assertTrue(forge.setGhost(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get())), "an input slot takes a ghost");
        forge.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get(), ALREADY_LOADED));
        helper.assertValueEqual(DistributorBlockEntity.LinkResult.LINKED, distributor.toggle(helper.absolutePos(FORGE), Direction.NORTH), "link the forge");
        return forge;
    }

    static int loaded(PressureForgeBlockEntity forge) {
        ItemStack stack = forge.getItemStackHandler().getStackInSlot(0);
        return stack.is(NTItems.AQUARINE_STEEL_INGOT.get()) ? stack.getCount() : 0;
    }

    static int slotSpace(PressureForgeBlockEntity forge) {
        return new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get()).getMaxStackSize() - ALREADY_LOADED;
    }
}
