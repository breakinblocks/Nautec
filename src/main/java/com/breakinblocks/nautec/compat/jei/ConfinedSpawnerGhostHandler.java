package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.client.screen.ConfinedSpawnerScreen;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ConfinedSpawnerGhostHandler implements IGhostIngredientHandler<ConfinedSpawnerScreen>, IGuiContainerHandler<ConfinedSpawnerScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(ConfinedSpawnerScreen screen, ITypedIngredient<I> ingredient, boolean doStart) {
        Optional<ItemStack> stack = ingredient.getItemStack();
        if (stack.isEmpty() || stack.get().isEmpty() || !screen.isPanelOpen()) {
            return List.of();
        }
        List<Rect2i> areas = screen.filterSlotAreas();
        List<Target<I>> targets = new ArrayList<>(areas.size());
        for (int slot = 0; slot < areas.size(); slot++) {
            int index = slot;
            Rect2i area = areas.get(slot);
            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return area;
                }

                @Override
                public void accept(I value) {
                    screen.setFilterSlot(index, SpawnerFilterEntry.of(stack.get()));
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    @Override
    public List<Rect2i> getGuiExtraAreas(ConfinedSpawnerScreen screen) {
        return screen.extraAreas();
    }
}
