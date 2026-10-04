package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.ResonanceNodeMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceNodeBlockEntity;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ResonanceNodeScreen extends ResonanceNetworkScreen<ResonanceNodeMenu> {
    private static final int ONLINE = 0xFF5FE8B0;
    private static final int OFFLINE = 0xFFE36A5C;
    private static final int[] LIMITS = {1_000, 5_000, 10_000, 25_000, 50_000, 100_000, 250_000, 500_000, 1_000_000};

    public ResonanceNodeScreen(ResonanceNodeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int chunkState() {
        return this.menu.getChunkState();
    }

    @Override
    protected void addHeaderWidgets(int x, int y) {
        int left = x + IMAGE_WIDTH - 136;
        addRenderableWidget(new PanelButton(left, y + 4, 12, 14, () -> Component.literal("-"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.satellite.priority.lower"),
                () -> sendNumber(ResonanceActionPayload.PRIORITY, this.menu.getPriority() - 1)));
        addRenderableWidget(new PanelButton(left + 12, y + 4, 30, 14, () -> Component.literal("P " + this.menu.getPriority()),
                () -> RECEIVE_COLOR, () -> RECEIVE_HOVER,
                () -> Component.translatable("nautec.satellite.priority", this.menu.getPriority()).append("\n")
                        .append(Component.translatable("nautec.resonance_node.priority.desc").withStyle(ChatFormatting.GRAY)),
                () -> sendNumber(ResonanceActionPayload.PRIORITY, 0)));
        addRenderableWidget(new PanelButton(left + 42, y + 4, 12, 14, () -> Component.literal("+"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.satellite.priority.raise"),
                () -> sendNumber(ResonanceActionPayload.PRIORITY, this.menu.getPriority() + 1)));

        int limitLeft = left + 58;
        addRenderableWidget(new PanelButton(limitLeft, y + 4, 12, 14, () -> Component.literal("-"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.satellite.limit.lower"), () -> sendNumber(ResonanceActionPayload.LIMIT, step(-1))));
        addRenderableWidget(new PanelButton(limitLeft + 12, y + 4, 42, 14, () -> Component.literal(compact(this.menu.getLimit()) + "/t"),
                () -> RECEIVE_COLOR, () -> RECEIVE_HOVER,
                () -> Component.translatable("nautec.satellite.limit", number(this.menu.getLimit()), number(this.menu.getLimit()), number(NTConfig.satelliteTransferLimit))
                        .append("\n").append(Component.translatable("nautec.resonance_node.limit.desc").withStyle(ChatFormatting.GRAY)),
                () -> sendNumber(ResonanceActionPayload.LIMIT, NTConfig.satelliteTransferLimit)));
        addRenderableWidget(new PanelButton(limitLeft + 54, y + 4, 12, 14, () -> Component.literal("+"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.satellite.limit.raise"), () -> sendNumber(ResonanceActionPayload.LIMIT, step(1))));

        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 50, y + READOUT_Y + 3, 40, 12,
                () -> Component.translatable(this.menu.isOutput() ? "nautec.resonance_node.mode.output" : "nautec.resonance_node.mode.input"),
                () -> this.menu.isOutput() ? RECEIVE_COLOR : SEND_COLOR,
                () -> this.menu.isOutput() ? RECEIVE_HOVER : SEND_HOVER,
                () -> Component.translatable(this.menu.isOutput() ? "nautec.resonance_node.mode.output.desc" : "nautec.resonance_node.mode.input.desc")
                        .append("\n").append(Component.translatable("nautec.resonance.mode.click").withStyle(ChatFormatting.GRAY)),
                () -> send(ResonanceActionPayload.MODE, null, "")));
    }

    private void sendNumber(int action, int value) {
        send(action, null, Integer.toString(value));
    }

    private int step(int direction) {
        int current = this.menu.getLimit();
        int max = NTConfig.satelliteTransferLimit;
        if (direction > 0) {
            for (int value : LIMITS) {
                if (value > current) {
                    return Math.min(value, max);
                }
            }
            return max;
        }
        int result = 0;
        for (int value : LIMITS) {
            if (value < current) {
                result = value;
            }
        }
        return Math.min(result, max);
    }

    private String statusKey() {
        return ResonanceNodeBlockEntity.statusKey(this.menu.getStatus());
    }

    @Override
    protected void extractReadoutBackground(GuiGraphicsExtractor graphics, int rx, int ry, int rw) {
        int color = this.menu.getStatus() == ResonanceNodeBlockEntity.STATUS_ONLINE ? ONLINE : OFFLINE;
        graphics.fill(rx + 5, ry + 5, rx + 9, ry + 9, color);
    }

    @Override
    protected void extractReadout(GuiGraphicsExtractor graphics, int tx, int ty, ResonanceSyncPayload.@Nullable NetworkView view) {
        boolean online = this.menu.getStatus() == ResonanceNodeBlockEntity.STATUS_ONLINE;
        graphics.text(this.font, Component.translatable(statusKey()), tx + 9, ty - 10, online ? READOUT : READOUT_DIM, false);
        String purity = String.format(Locale.ROOT, "%.2f", this.menu.getPurity());
        graphics.text(this.font, Component.translatable(this.menu.isOutput() ? "nautec.resonance_node.sending" : "nautec.resonance_node.taking",
                number(this.menu.getFlow()), purity), tx, ty + 2, READOUT, false);
        graphics.text(this.font, Component.translatable("nautec.satellite.stored", compact(this.menu.getAp()), compact(NTConfig.resonanceNodeApBuffer),
                compact(this.menu.getFe()), compact(NTConfig.resonanceNodeFeBuffer)), tx, ty + 13, READOUT_DIM, false);
    }

    @Override
    protected boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int rx, int ry) {
        if (!inside(mouseX, mouseY, rx, ry, IMAGE_WIDTH - 60, READOUT_HEIGHT)) {
            return false;
        }
        lines.add(Component.translatable(statusKey()));
        lines.add(Component.translatable(statusKey() + ".desc").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.resonance_node.cores", this.menu.getCores()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("nautec.resonance_node.dimension.desc",
                Math.round(NTConfig.satelliteCrossDimensionPurityLoss * 100)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(this.menu.isOutput() ? "nautec.resonance_node.buffer.output" : "nautec.resonance_node.buffer.input")
                .withStyle(ChatFormatting.GRAY));
        return true;
    }
}
