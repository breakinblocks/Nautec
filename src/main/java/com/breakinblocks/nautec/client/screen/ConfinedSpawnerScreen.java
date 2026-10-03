package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.menus.ConfinedSpawnerMenu;
import com.breakinblocks.nautec.content.spawner.SpawnerFilter;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.network.SetSpawnerFilterPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfinedSpawnerScreen extends AbstractContainerScreen<ConfinedSpawnerMenu> {
    private @Nullable SideConfigPanel sidePanel;

    private static final int PANEL = 0xFFC8C7B3;
    private static final int PANEL_LIGHT = 0xFFE7E7D6;
    private static final int OUTLINE = 0xFF070707;
    private static final int SLOT_EDGE = 0xFF1E2221;
    private static final int SLOT_FILL = 0xFF45504A;
    private static final int GHOST_FILL = 0xFF2F3A35;
    private static final int LABEL = 0xFF404040;
    private static final int POWER_FILL = 0xFF52E8FF;
    private static final int POWER_SHINE = 0xFFB3FCFF;
    private static final int CYCLE_FILL = 0xFF7FD9A0;
    private static final int WHITELIST_COLOR = 0xFF2E7D4F;
    private static final int WHITELIST_HOVER = 0xFF3C9A63;
    private static final int BLACKLIST_COLOR = 0xFF8C2F2F;
    private static final int BLACKLIST_HOVER = 0xFFA83C3C;
    private static final int RUNNING_TEXT = 0xFF2E7D4F;
    private static final int STOPPED_TEXT = 0xFF8C2F2F;
    private static final int TAG_MARK = 0xFFFFD86B;

    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 222;
    private static final int TOGGLE_SIZE = 12;
    private static final int PANEL_GAP = 2;
    private static final int PANEL_WIDTH = 70;
    private static final int PANEL_HEIGHT = 152;
    private static final int FILTER_COLUMNS = 3;
    private static final int MODE_BUTTON_Y = 18;
    private static final int MODE_BUTTON_HEIGHT = 14;
    private static final int FILTER_GRID_Y = 38;

    private static final int TAB_OUT = 22;
    private static final int TAB_OVERLAP = 4;
    private static final int TAB_Y = 18;
    private static final int TAB_HEIGHT = 92;
    private static final int POWER_BAR_X = 5;
    private static final int POWER_BAR_WIDTH = 7;
    private static final int CYCLE_BAR_X = 14;
    private static final int CYCLE_BAR_WIDTH = 4;
    private static final int BAR_TOP = 8;
    private static final int BAR_HEIGHT = 76;

    private static boolean panelOpen;

    private @Nullable ModeButton modeButton;

    public ConfinedSpawnerScreen(ConfinedSpawnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (sidePanel == null) {
            sidePanel = SideConfigPanel.create(this.menu.getBlockEntity(), this.menu.containerId, true);
        }
        addRenderableWidget(new FilterToggleButton(toggleX(), toggleY()));
        this.modeButton = addRenderableWidget(new ModeButton(modeButtonX(), modeButtonY()));
        this.modeButton.visible = panelOpen;
    }

    public boolean isPanelOpen() {
        return panelOpen;
    }

    public Rect2i panelArea() {
        return new Rect2i(panelX(), this.topPos, PANEL_WIDTH, PANEL_HEIGHT);
    }

    public Rect2i tabArea() {
        return new Rect2i(tabX(), this.topPos + TAB_Y, TAB_OUT + TAB_OVERLAP, TAB_HEIGHT);
    }

    public List<Rect2i> extraAreas() {
        List<Rect2i> areas = new ArrayList<>(panelOpen ? List.of(tabArea(), panelArea()) : List.of(tabArea()));
        if (sidePanel != null) {
            areas.add(sidePanel.area(sideAnchorX(), sideAnchorY()));
        }
        return areas;
    }

    private int sideAnchorX() {
        return this.leftPos - 2;
    }

    private int sideAnchorY() {
        return this.topPos + TAB_Y + TAB_HEIGHT + 4;
    }

    public List<Rect2i> filterSlotAreas() {
        List<Rect2i> areas = new ArrayList<>(SpawnerFilter.SIZE);
        for (int slot = 0; slot < SpawnerFilter.SIZE; slot++) {
            areas.add(new Rect2i(filterSlotX(slot), filterSlotY(slot), 16, 16));
        }
        return areas;
    }

    public void setFilterSlot(int slot, @Nullable SpawnerFilterEntry entry) {
        if (slot < 0 || slot >= SpawnerFilter.SIZE) {
            return;
        }
        this.menu.getBlockEntity().getFilter().set(slot, entry);
        ClientPacketDistributor.sendToServer(new SetSpawnerFilterPayload(this.menu.containerId, slot, Optional.ofNullable(entry)));
    }

    private void togglePanel() {
        panelOpen = !panelOpen;
        if (this.modeButton != null) {
            this.modeButton.visible = panelOpen;
        }
    }

    private int panelX() {
        return this.leftPos + this.imageWidth + PANEL_GAP;
    }

    private int tabX() {
        return this.leftPos - TAB_OUT;
    }

    private int toggleX() {
        return this.leftPos + this.imageWidth - 8 - TOGGLE_SIZE;
    }

    private int toggleY() {
        return this.topPos + 4;
    }

    private int modeButtonX() {
        return panelX() + 6;
    }

    private int modeButtonY() {
        return this.topPos + MODE_BUTTON_Y;
    }

    private int filterSlotX(int slot) {
        int gridWidth = FILTER_COLUMNS * 18;
        return panelX() + (PANEL_WIDTH - gridWidth) / 2 + 1 + (slot % FILTER_COLUMNS) * 18;
    }

    private int filterSlotY(int slot) {
        return this.topPos + FILTER_GRID_Y + 1 + (slot / FILTER_COLUMNS) * 18;
    }

    private int statusRight() {
        return this.leftPos + this.imageWidth - 8 - TOGGLE_SIZE - 4;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        tab(graphics);
        frame(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        graphics.fill(this.leftPos, this.topPos + TAB_Y + 3, this.leftPos + 3, this.topPos + TAB_Y + TAB_HEIGHT - 1, PANEL);
        graphics.fill(this.leftPos, this.topPos + TAB_Y + 1, this.leftPos + 1, this.topPos + TAB_Y + 3, PANEL_LIGHT);
        for (Slot slot : this.menu.slots) {
            slotFrame(graphics, this.leftPos + slot.x, this.topPos + slot.y, SLOT_FILL);
        }

        if (!panelOpen) {
            return;
        }
        frame(graphics, panelX(), this.topPos, PANEL_WIDTH, PANEL_HEIGHT);
        for (int slot = 0; slot < SpawnerFilter.SIZE; slot++) {
            slotFrame(graphics, filterSlotX(slot), filterSlotY(slot), GHOST_FILL);
        }
    }

    private void tab(GuiGraphicsExtractor graphics) {
        int x = tabX();
        int y = this.topPos + TAB_Y;
        int width = TAB_OUT + TAB_OVERLAP;
        roundedLeft(graphics, x, y, width, TAB_HEIGHT, OUTLINE);
        roundedLeft(graphics, x + 1, y + 1, width - 1, TAB_HEIGHT - 2, PANEL_LIGHT);
        roundedLeft(graphics, x + 3, y + 3, width - 3, TAB_HEIGHT - 4, PANEL);

        int barTop = y + BAR_TOP;
        int barBottom = barTop + BAR_HEIGHT;
        int powerX = x + POWER_BAR_X;
        graphics.fill(powerX - 1, barTop - 1, powerX + POWER_BAR_WIDTH + 1, barBottom + 1, OUTLINE);
        graphics.fill(powerX, barTop, powerX + POWER_BAR_WIDTH, barBottom, SLOT_EDGE);
        int power = Math.round(BAR_HEIGHT * Math.min(1.0F, this.menu.getBufferedPower() / (float) Math.max(1, NTConfig.confinedSpawnerPowerBuffer)));
        if (power > 0) {
            graphics.fill(powerX, barBottom - power, powerX + POWER_BAR_WIDTH, barBottom, POWER_FILL);
            graphics.fill(powerX + 1, barBottom - power, powerX + 2, barBottom, POWER_SHINE);
        }

        int cycleX = x + CYCLE_BAR_X;
        graphics.fill(cycleX - 1, barTop - 1, cycleX + CYCLE_BAR_WIDTH + 1, barBottom + 1, OUTLINE);
        graphics.fill(cycleX, barTop, cycleX + CYCLE_BAR_WIDTH, barBottom, SLOT_EDGE);
        int cycle = Math.round(BAR_HEIGHT * this.menu.getCycleProgress());
        if (cycle > 0) {
            graphics.fill(cycleX, barBottom - cycle, cycleX + CYCLE_BAR_WIDTH, barBottom, CYCLE_FILL);
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        if (sidePanel != null) {
            sidePanel.extract(graphics, this.font, sideAnchorX(), sideAnchorY(), mouseX, mouseY);
        }
        if (!panelOpen) {
            return;
        }
        Component header = Component.translatable("nautec.confined_spawner.filter");
        graphics.text(this.font, header, panelX() + (PANEL_WIDTH - this.font.width(header)) / 2, this.topPos + 6, LABEL, false);
        SpawnerFilter filter = this.menu.getBlockEntity().getFilter();
        for (int slot = 0; slot < SpawnerFilter.SIZE; slot++) {
            SpawnerFilterEntry entry = filter.get(slot);
            int x = filterSlotX(slot);
            int y = filterSlotY(slot);
            if (entry != null) {
                graphics.item(entry.displayStack(), x, y);
                if (entry.tag().isPresent()) {
                    graphics.nextStratum();
                    graphics.text(this.font, "#", x + 11, y + 8, TAG_MARK, true);
                }
            }
            if (inside(mouseX, mouseY, x, y, 16, 16)) {
                graphics.fill(x, y, x + 16, y + 16, 0x60FFFFFF);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        ConfinedSpawnerBlockEntity.Status status = this.menu.getStatus();
        Component text = Component.translatable(status.translationKey());
        int color = status == ConfinedSpawnerBlockEntity.Status.RUNNING ? RUNNING_TEXT : STOPPED_TEXT;
        int right = statusRight() - this.leftPos;
        graphics.text(this.font, text, right - this.font.width(text), this.titleLabelY, color, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        List<Component> lines = hoverLines(mouseX, mouseY);
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private List<Component> hoverLines(int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        int barTop = this.topPos + TAB_Y + BAR_TOP;
        if (inside(mouseX, mouseY, tabX() + POWER_BAR_X - 1, barTop - 1, POWER_BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.confined_spawner.power", this.menu.getBufferedPower(), NTConfig.confinedSpawnerPowerBuffer));
            lines.add(Component.translatable("nautec.confined_spawner.power_use", NTConfig.confinedSpawnerPowerPerTick).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.confined_spawner.power_source").withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }
        if (inside(mouseX, mouseY, tabX() + CYCLE_BAR_X - 1, barTop - 1, CYCLE_BAR_WIDTH + 2, BAR_HEIGHT + 2)) {
            lines.add(Component.translatable("nautec.confined_spawner.cycle", Math.round(this.menu.getCycleProgress() * 100)));
            lines.add(Component.translatable("nautec.confined_spawner.cycle.desc").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        ConfinedSpawnerBlockEntity.Status status = this.menu.getStatus();
        Component statusText = Component.translatable(status.translationKey());
        int statusWidth = this.font.width(statusText);
        if (inside(mouseX, mouseY, statusRight() - statusWidth, this.topPos + this.titleLabelY - 1, statusWidth, this.font.lineHeight + 1)) {
            lines.add(statusText);
            lines.add(Component.translatable(status.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        int slot = hoveredFilterSlot(mouseX, mouseY);
        if (slot < 0 || !this.menu.getCarried().isEmpty()) {
            return lines;
        }
        SpawnerFilterEntry entry = this.menu.getBlockEntity().getFilter().get(slot);
        if (entry != null) {
            lines.add(entry.displayStack().getHoverName());
            entry.tag().ifPresentOrElse(
                    tag -> lines.add(Component.translatable("nautec.confined_spawner.filter.tag", tag.location().toString()).withStyle(ChatFormatting.GOLD)),
                    () -> lines.add(Component.translatable("nautec.confined_spawner.filter.exact").withStyle(ChatFormatting.GRAY)));
            lines.add(Component.translatable("nautec.confined_spawner.filter.cycle").withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.translatable("nautec.confined_spawner.filter.clear").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(Component.translatable("nautec.confined_spawner.filter.empty").withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.confined_spawner.filter.right_click").withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (sidePanel != null && sidePanel.mouseClicked(event, sideAnchorX(), sideAnchorY())) {
            return true;
        }
        int slot = hoveredFilterSlot(event.x(), event.y());
        if (slot >= 0) {
            clickFilterSlot(slot, event.button());
            return true;
        }
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        return panelOpen && panelArea().contains((int) event.x(), (int) event.y());
    }

    private void clickFilterSlot(int slot, int button) {
        ItemStack carried = this.menu.getCarried();
        SpawnerFilterEntry current = this.menu.getBlockEntity().getFilter().get(slot);
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (current != null) {
                setFilterSlot(slot, current.nextTag());
            } else if (!carried.isEmpty()) {
                setFilterSlot(slot, SpawnerFilterEntry.of(carried).nextTag());
            }
            return;
        }
        setFilterSlot(slot, carried.isEmpty() ? null : SpawnerFilterEntry.of(carried));
    }

    private int hoveredFilterSlot(double mouseX, double mouseY) {
        if (!panelOpen) {
            return -1;
        }
        for (int slot = 0; slot < SpawnerFilter.SIZE; slot++) {
            if (inside(mouseX, mouseY, filterSlotX(slot) - 1, filterSlotY(slot) - 1, 18, 18)) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        for (Rect2i area : extraAreas()) {
            if (area.contains((int) mx, (int) my)) {
                return false;
            }
        }
        return super.hasClickedOutside(mx, my, xo, yo);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static void frame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, OUTLINE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + width - 1, y + height - 1, PANEL);
    }

    private static void roundedLeft(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 2, y, x + width, y + 1, color);
        graphics.fill(x + 1, y + 1, x + width, y + 2, color);
        graphics.fill(x, y + 2, x + width, y + height - 2, color);
        graphics.fill(x + 1, y + height - 2, x + width, y + height - 1, color);
        graphics.fill(x + 2, y + height - 1, x + width, y + height, color);
    }

    private static void slotFrame(GuiGraphicsExtractor graphics, int x, int y, int fill) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_EDGE);
        graphics.fill(x, y, x + 16, y + 16, fill);
    }

    private final class FilterToggleButton extends AbstractButton {
        private FilterToggleButton(int x, int y) {
            super(x, y, TOGGLE_SIZE, TOGGLE_SIZE, Component.translatable("nautec.confined_spawner.filter"));
            setTooltip(Tooltip.create(Component.translatable("nautec.confined_spawner.filter.toggle")));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            togglePanel();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            int x = getX();
            int y = getY();
            graphics.fill(x, y, x + TOGGLE_SIZE, y + TOGGLE_SIZE, OUTLINE);
            graphics.fill(x + 1, y + 1, x + TOGGLE_SIZE - 1, y + TOGGLE_SIZE - 1, isHoveredOrFocused() || panelOpen ? SLOT_FILL : SLOT_EDGE);
            graphics.fill(x + 3, y + 3, x + 9, y + 4, PANEL_LIGHT);
            graphics.fill(x + 4, y + 5, x + 8, y + 6, PANEL_LIGHT);
            graphics.fill(x + 5, y + 7, x + 7, y + 9, PANEL_LIGHT);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class ModeButton extends AbstractButton {
        private final Tooltip whitelistTooltip = modeTooltip("nautec.confined_spawner.whitelist.desc");
        private final Tooltip blacklistTooltip = modeTooltip("nautec.confined_spawner.blacklist.desc");

        private ModeButton(int x, int y) {
            super(x, y, PANEL_WIDTH - 12, MODE_BUTTON_HEIGHT, Component.empty());
        }

        private static Tooltip modeTooltip(String key) {
            return Tooltip.create(Component.translatable(key)
                    .append("\n")
                    .append(Component.translatable("nautec.confined_spawner.mode.click").withStyle(ChatFormatting.GRAY)));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            ConfinedSpawnerScreen.this.minecraft.gameMode.handleInventoryButtonClick(
                    ConfinedSpawnerScreen.this.menu.containerId, ConfinedSpawnerMenu.BUTTON_TOGGLE_MODE);
        }

        @Override
        public Component getMessage() {
            return Component.translatable(whitelist() ? "nautec.confined_spawner.whitelist" : "nautec.confined_spawner.blacklist");
        }

        private boolean whitelist() {
            return ConfinedSpawnerScreen.this.menu.getBlockEntity().getFilter().isWhitelist();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            boolean whitelist = whitelist();
            setTooltip(whitelist ? whitelistTooltip : blacklistTooltip);
            int x = getX();
            int y = getY();
            graphics.fill(x, y, x + getWidth(), y + getHeight(), OUTLINE);
            int fill = isHoveredOrFocused() ? (whitelist ? WHITELIST_HOVER : BLACKLIST_HOVER) : (whitelist ? WHITELIST_COLOR : BLACKLIST_COLOR);
            graphics.fill(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1, fill);
            Component message = getMessage();
            graphics.text(ConfinedSpawnerScreen.this.font, message, x + (getWidth() - ConfinedSpawnerScreen.this.font.width(message)) / 2,
                    y + 3, 0xFFFFFFFF, true);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
