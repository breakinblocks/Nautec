package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class FishingHookFlowTests {
    public static void register(NTTestRegistrar r) {
        r.add("fishing/custom_hook_reaches_a_bite", 100, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos surface = pool(helper);
            NautecFishingHook hook = cast(helper, surface);

            FishingHookAccessor accessor = (FishingHookAccessor) hook;
            for (int i = 0; i < 400 && accessor.nautec$getNibble() <= 0; i++) {
                if (accessor.nautec$getTimeUntilLured() > 1) {
                    accessor.nautec$setTimeUntilLured(1);
                }
                if (accessor.nautec$getTimeUntilHooked() > 1) {
                    accessor.nautec$setTimeUntilHooked(1);
                }
                hook.tick();
            }

            if (hook.isRemoved()) {
                helper.fail("The custom hook was discarded before it ever got a bite");
                return;
            }
            if (accessor.nautec$getNibble() <= 0) {
                helper.fail("The custom hook never registered a bite over open water");
                return;
            }
            helper.succeed();
        });

        r.add("fishing/live_bite_over_plain_water", 400, 1, helper ->
                liveBite(helper, false));

        r.add("fishing/live_bite_inside_a_lucky_zone", 400, 1, helper ->
                liveBite(helper, true));
    }

    private static void liveBite(GameTestHelper helper, boolean withZone) {
        ServerLevel level = helper.getLevel();
        BlockPos surface = pool(helper);

        if (withZone) {
            BlockPos zonePos = surface.above();
            level.setBlock(zonePos, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
            if (level.getBlockEntity(zonePos) instanceof LuckyFishingZoneBlockEntity zone) {
                zone.setRadius(3);
            }
            LuckyZoneIndex.get(level).add(new LuckyZoneIndex.Zone(zonePos, 3, level.getGameTime() + 100000L));
            if (!level.getBlockState(zonePos).is(NTBlocks.LUCKY_FISHING_ZONE.get())) {
                helper.fail("The lucky zone block would not stay placed on the test pool");
                return;
            }
        }

        NautecFishingHook hook = cast(helper, surface);
        FishingHookAccessor accessor = (FishingHookAccessor) hook;
        accessor.nautec$setTimeUntilLured(30);

        int[] bites = new int[1];
        for (int t = 1; t <= 340; t++) {
            helper.runAfterDelay(t, () -> {
                if (accessor.nautec$getNibble() > 0) {
                    bites[0]++;
                }
            });
        }

        helper.runAfterDelay(350, () -> {
            String where = withZone ? "inside a lucky zone" : "over plain water";
            if (bites[0] <= 0) {
                helper.fail("No bite " + where + " in 340 ticks"
                        + " (removed=" + hook.isRemoved()
                        + " lured=" + accessor.nautec$getTimeUntilLured()
                        + " hooked=" + accessor.nautec$getTimeUntilHooked()
                        + " y=" + String.format("%.3f", hook.getY())
                        + " inWater=" + hook.isInWater() + ")");
                return;
            }
            helper.succeed();
        });
    }

    private static NautecFishingHook cast(GameTestHelper helper, BlockPos surface) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 3.5);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.NAUTEC_FISHING_ROD.get()));

        NautecFishingHook hook = new NautecFishingHook(owner, level, 0, 0);
        hook.snapTo(surface.getX() + 0.5, surface.getY() + 0.4, surface.getZ() + 0.5);
        hook.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(hook);
        return hook;
    }

    private static BlockPos pool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlockAndUpdate(centre.offset(dx, -1, dz), Blocks.WATER.defaultBlockState());
                level.setBlockAndUpdate(centre.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
            }
        }
        return centre.below();
    }

    private FishingHookFlowTests() {
    }
}
