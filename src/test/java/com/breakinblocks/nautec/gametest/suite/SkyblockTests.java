package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.data.conditions.SkyblockOption;
import com.breakinblocks.nautec.data.conditions.SkyblockOptionCondition;
import com.breakinblocks.nautec.events.LuckyFishingZoneEvents;
import com.breakinblocks.nautec.loot.SkyblockOptionLootCondition;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Optional;

public final class SkyblockTests {
    private static final String[] SKYBLOCK_RECIPES = {
            "skyblock/prismarine_sand_mixing",
            "skyblock/budding_prismarine_mixing",
            "skyblock/resonant_shard_laser_crafting",
            "skyblock/prismarine_crystal_seed_laser_crafting",
            "skyblock/eldritch_heart_mixing"
    };

    private SkyblockTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("skyblock/options_default_off_and_recipes_absent", 20, helper -> {
            for (SkyblockOption option : SkyblockOption.values()) {
                helper.assertFalse(option.enabled(), option.getSerializedName() + " should be off by default");
            }
            var recipes = helper.getLevel().getRecipeManager();
            for (String id : SKYBLOCK_RECIPES) {
                helper.assertTrue(recipes.byKey(Nautec.rl(id)).isEmpty(),
                        "recipe " + id + " should not load while its option is off");
            }
            helper.succeed();
        });

