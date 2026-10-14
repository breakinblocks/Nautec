package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import com.breakinblocks.nautec.content.menus.ExtradimensionalStorageMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ExtradimensionalStorageScreen extends ResonanceNetworkScreen<ExtradimensionalStorageMenu> {
    private static final int ONLINE = 0xFF5FE8B0;
    private static final int OFFLINE = 0xFFE36A5C;
    private static final int BAR_BACK = 0xFF1A1030;
    private static final int BAR_FILL = 0xFF8A5CF0;
    private static final int BAR_SHINE = 0xFFB89BFF;
    private static final int PAD_FILL = 0xFF2A1F4A;
    private static final int HOLE = 0xFF000000;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final int[] PAD_SIZE = {8, 12, 16, 20, 22};
    private static final int TOP = 20;
    private static final int BLOCK_HEIGHT = 70;
    private static final int PORTS_X = 8;
    private static final int PORTS_WIDTH = 74;
    private static final int READOUT_LEFT = 88;
    private static final int PORT_WIDTH = 22;
    private static final int PORT_HEIGHT = 12;

    public ExtradimensionalStorageScreen(ExtradimensionalStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int chunkState() {
        return this.menu.getChunkState();
    }

    @Override
    protected int networkY() {
        return TOP + BLOCK_HEIGHT + 6;
    }

    @Override
    protected int readoutX() {
        return READOUT_LEFT;
    }

    @Override
    protected int readoutY() {
        return TOP;
    }

    @Override
    protected int readoutWidth() {
        return IMAGE_WIDTH - READOUT_LEFT - 8;
    }

    @Override
    protected int readoutHeight() {
        return BLOCK_HEIGHT;
    }

    @Override
    protected int manageY() {
        return networkY() + 50;
    }

    private static String side(Direction side) {
        return side.getSerializedName();
    }

    private Component modeName(ExtradimensionalStorageBlockEntity.PortMode mode) {
        return Component.translatable("nautec.extradimensional_storage.port." + mode.name().toLowerCase(Locale.ROOT));
    }

    private int color(ExtradimensionalStorageBlockEntity.PortMode mode, boolean hover) {
        return switch (mode) {
            case INPUT -> hover ? SEND_HOVER : SEND_COLOR;
            case OUTPUT -> hover ? RECEIVE_HOVER : RECEIVE_COLOR;
            case OFF -> hover ? NEUTRAL_HOVER : NEUTRAL;
        };
    }

    private static int portX(Direction side) {
        int center = PORTS_X + PORTS_WIDTH / 2;
        return switch (side) {
            case WEST -> PORTS_X + 2;
            case EAST -> PORTS_X + PORTS_WIDTH - 2 - PORT_WIDTH;
            default -> center - PORT_WIDTH / 2;
        };
    }

    private static int portY(Direction side) {
        int center = TOP + BLOCK_HEIGHT / 2;
        return switch (side) {
            case NORTH -> TOP + 3;
            case SOUTH -> TOP + BLOCK_HEIGHT - 3 - PORT_HEIGHT;
            default -> center - PORT_HEIGHT / 2;
        };
    }

    @Override
    protected void addHeaderWidgets(int x, int y) {
        for (Direction side : SIDES) {
            addRenderableWidget(new PanelButton(x + portX(side), y + portY(side), PORT_WIDTH, PORT_HEIGHT,
                    () -> Component.translatable("nautec.extradimensional_storage.port.short." + this.menu.getMode(side).name().toLowerCase(Locale.ROOT)),
                    () -> color(this.menu.getMode(side), false),
                    () -> color(this.menu.getMode(side), true),
                    () -> Component.translatable("nautec.extradimensional_storage.port.tooltip",
                                    Component.translatable("nautec.beam_faces.dir." + side(side)), modeName(this.menu.getMode(side)))
                            .append("\n").append(Component.translatable(this.menu.getTier() > 1 ? "nautec.extradimensional_storage.port.where"
                                    : "nautec.extradimensional_storage.port.where.core").withStyle(ChatFormatting.GRAY))
                            .append("\n").append(Component.translatable("nautec.extradimensional_storage.port.click").withStyle(ChatFormatting.DARK_GRAY)),
                    () -> send(ResonanceActionPayload.PORT, null, Integer.toString(side.get2DDataValue()))));
        }
    }

    @Override
    protected void extractPanelBackground(GuiGraphicsExtractor graphics, int x, int y) {
        int bx = x + PORTS_X;
        int by = y + TOP;
        graphics.fill(bx - 1, by - 1, bx + PORTS_WIDTH + 1, by + BLOCK_HEIGHT + 1, OUTLINE);
        graphics.fill(bx, by, bx + PORTS_WIDTH, by + BLOCK_HEIGHT, SCREEN_EDGE);
        graphics.fill(bx + 1, by + 1, bx + PORTS_WIDTH - 1, by + BLOCK_HEIGHT - 1, SCREEN_FILL);

        int size = PAD_SIZE[Math.clamp(this.menu.getTier(), 1, PAD_SIZE.length) - 1];
        int cx = bx + PORTS_WIDTH / 2;
        int cy = by + BLOCK_HEIGHT / 2;
        int half = size / 2;
        graphics.fill(cx - half - 1, cy - half - 1, cx + half + 1, cy + half + 1, BAR_FILL);
        graphics.fill(cx - half, cy - half, cx + half, cy + half, PAD_FILL);
        graphics.fill(cx - 3, cy - 3, cx + 3, cy + 3, BAR_SHINE);
        graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, HOLE);
    }

    @Override
    protected void extractReadoutBackground(GuiGraphicsExtractor graphics, int rx, int ry, int rw) {
        int color = this.menu.getStatus() == ExtradimensionalStorageBlockEntity.STATUS_ONLINE ? ONLINE : OFFLINE;
        graphics.fill(rx + 5, ry + 5, rx + 9, ry + 9, color);
        int barLeft = rx + 4;
        int barRight = rx + rw - 4;
        int barTop = ry + 37;
        graphics.fill(barLeft - 1, barTop - 1, barRight + 1, barTop + 7, OUTLINE);
        graphics.fill(barLeft, barTop, barRight, barTop + 6, BAR_BACK);
        int filled = (int) Math.round((barRight - barLeft) * fraction());
        if (filled > 0) {
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 6, BAR_FILL);
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 1, BAR_SHINE);
        }
    }

    private double fraction() {
        long capacity = Math.max(1, this.menu.getCapacity());
        return Math.min(1.0, this.menu.getStored() / (double) capacity);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, "N", PORTS_X + 4, TOP + 4, READOUT_DIM, false);
    }

    @Override
    protected void extractReadout(GuiGraphicsExtractor graphics, int tx, int ty, ResonanceSyncPayload.@Nullable NetworkView view) {
        int rx = readoutX();
        int ry = readoutY();
        int right = rx + readoutWidth() - 4;
        boolean online = this.menu.getStatus() == ExtradimensionalStorageBlockEntity.STATUS_ONLINE;
        graphics.text(this.font, Component.translatable(ResonanceNodeBlockEntity.statusKey(this.menu.getStatus())), rx + 13, ry + 4,
                online ? READOUT : READOUT_DIM, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.tier", this.menu.getTier()), rx + 4, ry + 15, READOUT, false);
        String percent = String.format(Locale.ROOT, "%.1f%%", fraction() * 100);
        graphics.text(this.font, percent, right - this.font.width(percent), ry + 15, READOUT_DIM, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.amount", compact(this.menu.getStored()),
                compact(this.menu.getCapacity())), rx + 4, ry + 26, READOUT, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.flow.in", compact(this.menu.getFlowIn())),
                rx + 4, ry + 48, READOUT_DIM, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.flow.out", compact(this.menu.getFlowOut())),
                rx + 4, ry + 59, READOUT_DIM, false);
    }

    @Override
    protected boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int rx, int ry) {
        if (!inside(mouseX, mouseY, rx, ry, readoutWidth(), readoutHeight())) {
            return false;
        }
        int tier = this.menu.getTier();
        String statusKey = ResonanceNodeBlockEntity.statusKey(this.menu.getStatus());
        lines.add(Component.translatable("nautec.extradimensional_storage.stored.exact", number(this.menu.getStored()), number(this.menu.getCapacity())));
        lines.add(Component.translatable("nautec.extradimensional_storage.rate", number(ExtradimensionalStorageBlockEntity.rate(tier))).withStyle(ChatFormatting.GRAY));
        if (tier < ExtradimensionalStorageBlockEntity.MAX_TIER) {
            lines.add(Component.translatable("nautec.extradimensional_storage.next", tier + 1, tier * 2 + 1, tier * 2 + 1,
                    compact(ExtradimensionalStorageBlockEntity.capacity(tier + 1))).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable(statusKey));
        lines.add(Component.translatable("nautec.extradimensional_storage.network.desc").withStyle(ChatFormatting.GRAY));
        return true;
    }
}
