package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.events.AugmentEvents;
import com.breakinblocks.nautec.events.NTEvents;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class CreativeFlightTests {
    private static final ResourceLocation MODIFIER_ID = Nautec.rl("creative_flight_augment");

    private CreativeFlightTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("creative_flight/survives_ability_resets", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AugmentHelper.createAugment(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), player, NTAugmentSlots.BODY.get());
            helper.assertValueEqual(1.0, player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT), "augment grants the flight attribute");
            GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
            GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
            helper.assertTrue(player.mayFly(), "flight survives returning to survival");
            AugmentHelper.restoreAugments(player);
            helper.assertValueEqual(1, player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT).getModifiers().size(), "restoring augments does not stack grants");
            helper.succeed();
        });

        r.add("creative_flight/restored_by_player_lifecycle", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AugmentHelper.createAugment(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), player, NTAugmentSlots.BODY.get());
            var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            attribute.removeModifier(MODIFIER_ID);
            NTEvents.Game.onPlayerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(player));
            helper.assertTrue(player.mayFly(), "login restores transient flight");
            attribute.removeModifier(MODIFIER_ID);
            AugmentEvents.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
            helper.assertTrue(player.mayFly(), "respawn restores transient flight");
            attribute.removeModifier(MODIFIER_ID);
            AugmentEvents.onDimensionChanged(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            helper.assertTrue(player.mayFly(), "dimension changes restore transient flight");
            helper.succeed();
        });

        r.add("creative_flight/removal_revokes_augment_and_legacy_grant", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AugmentHelper.createAugment(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), player, NTAugmentSlots.BODY.get());
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            AugmentHelper.removeAugment(player, NTAugmentSlots.BODY.get());
            helper.assertValueEqual(0.0, player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT), "removal clears the attribute grant");
            helper.assertFalse(player.mayFly(), "removal clears legacy saved flight permission");
            helper.assertFalse(player.getAbilities().flying, "removal stops flight without another source");
            helper.succeed();
        });

        r.add("creative_flight/removal_preserves_other_attribute_grants", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            var attribute = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            ResourceLocation otherGrant = Nautec.rl("test_other_flight_source");
            attribute.addTransientModifier(new AttributeModifier(otherGrant, 1.0, AttributeModifier.Operation.ADD_VALUE));
            AugmentHelper.createAugment(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), player, NTAugmentSlots.BODY.get());
            player.getAbilities().flying = true;
            AugmentHelper.removeAugment(player, NTAugmentSlots.BODY.get());
            helper.assertTrue(attribute.hasModifier(otherGrant), "removal leaves the other flight modifier intact");
            helper.assertTrue(player.mayFly(), "another flight source still grants permission");
            helper.assertTrue(player.getAbilities().flying, "removal does not interrupt flight from another source");
            helper.succeed();
        });

        r.add("creative_flight/removal_preserves_game_mode_flight", 20, helper -> {
            for (GameType mode : new GameType[]{GameType.CREATIVE, GameType.SPECTATOR}) {
                Player player = helper.makeMockPlayer(mode);
                mode.updatePlayerAbilities(player.getAbilities());
                AugmentHelper.createAugment(NTAugments.CREATIVE_FLIGHT_AUGMENT.get(), player, NTAugmentSlots.BODY.get());
                player.getAbilities().flying = true;
                AugmentHelper.removeAugment(player, NTAugmentSlots.BODY.get());
                helper.assertTrue(player.getAbilities().mayfly, "removal preserves " + mode + " flight permission");
                helper.assertTrue(player.getAbilities().flying, "removal preserves active " + mode + " flight");
            }
            helper.succeed();
        });
    }
}
