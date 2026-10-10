package com.breakinblocks.nautec.gametest;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuideReferencesGameTest {
    private static final String GUIDE_ROOT = "assets/nautec/guides/nautec/guide";
    private static final Pattern RECIPE = Pattern.compile("<Recipe id=\"([^\"]+)\"");
    private static final Pattern ITEM = Pattern.compile("<(?:ItemImage|ItemLink|ItemIcon|RecipeFor|RecipesFor) id=\"([^\"]+)\"");
    private static final Pattern BLOCK = Pattern.compile("<(?:Block|BlockImage) id=\"([^\"]+)\"");
    private static final Pattern ENTITY = Pattern.compile("<Entity id=\"([^\"]+)\"");
    private static final Pattern ITEM_LINK = Pattern.compile("<ItemLink id=\"([^\"]+)\"");
    private static final Pattern CLAIMED_ITEM = Pattern.compile("(?m)^  - ([a-z0-9_.-]+:[a-z0-9_/.-]+)\\s*$");
    private static final Pattern FRONTMATTER_ITEM = Pattern.compile("(?m)^\\s*(?:icon:|-)\\s*([a-z0-9_.-]+:[a-z0-9_/.-]+)\\s*$");

    public static void allGuideReferencesResolve(GameTestHelper helper) {
        Map<String, String> pages = new TreeMap<>();
        Path root = ModList.get().getModFileById(Nautec.MODID).getFile().findResource(GUIDE_ROOT.split("/"));
        if (Files.isDirectory(root)) {
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(file -> file.toString().endsWith(".md")).forEach(file -> {
                    String path = GUIDE_ROOT + "/" + root.relativize(file).toString().replace(root.getFileSystem().getSeparator(), "/");
                    try {
                        pages.put(path, Files.readString(file, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        pages.put(path, "");
                    }
                });
            } catch (IOException e) {
                helper.fail("Could not read guide pages: " + e.getMessage());
                return;
            }
        }
        if (pages.isEmpty()) {
            helper.fail("No guide pages found under " + GUIDE_ROOT);
            return;
        }

        RecipeManager recipes = helper.getLevel().getRecipeManager();
        List<String> errors = new ArrayList<>();
        Map<ResourceLocation, String> claimed = new HashMap<>();
        pages.forEach((path, text) -> each(CLAIMED_ITEM, text, id -> {
            String previous = claimed.putIfAbsent(id, path);
            if (previous != null) errors.add(path + " claims " + id + " already claimed by " + previous);
        }));
        pages.forEach((path, text) -> {
            each(ITEM_LINK, text, id -> {
                if (id.getNamespace().equals(Nautec.MODID) && !claimed.containsKey(id)) errors.add(path + " links " + id + " which no page lists in item_ids");
            });
            each(RECIPE, text, id -> {
                if (recipes.byKey(id).isEmpty()) errors.add(path + " recipe " + id);
            });
            each(ITEM, text, id -> {
                if (!BuiltInRegistries.ITEM.containsKey(id)) errors.add(path + " item " + id);
            });
            each(FRONTMATTER_ITEM, text, id -> {
                if (!BuiltInRegistries.ITEM.containsKey(id)) errors.add(path + " frontmatter item " + id);
            });
            each(BLOCK, text, id -> {
                if (!BuiltInRegistries.BLOCK.containsKey(id)) errors.add(path + " block " + id);
            });
            each(ENTITY, text, id -> {
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) errors.add(path + " entity " + id);
            });
        });

        if (!errors.isEmpty()) {
            helper.fail("Guide references that don't resolve:\n  " + String.join("\n  ", errors));
            return;
        }
        helper.succeed();
    }

    private static void each(Pattern pattern, String text, Consumer<ResourceLocation> action) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String raw = matcher.group(1);
            action.accept(raw.contains(":") ? ResourceLocation.parse(raw) : Nautec.rl(raw));
        }
    }
}
