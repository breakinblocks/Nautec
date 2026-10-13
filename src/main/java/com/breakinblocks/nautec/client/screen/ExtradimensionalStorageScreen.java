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
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static final int PORT_WIDTH = 31;

    public ExtradimensionalStorageScreen(ExtradimensionalStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int chunkState() {
        return this.menu.getChunkState();
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

    @Override
    protected void addHeaderWidgets(int x, int y) {
        int left = x + IMAGE_WIDTH - 8 - PORT_WIDTH * SIDES.length - (SIDES.length - 1);
        for (int i = 0; i < SIDES.length; i++) {
            Direction side = SIDES[i];
            addRenderableWidget(new PanelButton(left + i * (PORT_WIDTH + 1), y + 4, PORT_WIDTH, 14,
                    () -> Component.translatable("nautec.extradimensional_storage.port.short." + side(side),
                            Component.translatable("nautec.extradimensional_storage.port.short." + this.menu.getMode(side).name().toLowerCase(Locale.ROOT))),
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
    protected void extractReadoutBackground(GuiGraphicsExtractor graphics, int rx, int ry, int rw) {
        int color = this.menu.getStatus() == ExtradimensionalStorageBlockEntity.STATUS_ONLINE ? ONLINE : OFFLINE;
        graphics.fill(rx + 5, ry + 5, rx + 9, ry + 9, color);
        int barLeft = rx + 4;
        int barRight = rx + rw - 4;
        int barTop = ry + READOUT_HEIGHT - 7;
        graphics.fill(barLeft, barTop, barRight, barTop + 3, BAR_BACK);
        long capacity = Math.max(1, this.menu.getCapacity());
        int filled = (int) Math.round((barRight - barLeft) * Math.min(1.0, this.menu.getStored() / (double) capacity));
        if (filled > 0) {
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 3, BAR_FILL);
            graphics.fill(barLeft, barTop, barLeft + filled, barTop + 1, BAR_SHINE);
        }
    }

    @Override
    protected void extractReadout(GuiGraphicsExtractor graphics, int tx, int ty, ResonanceSyncPayload.@Nullable NetworkView view) {
        boolean online = this.menu.getStatus() == ExtradimensionalStorageBlockEntity.STATUS_ONLINE;
        graphics.text(this.font, Component.translatable(ResonanceNodeBlockEntity.statusKey(this.menu.getStatus())), tx + 9, ty - 10,
                online ? READOUT : READOUT_DIM, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.stored", this.menu.getTier(),
                compact(this.menu.getStored()), compact(this.menu.getCapacity())), tx, ty + 2, READOUT, false);
        graphics.text(this.font, Component.translatable("nautec.extradimensional_storage.flow", compact(this.menu.getFlowIn()),
                compact(this.menu.getFlowOut())), tx, ty + 12, READOUT_DIM, false);
    }

    @Override
    protected boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int rx, int ry) {
        if (!inside(mouseX, mouseY, rx, ry, IMAGE_WIDTH - 16, READOUT_HEIGHT)) {
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
