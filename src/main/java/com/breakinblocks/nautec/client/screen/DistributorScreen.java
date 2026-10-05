package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.FluidTankRenderer;
import com.breakinblocks.nautec.content.distributor.DistributorLink;
import com.breakinblocks.nautec.content.menus.DistributorMenu;
import com.breakinblocks.nautec.network.DistributorEditPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DistributorScreen extends AbstractContainerScreen<DistributorMenu> {
    private static final int PANEL = PanelStyle.PANEL;
    private static final int PANEL_LIGHT = PanelStyle.PANEL_LIGHT;
    private static final int OUTLINE = PanelStyle.OUTLINE;
    private static final int SLOT_EDGE = PanelStyle.SLOT_EDGE;
    private static final int SLOT_FILL = PanelStyle.SLOT_FILL;
    private static final int SCREEN_FILL = PanelStyle.SCREEN_FILL;
    private static final int ROW_SELECTED = 0xFF2F5C8C;
    private static final int ROW_HOVER = 0xFF2A3634;
    private static final int LABEL = PanelStyle.LABEL;
    private static final int READOUT = PanelStyle.READOUT;
    private static final int READOUT_DIM = PanelStyle.READOUT_DIM;
    private static final int WARNING = 0xFFE36A5C;

    private static final int LIST_X = 7;
    private static final int LIST_Y = 18;
    private static final int LIST_WIDTH = 98;
    private static final int ROW_HEIGHT = 18;
    private static final int ROWS = 5;
    private static final int GRID_X = 112;
    private static final int GRID_Y = 19;
    private static final int FLUID_Y = 82;

    private final FluidTankRenderer fluidRenderer = new FluidTankRenderer(1, false, 16, 16);
    private int selected;
    private int scroll;

    public DistributorScreen(DistributorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 222);
        this.titleLabelY = 6;
        this.inventoryLabelY = DistributorMenu.INVENTORY_Y - 11;
    }

    private List<DistributorLink> links() {
        return this.menu.blockEntity.getLinks();
    }

    private DistributorLink current() {
        List<DistributorLink> links = links();
        if (links.isEmpty()) {
            return null;
        }
        selected = Math.max(0, Math.min(selected, links.size() - 1));
        return links.get(selected);
    }

    public Rect2i itemSlotArea(int slot) {
        return new Rect2i(leftPos + GRID_X + (slot % 3) * 18, topPos + GRID_Y + (slot / 3) * 18, 16, 16);
    }

    public Rect2i fluidSlotArea(int slot) {
        return new Rect2i(leftPos + GRID_X + slot * 18, topPos + FLUID_Y, 16, 16);
    }

    public boolean hasSelection() {
        return current() != null;
    }

    public void setItemRequest(int slot, ItemStack stack) {
        if (current() != null) {
            ClientPacketDistributor.sendToServer(DistributorEditPayload.item(this.menu.containerId, selected, slot,
                    stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(stack.getMaxStackSize())));
        }
    }

    public void setFluidRequest(int slot, FluidStack stack) {
        if (current() != null) {
            ClientPacketDistributor.sendToServer(DistributorEditPayload.fluid(this.menu.containerId, selected, slot,
                    stack.isEmpty() ? FluidStack.EMPTY : stack.copyWithAmount(Math.max(1000, stack.getAmount()))));
        }
    }

    private static boolean inside(double mouseX, double mouseY, Rect2i area) {
        return mouseX >= area.getX() && mouseX < area.getX() + area.getWidth() && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight();
    }

    private void frame(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, OUTLINE);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + w - 1, y + h - 1, PANEL);
    }

    private void slotFrame(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        frame(graphics, leftPos, topPos, imageWidth, imageHeight);
        int lx = leftPos + LIST_X;
        int ly = topPos + LIST_Y;
        graphics.fill(lx - 1, ly - 1, lx + LIST_WIDTH + 1, ly + ROWS * ROW_HEIGHT + 1, OUTLINE);
        graphics.fill(lx, ly, lx + LIST_WIDTH, ly + ROWS * ROW_HEIGHT, SCREEN_FILL);
        for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
            Rect2i area = itemSlotArea(i);
            slotFrame(graphics, area.getX(), area.getY());
        }
        for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
            Rect2i area = fluidSlotArea(i);
            slotFrame(graphics, area.getX(), area.getY());
        }
        for (Slot slot : this.menu.slots) {
            slotFrame(graphics, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        List<DistributorLink> links = links();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, links.size() - ROWS)));
        int lx = leftPos + LIST_X;
        int ly = topPos + LIST_Y;
        List<Component> tooltip = new ArrayList<>();
        for (int row = 0; row < ROWS && row + scroll < links.size(); row++) {
            int index = row + scroll;
            DistributorLink link = links.get(index);
            int y = ly + row * ROW_HEIGHT;
            boolean hover = mouseX >= lx && mouseX < lx + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT;
            if (index == selected) {
                graphics.fill(lx, y, lx + LIST_WIDTH, y + ROW_HEIGHT, ROW_SELECTED);
            } else if (hover) {
                graphics.fill(lx, y, lx + LIST_WIDTH, y + ROW_HEIGHT, ROW_HOVER);
            }
            BlockState state = this.minecraft.level.getBlockState(link.pos());
            ItemStack icon = new ItemStack(state.getBlock().asItem());
            graphics.item(icon, lx + 1, y + 1);
            Component name = state.isAir() ? Component.translatable("nautec.distributor.missing") : state.getBlock().getName();
            String text = this.font.plainSubstrByWidth(name.getString(), LIST_WIDTH - 32);
            graphics.text(this.font, text, lx + 19, y + 5, state.isAir() ? WARNING : READOUT, false);
            boolean overUnlink = mouseX >= lx + LIST_WIDTH - 11 && mouseX < lx + LIST_WIDTH - 2 && mouseY >= y + 4 && mouseY < y + 14;
            graphics.text(this.font, "x", lx + LIST_WIDTH - 9, y + 5, overUnlink ? WARNING : READOUT_DIM, false);
            if (overUnlink) {
                tooltip.add(Component.translatable("nautec.distributor.unlink"));
            } else if (hover) {
                BlockPos pos = link.pos();
                tooltip.add(name);
                tooltip.add(Component.translatable("nautec.distributor.link.where", pos.getX(), pos.getY(), pos.getZ(),
                        Component.translatable("nautec.distributor.face." + link.face().getSerializedName())).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("nautec.distributor.link.distance",
                        String.format(Locale.ROOT, "%.1f", Math.sqrt(pos.distSqr(this.menu.blockEntity.getBlockPos())))).withStyle(ChatFormatting.GRAY));
            }
        }
        if (links.isEmpty()) {
            graphics.text(this.font, Component.translatable("nautec.distributor.empty"), lx + 4, ly + 4, READOUT_DIM, false);
            graphics.text(this.font, Component.translatable("nautec.distributor.empty.hint1"), lx + 4, ly + 16, READOUT_DIM, false);
            graphics.text(this.font, Component.translatable("nautec.distributor.empty.hint2"), lx + 4, ly + 28, READOUT_DIM, false);
        }

        DistributorLink link = current();
        if (link != null) {
            for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
                Rect2i area = itemSlotArea(i);
                ItemStack request = link.item(i);
                if (!request.isEmpty()) {
                    graphics.item(request, area.getX(), area.getY());
                    graphics.nextStratum();
                    String amount = compact(link.itemAmount(i));
                    graphics.text(this.font, amount, area.getX() + 17 - this.font.width(amount), area.getY() + 9, 0xFFFFFFFF, true);
                }
                if (inside(mouseX, mouseY, area)) {
                    tooltip.addAll(requestTooltip(request.isEmpty() ? null : request.getHoverName(), link.itemAmount(i), false));
                }
            }
            for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
                Rect2i area = fluidSlotArea(i);
                FluidStack request = link.fluid(i);
                if (!request.isEmpty()) {
                    fluidRenderer.render(graphics, area.getX(), area.getY(), request.copyWithAmount(1));
                    graphics.nextStratum();
                    String amount = compact(link.fluidAmount(i));
                    graphics.text(this.font, amount, area.getX() + 17 - this.font.width(amount), area.getY() + 9, 0xFFFFFFFF, true);
                }
                if (inside(mouseX, mouseY, area)) {
                    tooltip.addAll(requestTooltip(request.isEmpty() ? null : request.getHoverName(), link.fluidAmount(i), true));
                }
            }
        }
        if (!tooltip.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    private List<Component> requestTooltip(Component name, int amount, boolean fluid) {
        List<Component> lines = new ArrayList<>();
        if (name == null) {
            lines.add(Component.translatable(fluid ? "nautec.distributor.request.fluid.empty" : "nautec.distributor.request.item.empty"));
            lines.add(Component.translatable(fluid ? "nautec.distributor.request.fluid.how" : "nautec.distributor.request.item.how")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(name);
            lines.add(Component.translatable(fluid ? "nautec.distributor.request.fluid.keep" : "nautec.distributor.request.item.keep", amount)
                    .withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable(fluid ? "nautec.distributor.request.fluid.scroll" : "nautec.distributor.request.item.scroll")
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.distributor.request.clear").withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    private static String compact(int value) {
        if (value >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0).replace(".0M", "M");
        }
        if (value >= 10_000) {
            return (value / 1000) + "k";
        }
        if (value >= 1_000) {
            return String.format(Locale.ROOT, "%.1fk", value / 1_000.0).replace(".0k", "k");
        }
        return Integer.toString(value);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, LABEL, false);
        graphics.text(this.font, Component.translatable("nautec.distributor.fluids"), GRID_X, FLUID_Y - 10, LABEL, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, LABEL, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        List<DistributorLink> links = links();
        int lx = leftPos + LIST_X;
        int ly = topPos + LIST_Y;
        if (mouseX >= lx && mouseX < lx + LIST_WIDTH && mouseY >= ly && mouseY < ly + ROWS * ROW_HEIGHT) {
            int index = (int) ((mouseY - ly) / ROW_HEIGHT) + scroll;
            if (index < links.size()) {
                int rowY = ly + (index - scroll) * ROW_HEIGHT;
                if (mouseX >= lx + LIST_WIDTH - 11 && mouseX < lx + LIST_WIDTH - 2 && mouseY >= rowY + 4 && mouseY < rowY + 14) {
                    ClientPacketDistributor.sendToServer(DistributorEditPayload.unlink(this.menu.containerId, index));
                } else {
                    selected = index;
                }
            }
            return true;
        }
        if (current() != null) {
            ItemStack carried = this.menu.getCarried();
            boolean right = event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
                if (inside(mouseX, mouseY, itemSlotArea(i))) {
                    if (right) {
                        setItemRequest(i, ItemStack.EMPTY);
                    } else if (!carried.isEmpty()) {
                        setItemRequest(i, carried);
                    }
                    return true;
                }
            }
            for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
                if (inside(mouseX, mouseY, fluidSlotArea(i))) {
                    if (right) {
                        setFluidRequest(i, FluidStack.EMPTY);
                    } else {
                        FluidStack contained = contained(carried);
                        if (!contained.isEmpty()) {
                            setFluidRequest(i, contained);
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    public static FluidStack contained(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        for (int i = 0; i < handler.size(); i++) {
            FluidResource resource = handler.getResource(i);
            if (!resource.isEmpty()) {
                return resource.toStack(1000);
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int lx = leftPos + LIST_X;
        int ly = topPos + LIST_Y;
        if (mouseX >= lx && mouseX < lx + LIST_WIDTH && mouseY >= ly && mouseY < ly + ROWS * ROW_HEIGHT) {
            scroll -= (int) Math.signum(scrollY);
            return true;
        }
        DistributorLink link = current();
        if (link != null && scrollY != 0) {
            boolean shift = this.minecraft.hasShiftDown();
            int direction = scrollY > 0 ? 1 : -1;
            for (int i = 0; i < DistributorLink.ITEM_REQUESTS; i++) {
                if (inside(mouseX, mouseY, itemSlotArea(i)) && !link.item(i).isEmpty()) {
                    int step = shift ? 16 : 1;
                    ClientPacketDistributor.sendToServer(DistributorEditPayload.amount(this.menu.containerId, false, selected, i,
                            link.itemAmount(i) + direction * step));
                    return true;
                }
            }
            for (int i = 0; i < DistributorLink.FLUID_REQUESTS; i++) {
                if (inside(mouseX, mouseY, fluidSlotArea(i)) && !link.fluid(i).isEmpty()) {
                    int step = shift ? 10_000 : 1_000;
                    ClientPacketDistributor.sendToServer(DistributorEditPayload.amount(this.menu.containerId, true, selected, i,
                            Math.max(1_000, link.fluidAmount(i) + direction * step)));
                    return true;
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
