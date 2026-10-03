package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.OxygenDiffuserBlockEntity;
import com.breakinblocks.nautec.content.blocks.OxygenDiffuserBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public final class OxygenDiffuserTests {
    private static final BlockPos DIFFUSER = new BlockPos(4, 1, 4);

    private OxygenDiffuserTests() {
    }

    private static void beam(GameTestHelper helper, OxygenDiffuserBlockEntity diffuser, int power, float purity) {
        BlockPos origin = helper.absolutePos(DIFFUSER.above());
        diffuser.receivePower(power, Direction.UP, origin);
        diffuser.receiveNewPurity(purity, Direction.UP, origin);
        diffuser.commonTick();
    }

    public static void register(NTTestRegistrar r) {
        r.add("oxygen_diffuser/runs_on_ten_ap_at_any_purity", 20, helper -> {
            helper.setBlock(DIFFUSER, NTBlocks.OXYGEN_DIFFUSER.get());
            OxygenDiffuserBlockEntity diffuser = helper.getBlockEntity(DIFFUSER, OxygenDiffuserBlockEntity.class);
            helper.assertFalse(diffuser.isRunning(), "off without a beam");
            beam(helper, diffuser, NTConfig.oxygenDiffuserPower - 1, 3.0F);
            helper.assertFalse(diffuser.isRunning(), "a beam under the threshold is not enough");
            beam(helper, diffuser, NTConfig.oxygenDiffuserPower, 0.0F);
            helper.assertTrue(diffuser.isRunning(), "the threshold at zero purity runs it");
            helper.assertTrue(helper.getBlockState(DIFFUSER).getValue(OxygenDiffuserBlock.ACTIVE), "the block shows it is running");
            helper.succeed();
        });

        r.add("oxygen_diffuser/gives_water_breathing_in_range", 20, helper -> {
            helper.setBlock(DIFFUSER, NTBlocks.OXYGEN_DIFFUSER.get());
            OxygenDiffuserBlockEntity diffuser = helper.getBlockEntity(DIFFUSER, OxygenDiffuserBlockEntity.class);
            Player near = helper.makeMockPlayer(GameType.SURVIVAL);
            near.snapTo(helper.absoluteVec(DIFFUSER.above(2).getCenter()));
            near.setAirSupply(0);
            helper.getLevel().addFreshEntity(near);
            beam(helper, diffuser, NTConfig.oxygenDiffuserPower, 1.0F);
            int reached = diffuser.breathe(helper.getLevel());
            helper.assertTrue(reached >= 1, "a nearby player is reached");
            helper.assertTrue(near.hasEffect(MobEffects.WATER_BREATHING), "the player can breathe underwater");
            helper.assertValueEqual(near.getAirSupply(), near.getMaxAirSupply(), "their air is topped up");
            near.discard();
            helper.succeed();
        });
    }
}
