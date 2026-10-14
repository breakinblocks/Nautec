package com.breakinblocks.nautec.compat.refinedstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

final class RSKeys {
    private RSKeys() {
    }

    static ResourceKey item(Item item) {
        return new ItemResource(item);
    }

    static ResourceKey fluid(Fluid fluid) {
        return new FluidResource(fluid);
    }

    static @Nullable Item itemOf(ResourceKey key) {
        return key instanceof ItemResource resource ? resource.item() : null;
    }

    static @Nullable Fluid fluidOf(ResourceKey key) {
        return key instanceof FluidResource resource ? resource.fluid() : null;
    }

    static DataComponentPatch components(ResourceKey key) {
        if (key instanceof ItemResource resource) {
            return resource.components();
        }
        if (key instanceof FluidResource resource) {
            return resource.components();
        }
        return DataComponentPatch.EMPTY;
    }

    static int maxStackSize(ResourceKey key) {
        return key instanceof ItemResource resource ? resource.toItemStack().getMaxStackSize() : 1;
    }

    static ItemStack stack(ResourceKey key, int count) {
        return key instanceof ItemResource resource ? resource.toItemStack(count) : ItemStack.EMPTY;
    }
}
