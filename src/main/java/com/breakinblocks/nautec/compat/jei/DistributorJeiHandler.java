package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.client.screen.DistributorScreen;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DistributorJeiHandler implements IGhostIngredientHandler<DistributorScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(DistributorScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!screen.hasSelection()) {
            return List.of();
        }
        List<Target<I>> targets = new ArrayList<>();
        Optional<ItemStack> item = ingredient.getItemStack();
        Optional<FluidStack> fluid = ingredient.getIngredient(NeoForgeTypes.FLUID_STACK);
        if (item.isPresent() && !item.get().isEmpty()) {
            for (int slot = 0; slot < DistributorLink.ITEM_REQUESTS; slot++) {
                int index = slot;
                Rect2i area = screen.itemSlotArea(slot);
                targets.add(GhostTargets.of(area, () -> screen.setItemRequest(index, item.get())));
            }
            FluidStack contained = DistributorScreen.contained(item.get());
            if (!contained.isEmpty()) {
                for (int slot = 0; slot < DistributorLink.FLUID_REQUESTS; slot++) {
                    int index = slot;
                    targets.add(GhostTargets.of(screen.fluidSlotArea(slot), () -> screen.setFluidRequest(index, contained)));
                }
            }
        }
        if (fluid.isPresent() && !fluid.get().isEmpty()) {
            for (int slot = 0; slot < DistributorLink.FLUID_REQUESTS; slot++) {
                int index = slot;
                targets.add(GhostTargets.of(screen.fluidSlotArea(slot), () -> screen.setFluidRequest(index, fluid.get())));
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
