package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.datagen.BiomeTagProvider;
import com.breakinblocks.nautec.registries.NTLootTables;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class LootTableTests {
    public static void register(NTTestRegistrar r) {
        r.add("loot/every_mod_table_parses", 10, helper -> {
            MinecraftServer server = helper.getLevel().getServer();
            List<String> broken = new ArrayList<>();
            int checked = 0;

            for (Identifier file : server.getResourceManager()
                    .listResources("loot_table", path -> path.getPath().endsWith(".json")).keySet()) {
                if (!file.getNamespace().equals(Nautec.MODID)) {
                    continue;
                }
                String path = file.getPath();
                path = path.substring("loot_table/".length(), path.length() - ".json".length());

                checked++;
                ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE,
                        Identifier.fromNamespaceAndPath(Nautec.MODID, path));
                if (server.reloadableRegistries().getLootTable(key) == LootTable.EMPTY) {
                    broken.add(key.identifier().toString());
                }
            }

            if (checked == 0) {
                helper.fail("No Nautec loot table files were found at all, the check is not looking where it should");
            }
            if (!broken.isEmpty()) {
                helper.fail("Loot tables that failed to parse (one bad item or component id kills the whole table): "
                        + String.join(", ", broken));
            }
            Nautec.LOGGER.info("Checked {} Nautec loot tables, all parsed", checked);
            helper.succeed();
        });

        r.add("loot/lucky_zone_tables_produce_items", 10, helper -> {
            ServerLevel level = helper.getLevel();
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1))))
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                    .create(LootContextParamSets.FISHING);

            for (ResourceKey<LootTable> key : List.of(NTLootTables.LUCKY_ZONE,
                    NTLootTables.LUCKY_ZONE_CATCH, NTLootTables.LUCKY_ZONE_TREASURE)) {
                LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
                if (table == LootTable.EMPTY) {
                    helper.fail(key.identifier() + " did not load");
                    return;
                }

                boolean gotSomething = false;
                for (int attempt = 0; attempt < 40 && !gotSomething; attempt++) {
                    gotSomething = !table.getRandomItems(params).isEmpty();
                }
                if (!gotSomething) {
                    helper.fail(key.identifier() + " rolled 40 times without producing a single item");
                }
            }
            helper.succeed();
        });

        r.add("loot/catch_as_entity_spawns_and_removes_the_stack", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            net.minecraft.core.BlockPos pos = helper.absolutePos(new net.minecraft.core.BlockPos(4, 2, 4));
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());

            net.minecraft.world.entity.projectile.FishingHook hook =
                    new net.minecraft.world.entity.projectile.FishingHook(
                            net.minecraft.world.entity.EntityType.FISHING_BOBBER, level);
            hook.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(hook);

            ItemStack marked = new ItemStack(Items.COD);
            marked.set(com.breakinblocks.nautec.data.NTDataComponents.CATCH_ENTITY.get(),
                    net.minecraft.world.entity.EntityType.COD);
            ItemStack plain = new ItemStack(Items.SALMON);

            List<ItemStack> drops = new ArrayList<>(List.of(marked, plain));
            int codsBefore = level.getEntities(net.minecraft.world.entity.EntityType.COD,
                    hook.getBoundingBox().inflate(6.0), e -> true).size();

            com.breakinblocks.nautec.content.fishing.CaughtEntitySpawner.releaseAll(hook, drops);

            if (drops.size() != 1 || !drops.getFirst().is(Items.SALMON)) {
                helper.fail("The marked stack should have been removed from the drops, leaving only the salmon, got " + drops);
                return;
            }
            int codsAfter = level.getEntities(net.minecraft.world.entity.EntityType.COD,
                    hook.getBoundingBox().inflate(6.0), e -> true).size();
            if (codsAfter <= codsBefore) {
                helper.fail("No cod entity was released when a marked stack was caught");
            }
            hook.discard();
            helper.succeed();
        });

        r.add("loot/biome_gated_entries_have_real_tags", 5, helper -> {
            List<TagKey<Biome>> tags = List.of(BiomeTagProvider.ABYSSAL, BiomeTagProvider.VENTS,
                    BiomeTagProvider.BIOLUMINESCENT, BiomeTagProvider.REEF);

            for (TagKey<Biome> tag : tags) {
                long size = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME)
                        .get(tag).map(named -> named.stream().count()).orElse(0L);
                if (size == 0) {
                    helper.fail("Biome tag " + tag.location() + " is empty, so every lucky zone entry gated on it can never drop");
                }
            }
            helper.succeed();
        });
    }

    private LootTableTests() {
    }
}
