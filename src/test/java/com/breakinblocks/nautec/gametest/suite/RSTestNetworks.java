package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.registries.NTItems;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.iface.InterfaceNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

final class RSTestNetworks {
    private RSTestNetworks() {
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("refinedstorage", path));
    }

    private static @Nullable StorageNetworkComponent storage(NTGameTestHelper helper, BlockPos interfacePos) {
        InterfaceBlockEntity blockEntity = helper.getBlockEntity(interfacePos, InterfaceBlockEntity.class);
        for (InWorldNetworkNodeContainer container : blockEntity.getContainerProvider().getContainers()) {
            if (container.getNode() instanceof InterfaceNetworkNode node && node.isActive()) {
                Network network = node.getNetwork();
                return network == null ? null : network.getComponent(StorageNetworkComponent.class);
            }
        }
        return null;
    }

    static void distributorPulls(NTGameTestHelper helper) {
        DistributorBlockEntity distributor = StorageNetworkCompatTests.distributor(helper);
        BlockPos interfacePos = new BlockPos(4, 1, 5);
        helper.setBlock(interfacePos, block("interface"));
        helper.setBlock(new BlockPos(4, 1, 6), block("creative_controller"));
        helper.setBlock(new BlockPos(5, 1, 6), block("1k_storage_block"));
        ItemResource ingot = new ItemResource(NTItems.AQUARINE_STEEL_INGOT.get());
        PressureForgeBlockEntity[] forge = new PressureForgeBlockEntity[1];
        int[] expected = new int[1];
        helper.startSequence()
                .thenWaitUntil(() -> {
                    StorageNetworkComponent storage = storage(helper, interfacePos);
                    helper.assertTrue(storage != null, "waiting for the Refined Storage network");
                    helper.assertValueEqual((long) StorageNetworkCompatTests.STOCKED,
                            storage.insert(ingot, StorageNetworkCompatTests.STOCKED, Action.SIMULATE, Actor.EMPTY), "waiting for network storage");
                })
                .thenExecute(() -> {
                    helper.assertValueEqual((long) StorageNetworkCompatTests.STOCKED,
                            storage(helper, interfacePos).insert(ingot, StorageNetworkCompatTests.STOCKED, Action.EXECUTE, Actor.EMPTY), "stock the network");
                    forge[0] = StorageNetworkCompatTests.forgeWanting(helper, distributor);
                    expected[0] = StorageNetworkCompatTests.ALREADY_LOADED + Math.min(StorageNetworkCompatTests.STOCKED, StorageNetworkCompatTests.slotSpace(forge[0]));
                })
                .thenWaitUntil(() -> helper.assertValueEqual(expected[0], StorageNetworkCompatTests.loaded(forge[0]), "the forge fills from the Refined Storage network"))
                .thenExecute(() -> helper.assertValueEqual((long) (StorageNetworkCompatTests.ALREADY_LOADED + StorageNetworkCompatTests.STOCKED - expected[0]),
                        storage(helper, interfacePos).get(ingot), "the network gives up exactly what the forge took"))
                .thenSucceed();
    }
}
