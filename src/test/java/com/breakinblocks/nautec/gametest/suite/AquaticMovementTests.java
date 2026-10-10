package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.entities.mobs.AbyssalMaw;
import com.breakinblocks.nautec.registries.NTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LightLayer;
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
        registrar.add("fauna/maw_spawns_only_deep_and_dark", 200, AquaticMovementTests::mawSpawnRules);
    }

    private static void mawSpawnRules(NTGameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos arena = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos deepDark = pocket(level, arena.offset(-2, 0, 0), Blocks.WATER);
        BlockPos deepLit = pocket(level, arena.offset(2, 0, 0), Blocks.WATER);
        level.setBlockAndUpdate(deepLit.above(), Blocks.GLOWSTONE.defaultBlockState());
        BlockPos deepDry = pocket(level, arena.offset(0, 0, 2), Blocks.AIR);
        BlockPos shallowDark = pocket(level, new BlockPos(arena.getX(), 45, arena.getZ()), Blocks.WATER);
        helper.succeedWhen(() -> {
            helper.assertTrue(deepDark.getY() < 40, "The deep pocket must sit below y=40, it is at " + deepDark.getY());
            helper.assertTrue(level.getMaxLocalRawBrightness(deepDark) <= 7, "The deep dark pocket is lit, " + light(level, deepDark));
            helper.assertTrue(level.getMaxLocalRawBrightness(shallowDark) <= 7, "The shallow dark pocket is lit, " + light(level, shallowDark));
            helper.assertTrue(level.getMaxLocalRawBrightness(deepLit) > 7, "The lit pocket is dark, " + light(level, deepLit));
            helper.assertTrue(mawMaySpawn(level, deepDark, MobSpawnType.NATURAL), "A Maw may not spawn in deep, dark water");
            helper.assertFalse(mawMaySpawn(level, deepLit, MobSpawnType.NATURAL), "A Maw spawned in lit water");
            helper.assertFalse(mawMaySpawn(level, deepDry, MobSpawnType.NATURAL), "A Maw spawned out of water");
            helper.assertFalse(mawMaySpawn(level, shallowDark, MobSpawnType.NATURAL), "A Maw spawned at y=45, above the y=40 limit");
            helper.assertTrue(mawMaySpawn(level, shallowDark, MobSpawnType.SPAWN_EGG), "A spawn egg could not place a Maw at y=45");
            for (BlockPos pos : BlockPos.betweenClosed(shallowDark.offset(-1, -1, -1), shallowDark.offset(1, 1, 1))) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        });
    }

    private static String light(ServerLevel level, BlockPos pos) {
        return "sky " + level.getBrightness(LightLayer.SKY, pos) + ", block " + level.getBrightness(LightLayer.BLOCK, pos);
    }

    private static boolean mawMaySpawn(ServerLevel level, BlockPos pos, MobSpawnType reason) {
        return SpawnPlacements.checkSpawnRules(NTEntities.ABYSSAL_MAW.get(), level, reason, pos, level.getRandom());
    }

    private static BlockPos pocket(ServerLevel level, BlockPos centre, Block fill) {
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-1, -1, -1), centre.offset(1, 1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(centre, fill.defaultBlockState());
        return centre.immutable();
    }

    private static void keepsBiting(NTGameTestHelper helper) {
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

    private static void pursuesAndBites(NTGameTestHelper helper) {
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

    private static void tank(NTGameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getX() == 0 || pos.getX() == 8 || pos.getY() == 0 || pos.getY() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.GLASS : Blocks.WATER);
        }
    }

    private static void swimsUpward(NTGameTestHelper helper, EntityType<? extends Mob> type) {
        tank(helper);
        Mob mob = type.spawn(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 4)), MobSpawnType.COMMAND);
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
