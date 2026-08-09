package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import java.util.List;
import com.breakinblocks.nautec.events.LuckyFishingZoneEvents;
import com.breakinblocks.nautec.loot.InLuckyFishingZoneCondition;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public final class LuckyZoneTests {
    public static void register(NTTestRegistrar r) {
        r.add("fishing/radius_requires_every_block_to_be_water", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos centre = pool(helper, 4);

            int open = LuckyFishingZoneEvents.largestOpenRadius(level, centre);
            if (open < 3) {
                helper.fail("A wide open pool should allow the maximum radius, got " + open);
                return;
            }

            level.setBlockAndUpdate(centre.offset(2, -1, 0), Blocks.STONE.defaultBlockState());
            int blocked = LuckyFishingZoneEvents.largestOpenRadius(level, centre);
            if (blocked >= 2) {
                helper.fail("A stone block two out should cap the radius below 2, got " + blocked);
                return;
            }

            level.setBlockAndUpdate(centre.offset(1, -1, 0), Blocks.STONE.defaultBlockState());
            if (LuckyFishingZoneEvents.largestOpenRadius(level, centre) != 0) {
                helper.fail("A stone block directly adjacent should leave no usable radius");
            }
            helper.succeed();
        });

        r.add("fishing/zone_block_dies_when_water_goes", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos centre = pool(helper, 2);

            level.setBlock(centre, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
            if (!level.getBlockState(centre).is(NTBlocks.LUCKY_FISHING_ZONE.get())) {
                helper.fail("Zone block would not place on open water");
                return;
            }

            level.setBlockAndUpdate(centre.below(), Blocks.STONE.defaultBlockState());
            if (level.getBlockState(centre).is(NTBlocks.LUCKY_FISHING_ZONE.get())) {
                helper.fail("Zone block survived after its water was replaced with stone");
            }
            helper.succeed();
        });

        r.add("fishing/zone_block_stores_its_radius", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos centre = pool(helper, 3);

            level.setBlock(centre, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
            if (!(level.getBlockEntity(centre) instanceof LuckyFishingZoneBlockEntity zone)) {
                helper.fail("Zone block has no block entity");
                return;
            }
            zone.setRadius(3);
            if (zone.getRadius() != 3) {
                helper.fail("Zone radius did not round-trip through the block entity");
            }
            helper.succeed();
        });

        r.add("fishing/index_self_heals_when_block_is_gone", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos ghost = helper.absolutePos(new BlockPos(1, 5, 1));
            LuckyZoneIndex index = LuckyZoneIndex.get(level);
            int before = index.size();

            index.add(new LuckyZoneIndex.Zone(ghost, 2, level.getGameTime() + 10000L));
            if (index.zoneAt(level, ghost) != null) {
                helper.fail("The index returned a zone whose block does not exist");
                return;
            }
            if (index.size() != before) {
                helper.fail("The index did not drop the stale entry it just rejected");
            }
            helper.succeed();
        });

        r.add("fishing/loot_condition_tracks_the_zone", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos centre = pool(helper, 3);
            level.setBlock(centre, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);

            LuckyZoneIndex index = LuckyZoneIndex.get(level);
            index.add(new LuckyZoneIndex.Zone(centre, 2, level.getGameTime() + 10000L));

            if (!test(level, centre)) {
                helper.fail("The loot condition did not match at the centre of a live zone");
                return;
            }
            if (!test(level, centre.offset(2, 0, 0))) {
                helper.fail("The loot condition did not match inside the zone radius");
                return;
            }
            if (test(level, centre.offset(9, 0, 0))) {
                helper.fail("The loot condition matched well outside the zone radius");
                return;
            }

            level.removeBlock(centre, false);
            if (test(level, centre)) {
                helper.fail("The loot condition still matched after the zone block was removed");
            }
            index.remove(centre);
            helper.succeed();
        });

        registerMinigame(r);
    }

    private static void registerMinigame(NTTestRegistrar r) {
        r.add("fishing/minigame_windows_stay_inside_the_bar", 5, helper -> {
            for (MinigameKind kind : MinigameKind.values()) {
                for (long seed = -400; seed < 400; seed++) {
                    for (int[] window : kind.windows(seed)) {
                        if (window[0] < 0 || window[0] + window[1] > FishingMinigame.DURATION_TICKS || window[1] < 1) {
                            helper.fail(kind + " seed " + seed + " produced a bad window: "
                                    + window[0] + ".." + (window[0] + window[1]));
                            return;
                        }
                    }
                }
            }
            helper.succeed();
        });

        r.add("fishing/every_minigame_can_be_won_and_lost", 5, helper -> {
            long seed = 123456789L;

            for (MinigameKind kind : MinigameKind.values()) {
                if (!kind.validate(seed, winningReport(kind, seed))) {
                    helper.fail(kind + " rejected a report that plays it perfectly");
                    return;
                }
                int[] miss = winningReport(kind, seed);
                miss[0] = kind.windows(seed).getFirst()[0] - 1;
                if (kind.validate(seed, miss)) {
                    helper.fail(kind + " accepted a strike one tick before its window");
                    return;
                }
                if (kind.validate(seed, new int[0])) {
                    helper.fail(kind + " accepted an empty report");
                    return;
                }
                int[] outOfRange = winningReport(kind, seed);
                outOfRange[0] = FishingMinigame.DURATION_TICKS + 50;
                if (kind.validate(seed, outOfRange)) {
                    helper.fail(kind + " accepted a tick outside the challenge entirely");
                    return;
                }
            }
            helper.succeed();
        });

        r.add("fishing/rhythm_requires_strikes_in_order", 5, helper -> {
            long seed = 999L;
            int[] good = winningReport(MinigameKind.RHYTHM, seed);
            int[] swapped = new int[]{good[2], good[1], good[0]};
            if (MinigameKind.RHYTHM.validate(seed, swapped)) {
                helper.fail("Rhythm accepted three strikes reported out of order");
            }
            helper.succeed();
        });

        r.add("fishing/hold_requires_a_real_hold", 5, helper -> {
            long seed = 4242L;
            int[] window = MinigameKind.HOLD.windows(seed).getFirst();
            if (MinigameKind.HOLD.validate(seed, new int[]{window[0], window[0]})) {
                helper.fail("Hold accepted an instant tap as a hold");
            }
            helper.succeed();
        });

        r.add("fishing/minigame_rewards_follow_the_rule", 5, helper -> {
            if (FishingMinigame.rewardTables(false, false).size() != 1
                    || FishingMinigame.rewardTables(false, true).size() != 1) {
                helper.fail("Without a minigame win the reward should be a single roll");
                return;
            }

            List<ResourceKey<LootTable>> plainWin = FishingMinigame.rewardTables(true, false);
            if (plainWin.size() != 2 || !plainWin.contains(NTLootTables.LUCKY_ZONE_TREASURE)) {
                helper.fail("A win on a non treasure catch should add exactly one treasure roll, got " + plainWin);
                return;
            }

            List<ResourceKey<LootTable>> treasureWin = FishingMinigame.rewardTables(true, true);
            long treasureRolls = treasureWin.stream().filter(NTLootTables.LUCKY_ZONE_TREASURE::equals).count();
            long catchRolls = treasureWin.stream().filter(NTLootTables.LUCKY_ZONE_CATCH::equals).count();
            if (treasureRolls != 2 || catchRolls != 1) {
                helper.fail("A win on a treasure catch should give 2 treasure rolls and 1 catch roll, got " + treasureWin);
            }
            helper.succeed();
        });
    }

    private static int[] winningReport(MinigameKind kind, long seed) {
        List<int[]> windows = kind.windows(seed);
        return switch (kind) {
            case TIMING_BAR -> new int[]{windows.getFirst()[0]};
            case RHYTHM -> new int[]{windows.get(0)[0], windows.get(1)[0], windows.get(2)[0]};
            case HOLD -> new int[]{windows.getFirst()[0], windows.getFirst()[0] + windows.getFirst()[1] - 1};
        };
    }

    private static boolean test(ServerLevel level, BlockPos pos) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .create(LootContextParamSets.FISHING);
        return InLuckyFishingZoneCondition.INSTANCE.test(new LootContext.Builder(params).create(java.util.Optional.empty()));
    }

    private static BlockPos pool(GameTestHelper helper, int half) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int dx = -half - 1; dx <= half + 1; dx++) {
            for (int dz = -half - 1; dz <= half + 1; dz++) {
                level.setBlockAndUpdate(centre.offset(dx, -1, dz), Blocks.WATER.defaultBlockState());
                level.setBlockAndUpdate(centre.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
            }
        }
        NTConfig.luckyZoneMinRadius = 1;
        NTConfig.luckyZoneMaxRadius = 3;
        return centre;
    }

    private LuckyZoneTests() {
    }
}
