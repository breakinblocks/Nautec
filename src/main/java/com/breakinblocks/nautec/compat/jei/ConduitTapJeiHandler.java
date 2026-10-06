package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.client.screen.ConduitTapScreen;
import com.breakinblocks.nautec.client.screen.DistributorScreen;
import com.breakinblocks.nautec.content.conduits.TapFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ConduitTapJeiHandler implements IGhostIngredientHandler<ConduitTapScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(ConduitTapScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        if (!screen.filtersOpen()) {
            return List.of();
        }
        List<Target<I>> targets = new ArrayList<>();
        Optional<ItemStack> item = ingredient.getItemStack();
        Optional<FluidStack> fluid = ingredient.getIngredient(NeoForgeTypes.FLUID_STACK);
        if (item.isPresent() && !item.get().isEmpty()) {
            ItemStack stack = item.get();
            int open = screen.itemFilterSlots();
            for (int slot = 0; slot < open; slot++) {
                int index = slot;
                targets.add(GhostTargets.of(screen.itemSlotArea(slot), () -> screen.setItemFilter(index, stack)));
            }
            FluidStack contained = DistributorScreen.contained(stack);
            if (!contained.isEmpty()) {
                for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
                    int index = slot;
                    targets.add(GhostTargets.of(screen.fluidSlotArea(slot), () -> screen.setFluidFilter(index, contained)));
                }
            }
        }
        if (fluid.isPresent() && !fluid.get().isEmpty()) {
            FluidStack stack = fluid.get();
            for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
                int index = slot;
                targets.add(GhostTargets.of(screen.fluidSlotArea(slot), () -> screen.setFluidFilter(index, stack)));
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
