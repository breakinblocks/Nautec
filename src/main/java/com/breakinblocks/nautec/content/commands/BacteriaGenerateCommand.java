package com.breakinblocks.nautec.content.commands;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.content.bacteria.SimpleBacteria;
import com.breakinblocks.nautec.data.generated.BacteriaBalance;
import com.breakinblocks.nautec.data.generated.BacteriaJsonWriter;
import com.breakinblocks.nautec.data.generated.GeneratedPackFinder;
import com.breakinblocks.nautec.data.generated.GeneratedPackPaths;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import com.mojang.serialization.JsonOps;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.NTRegistries;

public final class BacteriaGenerateCommand {
    private static final SuggestionProvider<CommandSourceStack> RARITIES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(BacteriaBalance.Rarity.names(), builder);
    private static final SuggestionProvider<CommandSourceStack> ITEM_TAGS =
            (ctx, builder) -> SharedSuggestionProvider.suggestResource(
                    BuiltInRegistries.ITEM.getTags().map(tag -> tag.key().location()), builder);
    private static final SuggestionProvider<CommandSourceStack> BIOME_TAGS =
            (ctx, builder) -> SharedSuggestionProvider.suggestResource(
                    ctx.getSource().registryAccess().lookupOrThrow(Registries.BIOME).listTagIds().map(TagKey::location), builder);

