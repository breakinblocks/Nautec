package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blocks.BeamFaces;
import com.breakinblocks.nautec.network.SetBeamFacePayload;
import com.breakinblocks.nautec.network.SetJunctionLimitPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BeamFacesScreen extends Screen {
    private static final int BOX_W = 44;
    private static final int BOX_H = 34;
    private static final int GAP = 4;
    private static final int PAD = 8;
    private static final int HEADER_H = 22;
    private static final int LINE_H = 10;
    private static final int NET_W = BOX_W * 4 + GAP * 3;
    private static final int NET_H = BOX_H * 3 + GAP * 2;
    private static final int PANEL_W = NET_W + PAD * 2;
    private static final int INPUT_COLOR = 0xFF2F6FA8;
    private static final int OUTPUT_COLOR = 0xFFB07A1C;
    private static final int OFF_COLOR = PanelStyle.NEUTRAL;
    private static final int RANGE_BUFFER = 1;

    private static final String[] POSITIONS = {"top", "left", "front", "right", "back", "bottom"};
    private static final int[][] CELLS = {{1, 0}, {0, 1}, {1, 1}, {2, 1}, {3, 1}, {1, 2}};

    private final BlockPos pos;
    private final Direction[] faces = new Direction[6];

    private BeamFacesScreen(BlockPos pos, Direction look) {
        super(Component.empty());
        this.pos = pos;
        faces[0] = Direction.UP;
        faces[1] = look.getCounterClockWise();
        faces[2] = look.getOpposite();
        faces[3] = look.getClockWise();
        faces[4] = look;
        faces[5] = Direction.DOWN;
    }

    public static void open(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || !BeamFaces.configurable(minecraft.level.getBlockState(pos))) {
            return;
        }
        minecraft.setScreen(new BeamFacesScreen(pos.immutable(), Direction.fromYRot(player.getYRot())));
    }

    private @Nullable BlockState state() {
        return this.minecraft == null || this.minecraft.level == null ? null : this.minecraft.level.getBlockState(this.pos);
    }

    private @Nullable LaserJunctionBlockEntity junction() {
        return this.minecraft != null && this.minecraft.level != null
                && this.minecraft.level.getBlockEntity(this.pos) instanceof LaserJunctionBlockEntity junction ? junction : null;
    }

    private static String compact(int value) {
        if (value >= 10_000_000) {
            return value / 1_000_000 + "M";
        }
        if (value >= 10_000) {
            return value / 1_000 + "k";
        }
        return Integer.toString(value);
    }

    private static Component limitLabel(int limit) {
        return limit > 0 ? Component.translatable("nautec.beam_faces.limit", compact(limit)) : Component.translatable("nautec.beam_faces.limit.none");
    }

    private Component hint(BlockState state) {
        return Component.translatable(BeamFaces.isRelay(state) ? "nautec.beam_faces.hint.relay" : "nautec.beam_faces.hint.junction");
    }

    private int panelH() {
        BlockState state = state();
        int lines = state == null ? 1 : this.font.split(hint(state), NET_W).size();
        return HEADER_H + NET_H + PAD + lines * LINE_H + PAD;
    }

    private int panelX() {
        return (this.width - PANEL_W) / 2;
    }

    private int panelY() {
        return (this.height - panelH()) / 2;
    }

    private int boxX(int index) {
        return panelX() + PAD + CELLS[index][0] * (BOX_W + GAP);
    }

    private int boxY(int index) {
        return panelY() + HEADER_H + CELLS[index][1] * (BOX_H + GAP);
    }

    private int hovered(double mouseX, double mouseY) {
        for (int i = 0; i < faces.length; i++) {
            int x = boxX(i);
            int y = boxY(i);
            if (mouseX >= x && mouseX < x + BOX_W && mouseY >= y && mouseY < y + BOX_H) {
                return i;
            }
        }
        return -1;
    }

    private static int color(BeamFaces.Mode mode) {
        return switch (mode) {
            case INPUT -> INPUT_COLOR;
            case OUTPUT -> OUTPUT_COLOR;
            case OFF -> OFF_COLOR;
        };
    }

    private static Component modeName(BeamFaces.Mode mode) {
        return Component.translatable("nautec.beam_faces.mode." + mode.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public void tick() {
        super.tick();
        BlockState state = state();
        Player player = this.minecraft == null ? null : this.minecraft.player;
        if (state == null || player == null || !BeamFaces.configurable(state) || !player.canInteractWithBlock(this.pos, RANGE_BUFFER)) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BlockState state = state();
        if (state == null) {
            return;
        }
        graphics.fill(0, 0, this.width, this.height, 0x90000000);
        int x = panelX();
        int y = panelY();
        PanelStyle.panel(graphics, x, y, PANEL_W, panelH());
        graphics.drawString(this.font, state.getBlock().getName(), x + PAD, y + 8, PanelStyle.LABEL, false);

        int hover = hovered(mouseX, mouseY);
        for (int i = 0; i < faces.length; i++) {
            BeamFaces.Mode mode = BeamFaces.mode(state, faces[i]);
            int bx = boxX(i);
            int by = boxY(i);
            graphics.fill(bx, by, bx + BOX_W, by + BOX_H, PanelStyle.OUTLINE);
            int fill = color(mode);
            graphics.fill(bx + 1, by + 1, bx + BOX_W - 1, by + BOX_H - 1, fill);
            if (i == hover) {
                graphics.fill(bx + 1, by + 1, bx + BOX_W - 1, by + BOX_H - 1, 0x30FFFFFF);
            }
            Component position = Component.translatable("nautec.beam_faces." + POSITIONS[i]);
            Component modeLabel = modeName(mode);
            LaserJunctionBlockEntity junction = junction();
            if (junction != null && mode == BeamFaces.Mode.OUTPUT) {
                int limit = junction.getOutputLimit(faces[i]);
                Component limitLabel = limitLabel(limit);
                graphics.drawString(this.font, position, bx + (BOX_W - this.font.width(position)) / 2, by + 3, 0xFFFFFFFF, true);
                graphics.drawString(this.font, modeLabel, bx + (BOX_W - this.font.width(modeLabel)) / 2, by + 13, 0xFFE0E0E0, true);
                graphics.drawString(this.font, limitLabel, bx + (BOX_W - this.font.width(limitLabel)) / 2, by + 23, limit > 0 ? 0xFFFFFFFF : 0xFFB8B8B8, true);
            } else {
                graphics.drawString(this.font, position, bx + (BOX_W - this.font.width(position)) / 2, by + 7, 0xFFFFFFFF, true);
                graphics.drawString(this.font, modeLabel, bx + (BOX_W - this.font.width(modeLabel)) / 2, by + 19, 0xFFE0E0E0, true);
            }
        }

        List<FormattedCharSequence> lines = this.font.split(hint(state), NET_W);
        int lineY = y + HEADER_H + NET_H + PAD;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, x + PAD, lineY, PanelStyle.LABEL, false);
            lineY += LINE_H;
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        if (hover >= 0) {
            Direction face = faces[hover];
            BeamFaces.Mode mode = BeamFaces.mode(state, face);
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("nautec.beam_faces.tooltip",
                    Component.translatable("nautec.beam_faces." + POSITIONS[hover]),
                    Component.translatable("nautec.beam_faces.dir." + face.getSerializedName()),
                    modeName(mode)));
            LaserJunctionBlockEntity junction = junction();
            if (junction != null && mode == BeamFaces.Mode.OUTPUT) {
                int limit = junction.getOutputLimit(face);
                tooltip.add((limit > 0 ? Component.translatable("nautec.beam_faces.tooltip.limit", limit)
                        : Component.translatable("nautec.beam_faces.tooltip.no_limit")).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("nautec.beam_faces.tooltip.scroll").withStyle(ChatFormatting.DARK_GRAY));
            }
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int index = hovered(mouseX, mouseY);
        if (index >= 0 && (button == 0 || button == 1)) {
            PacketDistributor.sendToServer(new SetBeamFacePayload(this.pos, faces[index].get3DDataValue(), button == 0));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int index = hovered(mouseX, mouseY);
        BlockState state = state();
        LaserJunctionBlockEntity junction = junction();
        if (index >= 0 && scrollY != 0 && state != null && junction != null && BeamFaces.mode(state, faces[index]) == BeamFaces.Mode.OUTPUT) {
            Direction face = faces[index];
            int limit = LimitSteps.step(junction.getOutputLimit(face), scrollY > 0 ? 1 : -1, Screen.hasShiftDown(),
                    Screen.hasControlDown(), Integer.MAX_VALUE);
            junction.setOutputLimit(face, limit);
            PacketDistributor.sendToServer(new SetJunctionLimitPayload(this.pos, face.get3DDataValue(), limit));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
