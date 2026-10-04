package com.breakinblocks.nautec.utils;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class ItemTemplates {
    private ItemTemplates() {
    }

    public static boolean matches(ItemStack template, ItemResource resource) {
        if (template.isEmpty() || resource.isEmpty() || !resource.is(template.getItem())) {
            return false;
        }
        if (DishPort.isDish(template)) {
            BacteriaInstance wanted = DishPort.colonyOf(template);
            BacteriaInstance offered = DishPort.colonyOf(resource.toStack());
            return wanted.isEmpty() ? offered.isEmpty() : !offered.isEmpty() && offered.is(wanted.getBacteria());
        }
        return ItemResource.of(template).equals(resource);
    }
}
