package com.breakinblocks.nautec.datagen;

import net.minecraft.resources.ResourceLocation;

public final class ModelPaths {
    private ModelPaths() {
    }

    public static ResourceLocation blockModel(ResourceLocation name) {
        return blockModel(name, "");
    }

    public static ResourceLocation blockModel(ResourceLocation name, String suffix) {
        return inFolder(name, "block/", suffix);
    }

    public static ResourceLocation itemModel(ResourceLocation name) {
        return inFolder(name, "item/", "");
    }

    public static ResourceLocation extend(ResourceLocation id, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + suffix);
    }

    private static ResourceLocation inFolder(ResourceLocation name, String folder, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), folder + name.getPath() + suffix);
    }
}
