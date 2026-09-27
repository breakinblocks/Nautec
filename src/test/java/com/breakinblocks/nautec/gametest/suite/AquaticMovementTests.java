package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class AquaticMovementTests {
    public static void register(NTTestRegistrar registrar) {
        registrar.add("fauna/jelly_can_swim_upward", 220,
                helper -> swimsUpward(helper, NTEntities.LANTERN_JELLY.get()));
        registrar.add("fauna/maw_can_swim_upward", 220,
                helper -> swimsUpward(helper, NTEntities.ABYSSAL_MAW.get()));
    }

    private static void swimsUpward(GameTestHelper helper, EntityType<? extends Mob> type) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getX() == 0 || pos.getX() == 8 || pos.getY() == 0 || pos.getY() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.GLASS : Blocks.WATER);
        }
        Mob mob = type.spawn(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 4)), EntitySpawnReason.COMMAND);
        helper.assertTrue(mob != null, "Aquatic mob must spawn");
        mob.goalSelector.removeAllGoals(goal -> true);
        mob.targetSelector.removeAllGoals(goal -> true);
        mob.setPersistenceRequired();
        Vec3 destination = Vec3.atCenterOf(helper.absolutePos(new BlockPos(5, 5, 4)));
        double startingY = mob.getY();
        helper.onEachTick(() -> {
            mob.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.0);
            if (mob.isAlive() && mob.getY() >= startingY + 2.0 && mob.distanceToSqr(destination) < 4.0) {
                helper.succeed();
            }
        });
        helper.runAfterDelay(200, () -> helper.fail("Could not swim upward: start Y=" + startingY
                + ", final position=" + mob.position() + ", destination=" + destination));
    }

    private AquaticMovementTests() {
    }
}
