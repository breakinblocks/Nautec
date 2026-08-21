package com.breakinblocks.nautec.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class Utils {
    public static IntList intArrayToList(int[] array) {
        return IntList.of(array);
    }

    public static <T> Component registryTranslation(Registry<T> registry, T registryObject) {
        Identifier objLoc = registry.getKey(registryObject);
        return Component.translatable(registry.key().identifier().getPath() + "." + objLoc.getNamespace() + "." + objLoc.getPath());
    }
    public static <T> Component registryTranslation(ResourceKey<T> registryObject) {
        Identifier objLoc = registryObject.identifier();
        String key = registryObject.registry().getPath() + "." + objLoc.getNamespace() + "." + objLoc.getPath();
        return Component.translatableWithFallback(key, prettify(objLoc.getPath()));
    }

    public static String prettify(String path) {
        return Arrays.stream(path.split("_"))
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .collect(Collectors.joining(" "));
    }
}
