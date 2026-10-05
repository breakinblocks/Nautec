package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.resonance.ResonanceChunkLoading;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceClientState;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class ResonanceNetworkScreen<M extends NTAbstractContainerMenu<?>> extends AbstractContainerScreen<M> {
    protected static final int PANEL = PanelStyle.PANEL;
    protected static final int PANEL_LIGHT = PanelStyle.PANEL_LIGHT;
    protected static final int OUTLINE = PanelStyle.OUTLINE;
    protected static final int SLOT_EDGE = PanelStyle.SLOT_EDGE;
    protected static final int SCREEN_FILL = PanelStyle.SCREEN_FILL;
    protected static final int SCREEN_EDGE = PanelStyle.SCREEN_EDGE;
    protected static final int LABEL = PanelStyle.LABEL;
    protected static final int READOUT = PanelStyle.READOUT;
    protected static final int READOUT_DIM = PanelStyle.READOUT_DIM;
    protected static final int ENERGY_FILL = PanelStyle.ENERGY_FILL;
    protected static final int ENERGY_SHINE = PanelStyle.ENERGY_SHINE;
    protected static final int SEND_COLOR = PanelStyle.SEND_COLOR;
    protected static final int SEND_HOVER = PanelStyle.SEND_HOVER;
    protected static final int RECEIVE_COLOR = PanelStyle.RECEIVE_COLOR;
    protected static final int RECEIVE_HOVER = PanelStyle.RECEIVE_HOVER;
    protected static final int NEUTRAL = PanelStyle.NEUTRAL;
    protected static final int NEUTRAL_HOVER = PanelStyle.NEUTRAL_HOVER;
    protected static final int DANGER = PanelStyle.DANGER;
    protected static final int DANGER_HOVER = PanelStyle.DANGER_HOVER;

    protected static final int IMAGE_WIDTH = 232;
    protected static final int IMAGE_HEIGHT = 206;
    protected static final int READOUT_Y = 72;
    protected static final int READOUT_HEIGHT = 40;
    private static final int MANAGE_Y = 118;
    private static final int TRUSTED_ROWS = 4;
    private static final int ROW = 11;

    private @Nullable ResonanceSyncPayload shown;
    private @Nullable EditBox nameBox;
    private @Nullable EditBox trustBox;
    private String nameDraft = "";
    private String trustDraft = "";
    private boolean confirmDelete;

    protected ResonanceNetworkScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    protected abstract void addHeaderWidgets(int x, int y);

    protected abstract int chunkState();

    protected abstract void extractReadoutBackground(GuiGraphicsExtractor graphics, int x, int y, int width);

    protected abstract void extractReadout(GuiGraphicsExtractor graphics, int x, int y, ResonanceSyncPayload.@Nullable NetworkView view);

    protected abstract boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int x, int y);

    private @Nullable ResonanceSyncPayload sync() {
        ResonanceSyncPayload latest = ResonanceClientState.latest();
        return latest != null && latest.pos().equals(this.menu.getBlockEntity().getBlockPos()) ? latest : null;
    }

    private @Nullable ResonanceSyncPayload.NetworkView current() {
        ResonanceSyncPayload sync = sync();
        if (sync == null || sync.current().isEmpty()) {
            return null;
        }
        for (ResonanceSyncPayload.NetworkView view : sync.networks()) {
            if (view.id().equals(sync.current().get())) {
                return view;
            }
        }
        return null;
    }

    protected void send(int action, @Nullable UUID network, String text) {
        ClientPacketDistributor.sendToServer(new ResonanceActionPayload(this.menu.getBlockEntity().getBlockPos(), action,
                Optional.ofNullable(network), text));
    }

    @Override
    protected void init() {
        super.init();
        this.shown = sync();
        int x = this.leftPos;
        int y = this.topPos;

        addHeaderWidgets(x, y);

        addRenderableWidget(new PanelButton(x + 8, y + 32, 14, 14, () -> Component.literal("<"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.resonance.cycle.desc"), () -> cycle(-1)));
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 22, y + 32, 14, 14, () -> Component.literal(">"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.resonance.cycle.desc"), () -> cycle(1)));

        this.nameBox = new EditBox(this.font, x + 8, y + 52, IMAGE_WIDTH - 104, 14, Component.translatable("nautec.resonance.new_name"));
        this.nameBox.setMaxLength(24);
        this.nameBox.setHint(Component.translatable("nautec.resonance.new_name").withStyle(ChatFormatting.DARK_GRAY));
        this.nameBox.setValue(nameDraft);
        this.nameBox.setResponder(value -> nameDraft = value);
        this.nameBox.setTooltip(Tooltip.create(Component.translatable("nautec.resonance.new_name.desc")));
        addRenderableWidget(this.nameBox);
        PanelButton chunk = addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 36, y + 52, 28, 14, () -> Component.translatable("nautec.resonance.chunk"),
                () -> chunkState() == ResonanceChunkLoading.ON ? SEND_COLOR : NEUTRAL,
                () -> chunkState() == ResonanceChunkLoading.ON ? SEND_HOVER : NEUTRAL_HOVER,
                () -> Component.translatable(switch (chunkState()) {
                    case ResonanceChunkLoading.ON -> "nautec.resonance.chunk.on";
                    case ResonanceChunkLoading.DISABLED -> "nautec.resonance.chunk.disabled";
                    default -> "nautec.resonance.chunk.off";
                }).append("\n").append(Component.translatable("nautec.resonance.chunk.desc").withStyle(ChatFormatting.GRAY)),
                () -> send(ResonanceActionPayload.CHUNK, null, "")));
        chunk.active = chunkState() != ResonanceChunkLoading.DISABLED;
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 92, y + 52, 52, 14, () -> Component.translatable("nautec.resonance.create"),
                () -> NEUTRAL, () -> NEUTRAL_HOVER, () -> Component.translatable("nautec.resonance.create.desc"), () -> {
            if (!nameDraft.isBlank()) {
                send(ResonanceActionPayload.CREATE, null, nameDraft);
                nameDraft = "";
                if (this.nameBox != null) {
                    this.nameBox.setValue("");
                }
            }
        }));

        this.trustBox = null;
        ResonanceSyncPayload.NetworkView view = current();
        if (view == null || !view.manage()) {
            return;
        }
        boolean teams = this.shown != null && this.shown.teamsAvailable();
        PanelButton team = addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 92, y + MANAGE_Y, 84, 14,
                () -> Component.translatable(teamOn() ? "nautec.resonance.team.on" : "nautec.resonance.team.off"),
                () -> teamOn() ? SEND_COLOR : NEUTRAL, () -> teamOn() ? SEND_HOVER : NEUTRAL_HOVER,
                () -> Component.translatable(teams ? "nautec.resonance.team.desc" : "nautec.resonance.team.missing"),
                () -> send(ResonanceActionPayload.TEAM, null, "")));
        team.active = teams;

        List<ResonanceSyncPayload.Member> trusted = view.trusted();
        for (int i = 0; i < Math.min(TRUSTED_ROWS, trusted.size()); i++) {
            ResonanceSyncPayload.Member member = trusted.get(i);
            addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 22, y + MANAGE_Y + 18 + i * ROW, 12, 10, () -> Component.literal("x"),
                    () -> DANGER, () -> DANGER_HOVER, () -> Component.translatable("nautec.resonance.untrust.desc", member.name()),
                    () -> send(ResonanceActionPayload.UNTRUST, null, member.id().toString())));
        }

        int rowY = y + IMAGE_HEIGHT - 22;
        this.trustBox = new EditBox(this.font, x + 8, rowY, IMAGE_WIDTH - 124, 14, Component.translatable("nautec.resonance.trust_name"));
        this.trustBox.setMaxLength(16);
        this.trustBox.setHint(Component.translatable("nautec.resonance.trust_name").withStyle(ChatFormatting.DARK_GRAY));
        this.trustBox.setValue(trustDraft);
        this.trustBox.setResponder(value -> trustDraft = value);
        this.trustBox.setTooltip(Tooltip.create(Component.translatable("nautec.resonance.trust_name.desc")));
        addRenderableWidget(this.trustBox);
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 112, rowY, 48, 14, () -> Component.translatable("nautec.resonance.trust"),
                () -> NEUTRAL, () -> NEUTRAL_HOVER, () -> Component.translatable("nautec.resonance.trust.desc"), () -> {
            if (!trustDraft.isBlank()) {
                send(ResonanceActionPayload.TRUST, null, trustDraft);
                trustDraft = "";
                if (this.trustBox != null) {
                    this.trustBox.setValue("");
                }
            }
        }));
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 60, rowY, 52, 14,
                () -> Component.translatable(confirmDelete ? "nautec.resonance.delete.confirm" : "nautec.resonance.delete"),
                () -> DANGER, () -> DANGER_HOVER, () -> Component.translatable("nautec.resonance.delete.desc"), () -> {
            if (confirmDelete) {
                confirmDelete = false;
                send(ResonanceActionPayload.DELETE, null, "");
            } else {
                confirmDelete = true;
            }
        }));
    }

    private boolean teamOn() {
        ResonanceSyncPayload.NetworkView view = current();
        return view != null && view.teamAccess();
    }

    private void cycle(int step) {
        ResonanceSyncPayload sync = sync();
        if (sync == null) {
            return;
        }
        List<UUID> options = new ArrayList<>();
        options.add(null);
        for (ResonanceSyncPayload.NetworkView view : sync.networks()) {
            options.add(view.id());
        }
        int index = Math.max(0, options.indexOf(sync.current().orElse(null)));
        int next = Math.floorMod(index + step, options.size());
        confirmDelete = false;
        send(ResonanceActionPayload.SELECT, options.get(next), "");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (sync() != this.shown) {
            rebuildWidgets();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        EditBox focused = this.nameBox != null && this.nameBox.isFocused() ? this.nameBox
                : this.trustBox != null && this.trustBox.isFocused() ? this.trustBox : null;
        if (focused != null && event.key() != GLFW.GLFW_KEY_ESCAPE) {
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                focused.setFocused(false);
                return true;
            }
            focused.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    protected static String number(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    protected static String compact(long value) {
        if (value >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", value / 1_000_000.0).replace(".0M", "M");
        }
        if (value >= 1_000) {
            return String.format(Locale.ROOT, "%.1fk", value / 1_000.0).replace(".0k", "k");
        }
        return Long.toString(value);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, OUTLINE);
        graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, PANEL_LIGHT);
        graphics.fill(x + 3, y + 3, x + this.imageWidth - 1, y + this.imageHeight - 1, PANEL);

        graphics.fill(x + 23, y + 31, x + IMAGE_WIDTH - 23, y + 47, OUTLINE);
        graphics.fill(x + 24, y + 32, x + IMAGE_WIDTH - 24, y + 46, SCREEN_FILL);

        int rx = x + 8;
        int ry = y + READOUT_Y;
        int rw = IMAGE_WIDTH - 16;
        graphics.fill(rx - 1, ry - 1, rx + rw + 1, ry + READOUT_HEIGHT + 1, OUTLINE);
        graphics.fill(rx, ry, rx + rw, ry + READOUT_HEIGHT, SCREEN_EDGE);
        graphics.fill(rx + 1, ry + 1, rx + rw - 1, ry + READOUT_HEIGHT - 1, SCREEN_FILL);
        extractReadoutBackground(graphics, rx, ry, rw);

        ResonanceSyncPayload.NetworkView view = current();
        if (view != null && view.manage()) {
            int ly = y + MANAGE_Y + 17;
            graphics.fill(x + 7, ly - 1, x + IMAGE_WIDTH - 7, ly + TRUSTED_ROWS * ROW + 1, OUTLINE);
            graphics.fill(x + 8, ly, x + IMAGE_WIDTH - 8, ly + TRUSTED_ROWS * ROW, SCREEN_FILL);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, 8, 8, LABEL, false);
        graphics.text(this.font, Component.translatable("nautec.resonance.network"), 8, 22, LABEL, false);

        ResonanceSyncPayload.NetworkView view = current();
        Component name = view == null ? Component.translatable("nautec.resonance.none") : Component.literal(view.name());
        int nameWidth = this.font.width(name);
        graphics.text(this.font, name, (IMAGE_WIDTH - nameWidth) / 2, 35, view == null ? READOUT_DIM : READOUT, false);
        if (view != null) {
            Component owner = Component.translatable("nautec.resonance.owner", view.ownerName());
            graphics.text(this.font, owner, IMAGE_WIDTH - 24 - this.font.width(owner), 22, LABEL, false);
        }

        extractReadout(graphics, 13, READOUT_Y + 14, view);

        if (view != null && view.manage()) {
            graphics.text(this.font, Component.translatable("nautec.resonance.trusted"), 8, MANAGE_Y + 3, LABEL, false);
            List<ResonanceSyncPayload.Member> trusted = view.trusted();
            if (trusted.isEmpty()) {
                graphics.text(this.font, Component.translatable("nautec.resonance.trusted.none"), 12, MANAGE_Y + 19, READOUT_DIM, false);
            }
            for (int i = 0; i < Math.min(TRUSTED_ROWS, trusted.size()); i++) {
                graphics.text(this.font, trusted.get(i).name(), 12, MANAGE_Y + 19 + i * ROW, READOUT, false);
            }
            if (trusted.size() > TRUSTED_ROWS) {
                Component more = Component.translatable("nautec.resonance.trusted.more", trusted.size() - TRUSTED_ROWS);
                graphics.text(this.font, more, IMAGE_WIDTH - 30 - this.font.width(more), MANAGE_Y + 19 + (TRUSTED_ROWS - 1) * ROW, READOUT_DIM, false);
            }
        } else if (view != null) {
            graphics.text(this.font, Component.translatable("nautec.resonance.member"), 8, MANAGE_Y + 3, READOUT_DIM, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        List<Component> lines = new ArrayList<>();
        int rx = this.leftPos + 8;
        int ry = this.topPos + READOUT_Y;
        if (!readoutTooltip(lines, mouseX, mouseY, rx, ry) && inside(mouseX, mouseY, this.leftPos + 23, this.topPos + 31, IMAGE_WIDTH - 46, 16)) {
            ResonanceSyncPayload.NetworkView view = current();
            lines.add(view == null ? Component.translatable("nautec.resonance.none") : Component.literal(view.name()));
            lines.add(Component.translatable(view == null ? "nautec.resonance.none.desc" : "nautec.resonance.network.desc").withStyle(ChatFormatting.GRAY));
        }
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    protected static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected final class PanelButton extends NTPanelButton {
        protected PanelButton(int x, int y, int width, int height, Supplier<Component> label, Supplier<Integer> color,
                              Supplier<Integer> hover, Supplier<Component> tooltip, Runnable action) {
            super(ResonanceNetworkScreen.this.font, x, y, width, height, label, color, hover, tooltip, action);
        }
    }
}