        r.add("skyblock/conditions_follow_their_option", 20, helper -> {
            LootContext context = lootContext(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 4)));
            for (SkyblockOption option : SkyblockOption.values()) {
                boolean previous = option.enabled();
                try {
                    set(option, true);
                    helper.assertTrue(new SkyblockOptionCondition(option).test(ICondition.IContext.EMPTY),
                            option.getSerializedName() + " recipe condition with the option on");
                    helper.assertTrue(new SkyblockOptionLootCondition(option).test(context),
                            option.getSerializedName() + " loot condition with the option on");
                    set(option, false);
                    helper.assertFalse(new SkyblockOptionCondition(option).test(ICondition.IContext.EMPTY),
                            option.getSerializedName() + " recipe condition with the option off");
                    helper.assertFalse(new SkyblockOptionLootCondition(option).test(context),
                            option.getSerializedName() + " loot condition with the option off");
                } finally {
                    set(option, previous);
                }
            }
            helper.succeed();
        });

        r.add("skyblock/lucky_zones_any_biome_accepts_plains", 20, helper -> {
            var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
            Holder<Biome> plains = biomes.getOrThrow(Biomes.PLAINS);
            Holder<Biome> ocean = biomes.getOrThrow(Biomes.OCEAN);
            boolean previous = NTConfig.skyblockLuckyZonesAnyBiome;
            try {
                NTConfig.skyblockLuckyZonesAnyBiome = false;
                helper.assertFalse(LuckyFishingZoneEvents.zoneAllowedIn(plains), "plains water should not host a zone with the option off");
                helper.assertTrue(LuckyFishingZoneEvents.zoneAllowedIn(ocean), "ocean water hosts a zone with the option off");
                NTConfig.skyblockLuckyZonesAnyBiome = true;
                helper.assertTrue(LuckyFishingZoneEvents.zoneAllowedIn(plains), "plains water hosts a zone with the option on");
            } finally {
                NTConfig.skyblockLuckyZonesAnyBiome = previous;
            }
            helper.succeed();
        });

        r.add("skyblock/grafting_any_biome_ignores_the_biome", 20, helper -> {
            BlockPos netherrack = new BlockPos(4, 1, 4);
            helper.setBlock(netherrack, Blocks.NETHERRACK);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            boolean previous = NTConfig.skyblockGraftingAnyBiome;
            try {
                NTConfig.skyblockGraftingAnyBiome = false;
                BacteriaInstance off = graft(helper, player, netherrack, 30);
                helper.assertTrue(off.isEmpty(), "netherrack outside the Nether should graft nothing with the option off");

                NTConfig.skyblockGraftingAnyBiome = true;
                BacteriaInstance on = graft(helper, player, netherrack, 30);
                helper.assertFalse(on.isEmpty(), "netherrack should graft within 30 tries with the option on");
                helper.assertValueEqual(NTBacterias.THERMOPHILES, on.getBacteria(), "strain grafted from netherrack");
            } finally {
                NTConfig.skyblockGraftingAnyBiome = previous;
            }
            helper.succeed();
        });

        r.add("skyblock/fishing_loot_follows_its_option", 40, helper -> {
            ServerLevel level = helper.getLevel();
            LootTable catchTable = level.getServer().reloadableRegistries().getLootTable(NTLootTables.LUCKY_ZONE_CATCH);
            LootTable treasureTable = level.getServer().reloadableRegistries().getLootTable(NTLootTables.LUCKY_ZONE_TREASURE);
            LootParams params = lootParams(level, helper.absolutePos(new BlockPos(4, 1, 4)));
            boolean previous = NTConfig.skyblockFishingLoot;
            try {
                NTConfig.skyblockFishingLoot = false;
                helper.assertFalse(rolls(catchTable, params, 600, NTBlocks.PRISMARINE_SAND.asItem()),
                        "Prismarine Sand should not be caught with the option off");
                helper.assertFalse(rolls(treasureTable, params, 2000, Items.HEART_OF_THE_SEA),
                        "Heart of the Sea should not be caught with the option off");

                NTConfig.skyblockFishingLoot = true;
                helper.assertTrue(rolls(catchTable, params, 600, NTBlocks.PRISMARINE_SAND.asItem()),
                        "Prismarine Sand should be caught with the option on");
                helper.assertTrue(rolls(treasureTable, params, 2000, Items.HEART_OF_THE_SEA),
                        "Heart of the Sea should be caught with the option on");
                helper.assertTrue(rolls(treasureTable, params, 2000, NTItems.ATLANTIC_GOLD_NUGGET.get()),
                        "Atlantic Gold nuggets should be caught anywhere with the option on");
            } finally {
                NTConfig.skyblockFishingLoot = previous;
            }
            helper.succeed();
        });

        r.add("skyblock/geode_template_crystal_reaches_full_purity", 40, helper -> {
            ServerLevel level = helper.getLevel();
            StructureTemplate template = level.getStructureManager()
                    .get(Nautec.rl("stone_crystal_geode"))
                    .orElseThrow(() -> helper.assertionException("stone_crystal_geode template missing"));
            BoundingBox arena = BoundingBox.fromCorners(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(8, 8, 8)));
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setBoundingBox(arena)
                    .setLiquidSettings(LiquidSettings.APPLY_WATERLOGGING);
            template.placeInWorld(level, helper.absolutePos(new BlockPos(-6, -2, -5)), BlockPos.ZERO, settings, level.getRandom(), 18);

            BlockPos core = new BlockPos(4, 4, 4);
            helper.runAfterDelay(5, () -> {
                helper.assertBlockPresent(NTBlocks.PRISMARINE_CRYSTAL.get(), core);
                PrismarineCrystalBlockEntity crystal = helper.getBlockEntity(core, PrismarineCrystalBlockEntity.class);
                helper.assertFalse(crystal.isCultivated(), "a template crystal is a natural crystal");
                helper.assertValueEqual(3.0f, crystal.getPurity(), "purity of a template crystal");
                helper.succeed();
            });
        });
    }

    private static BacteriaInstance graft(NTGameTestHelper helper, Player player, BlockPos target, int attempts) {
        ItemStack dish = new ItemStack(NTItems.PETRI_DISH.get());
        player.setItemInHand(InteractionHand.OFF_HAND, dish);
        BlockPos clicked = helper.absolutePos(target);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked).add(0, 0.5, 0), Direction.UP, clicked, false);
        for (int i = 0; i < attempts; i++) {
            ItemStack tool = new ItemStack(NTItems.GRAFTING_TOOL.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            tool.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            BacteriaInstance grafted = storage(helper, player.getOffhandItem()).getBacteria(0);
            if (!grafted.isEmpty()) {
                return grafted;
            }
        }
        return storage(helper, player.getOffhandItem()).getBacteria(0);
    }

    private static IBacteriaStorage storage(NTGameTestHelper helper, ItemStack dish) {
        IBacteriaStorage storage = dish.getCapability(NTCapabilities.BacteriaStorage.ITEM);
        if (storage == null) {
            throw helper.assertionException("petri dish has no bacteria storage");
        }
        return storage;
    }

    private static boolean rolls(LootTable table, LootParams params, int times, Item item) {
        for (int i = 0; i < times; i++) {
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static LootParams lootParams(ServerLevel level, BlockPos pos) {
        return new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .create(LootContextParamSets.FISHING);
    }

    private static LootContext lootContext(ServerLevel level, BlockPos pos) {
        return new LootContext.Builder(lootParams(level, pos)).create(Optional.empty());
    }

    private static void set(SkyblockOption option, boolean enabled) {
        switch (option) {
            case LUCKY_ZONES_ANY_BIOME -> NTConfig.skyblockLuckyZonesAnyBiome = enabled;
            case FISHING_LOOT -> NTConfig.skyblockFishingLoot = enabled;
            case GRAFTING_ANY_BIOME -> NTConfig.skyblockGraftingAnyBiome = enabled;
            case PRISMARINE_SAND_MIXING -> NTConfig.skyblockPrismarineSandMixing = enabled;
            case BUDDING_PRISMARINE_MIXING -> NTConfig.skyblockBuddingPrismarineMixing = enabled;
            case FUEL_CELL_RESONANCE -> NTConfig.skyblockFuelCellResonance = enabled;
            case ELDRITCH_HEART_MIXING -> NTConfig.skyblockEldritchHeartMixing = enabled;
        }
    }
}
