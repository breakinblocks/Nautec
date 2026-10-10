package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.network.SetGhostInputPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import com.breakinblocks.nautec.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class GhostSlots {
    private static final int FADE = PanelStyle.GHOST_FADE;

    private GhostSlots() {
    }

    public static int machineSlot(ContainerBlockEntity machine, Slot slot) {
        if (slot instanceof ResourceHandlerSlot handlerSlot && handlerSlot.getResourceHandler() == machine.getItemStackHandler()
                && machine.isGhostSlot(slot.getContainerSlot())) {
            return slot.getContainerSlot();
        }
        return -1;
    }

    public static void extract(GuiGraphics graphics, AbstractContainerMenu menu, ContainerBlockEntity machine, int left, int top) {
        if (machine.ghosts().isEmpty()) {
            return;
        }
        for (Slot slot : menu.slots) {
            int index = machineSlot(machine, slot);
            if (index < 0 || slot.hasItem()) {
                continue;
            }
            ItemStack ghost = machine.getGhost(index);
            if (!ghost.isEmpty()) {
                graphics.renderItem(ghost, left + slot.x, top + slot.y);
                NTGui.pushOverItems(graphics);
                graphics.fill(left + slot.x, top + slot.y, left + slot.x + 16, top + slot.y + 16, FADE);
                NTGui.popOverItems(graphics);
            }
        }
    }

    public static void tooltip(GuiGraphics graphics, Font font, ContainerBlockEntity machine, @Nullable Slot hovered, int mouseX, int mouseY) {
        if (hovered == null || hovered.hasItem()) {
            return;
        }
        int index = machineSlot(machine, hovered);
        if (index < 0) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        ItemStack ghost = machine.getGhost(index);
        if (!ghost.isEmpty()) {
            lines.add(Component.translatable("nautec.ghost_input.set", ghost.getHoverName()).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("nautec.ghost_input.clear").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("nautec.ghost_input.title"));
            lines.add(Component.translatable("nautec.ghost_input.how").withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("nautec.ghost_input.distributor").withStyle(ChatFormatting.DARK_GRAY));
        graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
    }

    public static boolean click(AbstractContainerMenu menu, ContainerBlockEntity machine, @Nullable Slot hovered) {
        if (!Screen.hasShiftDown() || hovered == null || hovered.hasItem()) {
            return false;
        }
        int index = machineSlot(machine, hovered);
        if (index < 0) {
            return false;
        }
        ItemStack carried = menu.getCarried();
        if (!carried.isEmpty()) {
            send(menu, index, carried.copyWithCount(1));
            return true;
        }
        if (!machine.getGhost(index).isEmpty()) {
            send(menu, index, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    public static void send(AbstractContainerMenu menu, int index, ItemStack stack) {
        PacketDistributor.sendToServer(new SetGhostInputPayload(menu.containerId, index, stack));
    }
}
