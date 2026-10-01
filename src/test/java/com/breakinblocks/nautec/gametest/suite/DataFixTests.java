package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.CrateBlockEntity;
import com.breakinblocks.nautec.content.recipes.AugmentationRecipe;
import com.breakinblocks.nautec.content.recipes.MixingRecipe;
import com.breakinblocks.nautec.content.recipes.inputs.AugmentationRecipeInput;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTLootTables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DataFixTests {
    public static void register(NTTestRegistrar r) {
        r.add("datafix/atlantic_gold_nuggets_craft_into_an_ingot_and_back", 20, 1, helper -> {
            ItemStack nugget = new ItemStack(NTItems.ATLANTIC_GOLD_NUGGET.get());
            ItemStack ingot = craft(helper, 3, 3, Collections.nCopies(9, nugget));
            if (!ingot.is(NTItems.ATLANTIC_GOLD_INGOT.get()) || ingot.getCount() != 1) {
                helper.fail("Nine Atlantic Gold Nuggets should craft one Atlantic Gold Ingot, got " + ingot);
                return;
            }

            ItemStack nuggets = craft(helper, 1, 1, List.of(new ItemStack(NTItems.ATLANTIC_GOLD_INGOT.get())));
            if (!nuggets.is(NTItems.ATLANTIC_GOLD_NUGGET.get()) || nuggets.getCount() != 9) {
                helper.fail("One Atlantic Gold Ingot should craft nine nuggets, got " + nuggets);
                return;
            }

            ServerLevel level = helper.getLevel();
            LootTable crate = level.getServer().reloadableRegistries().getLootTable(NTLootTables.CRATE);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))))
                    .create(LootContextParamSets.CHEST);
            boolean found = false;
            for (int attempt = 0; attempt < 200 && !found; attempt++) {
                found = crate.getRandomItems(params).stream().anyMatch(stack -> stack.is(NTItems.ATLANTIC_GOLD_NUGGET.get()));
            }
            if (!found) {
                helper.fail("The crate loot table rolled 200 times without an Atlantic Gold Nugget, so nuggets have no reliable source");
                return;
            }
            helper.succeed();
        });

        r.add("datafix/eas_vials_are_filled_from_an_eas_bucket", 20, 1, helper -> {
            ItemStack bucket = new ItemStack(NTFluids.EAS.getBucket());
            ItemStack vial = new ItemStack(NTItems.GLASS_VIAL.get());
            CraftingInput input = CraftingInput.of(2, 2, List.of(bucket, vial, vial.copy(), vial.copy()));
            Optional<RecipeHolder<CraftingRecipe>> holder =
                    helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
            if (holder.isEmpty()) {
                helper.fail("No crafting recipe fills Glass Vials from an EAS Bucket");
                return;
            }

            ItemStack result = holder.get().value().assemble(input);
            if (!result.is(NTItems.ELECTROLYTE_ALGAE_SERUM_VIAL.get()) || result.getCount() != 3) {
                helper.fail("An EAS Bucket and three Glass Vials should craft three EAS Vials, got " + result);
                return;
            }

            NonNullList<ItemStack> remaining = holder.get().value().getRemainingItems(input);
            if (remaining.stream().noneMatch(stack -> stack.is(Items.BUCKET))) {
                helper.fail("Filling vials from an EAS Bucket should hand the empty bucket back, remainders were " + remaining);
                return;
            }

            boolean mixerMakesEas = helper.getLevel().recipeAccess().recipeMap().byType(MixingRecipe.Type.INSTANCE).stream()
                    .anyMatch(recipe -> recipe.value().fluidResult().is(NTFluids.EAS.getStillFluid()));
            if (!mixerMakesEas) {
                helper.fail("No Mixer recipe produces EAS, so the bucket has no source");
                return;
            }
            helper.succeed();
        });

        r.add("datafix/budding_prismarine_is_mined_with_a_pickaxe", 20, 1, helper -> {
            BlockState state = NTBlocks.BUDDING_PRISMARINE.get().defaultBlockState();
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
                helper.fail("Budding Prismarine is not in minecraft:mineable/pickaxe");
                return;
            }
            if (state.requiresCorrectToolForDrops() && !new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(state)) {
                helper.fail("An iron pickaxe is not the correct tool for Budding Prismarine, so it drops nothing");
                return;
            }

            ServerLevel level = helper.getLevel();
            BlockPos pos = new BlockPos(1, 2, 1);
            helper.setBlock(pos, state);
            ItemStack silk = new ItemStack(Items.IRON_PICKAXE);
            silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
            List<ItemStack> drops = Block.getDrops(state, level, helper.absolutePos(pos), null, null, silk);
            if (drops.size() != 1 || !drops.getFirst().is(NTBlocks.BUDDING_PRISMARINE.asItem())) {
                helper.fail("Silk Touch should drop the Budding Prismarine block, got " + drops);
                return;
            }
            helper.succeed();
        });

        r.add("datafix/salvage_and_power_blocks_drop_themselves", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            List<Block> blocks = List.of(
                    NTBlocks.BROWN_POLYMER_BLOCK.get(),
                    NTBlocks.CAST_IRON_BLOCK.get(),
                    NTBlocks.ANCHOR.get(),
                    NTBlocks.OIL_BARREL.get(),
                    NTBlocks.CREATIVE_POWER_SOURCE.get(),
                    NTBlocks.CREATIVE_ENERGY_SOURCE.get(),
                    NTBlocks.ENERGY_CONVERTER.get());
            ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
            List<String> problems = new ArrayList<>();

            int x = 1;
            for (Block block : blocks) {
                BlockPos pos = new BlockPos(x, 2, 1);
                x++;
                helper.setBlock(pos, block);
                BlockPos absolute = helper.absolutePos(pos);
                BlockState state = level.getBlockState(absolute);
                BlockEntity blockEntity = level.getBlockEntity(absolute);
                String name = BuiltInRegistries.BLOCK.getKey(block).toString();

                if (state.requiresCorrectToolForDrops() && !pickaxe.isCorrectToolForDrops(state)) {
                    problems.add(name + " needs a tool and an iron pickaxe is not it");
                }
                List<ItemStack> drops = Block.getDrops(state, level, absolute, blockEntity, null, pickaxe);
                if (drops.stream().noneMatch(stack -> stack.is(block.asItem()))) {
                    problems.add(name + " dropped " + drops + " instead of itself");
                }
            }

            if (!problems.isEmpty()) {
                helper.fail(String.join("; ", problems));
                return;
            }
            helper.succeed();
        });

        r.add("datafix/oil_barrel_is_craftable", 20, 1, helper -> {
            ItemStack ingot = new ItemStack(NTItems.CAST_IRON_INGOT.get());
            ItemStack rod = new ItemStack(NTItems.CAST_IRON_ROD.get());
            ItemStack empty = ItemStack.EMPTY;
            ItemStack result = craft(helper, 3, 3, List.of(
                    ingot, rod, ingot,
                    ingot, empty, ingot,
                    ingot, rod, ingot));
            if (!result.is(NTBlocks.OIL_BARREL.asItem())) {
                helper.fail("Six Cast Iron Ingots around two Cast Iron Rods should craft an Oil Barrel, got " + result);
                return;
            }
            helper.succeed();
        });

        r.add("datafix/energy_converter_is_craftable_and_both_fe_blocks_are_in_the_tab", 20, 1, helper -> {
            ItemStack prismarine = new ItemStack(NTBlocks.POLISHED_PRISMARINE.asItem());
            ItemStack redstone = new ItemStack(Items.REDSTONE);
            ItemStack steel = new ItemStack(NTItems.AQUARINE_STEEL_INGOT.get());
            ItemStack coil = new ItemStack(NTItems.LASER_CHANNELING_COIL.get());
            ItemStack result = craft(helper, 3, 3, List.of(
                    prismarine, redstone, prismarine,
                    steel, coil, steel,
                    prismarine, redstone, prismarine));
            if (!result.is(NTBlocks.ENERGY_CONVERTER.asItem())) {
                helper.fail("The Energy Converter recipe did not match, got " + result);
                return;
            }

            List<String> missing = new ArrayList<>();
            for (Block block : List.of(NTBlocks.ENERGY_CONVERTER.get(), NTBlocks.CREATIVE_ENERGY_SOURCE.get())) {
                Item item = block.asItem();
                if (NTItems.creativeTabItems().stream().noneMatch(entry -> entry.asItem() == item)) {
                    missing.add(BuiltInRegistries.ITEM.getKey(item).toString());
                }
            }
            if (!missing.isEmpty()) {
                helper.fail("Missing from the NauTec creative tab: " + String.join(", ", missing));
                return;
            }
            helper.succeed();
        });

        r.add("datafix/every_augmentation_recipe_fits_the_station", 20, 1, helper -> {
            List<String> problems = new ArrayList<>();
            for (RecipeHolder<AugmentationRecipe> holder
                    : helper.getLevel().recipeAccess().recipeMap().byType(AugmentationRecipe.Type.INSTANCE)) {
                List<IngredientWithCount> ingredients = holder.value().ingredients();
                if (ingredients.size() > 4) {
                    problems.add(holder.id().identifier() + " needs " + ingredients.size() + " extensions but the station has 4");
                }
                for (IngredientWithCount ingredient : ingredients) {
                    if (ingredient.count() != 1) {
                        problems.add(holder.id().identifier() + " asks for a stack of " + ingredient.count()
                                + " but an extension slot holds one item");
                    }
                }
            }
            if (!problems.isEmpty()) {
                helper.fail(String.join("; ", problems));
                return;
            }

            RecipeHolder<?> carapace = helper.getLevel().recipeAccess()
                    .byKey(ResourceKey.create(Registries.RECIPE, Nautec.rl("vent_carapace")))
                    .orElse(null);
            if (carapace == null || !(carapace.value() instanceof AugmentationRecipe recipe)) {
                helper.fail("nautec:vent_carapace is not a loaded augmentation recipe: " + carapace);
                return;
            }
            List<ItemStack> plates = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                plates.add(new ItemStack(NTItems.CHITIN_PLATE.get()));
            }
            if (!recipe.matches(new AugmentationRecipeInput(plates, 100), helper.getLevel())) {
                helper.fail("Four extensions holding one Chitin Plate each do not match the Vent Carapace recipe");
                return;
            }
            if (recipe.matches(new AugmentationRecipeInput(plates.subList(0, 3), 100), helper.getLevel())) {
                helper.fail("Three Chitin Plates matched Vent Carapace, the cost should stay at four");
                return;
            }
            helper.succeed();
        });

        r.add("datafix/lucky_zone_crate_comes_with_loot", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            LootTable treasure = level.getServer().reloadableRegistries().getLootTable(NTLootTables.LUCKY_ZONE_TREASURE);
            LootParams params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1))))
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                    .create(LootContextParamSets.FISHING);

            ItemStack crate = ItemStack.EMPTY;
            for (int attempt = 0; attempt < 600 && crate.isEmpty(); attempt++) {
                for (ItemStack stack : treasure.getRandomItems(params)) {
                    if (stack.is(NTBlocks.CRATE.asItem())) {
                        crate = stack;
                    }
                }
            }
            if (crate.isEmpty()) {
                helper.fail("Lucky zone treasure rolled 600 times without a Crate");
                return;
            }

            SeededContainerLoot loot = crate.get(DataComponents.CONTAINER_LOOT);
            if (loot == null || !loot.lootTable().equals(NTLootTables.CRATE)) {
                helper.fail("A fished Crate should carry the nautec:chests/crate loot table, it had " + loot);
                return;
            }

            BlockPos pos = new BlockPos(1, 2, 1);
            helper.setBlock(pos, NTBlocks.CRATE.get());
            CrateBlockEntity blockEntity = helper.getBlockEntity(pos, CrateBlockEntity.class);
            blockEntity.applyComponentsFromItemStack(crate);
            blockEntity.unpackLootTable(null);
            if (blockEntity.isEmpty()) {
                helper.fail("A placed fished Crate was still empty after its loot table unpacked");
                return;
            }
            helper.succeed();
        });

        r.add("datafix/every_item_lang_key_is_a_registered_item", 5, helper -> {
            JsonObject lang = readJson("/assets/" + Nautec.MODID + "/lang/en_us.json");
            if (lang == null) {
                helper.fail("Could not read en_us.json from the mod jar");
                return;
            }
            String prefix = "item." + Nautec.MODID + ".";
            List<String> orphans = new ArrayList<>();
            for (String key : lang.keySet()) {
                if (!key.startsWith(prefix)) {
                    continue;
                }
                String path = key.substring(prefix.length());
                if (path.contains(".")) {
                    continue;
                }
                if (!BuiltInRegistries.ITEM.containsKey(Nautec.rl(path))) {
                    orphans.add(key);
                }
            }
            if (!orphans.isEmpty()) {
                helper.fail("Lang keys for items that are not registered: " + String.join(", ", orphans));
                return;
            }
            helper.succeed();
        });

        r.add("datafix/atlantean_rifle_only_takes_infinity", 5, helper -> {
            HolderLookup.RegistryLookup<Enchantment> enchantments =
                    helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            ItemStack rifle = new ItemStack(NTItems.ATLANTEAN_RIFLE.get());

            if (!rifle.supportsEnchantment(enchantments.getOrThrow(Enchantments.INFINITY))
                    || !rifle.isPrimaryItemFor(enchantments.getOrThrow(Enchantments.INFINITY))) {
                helper.fail("The Atlantean Rifle must still take Infinity, which halves its power draw");
                return;
            }

            List<String> accepted = new ArrayList<>();
            for (ResourceKey<Enchantment> key : List.of(Enchantments.POWER, Enchantments.PUNCH, Enchantments.FLAME,
                    Enchantments.UNBREAKING, Enchantments.MENDING)) {
                if (rifle.supportsEnchantment(enchantments.getOrThrow(key))) {
                    accepted.add(key.identifier().toString());
                }
            }
            if (!accepted.isEmpty()) {
                helper.fail("The Atlantean Rifle accepts enchantments that do nothing on it: " + String.join(", ", accepted));
                return;
            }
            helper.succeed();
        });
    }

    private static ItemStack craft(GameTestHelper helper, int width, int height, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, height, items);
        return helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(holder -> holder.value().assemble(input))
                .orElse(ItemStack.EMPTY);
    }

    private static JsonObject readJson(String path) {
        try (InputStream stream = DataFixTests.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private DataFixTests() {
    }
}
