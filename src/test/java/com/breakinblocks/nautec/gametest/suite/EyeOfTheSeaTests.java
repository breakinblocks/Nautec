package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.EyeOfTheSeaEntity;
import com.breakinblocks.nautec.content.items.EyeOfTheSeaItem;
import com.breakinblocks.nautec.content.items.SeaEyeSearch;
import com.breakinblocks.nautec.content.items.SeaEyeTarget;
import com.breakinblocks.nautec.events.LuckyFishingZoneEvents;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class EyeOfTheSeaTests {
    public static void register(NTTestRegistrar r) {
        r.add("eye_of_the_sea/defaults_to_geodes_and_cycles_every_target", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.EYE_OF_THE_SEA.get());
            if (EyeOfTheSeaItem.targetOf(stack) != SeaEyeTarget.CRYSTAL_GEODES) {
                helper.fail("A fresh Eye of the Sea should seek crystal geodes, got " + EyeOfTheSeaItem.targetOf(stack));
                return;
            }
            List<SeaEyeTarget> expected = List.of(SeaEyeTarget.NAUTEC_RUINS, SeaEyeTarget.GATEWAYS,
                    SeaEyeTarget.OCEAN_RUINS, SeaEyeTarget.OCEAN_MONUMENTS, SeaEyeTarget.CRYSTAL_GEODES);
            for (SeaEyeTarget want : expected) {
                SeaEyeTarget got = EyeOfTheSeaItem.cycle(stack);
                if (got != want || EyeOfTheSeaItem.targetOf(stack) != want) {
                    helper.fail("Cycling should reach " + want + " but the stack holds " + EyeOfTheSeaItem.targetOf(stack));
                    return;
                }
            }
            helper.succeed();
        });

        r.add("eye_of_the_sea/every_target_tag_names_its_structures", 20, helper -> {
            HolderLookup.RegistryLookup<Structure> structures =
                    helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
            if (!contains(structures, SeaEyeTarget.CRYSTAL_GEODES, Nautec.rl("stone_crystal_geode"))
                    || !contains(structures, SeaEyeTarget.CRYSTAL_GEODES, Nautec.rl("deepslate_crystal_geode"))
                    || !contains(structures, SeaEyeTarget.NAUTEC_RUINS, Nautec.rl("ruins_1"))
                    || !contains(structures, SeaEyeTarget.GATEWAYS, Nautec.rl("underwater_gateway"))
                    || !contains(structures, SeaEyeTarget.OCEAN_RUINS, Identifier.withDefaultNamespace("ocean_ruin_cold"))
                    || !contains(structures, SeaEyeTarget.OCEAN_RUINS, Identifier.withDefaultNamespace("ocean_ruin_warm"))
                    || !contains(structures, SeaEyeTarget.OCEAN_MONUMENTS, Identifier.withDefaultNamespace("monument"))) {
                helper.fail("An Eye of the Sea structure tag is missing one of its structures");
                return;
            }
            helper.succeed();
        });

        r.add("eye_of_the_sea/nothing_found_keeps_the_eye", 600, helper -> {
            ServerLevel level = helper.getLevel();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack stack = new ItemStack(NTItems.EYE_OF_THE_SEA.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            if (EyeOfTheSeaItem.locate(level, player.blockPosition(), SeaEyeTarget.GATEWAYS) != null) {
                helper.succeed();
                return;
            }
            InteractionResult result = stack.getItem().use(level, player, InteractionHand.MAIN_HAND);
            if (!result.consumesAction()) {
                helper.fail("Using the eye should start a search, got " + result);
                return;
            }
            if (!SeaEyeSearch.isSearching(player.getUUID())) {
                helper.fail("The search should run in the background after the throw");
                return;
            }
            helper.succeedWhen(() -> {
                helper.assertTrue(!SeaEyeSearch.isSearching(player.getUUID()), "the background search finishes");
                helper.assertValueEqual(1, player.getMainHandItem().getCount(), "a failed throw keeps the eye");
                AABB around = new AABB(player.blockPosition()).inflate(8);
                helper.assertTrue(level.getEntitiesOfClass(EyeOfTheSeaEntity.class, around).isEmpty(), "a failed throw spawns no eye");
            });
        });

        r.add("eye_of_the_sea/thrown_eye_vanishes_without_a_drop", EyeOfTheSeaEntity.LIFETIME + 40, helper -> {
            ServerLevel level = helper.getLevel();
            Vec3 start = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 4)));
            EyeOfTheSeaEntity eye = new EyeOfTheSeaEntity(level, start.x, start.y, start.z);
            eye.setItem(new ItemStack(NTItems.EYE_OF_THE_SEA.get()));
            eye.signalTo(start.add(6, 0, 0));
            level.addFreshEntity(eye);
            if (eye.getType() != NTEntities.EYE_OF_THE_SEA.get() || eye.target() == null) {
                helper.fail("The eye did not take a target");
                return;
            }
            helper.runAfterDelay(EyeOfTheSeaEntity.LIFETIME + 10, () -> {
                if (!eye.isRemoved()) {
                    helper.fail("The eye was still flying after its lifetime");
                    return;
                }
                AABB around = new AABB(BlockPos.containing(start)).inflate(48);
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, around)) {
                    if (item.getItem().is(NTItems.EYE_OF_THE_SEA.get())) {
                        helper.fail("The eye dropped an item at " + item.blockPosition());
                        return;
                    }
                }
                helper.succeed();
            });
        });

        r.add("eye_of_the_sea/boost_raises_lucky_zone_rate", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            if (LuckyFishingZoneEvents.zoneRateMultiplier(player) != 1) {
                helper.fail("A player who never threw an eye should have the normal zone rate");
                return;
            }
            LuckyFishingZoneEvents.boost(player, 100);
            int boosted = LuckyFishingZoneEvents.zoneRateMultiplier(player);
            if (boosted != NTConfig.eyeOfTheSeaLuckyZoneMultiplier || boosted < 2) {
                helper.fail("The boost should multiply the zone rate by " + NTConfig.eyeOfTheSeaLuckyZoneMultiplier + ", got " + boosted);
                return;
            }
            LuckyFishingZoneEvents.boost(player, 1);
            if (LuckyFishingZoneEvents.zoneRateMultiplier(player) != boosted) {
                helper.fail("A shorter boost must not cut an active longer one short");
                return;
            }
            helper.succeed();
        });
    }

    private static boolean contains(HolderLookup.RegistryLookup<Structure> structures, SeaEyeTarget target, Identifier id) {
        Optional<HolderSet.Named<Structure>> tag = structures.get(target.structures());
        return tag.isPresent() && tag.get().stream()
                .anyMatch(holder -> holder.is(ResourceKey.create(Registries.STRUCTURE, id)));
    }
}
