package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.items.blocks.PrismarineCrystalItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class CrystalCultivationTests {
    private static final BlockPos CRADLE = new BlockPos(4, 1, 4);
    private static final BlockPos CORE = CRADLE.above(CrystalCradleBlockEntity.CRYSTAL_OFFSET);

    private CrystalCultivationTests() {
    }

    private static CrystalCradleBlockEntity seededCradle(GameTestHelper helper) {
        helper.setBlock(CRADLE, NTBlocks.CRYSTAL_CRADLE.get());
        CrystalCradleBlockEntity cradle = helper.getBlockEntity(CRADLE, CrystalCradleBlockEntity.class);
        helper.assertTrue(cradle.insertSeed(new ItemStack(NTItems.PRISMARINE_CRYSTAL_SEED.get())), "an empty cradle takes a seed");
        return cradle;
    }

    private static void feed(GameTestHelper helper, int power, float purity) {
        helper.onEachTick(() -> {
            CrystalCradleBlockEntity cradle = helper.getBlockEntity(CRADLE, CrystalCradleBlockEntity.class);
            BlockPos origin = helper.absolutePos(CRADLE.north(2));
            cradle.receivePower(power, Direction.SOUTH, origin);
            cradle.receiveNewPurity(purity, Direction.SOUTH, origin);
        });
    }

    private static Player wrenchPlayer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.AQUARINE_WRENCH.get()));
        player.setShiftKeyDown(true);
        return player;
    }

    private static boolean holdsCultivatedCrystal(Player player) {
        for (ItemStack stack : player.getInventory()) {
            if (stack.is(NTBlocks.PRISMARINE_CRYSTAL.get().asItem()) && PrismarineCrystalItem.isCultivated(stack)) {
                return true;
            }
        }
        return false;
    }

    public static void register(NTTestRegistrar r) {
        r.add("crystal/cradle_grows_a_cultivated_crystal", 80, helper -> {
            CrystalCradleBlockEntity cradle = seededCradle(helper);
            cradle.setGrowth(NTConfig.crystalGrowthPower - 500);
            feed(helper, 100, 2.5F);
            helper.succeedWhen(() -> {
                helper.assertTrue(helper.getBlockState(CORE).is(NTBlocks.PRISMARINE_CRYSTAL.get()), "a crystal core stands four blocks up");
                for (int y = 1; y <= 6; y++) {
                    Block block = helper.getBlockState(CRADLE.above(y)).getBlock();
                    helper.assertTrue(block == NTBlocks.PRISMARINE_CRYSTAL.get() || block == NTBlocks.PRISMARINE_CRYSTAL_PART.get(),
                            "crystal block at height " + y);
                }
                PrismarineCrystalBlockEntity crystal = helper.getBlockEntity(CORE, PrismarineCrystalBlockEntity.class);
                helper.assertTrue(crystal.isCultivated(), "the grown crystal is cultivated");
                helper.assertTrue(!cradle.hasSeed(), "the seed is used up");
                helper.assertValueEqual(0L, cradle.getGrowth(), "growth resets");
            });
        });

        r.add("crystal/low_purity_beams_add_nothing", 40, helper -> {
            CrystalCradleBlockEntity cradle = seededCradle(helper);
            feed(helper, 500, (float) NTConfig.crystalGrowthPurity - 0.5F);
            helper.runAfterDelay(20, () -> {
                helper.assertValueEqual(CrystalCradleBlockEntity.Status.LOW_PURITY, cradle.getStatus(), "status below the purity gate");
                helper.assertValueEqual(0L, cradle.getGrowth(), "no growth below the purity gate");
                helper.succeed();
            });
        });

        r.add("crystal/growth_counts_every_ap", 40, helper -> {
            CrystalCradleBlockEntity cradle = seededCradle(helper);
            feed(helper, 250, 3.0F);
            helper.runAfterDelay(20, () -> {
                helper.assertValueEqual(CrystalCradleBlockEntity.Status.GROWING, cradle.getStatus(), "status with a pure beam");
                long growth = cradle.getGrowth();
                helper.assertTrue(growth > 0 && growth % 250 == 0, "growth rises by the beam's AP each tick, got " + growth);
                helper.succeed();
            });
        });

        r.add("crystal/no_room_pauses_growth", 40, helper -> {
            CrystalCradleBlockEntity cradle = seededCradle(helper);
            helper.setBlock(CRADLE.above(5), Blocks.STONE);
            feed(helper, 500, 3.0F);
            helper.runAfterDelay(25, () -> {
                helper.assertValueEqual(CrystalCradleBlockEntity.Status.BLOCKED, cradle.getStatus(), "status with a block in the way");
                helper.assertValueEqual(0L, cradle.getGrowth(), "no growth without room");
                helper.succeed();
            });
        });

        r.add("crystal/broken_cradle_keeps_seed_and_growth", 20, helper -> {
            CrystalCradleBlockEntity cradle = seededCradle(helper);
            cradle.setGrowth(1_234_567L);
            List<ItemStack> drops = Block.getDrops(helper.getBlockState(CRADLE), helper.getLevel(), helper.absolutePos(CRADLE), cradle);
            ItemStack drop = drops.stream().filter(stack -> stack.is(NTBlocks.CRYSTAL_CRADLE.get().asItem())).findFirst().orElse(ItemStack.EMPTY);
            helper.assertTrue(!drop.isEmpty(), "the cradle drops itself");
            helper.assertTrue(Boolean.TRUE.equals(drop.get(NTDataComponents.CRADLE_SEEDED)), "the drop keeps the seed");
            helper.assertValueEqual(1_234_567L, drop.get(NTDataComponents.CRADLE_GROWTH), "the drop keeps the growth");
            helper.assertTrue(cradle.removeSeed().isEmpty(), "a growing seed cannot be pulled out by hand");
            helper.succeed();
        });

        r.add("crystal/wrench_picks_up_a_cultivated_crystal", 20, helper -> {
            PrismarineCrystalBlock.build(helper.getLevel(), helper.absolutePos(CORE), true);
            Player player = wrenchPlayer(helper);
            BlockPos clicked = helper.absolutePos(CORE.below(2));
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, clicked,
                    new BlockHitResult(Vec3.atCenterOf(clicked), Direction.NORTH, clicked, false)));
            helper.assertTrue(helper.getBlockState(CORE).isAir(), "the crystal is lifted out");
            helper.assertTrue(holdsCultivatedCrystal(player), "the player gets a cultivated crystal");
            helper.succeed();
        });

        r.add("crystal/natural_crystals_stay_rooted", 20, helper -> {
            PrismarineCrystalBlock.build(helper.getLevel(), helper.absolutePos(CORE), false);
            Player player = wrenchPlayer(helper);
            BlockPos clicked = helper.absolutePos(CORE);
            NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, clicked,
                    new BlockHitResult(Vec3.atCenterOf(clicked), Direction.NORTH, clicked, false)));
            helper.assertTrue(helper.getBlockState(CORE).is(NTBlocks.PRISMARINE_CRYSTAL.get()), "a natural crystal stays put");
            helper.assertTrue(!holdsCultivatedCrystal(player), "no crystal item for a natural crystal");
            helper.succeed();
        });

        r.add("crystal/placing_a_cultivated_crystal_keeps_it_movable", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack stack = PrismarineCrystalBlock.cultivatedItem();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            BlockPos floor = helper.absolutePos(CRADLE);
            helper.setBlock(CRADLE, Blocks.STONE);
            stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false)));
            PrismarineCrystalBlockEntity crystal = helper.getBlockEntity(CORE, PrismarineCrystalBlockEntity.class);
            helper.assertTrue(crystal.isCultivated(), "a placed cultivated crystal is still cultivated");
            helper.succeed();
        });
    }
}
