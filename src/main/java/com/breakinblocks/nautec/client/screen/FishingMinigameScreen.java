package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.network.FishingMinigameResultPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FishingMinigameScreen extends Screen {
    private static final int PANEL_W = 208;
    private static final int PANEL_H = 60;
    private static final int PANEL_BOTTOM_GAP = 46;
    private static final int CHAMFER = 4;

    private static final int TRACK_W = 176;
    private static final int TRACK_H = 13;
    private static final int TRACK_TOP = 33;
    private static final int TITLE_ROW = 6;
    private static final int STATUS_ROW = 17;
    private static final int COUNTDOWN_ROW = TRACK_TOP + TRACK_H + 9;

    private static final int WIN_HOLD_TICKS = 10;
    private static final int SHAKE_TICKS = 8;
    private static final int POP_TICKS = 6;

    private static final int PLATE = 0xD9070B10;
    private static final int PLATE_EDGE = 0xF0050809;
    private static final int CYAN = 0xFF3EFDFF;
    private static final int CYAN_DIM = 0xFF19646B;
    private static final int GREEN = 0xFF5FE8B0;
    private static final int AMBER = 0xFFFFB03C;
    private static final int RED = 0xFFFF5A3C;
    private static final int WHITE = 0xFFF0F4F5;
    private static final int WELL_TOP = 0xE6132631;
    private static final int WELL_BOTTOM = 0xE60B161D;

    private final MinigameKind kind;
    private final long nonce;
    private final long seed;
    private final List<int[]> windows;
    private final List<Integer> report = new ArrayList<>();

    private int elapsed;
    private boolean reported;
    private boolean won;
    private boolean missed;
    private boolean actionKeyDown;
    private int resultTicks;
    private int holdStart = -1;

    private FishingMinigameScreen(MinigameKind kind, long nonce, long seed) {
        super(Component.translatable("nautec.fishing_minigame." + kind.name().toLowerCase(Locale.ROOT)));
        this.kind = kind;
        this.nonce = nonce;
        this.seed = seed;
        this.windows = kind.windows(seed);
    }

    public static void open(int kindId, long nonce, long seed) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen == null) {
            minecraft.setScreen(new FishingMinigameScreen(MinigameKind.byId(kindId), nonce, seed));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.minecraft.gui.extractDeferredSubtitles();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.reported) {
            this.resultTicks++;
        }

        if (this.won) {
            if (this.resultTicks >= WIN_HOLD_TICKS) {
                onClose();
            }
            return;
        }

        this.elapsed++;
        if (this.elapsed > FishingMinigame.DURATION_TICKS) {
            send();
            onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isActionKey(event)) {
            if (!this.actionKeyDown) {
                this.actionKeyDown = true;
                press();
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (isActionKey(event)) {
            this.actionKeyDown = false;
            release();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isStrikeButton(event)) {
            press();
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isStrikeButton(event)) {
            release();
        }
        return true;
    }

    private static boolean isActionKey(KeyEvent event) {
        return event.key() == 32 || event.key() == 257;
    }

    private static boolean isStrikeButton(MouseButtonEvent event) {
        return event.button() == 0;
    }

    private void press() {
        if (this.reported || this.elapsed < FishingMinigame.INPUT_GRACE_TICKS) {
            return;
        }
        switch (this.kind) {
            case TIMING_BAR -> {
                this.report.add(this.elapsed);
                finish();
            }
            case RHYTHM -> {
                this.report.add(this.elapsed);
                if (this.report.size() >= this.kind.reportSize()) {
                    finish();
                } else {
                    playUi(SoundEvents.NOTE_BLOCK_BIT.value(), 1.0F + this.report.size() * 0.25F);
                }
            }
            case HOLD -> {
                if (this.holdStart < 0) {
                    this.holdStart = this.elapsed;
                    playUi(SoundEvents.NOTE_BLOCK_BIT.value(), 0.8F);
                }
            }
        }
    }

    private void release() {
        if (this.reported || this.kind != MinigameKind.HOLD || this.holdStart < 0) {
            return;
        }
        this.report.add(this.holdStart);
        this.report.add(this.elapsed);
        finish();
    }

    private void finish() {
        int[] strikes = new int[this.report.size()];
        for (int i = 0; i < strikes.length; i++) {
            strikes[i] = this.report.get(i);
        }

        send();
        this.resultTicks = 0;
        if (this.kind.validate(this.seed, strikes)) {
            this.won = true;
            playUi(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.6F);
        } else {
            this.missed = true;
            playUi(SoundEvents.FISHING_BOBBER_RETRIEVE, 0.6F);
        }
    }

    private void send() {
        if (this.reported) {
            return;
        }
        this.reported = true;
        ClientPacketDistributor.sendToServer(new FishingMinigameResultPayload(this.nonce, List.copyOf(this.report)));
    }

    private void playUi(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        float progress = Math.min(this.elapsed + (this.won ? 0.0F : partialTick), FishingMinigame.DURATION_TICKS);
        float result = this.resultTicks + partialTick;
        boolean onTarget = !this.reported && windowAt(progress) >= 0;

        int panelX = (this.width - PANEL_W) / 2 + shake(result);
        int panelY = this.height - PANEL_H - PANEL_BOTTOM_GAP;
        int trackX = panelX + (PANEL_W - TRACK_W) / 2;
        int trackY = panelY + TRACK_TOP;

        drawPlate(guiGraphics, panelX, panelY, onTarget);
        drawHeader(guiGraphics, panelX, panelY, onTarget);
        drawTrack(guiGraphics, trackX, trackY, progress, onTarget);
        drawWindows(guiGraphics, trackX, trackY, progress);
        drawHold(guiGraphics, trackX, trackY, progress);
        drawNeedle(guiGraphics, trackX, trackY, progress, onTarget);
        drawCountdown(guiGraphics, panelX, panelY + COUNTDOWN_ROW, progress);

        if (this.won) {
            drawWinFlash(guiGraphics, trackX, trackY, result);
        }
    }

    private int shake(float result) {
        if (!this.missed || this.resultTicks >= SHAKE_TICKS) {
            return 0;
        }
        float decay = (SHAKE_TICKS - result) / SHAKE_TICKS;
        return Math.round(Mth.sin(result * 2.4F) * 3.0F * decay);
    }

    private void drawPlate(GuiGraphicsExtractor guiGraphics, int x, int y, boolean onTarget) {
        for (int row = 0; row < PANEL_H; row++) {
            int inset = 0;
            if (row < CHAMFER) {
                inset = CHAMFER - row;
            } else if (row >= PANEL_H - CHAMFER) {
                inset = row - (PANEL_H - CHAMFER) + 1;
            }
            guiGraphics.fill(x + inset, y + row, x + PANEL_W - inset, y + row + 1,
                    row == 0 || row == PANEL_H - 1 ? PLATE_EDGE : PLATE);
        }

        int trim = accent(onTarget);
        guiGraphics.fill(x + CHAMFER, y, x + PANEL_W - CHAMFER, y + 1, trim);
        guiGraphics.fill(x + CHAMFER, y + PANEL_H - 1, x + PANEL_W - CHAMFER, y + PANEL_H, ARGB.multiplyAlpha(trim, 0.5F));

        for (int i = 0; i < 6; i++) {
            guiGraphics.fill(x + CHAMFER + i, y + 1, x + CHAMFER + i + 1, y + 2, ARGB.multiplyAlpha(trim, 0.7F));
            guiGraphics.fill(x + PANEL_W - CHAMFER - i - 1, y + 1, x + PANEL_W - CHAMFER - i, y + 2, ARGB.multiplyAlpha(trim, 0.7F));
        }
    }

    private void drawHeader(GuiGraphicsExtractor guiGraphics, int x, int y, boolean onTarget) {
        int centreX = x + PANEL_W / 2;
        guiGraphics.centeredText(this.font, this.title, centreX, y + TITLE_ROW, headline());

        Component status = this.won
                ? Component.translatable("nautec.fishing_minigame.hooked")
                : this.missed
                ? Component.translatable("nautec.fishing_minigame.missed")
                : Component.translatable("nautec.fishing_minigame.prompt." + this.kind.name().toLowerCase(Locale.ROOT));
        guiGraphics.centeredText(this.font, status, centreX, y + STATUS_ROW,
                this.won ? GREEN : this.missed ? RED : onTarget ? WHITE : CYAN_DIM);
    }

    private void drawTrack(GuiGraphicsExtractor guiGraphics, int x, int y, float progress, boolean onTarget) {
        guiGraphics.fill(x - 1, y - 1, x + TRACK_W + 1, y + TRACK_H + 1, PLATE_EDGE);
        guiGraphics.fillGradient(x, y, x + TRACK_W, y + TRACK_H, WELL_TOP, WELL_BOTTOM);

        for (int i = 1; i < 12; i++) {
            int tickX = x + i * TRACK_W / 12;
            guiGraphics.fill(tickX, y + TRACK_H - 3, tickX + 1, y + TRACK_H - 1, ARGB.multiplyAlpha(CYAN_DIM, 0.6F));
        }

        drawTension(guiGraphics, x, y, progress, onTarget);
    }

    private void drawTension(GuiGraphicsExtractor guiGraphics, int x, int y, float progress, boolean onTarget) {
        float urgency = progress / FishingMinigame.DURATION_TICKS;
        float amplitude = this.missed ? 0.4F : 1.4F + urgency * 2.6F;
        int colour = this.missed
                ? ARGB.color(120, 110, 70, 70)
                : ARGB.color(onTarget ? 190 : 130, 62, 190, 210);
        int midline = y + TRACK_H / 2;

        for (int column = 0; column < TRACK_W; column += 2) {
            float phase = column * 0.17F - progress * 0.55F;
            int waveY = midline + Math.round(Mth.sin(phase) * amplitude);
            guiGraphics.fill(x + column, waveY, x + column + 2, waveY + 1, colour);
        }
    }

    private void drawWindows(GuiGraphicsExtractor guiGraphics, int x, int y, float progress) {
        float pulse = this.reported ? 1.0F : 0.75F + Mth.sin(progress * 0.4F) * 0.25F;

        for (int i = 0; i < this.windows.size(); i++) {
            int[] window = this.windows.get(i);
            int wx = x + window[0] * TRACK_W / FishingMinigame.DURATION_TICKS;
            int ww = Math.max(3, window[1] * TRACK_W / FishingMinigame.DURATION_TICKS);
            boolean struck = this.kind == MinigameKind.RHYTHM && i < this.report.size();
            int base = this.missed ? RED : struck ? CYAN : GREEN;

            guiGraphics.fill(wx, y, wx + ww, y + TRACK_H, ARGB.multiplyAlpha(base, struck ? 0.5F : 0.28F * pulse));
            guiGraphics.fill(wx, y, wx + ww, y + 1, ARGB.multiplyAlpha(base, pulse));
            guiGraphics.fill(wx, y + TRACK_H - 1, wx + ww, y + TRACK_H, ARGB.multiplyAlpha(base, pulse));
            guiGraphics.fill(wx, y, wx + 1, y + TRACK_H, base);
            guiGraphics.fill(wx + ww - 1, y, wx + ww, y + TRACK_H, base);

            if (struck) {
                drawStamp(guiGraphics, wx, y, ww, this.report.get(i));
            }
        }
    }

    private void drawStamp(GuiGraphicsExtractor guiGraphics, int wx, int y, int ww, int struckAt) {
        int age = this.elapsed - struckAt;
        int centreX = wx + ww / 2;
        guiGraphics.fill(centreX - 1, y + 3, centreX + 1, y + TRACK_H - 3, WHITE);

        if (age < POP_TICKS) {
            float spread = age / (float) POP_TICKS;
            int grow = Math.round(spread * 5.0F);
            int ring = ARGB.multiplyAlpha(WHITE, 1.0F - spread);
            guiGraphics.fill(wx - grow, y - grow, wx + ww + grow, y - grow + 1, ring);
            guiGraphics.fill(wx - grow, y + TRACK_H + grow - 1, wx + ww + grow, y + TRACK_H + grow, ring);
        }
    }

    private void drawHold(GuiGraphicsExtractor guiGraphics, int x, int y, float progress) {
        if (this.kind != MinigameKind.HOLD || this.holdStart < 0) {
            return;
        }

        int hx = x + this.holdStart * TRACK_W / FishingMinigame.DURATION_TICKS;
        int end = x + Math.round(progress * TRACK_W / FishingMinigame.DURATION_TICKS);
        int hw = Math.max(1, end - hx);
        boolean inside = windowAt(this.holdStart) >= 0 && windowAt(progress) >= 0;
        int colour = this.missed ? RED : inside ? GREEN : AMBER;

        guiGraphics.fill(hx, y + 3, hx + hw, y + TRACK_H - 3, ARGB.multiplyAlpha(colour, 0.85F));
        guiGraphics.fill(hx + hw - 1, y + 2, hx + hw, y + TRACK_H - 2, WHITE);
    }

    private void drawNeedle(GuiGraphicsExtractor guiGraphics, int x, int y, float progress, boolean onTarget) {
        int needleX = x + Math.round(progress * TRACK_W / FishingMinigame.DURATION_TICKS);
        int colour = this.missed ? RED : onTarget ? GREEN : WHITE;

        for (int i = 1; i <= 6; i++) {
            int trailX = needleX - i * 2;
            if (trailX < x) {
                break;
            }
            guiGraphics.fill(trailX, y + 2, trailX + 1, y + TRACK_H - 2, ARGB.multiplyAlpha(colour, 0.30F - i * 0.045F));
        }

        guiGraphics.fill(needleX - 2, y, needleX + 3, y + TRACK_H, ARGB.multiplyAlpha(colour, 0.25F));
        guiGraphics.fill(needleX - 1, y - 1, needleX + 1, y + TRACK_H + 1, colour);

        drawChevron(guiGraphics, needleX, y - 6, true, colour);
        drawChevron(guiGraphics, needleX, y + TRACK_H + 5, false, colour);
    }

    private static void drawChevron(GuiGraphicsExtractor guiGraphics, int centreX, int y, boolean pointingDown, int colour) {
        for (int i = 0; i < 3; i++) {
            int half = 3 - i;
            int rowY = pointingDown ? y + i : y - i;
            guiGraphics.fill(centreX - half, rowY, centreX + half, rowY + 1, colour);
        }
    }

    private void drawCountdown(GuiGraphicsExtractor guiGraphics, int x, int y, float progress) {
        int barX = x + (PANEL_W - TRACK_W) / 2;
        float left = 1.0F - progress / FishingMinigame.DURATION_TICKS;
        int width = Math.round(TRACK_W * left);
        int colour = left > 0.5F ? CYAN : left > 0.25F ? AMBER : RED;
        if (left <= 0.25F && this.elapsed % 6 < 3) {
            colour = ARGB.multiplyAlpha(colour, 0.45F);
        }

        guiGraphics.fill(barX, y, barX + TRACK_W, y + 1, ARGB.multiplyAlpha(CYAN_DIM, 0.5F));
        guiGraphics.fill(barX, y, barX + width, y + 1, colour);
    }

    private void drawWinFlash(GuiGraphicsExtractor guiGraphics, int x, int y, float result) {
        float fade = 1.0F - Math.min(1.0F, result / WIN_HOLD_TICKS);
        guiGraphics.fill(x - 1, y - 1, x + TRACK_W + 1, y + TRACK_H + 1, ARGB.color(fade * 0.6F, 0xFFFFFF));

        int spread = Math.round((1.0F - fade) * 6.0F);
        int ring = ARGB.color(fade, 0x5FE8B0);
        guiGraphics.fill(x - spread, y - spread, x + TRACK_W + spread, y - spread + 1, ring);
        guiGraphics.fill(x - spread, y + TRACK_H + spread - 1, x + TRACK_W + spread, y + TRACK_H + spread, ring);
        guiGraphics.fill(x - spread, y - spread, x - spread + 1, y + TRACK_H + spread, ring);
        guiGraphics.fill(x + TRACK_W + spread - 1, y - spread, x + TRACK_W + spread, y + TRACK_H + spread, ring);
    }

    private int accent(boolean onTarget) {
        if (this.won) {
            return GREEN;
        }
        if (this.missed) {
            return RED;
        }
        return onTarget ? GREEN : CYAN;
    }

    private int headline() {
        if (this.won) {
            return GREEN;
        }
        return this.missed ? RED : CYAN;
    }

    private int windowAt(float tick) {
        for (int i = 0; i < this.windows.size(); i++) {
            int[] window = this.windows.get(i);
            if (tick >= window[0] && tick < window[0] + window[1]) {
                return i;
            }
        }
        return -1;
    }
}
