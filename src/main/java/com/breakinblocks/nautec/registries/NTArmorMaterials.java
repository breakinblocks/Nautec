package com.breakinblocks.nautec.registries;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class NTArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Nautec.MODID);

    public static final int PRISMARINE_DURABILITY = 5;
    public static final int DIVING_SUIT_DURABILITY = 20;
    public static final int AQUARINE_STEEL_DURABILITY = 5;

    public static final ResourceLocation DIVING_SUIT_HELMET_TEXTURE = Nautec.rl("textures/example/diving_suit.png");

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRISMARINE = register("prismarine",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 0);
                map.put(ArmorItem.Type.LEGGINGS, 0);
                map.put(ArmorItem.Type.CHESTPLATE, 0);
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.BODY, 2);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_ELYTRA,
            0,
            0,
            NTTags.Items.REPAIRS_PRISMARINE_ARMOR
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DIVING_SUIT = register("diving_suit",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 4);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            1,
            0.05f,
            NTTags.Items.REPAIRS_DIVING_SUIT
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> AQUARINE_STEEL = register("aquarine_steel",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.BODY, 11);
            }),
            10,
            SoundEvents.ARMOR_EQUIP_IRON,
            3,
            0.1f,
            NTTags.Items.REPAIRS_AQUARINE_ARMOR
    );

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> defense,
                                                                         int enchantmentValue, Holder<SoundEvent> equipSound,
                                                                         float toughness, float knockbackResistance,
                                                                         TagKey<Item> repairTag) {
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(Nautec.rl(name)));
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(defense, enchantmentValue, equipSound,
                () -> Ingredient.of(repairTag), layers, toughness, knockbackResistance));
    }

    private NTArmorMaterials() {
    }
}
