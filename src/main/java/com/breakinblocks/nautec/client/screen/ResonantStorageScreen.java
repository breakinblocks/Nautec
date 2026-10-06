package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.menus.ResonantStorageMenu;
import com.breakinblocks.nautec.content.resonantstorage.ChannelAccess;
import com.breakinblocks.nautec.content.resonantstorage.FaceMode;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorageBlockEntity;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStorageItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class ResonantStorageScreen<M extends ResonantStorageMenu<?>> extends AbstractContainerScreen<M> {
    protected static final int WIDTH = 176;
    protected static final int HEIGHT = 214;
    protected static final int TAB_Y = 17;
    protected static final int TAB_WIDTH = 52;
    protected static final int TAB_HEIGHT = 13;
    protected static final int CONTENT_Y = 34;
    private static final int SWATCH = 10;
    private static final int SWATCH_PITCH = 13;
    private static final int SWATCH_X = 22;
    private static final int SWATCH_Y = 64;
    private static final int SWATCH_ROW_PITCH = 13;
    private static final int ACCESS_WIDTH = 52;
    private static final int FACE_WIDTH = 78;
    private static final int FACE_HEIGHT = 14;
    private static final int FACE_PITCH = 17;
    private static final int SELECTED = PanelStyle.READOUT;
    private static final int UNSELECTED = 0x90000000;
    private static final Direction[] FACE_ORDER = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    protected Tab tab = Tab.STORAGE;
    private final List<AbstractWidget> channelWidgets = new ArrayList<>();
    private final List<AbstractWidget> sideWidgets = new ArrayList<>();
    protected final List<AbstractWidget> storageWidgets = new ArrayList<>();

    protected ResonantStorageScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.titleLabelY = 6;
        this.inventoryLabelY = ResonantStorageMenu.INVENTORY_Y - 11;
    }

    protected ResonantStorageBlockEntity storage() {
        return this.menu.blockEntity;
    }

    protected void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.menu.clickMenuButton(this.minecraft.player, id);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void init() {
        super.init();
        channelWidgets.clear();
        sideWidgets.clear();
        storageWidgets.clear();
        for (Tab each : Tab.values()) {
            addRenderableWidget(new NTPanelButton(this.font, this.leftPos + 8 + each.ordinal() * (TAB_WIDTH + 2), this.topPos + TAB_Y, TAB_WIDTH, TAB_HEIGHT,
                    () -> Component.translatable(each.key()),
                    () -> tab == each ? PanelStyle.RECEIVE_COLOR : PanelStyle.NEUTRAL,
                    () -> tab == each ? PanelStyle.RECEIVE_HOVER : PanelStyle.NEUTRAL_HOVER,
                    () -> Component.translatable(each.key() + ".desc"),
                    () -> setTab(each)));
        }
        for (ChannelAccess access : ChannelAccess.ALL) {
            channelWidgets.add(addRenderableWidget(new NTPanelButton(this.font, this.leftPos + 8 + access.ordinal() * (ACCESS_WIDTH + 2), this.topPos + CONTENT_Y,
                    ACCESS_WIDTH, TAB_HEIGHT,
                    () -> Component.translatable(access.translationKey()),
                    () -> storage().channel().access() == access ? PanelStyle.SEND_COLOR : PanelStyle.NEUTRAL,
                    () -> storage().channel().access() == access ? PanelStyle.SEND_HOVER : PanelStyle.NEUTRAL_HOVER,
                    () -> Component.translatable(access.translationKey() + ".desc"),
                    () -> press(ResonantStorageMenu.ACCESS_BUTTON + access.ordinal()))));
        }
        for (int i = 0; i < FACE_ORDER.length; i++) {
            Direction direction = FACE_ORDER[i];
            int x = this.leftPos + 8 + (i % 2) * (FACE_WIDTH + 4);
            int y = this.topPos + CONTENT_Y + (i / 2) * FACE_PITCH;
            sideWidgets.add(addRenderableWidget(new NTPanelButton(this.font, x, y, FACE_WIDTH, FACE_HEIGHT,
                    () -> Component.translatable("nautec.resonant_storage.side", Component.translatable("nautec.resonant_storage.side." + direction.getSerializedName()),
                            Component.translatable(storage().face(direction).translationKey())),
                    () -> storage().face(direction).color(),
                    () -> storage().face(direction).color() | 0x00202020,
                    () -> Component.translatable(storage().face(direction).translationKey() + ".desc"),
                    () -> cycleFace(direction, false))));
        }
        initStorage();
        setTab(tab);
    }

    protected void initStorage() {
    }

    private void cycleFace(Direction direction, boolean backwards) {
        FaceMode current = storage().face(direction);
        FaceMode next = backwards ? current.previous() : current.next();
        press(ResonantStorageMenu.FACE_BUTTON + direction.ordinal() * FaceMode.ALL.length + next.ordinal());
    }

    protected void setTab(Tab tab) {
        this.tab = tab;
        this.menu.setShowsStorage(tab == Tab.STORAGE);
        for (AbstractWidget widget : channelWidgets) {
            widget.visible = tab == Tab.CHANNEL;
        }
        for (AbstractWidget widget : sideWidgets) {
            widget.visible = tab == Tab.SIDES;
        }
        for (AbstractWidget widget : storageWidgets) {
            widget.visible = tab == Tab.STORAGE;
        }
    }

    private int swatchX(int colour) {
        return this.leftPos + SWATCH_X + colour * SWATCH_PITCH;
    }

    private int swatchY(int slot) {
        return this.topPos + SWATCH_Y + slot * SWATCH_ROW_PITCH;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        PanelStyle.panel(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                PanelStyle.slot(graphics, this.leftPos + slot.x, this.topPos + slot.y);
            }
        }
        switch (tab) {
            case STORAGE -> extractStorage(graphics, mouseX, mouseY);
            case CHANNEL -> extractChannel(graphics);
            case SIDES -> PanelStyle.screen(graphics, this.leftPos + 8, this.topPos + CONTENT_Y + 3 * FACE_PITCH + 2, WIDTH - 16, 31);
        }
    }

    protected abstract void extractStorage(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

    private void extractChannel(GuiGraphicsExtractor graphics) {
        GatewayAddress address = storage().channel().address();
        List<DyeColor> palette = GatewayAddress.PALETTE;
        PanelStyle.screen(graphics, this.leftPos + 8, this.topPos + SWATCH_Y - 4, WIDTH - 16, GatewayAddress.SLOTS * SWATCH_ROW_PITCH + 5);
        for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
            int y = swatchY(slot);
            graphics.text(this.font, String.valueOf(slot + 1), this.leftPos + 12, y + 1, PanelStyle.READOUT_DIM, false);
            for (int colour = 0; colour < palette.size(); colour++) {
                DyeColor dye = palette.get(colour);
                int x = swatchX(colour);
                boolean chosen = address.slots().get(slot) == dye;
                if (chosen) {
                    graphics.fill(x - 1, y - 1, x + SWATCH + 1, y + SWATCH + 1, SELECTED);
                }
                graphics.fill(x, y, x + SWATCH, y + SWATCH, 0xFF000000 | dye.getTextColor());
                if (!chosen) {
                    graphics.fill(x, y, x + SWATCH, y + SWATCH, UNSELECTED);
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, PanelStyle.LABEL, false);
        if (tab == Tab.CHANNEL) {
            Component owner = ResonantStorageItem.describe(storage().channel().access(), storage().ownerName());
            graphics.text(this.font, owner, 8, CONTENT_Y + 17, PanelStyle.LABEL, false);
        } else if (tab == Tab.SIDES) {
            int y = CONTENT_Y + 3 * FACE_PITCH + 5;
            for (FormattedCharSequence line : this.font.split(Component.translatable("nautec.resonant_storage.sides_hint"), WIDTH - 24)) {
                graphics.text(this.font, line, 12, y, PanelStyle.READOUT, false);
                y += 9;
            }
        }
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, PanelStyle.LABEL, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (tab == Tab.CHANNEL) {
            List<DyeColor> palette = GatewayAddress.PALETTE;
            for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
                int y = swatchY(slot);
                for (int colour = 0; colour < palette.size(); colour++) {
                    int x = swatchX(colour);
                    if (PanelStyle.inside(mouseX, mouseY, x, y, SWATCH, SWATCH)) {
                        graphics.setTooltipForNextFrame(this.font, Component.translatable("color.minecraft." + palette.get(colour).getSerializedName()), mouseX, mouseY);
                    }
                }
            }
        }
        extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (tab == Tab.CHANNEL) {
            List<DyeColor> palette = GatewayAddress.PALETTE;
            for (int slot = 0; slot < GatewayAddress.SLOTS; slot++) {
                int y = swatchY(slot);
                for (int colour = 0; colour < palette.size(); colour++) {
                    int x = swatchX(colour);
                    if (PanelStyle.inside(event.x(), event.y(), x, y, SWATCH, SWATCH)) {
                        GatewayAddress wanted = storage().channel().address().withSlot(slot, palette.get(colour));
                        press(ResonantStorageMenu.ADDRESS_BUTTON + wanted.pack());
                        return true;
                    }
                }
            }
        }
        if (tab == Tab.SIDES && event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            for (int i = 0; i < FACE_ORDER.length; i++) {
                AbstractWidget widget = sideWidgets.get(i);
                if (widget.isMouseOver(event.x(), event.y())) {
                    cycleFace(FACE_ORDER[i], true);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    protected enum Tab {
        STORAGE,
        CHANNEL,
        SIDES;

        String key() {
            return "nautec.resonant_storage.tab." + name().toLowerCase(Locale.ROOT);
        }
    }
}
