package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.client.screen.ConduitTapScreen;
import com.breakinblocks.nautec.client.screen.ConfinedSpawnerScreen;
import com.breakinblocks.nautec.client.screen.DistributorScreen;
import com.breakinblocks.nautec.client.screen.GhostSlots;
import com.breakinblocks.nautec.content.conduits.TapFilter;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class NTDragDropHandler implements EmiDragDropHandler<Screen> {
    private static final int HIGHLIGHT = 0x8822BB33;

    private record Target(Bounds bounds, Runnable action) {
    }

    @Override
    public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
        for (Target target : targets(screen, stack)) {
            if (target.bounds().contains(x, y)) {
                target.action().run();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
        for (Target target : targets(screen, dragged)) {
            Bounds bounds = target.bounds();
            draw.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), HIGHLIGHT);
        }
    }

    private static List<Target> targets(Screen screen, EmiIngredient dragged) {
        List<Target> targets = new ArrayList<>();
        ItemStack item = NTEmiStacks.item(dragged);
        FluidStack fluid = NTEmiStacks.fluid(dragged);
        if (item.isEmpty() && fluid.isEmpty()) {
            return targets;
        }
        if (screen instanceof ConfinedSpawnerScreen spawner) {
            spawner(spawner, item, targets);
        } else if (screen instanceof DistributorScreen distributor) {
            distributor(distributor, item, fluid, targets);
        } else if (screen instanceof ConduitTapScreen tap) {
            tap(tap, item, fluid, targets);
        }
        if (screen instanceof AbstractContainerScreen<?> container) {
            ghostInputs(container, item, targets);
        }
        return targets;
    }

    private static void spawner(ConfinedSpawnerScreen screen, ItemStack item, List<Target> targets) {
        if (item.isEmpty() || !screen.isPanelOpen()) {
            return;
        }
        List<Rect2i> areas = screen.filterSlotAreas();
        for (int slot = 0; slot < areas.size(); slot++) {
            int index = slot;
            targets.add(new Target(NTEmiPlugin.bounds(areas.get(slot)), () -> screen.setFilterSlot(index, SpawnerFilterEntry.of(item))));
        }
    }

    private static void distributor(DistributorScreen screen, ItemStack item, FluidStack fluid, List<Target> targets) {
        if (!screen.hasSelection()) {
            return;
        }
        if (!item.isEmpty()) {
            for (int slot = 0; slot < DistributorLink.ITEM_REQUESTS; slot++) {
                int index = slot;
                targets.add(new Target(NTEmiPlugin.bounds(screen.itemSlotArea(slot)), () -> screen.setItemRequest(index, item)));
            }
        }
        FluidStack request = fluid.isEmpty() ? DistributorScreen.contained(item) : fluid;
        if (!request.isEmpty()) {
            for (int slot = 0; slot < DistributorLink.FLUID_REQUESTS; slot++) {
                int index = slot;
                targets.add(new Target(NTEmiPlugin.bounds(screen.fluidSlotArea(slot)), () -> screen.setFluidRequest(index, request)));
            }
        }
    }

    private static void tap(ConduitTapScreen screen, ItemStack item, FluidStack fluid, List<Target> targets) {
        if (!screen.filtersOpen()) {
            return;
        }
        if (!item.isEmpty()) {
            int open = screen.itemFilterSlots();
            for (int slot = 0; slot < open; slot++) {
                int index = slot;
                targets.add(new Target(NTEmiPlugin.bounds(screen.itemSlotArea(slot)), () -> screen.setItemFilter(index, item)));
            }
        }
        FluidStack filter = fluid.isEmpty() ? DistributorScreen.contained(item) : fluid;
        if (!filter.isEmpty()) {
            for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
                int index = slot;
                targets.add(new Target(NTEmiPlugin.bounds(screen.fluidSlotArea(slot)), () -> screen.setFluidFilter(index, filter)));
            }
        }
    }

    private static void ghostInputs(AbstractContainerScreen<?> screen, ItemStack item, List<Target> targets) {
        ContainerBlockEntity machine = machine(screen);
        if (item.isEmpty() || machine == null) {
            return;
        }
        for (Slot slot : screen.getMenu().slots) {
            int index = GhostSlots.machineSlot(machine, slot);
            if (index < 0 || slot.hasItem()) {
                continue;
            }
            Bounds bounds = new Bounds(screen.getGuiLeft() + slot.x, screen.getGuiTop() + slot.y, 16, 16);
            targets.add(new Target(bounds, () -> GhostSlots.send(screen.getMenu(), index, item.copyWithCount(1))));
        }
    }

    private static @Nullable ContainerBlockEntity machine(AbstractContainerScreen<?> screen) {
        if (screen instanceof NTMachineScreen<?> machineScreen) {
            return machineScreen.getMenu().blockEntity;
        }
        if (screen instanceof NTAbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().blockEntity;
        }
        return null;
    }
}
