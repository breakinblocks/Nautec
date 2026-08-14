package com.breakinblocks.nautec.datagen;

import net.minecraft.resources.Identifier;

public final class ModelPaths {
    private ModelPaths() {
    }

    public static Identifier blockModel(Identifier name) {
        return blockModel(name, "");
    }

    public static Identifier blockModel(Identifier name, String suffix) {
        return inFolder(name, "block/", suffix);
    }

    public static Identifier itemModel(Identifier name) {
        return inFolder(name, "item/", "");
    }

    public static Identifier extend(Identifier id, String suffix) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + suffix);
    }

    private static Identifier inFolder(Identifier name, String folder, String suffix) {
        return Identifier.fromNamespaceAndPath(name.getNamespace(), folder + name.getPath() + suffix);
    }
}