    private BacteriaGenerateCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> nautecCommand = Commands.literal(Nautec.MODID)
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()));

        LiteralArgumentBuilder<CommandSourceStack> generate = Commands.literal("generate")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                .executes(ctx -> generate(ctx, null, BacteriaBalance.Rarity.UNCOMMON, false))
                                .then(Commands.literal("preview")
                                        .executes(ctx -> generate(ctx, null, BacteriaBalance.Rarity.UNCOMMON, true)))
                                .then(Commands.argument("rarity", StringArgumentType.word())
                                        .suggests(RARITIES)
                                        .executes(ctx -> generate(ctx, null, rarity(ctx), false))
                                        .then(Commands.literal("preview")
                                                .executes(ctx -> generate(ctx, null, rarity(ctx), true))))));

        LiteralArgumentBuilder<CommandSourceStack> generateTag = Commands.literal("generate-tag")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("item", ItemArgument.item(buildContext))
                                .then(Commands.argument("tag", IdentifierArgument.id())
                                        .suggests(ITEM_TAGS)
                                        .executes(ctx -> generate(ctx, nutrientTag(ctx), BacteriaBalance.Rarity.UNCOMMON, false))
                                        .then(Commands.literal("preview")
                                                .executes(ctx -> generate(ctx, nutrientTag(ctx), BacteriaBalance.Rarity.UNCOMMON, true)))
                                        .then(Commands.argument("rarity", StringArgumentType.word())
                                                .suggests(RARITIES)
                                                .executes(ctx -> generate(ctx, nutrientTag(ctx), rarity(ctx), false))
                                                .then(Commands.literal("preview")
                                                        .executes(ctx -> generate(ctx, nutrientTag(ctx), rarity(ctx), true)))))));

        LiteralArgumentBuilder<CommandSourceStack> obtain = Commands.literal("obtain")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("block", BlockStateArgument.block(buildContext))
                                .then(Commands.argument("biometag", IdentifierArgument.id())
                                        .suggests(BIOME_TAGS)
                                        .then(Commands.argument("chance", FloatArgumentType.floatArg(0f, 1f))
                                                .executes(BacteriaGenerateCommand::obtain)))));

        LiteralArgumentBuilder<CommandSourceStack> list = Commands.literal("list-generated")
                .executes(BacteriaGenerateCommand::listGenerated);

        LiteralArgumentBuilder<CommandSourceStack> delete = Commands.literal("delete-generated")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(BacteriaGenerateCommand::deleteGenerated));

        dispatcher.register(nautecCommand.then(Commands.literal("bacteria")
                .then(generate)
                .then(generateTag)
                .then(obtain)
                .then(list)
                .then(delete)));
    }

    private static BacteriaBalance.Rarity rarity(CommandContext<CommandSourceStack> ctx) {
        String raw = StringArgumentType.getString(ctx, "rarity");
        return BacteriaBalance.Rarity.byName(raw).orElse(null);
    }

    private static TagKey<Item> nutrientTag(CommandContext<CommandSourceStack> ctx) {
        return TagKey.create(Registries.ITEM, IdentifierArgument.getId(ctx, "tag"));
    }

    private static int generate(CommandContext<CommandSourceStack> ctx, TagKey<Item> nutrientTag,
                                BacteriaBalance.Rarity rarity, boolean preview) {
        CommandSourceStack source = ctx.getSource();
        if (rarity == null) {
            source.sendFailure(Component.literal("Unknown rarity. Pick one of " + String.join(", ", BacteriaBalance.Rarity.names())));
            return 0;
        }

        String name = StringArgumentType.getString(ctx, "name");
        if (!GeneratedPackPaths.isValidName(name)) {
            source.sendFailure(Component.literal("'" + name + "' is not a usable bacteria name. Use lower case letters, numbers, _ , - and ."));
            return 0;
        }
        if (NTBacterias.bacterias().contains(GeneratedPackPaths.bacteriaKey(name))
                || NTBacterias.bacterias().contains(ResourceKey.create(NTRegistries.BACTERIA_KEY, Nautec.rl(name)))) {
            source.sendFailure(Component.literal("Nautec already ships a bacteria called '" + name + "'"));
            return 0;
        }
        if (!preview && Files.exists(GeneratedPackPaths.bacteriaFile(name))) {
            source.sendFailure(Component.literal("A generated bacteria called '" + name + "' already exists. Delete it first with /nautec bacteria delete-generated " + name));
            return 0;
        }

        Item item;
        try {
            item = ItemArgument.getItem(ctx, "item").item().value();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Could not read the item argument: " + e.getMessage()));
            return 0;
        }

        Ingredient nutrient;
        if (nutrientTag != null) {
            if (BuiltInRegistries.ITEM.get(nutrientTag).map(holders -> holders.size() == 0).orElse(true)) {
                source.sendSuccess(() -> Component.literal("Note: the item tag " + nutrientTag.location() + " is empty right now. The recipe will start working once a mod fills it.")
                        .withStyle(ChatFormatting.YELLOW), false);
            }
            nutrient = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(nutrientTag));
        } else {
            nutrient = Ingredient.of(item);
        }

        Identifier id = GeneratedPackPaths.bacteriaId(name);
        ResourceKey<Bacteria> key = GeneratedPackPaths.bacteriaKey(name);
        SimpleBacteria bacteria = BacteriaBalance.buildBacteria(item, rarity, id);

        JsonElement bacteriaJson;
        JsonElement incubationJson;
        JsonElement mutationJson;
        try {
            bacteriaJson = BacteriaJsonWriter.encodeBacteria(bacteria, source.registryAccess());
            incubationJson = BacteriaJsonWriter.encodeRecipe(BacteriaBalance.incubationRecipe(key, nutrient, rarity), source.registryAccess());
            mutationJson = BacteriaJsonWriter.encodeRecipe(BacteriaBalance.mutationRecipe(key, Ingredient.of(item), rarity), source.registryAccess());
        } catch (Exception e) {
            source.sendFailure(Component.literal("Could not build the bacteria json: " + e.getMessage()));
            return 0;
        }

        if (preview) {
            source.sendSuccess(() -> Component.literal("Preview of " + id + " (" + rarity.lowerName() + "), nothing was written")
                    .withStyle(ChatFormatting.AQUA), false);
            sendCopyable(source, "bacteria", bacteriaJson);
            sendCopyable(source, "incubation recipe", incubationJson);
            sendCopyable(source, "mutation recipe", mutationJson);
            return 1;
        }

        try {
            GeneratedPackFinder.scaffold();
            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.bacteriaFile(name), BacteriaJsonWriter.pretty(bacteriaJson));
            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.incubationRecipeFile(name), BacteriaJsonWriter.pretty(incubationJson));
            BacteriaJsonWriter.writeAtomic(GeneratedPackPaths.mutationRecipeFile(name), BacteriaJsonWriter.pretty(mutationJson));
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not write the generated files: " + e.getMessage()));
            Nautec.LOGGER.error("Could not write generated bacteria {}", name, e);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Wrote " + id + " (" + rarity.lowerName() + ") producing "
                + BuiltInRegistries.ITEM.getKey(item) + ", mutating from " + rarity.mutationParent().identifier()).withStyle(ChatFormatting.GREEN), true);
        sendLifecycleNote(source);
        return 1;
    }

    private static int obtain(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (!GeneratedPackPaths.isValidName(name)) {
            source.sendFailure(Component.literal("'" + name + "' is not a usable bacteria name"));
            return 0;
        }

        Block block = BlockStateArgument.getBlock(ctx, "block").getState().getBlock();
        Identifier biomeTagId = IdentifierArgument.getId(ctx, "biometag");
        TagKey<Biome> biomeTag = TagKey.create(Registries.BIOME, biomeTagId);
        float chance = FloatArgumentType.getFloat(ctx, "chance");

        String blockKey = BacteriaJsonWriter.blockKey(block);
        try {
            if (!BacteriaJsonWriter.readObtaining().has(blockKey) && shippedObtaining(source, block)) {
                source.sendFailure(Component.literal("Nautec already grants a bacteria from " + blockKey + ". Pick another block so the generated pack does not shadow it."));
                return 0;
            }
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not read the generated obtaining file: " + e.getMessage()));
            return 0;
        }

        if (source.registryAccess().lookupOrThrow(Registries.BIOME).get(biomeTag).isEmpty()) {
            source.sendSuccess(() -> Component.literal("Note: the biome tag " + biomeTagId + " is not known here, grafting will never fire until something defines it.")
                    .withStyle(ChatFormatting.YELLOW), false);
        }

        try {
            GeneratedPackFinder.scaffold();
            BacteriaJsonWriter.upsertObtaining(block, new BacteriaObtainValue(GeneratedPackPaths.bacteriaKey(name), biomeTag, chance));
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not write the obtaining entry: " + e.getMessage()));
            Nautec.LOGGER.error("Could not write the obtaining entry for {}", name, e);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Grafting " + blockKey + " in " + biomeTagId + " now has a "
                + Math.round(chance * 100) + "% chance of " + GeneratedPackPaths.bacteriaId(name)).withStyle(ChatFormatting.GREEN), true);
        sendLifecycleNote(source);
        return 1;
    }

    private static boolean shippedObtaining(CommandSourceStack source, Block block) {
        return block.builtInRegistryHolder().getData(NTDataMaps.BACTERIA_OBTAINING) != null;
    }

    private static int listGenerated(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        List<String> names = GeneratedPackPaths.listGeneratedNames();

        if (names.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No generated bacteria yet. Make one with /nautec bacteria generate <name> <item>"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal(names.size() + " generated bacteria in " + GeneratedPackPaths.root())
                .withStyle(ChatFormatting.AQUA), false);
        for (String name : names) {
            source.sendSuccess(() -> describe(name), false);
        }
        source.sendSuccess(() -> Component.literal("The generated pack is shared by every world in this instance."), false);
        return names.size();
    }

    private static Component describe(String name) {
        MutableComponent line = Component.literal(" - " + name).withStyle(ChatFormatting.WHITE);

        Bacteria bacteria = readBacteria(GeneratedPackPaths.bacteriaFile(name));
        if (bacteria == null) {
            return line.append(Component.literal(" (unreadable json)").withStyle(ChatFormatting.RED));
        }

        String rarity = BacteriaBalance.inferRarity(bacteria).map(BacteriaBalance.Rarity::lowerName).orElse("custom");
        Item item = bacteria.resource().resolve();
        if (item != Items.AIR) {
            line.append(Component.literal(", makes " + BuiltInRegistries.ITEM.getKey(item)));
        } else {
            line.append(Component.literal(", produces nothing (item missing)").withStyle(ChatFormatting.YELLOW));
        }
        line.append(Component.literal(", " + rarity));

        StringBuilder extras = new StringBuilder();
        if (Files.exists(GeneratedPackPaths.incubationRecipeFile(name))) {
            extras.append("incubation");
        }
        if (Files.exists(GeneratedPackPaths.mutationRecipeFile(name))) {
            extras.append(extras.isEmpty() ? "mutation" : ", mutation");
        }
        line.append(Component.literal(extras.isEmpty() ? ", no recipes" : ", " + extras));

        return line;
    }

    private static Bacteria readBacteria(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return Bacteria.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static int deleteGenerated(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        if (!GeneratedPackPaths.isValidName(name)) {
            source.sendFailure(Component.literal("'" + name + "' is not a usable bacteria name"));
            return 0;
        }

        int deleted = 0;
        try {
            if (Files.deleteIfExists(GeneratedPackPaths.bacteriaFile(name))) {
                deleted++;
            }
            if (Files.deleteIfExists(GeneratedPackPaths.incubationRecipeFile(name))) {
                deleted++;
            }
            if (Files.deleteIfExists(GeneratedPackPaths.mutationRecipeFile(name))) {
                deleted++;
            }
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not delete the generated files: " + e.getMessage()));
            return 0;
        }

        List<String> unlinked;
        try {
            unlinked = BacteriaJsonWriter.removeObtainingFor(GeneratedPackPaths.bacteriaId(name));
        } catch (IOException e) {
            source.sendFailure(Component.literal("Files were deleted but the obtaining entries could not be updated: " + e.getMessage()));
            return 0;
        }

        if (deleted == 0 && unlinked.isEmpty()) {
            source.sendFailure(Component.literal("Nothing generated is called '" + name + "'"));
            return 0;
        }

        int removedFiles = deleted;
        source.sendSuccess(() -> Component.literal("Deleted " + removedFiles + " file(s) for " + GeneratedPackPaths.bacteriaId(name)
                + (unlinked.isEmpty() ? "" : " and " + unlinked.size() + " obtaining entry(s)")).withStyle(ChatFormatting.GREEN), true);
        source.sendSuccess(() -> Component.literal("Recipes and obtaining entries go away after /reload. The registry entry is gone the next time you join a world."), false);
        source.sendSuccess(() -> Component.literal("The generated pack is shared by every world in this instance."), false);
        return 1;
    }

    private static void sendCopyable(CommandSourceStack source, String what, JsonElement json) {
        String text = BacteriaJsonWriter.pretty(json);
        source.sendSuccess(() -> Component.literal(what + " (click to copy):").withStyle(ChatFormatting.GRAY)
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent.CopyToClipboard(text)).withColor(ChatFormatting.GRAY)), false);
        source.sendSuccess(() -> Component.literal(text), false);
    }

    private static void sendLifecycleNote(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Run /reload to pick up the recipes and obtaining entries. The bacteria itself loads the next time you join a world."), false);
        source.sendSuccess(() -> Component.literal("The generated pack is shared by every world in this instance."), false);
    }
}
