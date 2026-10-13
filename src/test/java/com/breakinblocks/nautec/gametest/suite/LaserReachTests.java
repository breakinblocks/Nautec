package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.BeamScan;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CreativePowerSourceBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.block.Blocks;

public final class LaserReachTests {
    private static final BlockPos SOURCE = new BlockPos(2, 1, 4);

    private LaserReachTests() {
    }

    private static void assertNear(GameTestHelper helper, double expected, double actual, String what) {
        if (Math.abs(expected - actual) > 1.0e-3) {
            throw helper.assertionException(what + ": expected " + expected + " but was " + actual);
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("laser/beam_passes_through_glass", 100, helper -> {
            PowerAndLaserTests.placeShieldedSource(helper, SOURCE, Direction.EAST);
            helper.setBlock(new BlockPos(3, 1, 4), Blocks.GLASS);
            helper.setBlock(new BlockPos(4, 1, 4), Blocks.GLASS_PANE);
            helper.setBlock(new BlockPos(5, 1, 4), Blocks.LIGHT_BLUE_STAINED_GLASS);
            BlockPos mixerPos = new BlockPos(6, 1, 4);
            helper.setBlock(mixerPos, NTBlocks.MIXER.get());
            helper.succeedWhen(() -> {
                CreativePowerSourceBlockEntity source = helper.getBlockEntity(SOURCE, CreativePowerSourceBlockEntity.class);
                helper.assertValueEqual(4, source.getLaserDistances().getInt(Direction.EAST), "the beam reaches the mixer through the glass");
                helper.assertValueEqual(100, helper.getBlockEntity(mixerPos, MixerBlockEntity.class).getPower(), "mixer power through glass");
            });
        });

        r.add("laser/stone_still_blocks_where_glass_does_not", 40, helper -> {
            PowerAndLaserTests.placeShieldedSource(helper, SOURCE, Direction.EAST);
            helper.setBlock(new BlockPos(3, 1, 4), Blocks.GLASS);
            helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE);
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.MIXER.get());
            BeamScan scan = helper.getBlockEntity(SOURCE, CreativePowerSourceBlockEntity.class).scanBeam(Direction.EAST);
            helper.assertValueEqual(BeamScan.Status.BLOCKED, scan.status(), "stone stops the beam");
            helper.assertValueEqual(2, scan.distance(), "the beam stops at the stone, not the glass");
            helper.succeed();
        });

        r.add("laser/damage_grows_with_the_square_root_of_the_beam", 20, helper -> {
            double damage = NTConfig.laserDamage;
            int reference = NTConfig.laserDamageReferencePower;
            double scaling = NTConfig.laserDamageScaling;
            double max = NTConfig.laserDamageMax;
            try {
                NTConfig.laserDamage = 3.0;
                NTConfig.laserDamageReferencePower = 10;
                NTConfig.laserDamageScaling = 0.5;
                NTConfig.laserDamageMax = 0.0;
                assertNear(helper, 3.0, LaserBlockEntity.beamDamage(10), "a reference beam");
                assertNear(helper, 6.0, LaserBlockEntity.beamDamage(40), "four times the beam deals twice the damage");
                assertNear(helper, 12.0, LaserBlockEntity.beamDamage(160), "sixteen times the beam deals four times the damage");
                assertNear(helper, 0.0, LaserBlockEntity.beamDamage(0), "no beam deals nothing");
                NTConfig.laserDamageMax = 10.0;
                assertNear(helper, 10.0, LaserBlockEntity.beamDamage(160), "the cap holds");
                NTConfig.laserDamageMax = 0.0;
                NTConfig.laserDamageScaling = 0.0;
                assertNear(helper, 3.0, LaserBlockEntity.beamDamage(160), "flat damage at scaling 0");
                NTConfig.laserDamage = 0.0;
                assertNear(helper, 0.0, LaserBlockEntity.beamDamage(160), "damage off");
            } finally {
                NTConfig.laserDamage = damage;
                NTConfig.laserDamageReferencePower = reference;
                NTConfig.laserDamageScaling = scaling;
                NTConfig.laserDamageMax = max;
            }
            helper.succeed();
        });

        r.add("laser/beam_burns_a_creature_by_its_strength", 200, helper -> {
            PowerAndLaserTests.placeShieldedSource(helper, SOURCE, Direction.EAST);
            helper.setBlock(new BlockPos(6, 1, 4), NTBlocks.MIXER.get());
            Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 1, 4));
            float expected = LaserBlockEntity.beamDamage(100);
            helper.succeedWhen(() -> {
                float lost = cow.getMaxHealth() - cow.getHealth();
                helper.assertTrue(lost > 0 || !cow.isAlive(), "waiting for the beam to reach the cow");
                helper.assertTrue(cow.isAlive() || expected >= cow.getMaxHealth(), "the first hit should not kill a cow on a 100 AP beam");
                assertNear(helper, expected, lost, "damage from a 100 AP beam");
            });
        });
    }
}
