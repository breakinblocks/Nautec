package com.breakinblocks.nautec.gametest.suite;

import appeng.api.networking.IGrid;
import appeng.api.parts.PartHelper;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.me.service.P2PService;
import appeng.util.SettingsFrom;
import com.breakinblocks.nautec.compat.ae2.AE2Content;
import com.breakinblocks.nautec.compat.ae2.LaserP2PTunnelPart;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.PressureForgeBlockEntity;
import com.breakinblocks.nautec.content.distributor.DistributorBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jetbrains.annotations.Nullable;

final class AE2TestNetworks {
    private AE2TestNetworks() {
    }

    private static void cable(GameTestHelper helper, BlockPos pos) {
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(pos), null, null, AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT));
    }

    private static @Nullable LaserP2PTunnelPart tunnel(GameTestHelper helper, BlockPos pos, Direction side) {
        return PartHelper.getPart(helper.getLevel(), helper.absolutePos(pos), side) instanceof LaserP2PTunnelPart part ? part : null;
    }

    private static int count(Container container) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).is(NTItems.AQUARINE_STEEL_INGOT.get())) {
                total += container.getItem(i).getCount();
            }
        }
        return total;
    }

    static void distributorPulls(GameTestHelper helper, boolean part) {
        ServerLevel level = helper.getLevel();
        DistributorBlockEntity distributor = StorageNetworkCompatTests.distributor(helper);
        BlockPos interfacePos = new BlockPos(4, 1, 5);
        BlockPos cellPos = new BlockPos(4, 1, 6);
        BlockPos busPos = new BlockPos(5, 1, 6);
        BlockPos chestPos = new BlockPos(6, 1, 6);
        helper.setBlock(cellPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        if (part) {
            cable(helper, interfacePos);
            PartHelper.setPart(level, helper.absolutePos(interfacePos), Direction.NORTH, null, AEParts.INTERFACE.get());
        } else {
            helper.setBlock(interfacePos, AEBlocks.INTERFACE.block());
        }
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get(), StorageNetworkCompatTests.STOCKED));
        cable(helper, busPos);
        PartHelper.setPart(level, helper.absolutePos(busPos), Direction.EAST, null, AEParts.STORAGE_BUS.get());
        PressureForgeBlockEntity[] forge = new PressureForgeBlockEntity[1];
        int[] expected = new int[1];
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(PartHelper.getPart(level, helper.absolutePos(busPos), Direction.EAST) != null
                        && PartHelper.getPart(level, helper.absolutePos(busPos), Direction.EAST).getGridNode() != null
                        && PartHelper.getPart(level, helper.absolutePos(busPos), Direction.EAST).getGridNode().isActive(), "waiting for the ME network"))
                .thenExecute(() -> {
                    forge[0] = StorageNetworkCompatTests.forgeWanting(helper, distributor);
                    expected[0] = StorageNetworkCompatTests.ALREADY_LOADED + Math.min(StorageNetworkCompatTests.STOCKED, StorageNetworkCompatTests.slotSpace(forge[0]));
                })
                .thenWaitUntil(() -> helper.assertValueEqual(expected[0], StorageNetworkCompatTests.loaded(forge[0]), "the forge fills from the ME network"))
                .thenExecute(() -> helper.assertValueEqual(StorageNetworkCompatTests.ALREADY_LOADED + StorageNetworkCompatTests.STOCKED - expected[0], count(chest),
                        "the network gives up exactly what the forge took"))
                .thenSucceed();
    }

    private static IGrid link(GameTestHelper helper, BlockPos inputPos, Direction inputSide, BlockPos outputPos, Direction... outputSides) {
        LaserP2PTunnelPart input = tunnel(helper, inputPos, inputSide);
        helper.assertTrue(input != null, "input tunnel placed");
        IGrid grid = input.getMainNode().getGrid();
        helper.assertTrue(grid != null, "input tunnel on a grid");
        P2PService p2p = P2PService.get(grid);
        input.setFrequency(p2p.newFrequency());
        p2p.updateFreq(input, input.getFrequency());
        DataComponentMap.Builder settings = DataComponentMap.builder();
        input.exportSettings(SettingsFrom.MEMORY_CARD, settings);
        DataComponentMap exported = settings.build();
        for (Direction side : outputSides) {
            LaserP2PTunnelPart output = tunnel(helper, outputPos, side);
            helper.assertTrue(output != null, "output tunnel placed");
            output.importSettings(SettingsFrom.MEMORY_CARD, exported, null);
        }
        return grid;
    }

    private static boolean tunnelsActive(GameTestHelper helper, BlockPos pos, Direction side) {
        LaserP2PTunnelPart part = tunnel(helper, pos, side);
        return part != null && part.getMainNode().isActive();
    }

    static void laserTunnel(GameTestHelper helper) {
        BlockPos source = new BlockPos(2, 1, 4);
        BlockPos inputBus = new BlockPos(4, 1, 4);
        BlockPos outputBus = new BlockPos(4, 1, 6);
        BlockPos mixerPos = new BlockPos(5, 1, 6);
        PowerAndLaserTests.placeShieldedSource(helper, source, Direction.EAST);
        helper.setBlock(new BlockPos(4, 1, 5), AEBlocks.CREATIVE_ENERGY_CELL.block());
        cable(helper, inputBus);
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(inputBus), Direction.WEST, null, AE2Content.LASER_P2P_TUNNEL.get());
        cable(helper, outputBus);
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(outputBus), Direction.EAST, null, AE2Content.LASER_P2P_TUNNEL.get());
        helper.setBlock(mixerPos, NTBlocks.MIXER.get());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(tunnelsActive(helper, inputBus, Direction.WEST) && tunnelsActive(helper, outputBus, Direction.EAST),
                        "waiting for the tunnels to come online"))
                .thenExecute(() -> link(helper, inputBus, Direction.WEST, outputBus, Direction.EAST))
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(2, helper.getBlockEntity(source, CreativePowerSourceBlockEntity.class).getLaserDistances().getInt(Direction.EAST),
                            "the beam connects to the input tunnel");
                    helper.assertValueEqual(100, helper.getBlockEntity(mixerPos, MixerBlockEntity.class).getPower(), "the output tunnel powers the mixer");
                })
                .thenSucceed();
    }

    static void laserTunnelSplits(GameTestHelper helper) {
        BlockPos source = new BlockPos(2, 1, 4);
        BlockPos inputBus = new BlockPos(4, 1, 4);
        BlockPos outputBus = new BlockPos(4, 1, 6);
        BlockPos east = new BlockPos(5, 1, 6);
        BlockPos west = new BlockPos(3, 1, 6);
        PowerAndLaserTests.placeShieldedSource(helper, source, Direction.EAST);
        helper.setBlock(new BlockPos(4, 1, 5), AEBlocks.CREATIVE_ENERGY_CELL.block());
        cable(helper, inputBus);
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(inputBus), Direction.WEST, null, AE2Content.LASER_P2P_TUNNEL.get());
        cable(helper, outputBus);
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(outputBus), Direction.EAST, null, AE2Content.LASER_P2P_TUNNEL.get());
        PartHelper.setPart(helper.getLevel(), helper.absolutePos(outputBus), Direction.WEST, null, AE2Content.LASER_P2P_TUNNEL.get());
        helper.setBlock(east, NTBlocks.MIXER.get());
        helper.setBlock(west, NTBlocks.MIXER.get());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(tunnelsActive(helper, inputBus, Direction.WEST) && tunnelsActive(helper, outputBus, Direction.EAST)
                        && tunnelsActive(helper, outputBus, Direction.WEST), "waiting for the tunnels to come online"))
                .thenExecute(() -> link(helper, inputBus, Direction.WEST, outputBus, Direction.EAST, Direction.WEST))
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(50, helper.getBlockEntity(east, MixerBlockEntity.class).getPower(), "half the beam to the east mixer");
                    helper.assertValueEqual(50, helper.getBlockEntity(west, MixerBlockEntity.class).getPower(), "half the beam to the west mixer");
                })
                .thenSucceed();
    }
}
