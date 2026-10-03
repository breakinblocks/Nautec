package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.client.screen.GhostSlots;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GhostInputJeiHandler<T extends AbstractContainerScreen<?>> implements IGhostIngredientHandler<T> {
    private static @Nullable ContainerBlockEntity machine(AbstractContainerScreen<?> screen) {
        if (screen instanceof NTMachineScreen<?> machineScreen) {
            return machineScreen.getMenu().blockEntity;
        }
        if (screen instanceof NTAbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().blockEntity;
        }
        return null;
    }

    @Override
    public <I> List<Target<I>> getTargetsTyped(T screen, ITypedIngredient<I> ingredient, boolean doStart) {
        Optional<ItemStack> stack = ingredient.getItemStack();
        ContainerBlockEntity machine = machine(screen);
        if (stack.isEmpty() || stack.get().isEmpty() || machine == null) {
            return List.of();
        }
        List<Target<I>> targets = new ArrayList<>();
        for (Slot slot : screen.getMenu().slots) {
            int index = GhostSlots.machineSlot(machine, slot);
            if (index < 0 || slot.hasItem()) {
                continue;
            }
            Rect2i area = new Rect2i(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
            targets.add(new Target<>() {
                @Override
                public Rect2i getArea() {
                    return area;
                }

                @Override
                public void accept(I value) {
                    GhostSlots.send(screen.getMenu(), index, stack.get().copyWithCount(1));
                }
            });
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }
}
