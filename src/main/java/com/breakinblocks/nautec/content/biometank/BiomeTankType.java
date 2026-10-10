package com.breakinblocks.nautec.content.biometank;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public enum BiomeTankType {
    GLOW_POLYP("glow_polyp", "Glow Polyp", () -> NTBlocks.GLOW_POLYP.asItem(), Nautec.rl("block/glow_polyp")),
    LUMINESCENT_ALGAE("luminescent_algae", "Luminescent Algae", () -> NTBlocks.LUMINESCENT_ALGAE.asItem(), Nautec.rl("block/luminescent_algae")),
    DEEP_KELP("deep_kelp", "Deep Kelp", () -> NTBlocks.DEEP_KELP.asItem(), Nautec.rl("block/deep_kelp")),
    PRISMARINE_FROND("prismarine_frond", "Prismarine Frond", () -> NTBlocks.PRISMARINE_FROND.asItem(), Nautec.rl("block/prismarine_frond")),
    ABYSSAL_CORAL("abyssal_coral", "Abyssal Coral", () -> NTBlocks.ABYSSAL_CORAL.asItem(), Nautec.rl("block/abyssal_coral")),
    VENT_TUBEWORM("vent_tubeworm", "Vent Tubeworm", () -> NTBlocks.VENT_TUBEWORM.asItem(), Nautec.rl("block/vent_tubeworm")),
    KELP("kelp", "Kelp", () -> Items.KELP, vanilla("block/kelp_plant")),
    SEAGRASS("seagrass", "Seagrass", () -> Items.SEAGRASS, vanilla("block/seagrass")),
    SEA_PICKLE("sea_pickle", "Sea Pickle", () -> Items.SEA_PICKLE, vanilla("item/sea_pickle")),
    TUBE_CORAL("tube_coral", "Tube Coral", () -> Items.TUBE_CORAL, vanilla("block/tube_coral")),
    BRAIN_CORAL("brain_coral", "Brain Coral", () -> Items.BRAIN_CORAL, vanilla("block/brain_coral")),
    BUBBLE_CORAL("bubble_coral", "Bubble Coral", () -> Items.BUBBLE_CORAL, vanilla("block/bubble_coral")),
    FIRE_CORAL("fire_coral", "Fire Coral", () -> Items.FIRE_CORAL, vanilla("block/fire_coral")),
    HORN_CORAL("horn_coral", "Horn Coral", () -> Items.HORN_CORAL, vanilla("block/horn_coral")),
    CACTUS("cactus", "Cactus", () -> Items.CACTUS, vanilla("block/cactus_side")),
    SUGAR_CANE("sugar_cane", "Sugar Cane", () -> Items.SUGAR_CANE, vanilla("item/sugar_cane")),
    BAMBOO("bamboo", "Bamboo", () -> Items.BAMBOO, vanilla("item/bamboo")),
    SWEET_BERRIES("sweet_berries", "Sweet Berry", () -> Items.SWEET_BERRIES, vanilla("block/sweet_berry_bush_stage3")),
    GLOW_BERRIES("glow_berries", "Glow Berry", () -> Items.GLOW_BERRIES, vanilla("item/glow_berries")),
    RED_MUSHROOM("red_mushroom", "Red Mushroom", () -> Items.RED_MUSHROOM, vanilla("block/red_mushroom")),
    BROWN_MUSHROOM("brown_mushroom", "Brown Mushroom", () -> Items.BROWN_MUSHROOM, vanilla("block/brown_mushroom")),
    CRIMSON_FUNGUS("crimson_fungus", "Crimson Fungus", () -> Items.CRIMSON_FUNGUS, vanilla("block/crimson_fungus")),
    WARPED_FUNGUS("warped_fungus", "Warped Fungus", () -> Items.WARPED_FUNGUS, vanilla("block/warped_fungus")),
    CHORUS_FRUIT("chorus_fruit", "Chorus", () -> Items.CHORUS_FRUIT, vanilla("block/chorus_flower")),
    COCOA_BEANS("cocoa_beans", "Cocoa", () -> Items.COCOA_BEANS, vanilla("item/cocoa_beans"));

    private final String id;
    private final String displayName;
    private final Supplier<Item> plant;
    private final ResourceLocation texture;

    BiomeTankType(String id, String displayName, Supplier<Item> plant, ResourceLocation texture) {
        this.id = id;
        this.displayName = displayName;
        this.plant = plant;
        this.texture = texture;
    }

    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    public String id() {
        return id;
    }

    public String blockName() {
        return id + "_biome_tank";
    }

    public String displayName() {
        return displayName;
    }

    public Item plant() {
        return plant.get();
    }

    public ResourceLocation texture() {
        return texture;
    }
}
