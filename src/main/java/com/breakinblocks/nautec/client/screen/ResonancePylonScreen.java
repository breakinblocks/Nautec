package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.ResonancePylonMenu;
import com.breakinblocks.nautec.content.resonance.ResonanceClientState;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
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

public class ResonancePylonScreen extends AbstractContainerScreen<ResonancePylonMenu> {
    private static final int PANEL = 0xFFC8C7B3;
    private static final int PANEL_LIGHT = 0xFFE7E7D6;
    private static final int OUTLINE = 0xFF070707;
    private static final int SLOT_EDGE = 0xFF1E2221;
    private static final int SCREEN_FILL = 0xFF16201F;
    private static final int SCREEN_EDGE = 0xFF2E3A37;
    private static final int LABEL = 0xFF404040;
    private static final int READOUT = 0xFFB3FCFF;
    private static final int READOUT_DIM = 0xFF6FA6A8;
    private static final int ENERGY_FILL = 0xFFD8443C;
    private static final int ENERGY_SHINE = 0xFFF29A8C;
    private static final int SEND_COLOR = 0xFF2E7D4F;
    private static final int SEND_HOVER = 0xFF3C9A63;
    private static final int RECEIVE_COLOR = 0xFF2F5C8C;
    private static final int RECEIVE_HOVER = 0xFF3C73A8;
    private static final int NEUTRAL = 0xFF45504A;
    private static final int NEUTRAL_HOVER = 0xFF5A6A62;
    private static final int DANGER = 0xFF8C2F2F;
    private static final int DANGER_HOVER = 0xFFA83C3C;

    private static final int IMAGE_WIDTH = 232;
    private static final int IMAGE_HEIGHT = 206;
    private static final int READOUT_Y = 72;
    private static final int READOUT_HEIGHT = 40;
    private static final int MANAGE_Y = 118;
    private static final int TRUSTED_ROWS = 4;
    private static final int ROW = 11;

    private @Nullable ResonanceSyncPayload shown;
    private @Nullable EditBox nameBox;
    private @Nullable EditBox trustBox;
    private String nameDraft = "";
    private String trustDraft = "";
    private boolean confirmDelete;

    public ResonancePylonScreen(ResonancePylonMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

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

    private void send(int action, @Nullable UUID network, String text) {
        ClientPacketDistributor.sendToServer(new ResonanceActionPayload(this.menu.getBlockEntity().getBlockPos(), action,
                Optional.ofNullable(network), text));
    }

    @Override
    protected void init() {
        super.init();
        this.shown = sync();
        int x = this.leftPos;
        int y = this.topPos;

        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 72, y + 4, 64, 14,
                () -> Component.translatable(this.menu.isSendMode() ? "nautec.resonance.mode.send" : "nautec.resonance.mode.receive"),
                () -> this.menu.isSendMode() ? SEND_COLOR : RECEIVE_COLOR,
                () -> this.menu.isSendMode() ? SEND_HOVER : RECEIVE_HOVER,
                () -> Component.translatable(this.menu.isSendMode() ? "nautec.resonance.mode.send.desc" : "nautec.resonance.mode.receive.desc")
                        .append("\n").append(Component.translatable("nautec.resonance.mode.click").withStyle(ChatFormatting.GRAY)),
                () -> send(ResonanceActionPayload.MODE, null, "")));

