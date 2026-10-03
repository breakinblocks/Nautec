package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.resonance.PrismaticEmitterBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.UUID;

public final class PrismaticEmitterTests {
    private static final BlockPos EMITTER = new BlockPos(4, 1, 4);

    private PrismaticEmitterTests() {
    }

    private static PrismaticEmitterBlockEntity emitter(GameTestHelper helper) {
        helper.getLevel().setBlock(helper.absolutePos(EMITTER), NTBlocks.PRISMATIC_EMITTER.get().defaultBlockState(), Block.UPDATE_ALL);
        return (PrismaticEmitterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(EMITTER));
    }

    private static BlockPos converter(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        helper.getLevel().setBlock(pos, NTBlocks.ENERGY_CONVERTER.get().defaultBlockState(), Block.UPDATE_ALL);
        return pos;
    }

    private static int stored(GameTestHelper helper, BlockPos pos) {
        return ((EnergyConverterBlockEntity) helper.getLevel().getBlockEntity(pos)).getFeBuffer().getAmountAsInt();
    }

    private static ServerPlayer player(GameTestHelper helper, String name) {
        return new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), new GameProfile(UUID.randomUUID(), name),
                ClientInformation.createDefault());
    }

    public static void register(NTTestRegistrar r) {
        r.add("emitter/feeds_every_linked_machine", 60, helper -> {
            PrismaticEmitterBlockEntity emitter = emitter(helper);
            BlockPos first = converter(helper, new BlockPos(1, 1, 1));
            BlockPos second = converter(helper, new BlockPos(7, 1, 7));
            helper.assertValueEqual(emitter.toggle(first, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.LINKED, "first link");
            helper.assertValueEqual(emitter.toggle(second, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.LINKED, "second link");
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(emitter.getPort().insert(20_000, tx), 20_000, "the emitter takes FE from cables");
                tx.commit();
            }
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(emitter.getPort().extract(100, tx), 0, "nothing can pull FE back out of an emitter");
            }
            helper.succeedWhen(() -> {
                helper.assertValueEqual(emitter.getEnergyStorage().getAmountAsInt(), 0, "the emitter sent everything");
                helper.assertValueEqual(stored(helper, first) + stored(helper, second), 20_000, "nothing is lost on a local link");
                helper.assertTrue(stored(helper, first) > 0 && stored(helper, second) > 0, "both machines were fed");
            });
        });

        r.add("emitter/sends_no_more_than_its_throughput", 20, helper -> {
            PrismaticEmitterBlockEntity emitter = emitter(helper);
            BlockPos target = converter(helper, new BlockPos(1, 1, 1));
            emitter.toggle(target, Direction.UP);
            emitter.getEnergyStorage().set(NTConfig.emitterBuffer);
            helper.runAfterDelay(1, () -> {
                int sent = NTConfig.emitterBuffer - emitter.getEnergyStorage().getAmountAsInt();
                helper.assertTrue(sent > 0 && sent <= NTConfig.emitterThroughput, "one tick sends at most the throughput, sent " + sent);
                helper.succeed();
            });
        });

        r.add("emitter/refuses_bad_links", 20, helper -> {
            PrismaticEmitterBlockEntity emitter = emitter(helper);
            BlockPos stone = helper.absolutePos(new BlockPos(2, 1, 2));
            helper.getLevel().setBlock(stone, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            helper.assertValueEqual(emitter.toggle(stone, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.NO_ENERGY, "stone takes no FE");

            BlockPos pylon = helper.absolutePos(new BlockPos(6, 1, 2));
            helper.getLevel().setBlock(pylon, NTBlocks.RESONANCE_PYLON.get().defaultBlockState(), Block.UPDATE_ALL);
            helper.assertValueEqual(emitter.toggle(pylon, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.NO_ENERGY, "pylons cannot be linked");

            int range = NTConfig.emitterRange;
            NTConfig.emitterRange = 2;
            try {
                BlockPos far = converter(helper, new BlockPos(8, 1, 8));
                helper.assertValueEqual(emitter.toggle(far, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.TOO_FAR, "out of range");
            } finally {
                NTConfig.emitterRange = range;
            }

            BlockPos near = converter(helper, new BlockPos(4, 1, 6));
            helper.assertValueEqual(emitter.toggle(near, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.LINKED, "a converter links");
            helper.assertValueEqual(emitter.toggle(near, Direction.UP), PrismaticEmitterBlockEntity.LinkResult.UNLINKED, "a second click unlinks");
            helper.assertTrue(emitter.getLinks().isEmpty(), "no links left");
            helper.succeed();
        });

        r.add("emitter/only_the_owner_can_tune", 20, helper -> {
            PrismaticEmitterBlockEntity emitter = emitter(helper);
            ServerPlayer owner = player(helper, "EmitterOwner");
            ServerPlayer stranger = player(helper, "Passerby");
            emitter.setOwner(owner.getUUID(), "EmitterOwner");
            helper.assertTrue(emitter.canTune(owner), "the owner can tune it");
            helper.assertFalse(emitter.canTune(stranger), "someone else cannot");
            helper.succeed();
        });
    }
}
