package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.EyeOfTheSeaEntity;
import com.breakinblocks.nautec.content.items.EyeOfTheSeaItem;
import com.breakinblocks.nautec.content.items.SeaEyeSearch;
import com.breakinblocks.nautec.content.items.SeaEyeTarget;
import com.breakinblocks.nautec.events.LuckyFishingZoneEvents;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.worldgen.injection.NTOceanRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class EyeOfTheSeaTests {
    public static void register(NTTestRegistrar r) {
        r.add("eye_of_the_sea/defaults_to_geodes_and_cycles_every_target", 20, helper -> {
            ItemStack stack = new ItemStack(NTItems.EYE_OF_THE_SEA.get());
            if (EyeOfTheSeaItem.targetOf(stack) != SeaEyeTarget.CRYSTAL_GEODES) {
                helper.fail("A fresh Eye of the Sea should seek crystal geodes, got " + EyeOfTheSeaItem.targetOf(stack));
                return;
            }
            List<SeaEyeTarget> expected = List.of(SeaEyeTarget.NAUTEC_RUINS, SeaEyeTarget.RESEARCH_OUTPOSTS, SeaEyeTarget.GATEWAYS,
                    SeaEyeTarget.OCEAN_RUINS, SeaEyeTarget.OCEAN_MONUMENTS, SeaEyeTarget.PRISMARINE_REEF, SeaEyeTarget.ABYSSAL_TRENCH,
                    SeaEyeTarget.BIOLUMINESCENT_GROVE, SeaEyeTarget.HYDROTHERMAL_VENTS, SeaEyeTarget.CRYSTAL_GEODES);
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
                    || !contains(structures, SeaEyeTarget.RESEARCH_OUTPOSTS, Nautec.rl("research_outpost"))
                    || !contains(structures, SeaEyeTarget.GATEWAYS, Nautec.rl("underwater_gateway"))
                    || !contains(structures, SeaEyeTarget.OCEAN_RUINS, ResourceLocation.withDefaultNamespace("ocean_ruin_cold"))
                    || !contains(structures, SeaEyeTarget.OCEAN_RUINS, ResourceLocation.withDefaultNamespace("ocean_ruin_warm"))
                    || !contains(structures, SeaEyeTarget.OCEAN_MONUMENTS, ResourceLocation.withDefaultNamespace("monument"))) {
                helper.fail("An Eye of the Sea structure tag is missing one of its structures");
                return;
            }
            helper.succeed();
        });

        r.add("eye_of_the_sea/every_nautec_biome_is_a_target", 20, helper -> {
            Set<ResourceKey<Biome>> targeted = new HashSet<>();
            for (SeaEyeTarget target : SeaEyeTarget.values()) {
                if (target.biome() != null) {
                    targeted.add(target.biome());
                }
            }
            for (ResourceKey<Biome> biome : NTOceanRegion.biomes()) {
                helper.assertTrue(targeted.contains(biome), "the Eye of the Sea can seek " + biome.location());
            }
            helper.assertValueEqual(NTOceanRegion.biomes().size(), targeted.size(), "biome targets");
            helper.assertTrue(SeaEyeTarget.CRYSTAL_GEODES.biome() == null, "structure targets have no biome");
            helper.succeed();
        });

        r.add("eye_of_the_sea/overworld_biome_search_finds_every_nautec_biome", 200, helper -> {
            ServerLevel level = helper.getLevel();
            RegistryAccess access = level.registryAccess();
            BiomeSource source = MultiNoiseBiomeSource.createFromPreset(Holder.direct(new MultiNoiseBiomeSourceParameterList(
                    MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, access.lookupOrThrow(Registries.BIOME))));
            Climate.Sampler sampler = RandomState.create(access.asGetterLookup(), NoiseGeneratorSettings.OVERWORLD, 20261018L).sampler();
            BlockPos origin = new BlockPos(0, 40, 0);
            for (SeaEyeTarget target : SeaEyeTarget.values()) {
                ResourceKey<Biome> biome = target.biome();
                if (biome == null) {
                    continue;
                }
                BlockPos found = SeaEyeSearch.findBiome(source, sampler, level, origin, biome, NTConfig.eyeOfTheSeaBiomeSearchRadius);
                if (found == null) {
                    helper.fail("No " + biome.location() + " within " + NTConfig.eyeOfTheSeaBiomeSearchRadius + " blocks of the origin");
                    return;
                }
                Holder<Biome> at = source.getNoiseBiome(QuartPos.fromBlock(found.getX()), QuartPos.fromBlock(found.getY()),
                        QuartPos.fromBlock(found.getZ()), sampler);
                helper.assertTrue(at.is(biome), target + " pointed at " + found + " which is " + at.getRegisteredName());
                Nautec.LOGGER.info("Eye of the Sea found {} at {}, {} blocks away", biome.location(), found,
                        (int) Math.sqrt(found.distSqr(origin)));
            }
            helper.succeed();
        });

        r.add("eye_of_the_sea/nothing_found_keeps_the_eye", 600, helper -> {
            ServerLevel level = helper.getLevel();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            ItemStack stack = new ItemStack(NTItems.EYE_OF_THE_SEA.get());
            stack.set(NTDataComponents.SEA_EYE_TARGET.get(), SeaEyeTarget.GATEWAYS);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            int radius = NTConfig.eyeOfTheSeaSearchRadius;
            InteractionResult result;
            try {
                NTConfig.eyeOfTheSeaSearchRadius = 2;
                if (EyeOfTheSeaItem.locate(level, player.blockPosition(), SeaEyeTarget.GATEWAYS) != null) {
                    helper.succeed();
                    return;
                }
                result = stack.getItem().use(level, player, InteractionHand.MAIN_HAND).getResult();
            } finally {
                NTConfig.eyeOfTheSeaSearchRadius = radius;
            }
            if (!result.consumesAction()) {
                helper.fail("Using the eye should start a search, got " + result);
                return;
            }
            try {
                Field executor = SeaEyeSearch.class.getDeclaredField("EXECUTOR");
                executor.setAccessible(true);
                ((ExecutorService) executor.get(null)).submit(() -> {
                }).get(30, TimeUnit.SECONDS);
            } catch (ReflectiveOperationException | InterruptedException | ExecutionException | TimeoutException e) {
                helper.fail("Could not wait for the background search: " + e);
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

    private static boolean contains(HolderLookup.RegistryLookup<Structure> structures, SeaEyeTarget target, ResourceLocation id) {
        Optional<HolderSet.Named<Structure>> tag = structures.get(target.structures());
        return tag.isPresent() && tag.get().stream()
                .anyMatch(holder -> holder.is(ResourceKey.create(Registries.STRUCTURE, id)));
    }
}
