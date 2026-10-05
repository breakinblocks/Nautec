package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AugmentationStationBlockEntity;
import com.breakinblocks.nautec.network.AugmentationStationSyncPayload;
import com.breakinblocks.nautec.network.StartAugmentationPayload;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AugmentationStationScreen extends Screen {
    private static final int WIDTH = 236;
    private static final int HEIGHT = 178;
    private static final int STATUS_Y = 20;
    private static final int STATUS_HEIGHT = 24;
    private static final int BODY_Y = 52;
    private static final int VIEW_WIDTH = 60;
    private static final int VIEW_HEIGHT = 92;
    private static final int RIGHT_X = 76;
    private static final int RIGHT_WIDTH = WIDTH - RIGHT_X - 8;
    private static final int AUGMENT_HEIGHT = 40;
    private static final int EXTENSIONS_Y = BODY_Y + AUGMENT_HEIGHT + 6;
    private static final int EXTENSIONS_HEIGHT = 46;
    private static final int COLUMN = RIGHT_WIDTH / 4;
    private static final int FOOTER_Y = HEIGHT - 22;
    private static final int APPLY_WIDTH = 56;

    private final Player player;
    private AugmentationStationSyncPayload status;
    private @Nullable AugmentSlot selected;
    private @Nullable AugmentType<?> builtFor;
    private boolean pending;
    private @Nullable NTPanelButton apply;
    private int left;
    private int top;

    public AugmentationStationScreen(Player player, AugmentationStationSyncPayload status) {
        super(Component.translatable("block.nautec.augmentation_station"));
        this.player = player;
        this.status = status;
    }

    public BlockPos pos() {
        return this.status.pos();
    }

    public void update(AugmentationStationSyncPayload status) {
        AugmentType<?> previous = this.status.result().orElse(null);
        this.status = status;
        this.pending = false;
        if (status.result().orElse(null) != previous) {
            rebuildWidgets();
        }
    }

    private List<AugmentSlot> slots() {
        return this.status.result().map(AugmentType::getAugmentSlots).orElse(List.of());
    }

    @Override
    protected void init() {
        super.init();
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        List<AugmentSlot> slots = slots();
        this.builtFor = this.status.result().orElse(null);
        if (this.selected != null && !slots.contains(this.selected)) {
            this.selected = null;
        }
        if (this.selected == null && slots.size() == 1) {
            this.selected = slots.getFirst();
        }

        int x = this.left + 8;
        int maxX = this.left + WIDTH - 8 - APPLY_WIDTH - 4;
        for (AugmentSlot slot : slots) {
            Component name = slotName(slot);
            int buttonWidth = Math.max(36, this.font.width(name) + 10);
            if (x + buttonWidth > maxX) {
                break;
            }
            addRenderableWidget(new NTPanelButton(this.font, x, this.top + FOOTER_Y, buttonWidth, 14, () -> name,
                    () -> slot == this.selected ? PanelStyle.RECEIVE_COLOR : PanelStyle.NEUTRAL,
                    () -> slot == this.selected ? PanelStyle.RECEIVE_HOVER : PanelStyle.NEUTRAL_HOVER,
                    () -> slotTooltip(slot), () -> this.selected = slot));
            x += buttonWidth + 3;
        }

        this.apply = addRenderableWidget(new NTPanelButton(this.font, this.left + WIDTH - 8 - APPLY_WIDTH, this.top + FOOTER_Y, APPLY_WIDTH, 14,
                () -> Component.translatable("nautec.augmentation_station.apply"),
                () -> replaces(this.selected) ? PanelStyle.DANGER : PanelStyle.SEND_COLOR,
                () -> replaces(this.selected) ? PanelStyle.DANGER_HOVER : PanelStyle.SEND_HOVER,
                this::applyTooltip, this::apply));
        updateApply();
    }

    private void apply() {
        if (this.selected == null || !canApply()) {
            return;
        }
        this.pending = true;
        ClientPacketDistributor.sendToServer(new StartAugmentationPayload(this.status.pos(), this.selected));
        updateApply();
    }

    private boolean canApply() {
        return this.status.status() == AugmentationStationBlockEntity.STATUS_READY && this.selected != null && !this.pending;
    }

    private void updateApply() {
        if (this.apply != null) {
            this.apply.active = canApply();
            this.apply.visible = this.status.status() != AugmentationStationBlockEntity.STATUS_RUNNING;
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateApply();
    }

    private @Nullable Augment installed(@Nullable AugmentSlot slot) {
        return slot == null ? null : AugmentHelper.getAugmentBySlot(this.player, slot);
    }

    private boolean replaces(@Nullable AugmentSlot slot) {
        return installed(slot) != null;
    }

    private static Component slotName(AugmentSlot slot) {
        Identifier id = NTRegistries.AUGMENT_SLOT.getKey(slot);
        return id == null ? Component.literal("?") : Component.translatable("augment_slot." + id.getNamespace() + "." + id.getPath());
    }

    private static Component typeName(AugmentType<?> type) {
        Identifier id = NTRegistries.AUGMENT_TYPE.getKey(type);
        return id == null ? Component.literal("?") : Component.translatable("augment_type." + id.getNamespace() + "." + id.getPath());
    }

    private Component slotTooltip(AugmentSlot slot) {
        Augment current = installed(slot);
        Component line = current == null
                ? Component.translatable("nautec.augmentation_station.slot.empty").withStyle(ChatFormatting.GRAY)
                : current.getAugmentType() == this.builtFor
                ? Component.translatable("nautec.augmentation_station.slot.same", typeName(current.getAugmentType())).withStyle(ChatFormatting.YELLOW)
                : Component.translatable("nautec.augmentation_station.slot.replaces", typeName(current.getAugmentType())).withStyle(ChatFormatting.RED);
        return slotName(slot).copy().append("\n").append(line);
    }

    private Component applyTooltip() {
        int code = this.status.status();
        if (code != AugmentationStationBlockEntity.STATUS_READY) {
            return Component.translatable(statusKey(code)).append("\n")
                    .append(statusDescription(code).copy().withStyle(ChatFormatting.GRAY));
        }
        if (this.selected == null) {
            return Component.translatable("nautec.augmentation_station.apply.pick");
        }
        Component name = this.builtFor == null ? Component.literal("?") : typeName(this.builtFor);
        Component text = Component.translatable("nautec.augmentation_station.apply.desc", name, slotName(this.selected),
                String.format(Locale.ROOT, "%.0f", AugmentationStationBlockEntity.OPERATION_TICKS / 20f));
        Augment current = installed(this.selected);
        if (current != null) {
            text = text.copy().append("\n").append(Component.translatable("nautec.augmentation_station.slot.replaces",
                    typeName(current.getAugmentType())).withStyle(ChatFormatting.RED));
        }
        return text;
    }

    private static String statusKey(int code) {
        return "nautec.augmentation_station.status." + switch (code) {
            case AugmentationStationBlockEntity.STATUS_READY -> "ready";
            case AugmentationStationBlockEntity.STATUS_MISSING_EXTENSION -> "missing_extension";
            case AugmentationStationBlockEntity.STATUS_NO_ARM -> "no_arm";
            case AugmentationStationBlockEntity.STATUS_NO_RECIPE -> "no_recipe";
            case AugmentationStationBlockEntity.STATUS_LOW_POWER -> "low_power";
            case AugmentationStationBlockEntity.STATUS_RUNNING -> "running";
            case AugmentationStationBlockEntity.STATUS_INSTALLED -> "installed";
            default -> "empty";
        };
    }

    private static Component statusDescription(int code) {
        String key = statusKey(code) + ".desc";
        return code == AugmentationStationBlockEntity.STATUS_LOW_POWER
                ? Component.translatable(key, NTConfig.augmentationStationPower)
                : Component.translatable(key);
    }

    private int statusColor() {
        return switch (this.status.status()) {
            case AugmentationStationBlockEntity.STATUS_READY, AugmentationStationBlockEntity.STATUS_INSTALLED -> PanelStyle.ONLINE;
            case AugmentationStationBlockEntity.STATUS_RUNNING -> PanelStyle.WARNING;
            default -> PanelStyle.OFFLINE;
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);
        int x = this.left;
        int y = this.top;
        PanelStyle.panel(graphics, x, y, WIDTH, HEIGHT);

        PanelStyle.screen(graphics, x + 8, y + STATUS_Y, WIDTH - 16, STATUS_HEIGHT);
        graphics.fill(x + 13, y + STATUS_Y + 5, x + 17, y + STATUS_Y + 9, statusColor());

        PanelStyle.screen(graphics, x + 8, y + BODY_Y, VIEW_WIDTH, VIEW_HEIGHT);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x + 9, y + BODY_Y + 1, x + 8 + VIEW_WIDTH - 1,
                y + BODY_Y + VIEW_HEIGHT - 1, 34, 0.0625f, mouseX, mouseY, this.player);

        PanelStyle.screen(graphics, x + RIGHT_X, y + BODY_Y, RIGHT_WIDTH, AUGMENT_HEIGHT);
        PanelStyle.screen(graphics, x + RIGHT_X, y + EXTENSIONS_Y, RIGHT_WIDTH, EXTENSIONS_HEIGHT);

        if (this.status.status() == AugmentationStationBlockEntity.STATUS_RUNNING) {
            int barX = x + 8;
            int barWidth = WIDTH - 16;
            int filled = barWidth * Math.min(this.status.progress(), AugmentationStationBlockEntity.OPERATION_TICKS)
                    / AugmentationStationBlockEntity.OPERATION_TICKS;
            graphics.fill(barX - 1, y + FOOTER_Y - 1, barX + barWidth + 1, y + FOOTER_Y + 15, PanelStyle.OUTLINE);
            graphics.fill(barX, y + FOOTER_Y, barX + barWidth, y + FOOTER_Y + 14, PanelStyle.SCREEN_FILL);
            graphics.fill(barX, y + FOOTER_Y, barX + filled, y + FOOTER_Y + 14, PanelStyle.SEND_COLOR);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = this.left;
        int y = this.top;
        int code = this.status.status();

        graphics.text(this.font, this.title, x + 8, y + 8, PanelStyle.LABEL, false);
        graphics.text(this.font, Component.translatable(statusKey(code)), x + 21, y + STATUS_Y + 3, PanelStyle.READOUT, false);
        List<FormattedCharSequence> hint = this.font.split(Component.translatable(statusKey(code) + ".short"), WIDTH - 30);
        if (!hint.isEmpty()) {
            graphics.text(this.font, hint.getFirst(), x + 13, y + STATUS_Y + 13, PanelStyle.READOUT_DIM, false);
        }

        extractAugment(graphics, x + RIGHT_X, y + BODY_Y);
        extractExtensions(graphics, x + RIGHT_X, y + EXTENSIONS_Y);

        if (code == AugmentationStationBlockEntity.STATUS_RUNNING) {
            Component progress = Component.translatable("nautec.augmentation_station.progress",
                    String.format(Locale.ROOT, "%.1f", Math.max(0, AugmentationStationBlockEntity.OPERATION_TICKS - this.status.progress()) / 20f));
            graphics.text(this.font, progress, x + (WIDTH - this.font.width(progress)) / 2, y + FOOTER_Y + 3, 0xFFFFFFFF, true);
        } else if (slots().isEmpty()) {
            graphics.text(this.font, Component.translatable("nautec.augmentation_station.slots.none"), x + 9, y + FOOTER_Y + 3,
                    PanelStyle.LABEL, false);
        }

        tooltips(graphics, mouseX, mouseY);
    }

    private void extractAugment(GuiGraphicsExtractor graphics, int bx, int by) {
        ItemStack preview = this.status.preview();
        PanelStyle.slot(graphics, bx + 5, by + 5);
        if (!preview.isEmpty()) {
            graphics.item(preview, bx + 5, by + 5);
        }
        if (this.builtFor == null) {
            graphics.text(this.font, Component.translatable("nautec.augmentation_station.augment.none"), bx + 27, by + 5, PanelStyle.READOUT_DIM, false);
            return;
        }
        graphics.text(this.font, typeName(this.builtFor), bx + 27, by + 5, PanelStyle.READOUT, false);
        List<FormattedCharSequence> lines = this.font.split(Component.literal(this.status.description()), RIGHT_WIDTH - 32);
        for (int i = 0; i < Math.min(2, lines.size()); i++) {
            graphics.text(this.font, lines.get(i), bx + 27, by + 16 + i * 10, PanelStyle.READOUT_DIM, false);
        }
    }

    private void extractExtensions(GuiGraphicsExtractor graphics, int bx, int by) {
        List<AugmentationStationSyncPayload.Extension> extensions = this.status.extensions();
        for (int i = 0; i < extensions.size(); i++) {
            AugmentationStationSyncPayload.Extension extension = extensions.get(i);
            int cx = bx + i * COLUMN;
            Component side = Component.translatable("nautec.augmentation_station.side." + extension.side().getSerializedName());
            graphics.text(this.font, side, cx + (COLUMN - this.font.width(side)) / 2, by + 3, PanelStyle.READOUT_DIM, false);
            int sx = cx + (COLUMN - 16) / 2;
            int sy = by + 13;
            boolean loaded = !extension.part().isEmpty();
            if (loaded && !extension.arm()) {
                graphics.fill(sx - 2, sy - 2, sx + 18, sy + 18, PanelStyle.OFFLINE);
            }
            PanelStyle.slot(graphics, sx, sy);
            if (loaded) {
                graphics.item(extension.part(), sx, sy);
            } else if (extension.arm()) {
                graphics.fakeItem(NTItems.CLAW_ROBOT_ARM.toStack(), sx, sy);
                graphics.fill(sx, sy, sx + 16, sy + 16, 0xA016201F);
            }
            Component power = !extension.present() ? Component.literal("--")
                    : Component.translatable("nautec.augmentation_station.power", extension.power());
            int color = !extension.present() || !loaded ? PanelStyle.READOUT_DIM
                    : extension.power() >= NTConfig.augmentationStationPower ? PanelStyle.ONLINE : PanelStyle.OFFLINE;
            graphics.text(this.font, power, cx + (COLUMN - this.font.width(power)) / 2, by + 34, color, false);
        }
    }

    private void tooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = this.left;
        int y = this.top;
        List<Component> lines = new ArrayList<>();
        int code = this.status.status();
        if (PanelStyle.inside(mouseX, mouseY, x + 8, y + STATUS_Y, WIDTH - 16, STATUS_HEIGHT)) {
            lines.add(Component.translatable(statusKey(code)));
            lines.add(statusDescription(code).copy().withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.augmentation_station.reopen").withStyle(ChatFormatting.DARK_GRAY));
        } else if (PanelStyle.inside(mouseX, mouseY, x + RIGHT_X, y + BODY_Y, RIGHT_WIDTH, AUGMENT_HEIGHT)) {
            if (this.builtFor == null) {
                lines.add(Component.translatable("nautec.augmentation_station.augment.none"));
                lines.add(Component.translatable("nautec.augmentation_station.augment.none.desc").withStyle(ChatFormatting.GRAY));
            } else {
                lines.add(typeName(this.builtFor));
                if (!this.status.description().isEmpty()) {
                    lines.add(Component.literal(this.status.description()).withStyle(ChatFormatting.GRAY));
                }
                List<Component> names = new ArrayList<>();
                for (AugmentSlot slot : slots()) {
                    names.add(slotName(slot));
                }
                lines.add(Component.translatable("nautec.augmentation_station.augment.slots", joined(names)).withStyle(ChatFormatting.GRAY));
            }
        } else if (PanelStyle.inside(mouseX, mouseY, x + RIGHT_X, y + EXTENSIONS_Y, RIGHT_WIDTH, EXTENSIONS_HEIGHT)) {
            int index = (mouseX - x - RIGHT_X) / COLUMN;
            if (index >= 0 && index < this.status.extensions().size()) {
                extensionTooltip(lines, this.status.extensions().get(index));
            }
        } else if (PanelStyle.inside(mouseX, mouseY, x + 8, y + BODY_Y, VIEW_WIDTH, VIEW_HEIGHT)) {
            lines.add(Component.translatable("nautec.augmentation_station.installed"));
            boolean any = false;
            for (var entry : AugmentHelper.getAugments(this.player).entrySet()) {
                if (entry.getValue() != null) {
                    any = true;
                    lines.add(slotName(entry.getKey()).copy().append(": ").append(typeName(entry.getValue().getAugmentType()))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
            if (!any) {
                lines.add(Component.translatable("nautec.augmentation_station.installed.none").withStyle(ChatFormatting.GRAY));
            }
        }
        if (!lines.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }

    private void extensionTooltip(List<Component> lines, AugmentationStationSyncPayload.Extension extension) {
        Direction side = extension.side();
        lines.add(Component.translatable("nautec.augmentation_station.extension." + side.getSerializedName()));
        if (!extension.present()) {
            lines.add(Component.translatable("nautec.augmentation_station.extension.missing").withStyle(ChatFormatting.RED));
            return;
        }
        lines.add(Component.translatable(extension.arm() ? "nautec.augmentation_station.extension.arm" : "nautec.augmentation_station.extension.no_arm")
                .withStyle(extension.arm() ? ChatFormatting.GRAY : extension.part().isEmpty() ? ChatFormatting.DARK_GRAY : ChatFormatting.RED));
        lines.add(extension.part().isEmpty()
                ? Component.translatable("nautec.augmentation_station.extension.empty").withStyle(ChatFormatting.GRAY)
                : Component.translatable("nautec.augmentation_station.extension.part", extension.part().getHoverName()).withStyle(ChatFormatting.GRAY));
        boolean enough = extension.power() >= NTConfig.augmentationStationPower;
        lines.add(Component.translatable("nautec.augmentation_station.extension.power", extension.power(), NTConfig.augmentationStationPower)
                .withStyle(extension.part().isEmpty() ? ChatFormatting.DARK_GRAY : enough ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (!extension.part().isEmpty() && !enough) {
            lines.add(Component.translatable("nautec.augmentation_station.extension.power.desc").withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component joined(List<Component> parts) {
        Component result = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                result = result.copy().append(", ");
            }
            result = result.copy().append(parts.get(i));
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
