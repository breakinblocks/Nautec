package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.entities.mobs.AbyssalMaw;
import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class AquaticMovementTests {
    public static void register(NTTestRegistrar registrar) {
        registrar.add("fauna/jelly_can_swim_upward", 220,
                helper -> swimsUpward(helper, NTEntities.LANTERN_JELLY.get()));
        registrar.add("fauna/maw_can_swim_upward", 220,
                helper -> swimsUpward(helper, NTEntities.ABYSSAL_MAW.get()));
        registrar.add("fauna/maw_pursues_and_bites_a_target_on_the_seabed", 220,
                AquaticMovementTests::pursuesAndBites);
        registrar.add("fauna/maw_keeps_biting_a_target_that_stays_put", 320,
                AquaticMovementTests::keepsBiting);
    }

    private static void keepsBiting(GameTestHelper helper) {
        tank(helper);
        Pig prey = helper.spawn(EntityType.PIG, new BlockPos(4, 1, 4));
        prey.setNoAi(true);
        prey.setPersistenceRequired();
        prey.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0);
        prey.setHealth(500.0F);
        AbyssalMaw maw = helper.spawn(NTEntities.ABYSSAL_MAW.get(), new BlockPos(1, 2, 1));
        maw.setPersistenceRequired();
        maw.setTarget(prey);
        float[] lastHealth = {prey.getHealth()};
        int[] bites = {0};
        helper.onEachTick(() -> {
            if (maw.getTarget() == null && prey.isAlive()) {
                maw.setTarget(prey);
            }
            if (prey.getHealth() < lastHealth[0]) {
                bites[0]++;
            }
            lastHealth[0] = prey.getHealth();
            if (bites[0] >= 6) {
                helper.succeed();
            }
        });
        helper.runAfterDelay(300, () -> helper.fail("The Maw only bit " + bites[0] + " times in 300 ticks; it ended at "
                + maw.position() + ", " + String.format("%.2f", maw.distanceTo(prey)) + " blocks from the target at " + prey.position()));
    }

    private static void pursuesAndBites(GameTestHelper helper) {
        tank(helper);
        Pig prey = helper.spawn(EntityType.PIG, new BlockPos(6, 1, 6));
        prey.setNoAi(true);
        prey.setPersistenceRequired();
        AbyssalMaw maw = helper.spawn(NTEntities.ABYSSAL_MAW.get(), new BlockPos(2, 5, 2));
        maw.setPersistenceRequired();
        maw.setTarget(prey);
        double startingDistance = maw.distanceTo(prey);
        helper.onEachTick(() -> {
            if (maw.getTarget() == null && prey.isAlive()) {
                maw.setTarget(prey);
            }
            if (prey.getLastHurtByMob() == maw) {
                helper.succeed();
            }
        });
        helper.runAfterDelay(200, () -> helper.fail("The Maw never bit its target: started "
                + String.format("%.1f", startingDistance) + " blocks away, ended at " + maw.position()
                + " with the target at " + prey.position()));
    }

    private static void tank(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getX() == 0 || pos.getX() == 8 || pos.getY() == 0 || pos.getY() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.GLASS : Blocks.WATER);
        }
    }

    private static void swimsUpward(GameTestHelper helper, EntityType<? extends Mob> type) {
        tank(helper);
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