        addRenderableWidget(new PanelButton(x + 8, y + 32, 14, 14, () -> Component.literal("<"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.resonance.cycle.desc"), () -> cycle(-1)));
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 22, y + 32, 14, 14, () -> Component.literal(">"), () -> NEUTRAL, () -> NEUTRAL_HOVER,
                () -> Component.translatable("nautec.resonance.cycle.desc"), () -> cycle(1)));

        this.nameBox = new EditBox(this.font, x + 8, y + 52, IMAGE_WIDTH - 72, 14, Component.translatable("nautec.resonance.new_name"));
        this.nameBox.setMaxLength(24);
        this.nameBox.setHint(Component.translatable("nautec.resonance.new_name").withStyle(ChatFormatting.DARK_GRAY));
        this.nameBox.setValue(nameDraft);
        this.nameBox.setResponder(value -> nameDraft = value);
        this.nameBox.setTooltip(Tooltip.create(Component.translatable("nautec.resonance.new_name.desc")));
        addRenderableWidget(this.nameBox);
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 60, y + 52, 52, 14, () -> Component.translatable("nautec.resonance.create"),
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

    private static String number(long value) {
        return String.format(Locale.ROOT, "%,d", value);
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
        int bx = rx + 5;
        int by = ry + 5;
        int bw = rw - 10;
        graphics.fill(bx - 1, by - 1, bx + bw + 1, by + 6, SCREEN_EDGE);
        graphics.fill(bx, by, bx + bw, by + 5, SLOT_EDGE);
        int fill = Math.round(bw * Math.min(1F, this.menu.getEnergy() / (float) Math.max(1, this.menu.getCapacity())));
        if (fill > 0) {
            graphics.fill(bx, by, bx + fill, by + 5, ENERGY_FILL);
            graphics.fill(bx, by, bx + fill, by + 1, ENERGY_SHINE);
        }

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

        int tx = 13;
        int ty = READOUT_Y + 14;
        graphics.text(this.font, Component.translatable("nautec.resonance.flow", number(this.menu.getFlow())), tx, ty, READOUT, false);
        graphics.text(this.font, Component.translatable("nautec.resonance.buffer", number(this.menu.getEnergy()), number(this.menu.getCapacity())),
                tx, ty + 11, READOUT_DIM, false);
        Component tier = Component.translatable(this.menu.isInterdimensional() ? "nautec.resonance.tier.abyssal" : "nautec.resonance.tier.basic");
        graphics.text(this.font, tier, IMAGE_WIDTH - 13 - this.font.width(tier), ty, READOUT_DIM, false);
        if (view != null) {
            Component pylons = Component.translatable("nautec.resonance.pylons", view.pylons());
            graphics.text(this.font, pylons, IMAGE_WIDTH - 13 - this.font.width(pylons), ty + 11, READOUT_DIM, false);
        }

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
        if (inside(mouseX, mouseY, rx, ry, IMAGE_WIDTH - 16, 12)) {
            lines.add(Component.translatable("nautec.resonance.buffer", number(this.menu.getEnergy()), number(this.menu.getCapacity())));
            lines.add(Component.translatable("nautec.resonance.buffer.desc").withStyle(ChatFormatting.GRAY));
        } else if (inside(mouseX, mouseY, rx, ry + 12, IMAGE_WIDTH - 16, READOUT_HEIGHT - 12)) {
            lines.add(Component.translatable("nautec.resonance.flow", number(this.menu.getFlow())));
            int throughput = this.menu.isInterdimensional() ? NTConfig.abyssalPylonThroughput : NTConfig.resonancePylonThroughput;
            lines.add(Component.translatable("nautec.resonance.flow.desc", number(throughput)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(this.menu.isInterdimensional() ? "nautec.resonance.tier.abyssal.desc" : "nautec.resonance.tier.basic.desc",
                    Math.round(NTConfig.resonanceSameDimensionLoss * 100), Math.round(NTConfig.resonanceCrossDimensionLoss * 100))
                    .withStyle(ChatFormatting.GRAY));
        } else if (inside(mouseX, mouseY, this.leftPos + 23, this.topPos + 31, IMAGE_WIDTH - 46, 16)) {
            ResonanceSyncPayload.NetworkView view = current();
            lines.add(view == null ? Component.translatable("nautec.resonance.none") : Component.literal(view.name()));
            lines.add(Component.translatable(view == null ? "nautec.resonance.none.desc" : "nautec.resonance.network.desc").withStyle(ChatFormatting.GRAY));
        }
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private final class PanelButton extends AbstractButton {
        private final Supplier<Component> label;
        private final Supplier<Integer> color;
        private final Supplier<Integer> hover;
        private final Supplier<Component> tooltip;
        private final Runnable action;
        private @Nullable Component lastTooltip;

        private PanelButton(int x, int y, int width, int height, Supplier<Component> label, Supplier<Integer> color,
                            Supplier<Integer> hover, Supplier<Component> tooltip, Runnable action) {
            super(x, y, width, height, label.get());
            this.label = label;
            this.color = color;
            this.hover = hover;
            this.tooltip = tooltip;
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            action.run();
        }

        @Override
        public Component getMessage() {
            return label.get();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            Component tip = tooltip.get();
            if (!tip.equals(lastTooltip)) {
                lastTooltip = tip;
                setTooltip(Tooltip.create(tip));
            }
            int x = getX();
            int y = getY();
            graphics.fill(x, y, x + getWidth(), y + getHeight(), OUTLINE);
            int fill = !this.active ? SLOT_EDGE : isHoveredOrFocused() ? hover.get() : color.get();
            graphics.fill(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1, fill);
            Component message = getMessage();
            graphics.text(ResonancePylonScreen.this.font, message, x + (getWidth() - ResonancePylonScreen.this.font.width(message)) / 2,
                    y + (getHeight() - 8) / 2, this.active ? 0xFFFFFFFF : 0xFF808080, true);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
