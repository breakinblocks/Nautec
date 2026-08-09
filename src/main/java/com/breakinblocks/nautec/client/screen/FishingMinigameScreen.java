package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.network.FishingMinigameResultPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class FishingMinigameScreen extends Screen {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 9;

    private final MinigameKind kind;
    private final long nonce;
    private final long seed;
    private final List<Integer> report = new ArrayList<>();

    private int elapsed;
    private boolean reported;
    private int holdStart = -1;

    private FishingMinigameScreen(MinigameKind kind, long nonce, long seed) {
        super(Component.translatable("nautec.fishing_minigame." + kind.name().toLowerCase(java.util.Locale.ROOT)));
        this.kind = kind;
        this.nonce = nonce;
        this.seed = seed;
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
    public void tick() {
        super.tick();
        this.elapsed++;
        if (this.elapsed > FishingMinigame.DURATION_TICKS) {
            send();
            onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isActionKey(event)) {
            press();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (isActionKey(event)) {
            release();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        press();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        release();
        return true;
    }

    private static boolean isActionKey(KeyEvent event) {
        return event.key() == 32 || event.key() == 257;
    }

    private void press() {
        if (this.reported) {
            return;
        }
        switch (this.kind) {
            case TIMING_BAR -> {
                this.report.add(this.elapsed);
                send();
                onClose();
            }
            case RHYTHM -> {
                this.report.add(this.elapsed);
                if (this.report.size() >= this.kind.reportSize()) {
                    send();
                    onClose();
                }
            }
            case HOLD -> {
                if (this.holdStart < 0) {
                    this.holdStart = this.elapsed;
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
        send();
        onClose();
    }

    private void send() {
        if (this.reported) {
            return;
        }
        this.reported = true;
        ClientPacketDistributor.sendToServer(new FishingMinigameResultPayload(this.nonce, List.copyOf(this.report)));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - BAR_WIDTH) / 2;
        int y = this.height - 70;

        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, ARGB.color(200, 10, 14, 18));
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, ARGB.color(220, 30, 48, 58));

        List<int[]> windows = this.kind.windows(this.seed);
        for (int i = 0; i < windows.size(); i++) {
            int[] window = windows.get(i);
            int wx = x + window[0] * BAR_WIDTH / FishingMinigame.DURATION_TICKS;
            int ww = Math.max(2, window[1] * BAR_WIDTH / FishingMinigame.DURATION_TICKS);
            boolean done = this.kind == MinigameKind.RHYTHM && i < this.report.size();
            guiGraphics.fill(wx, y, wx + ww, y + BAR_HEIGHT,
                    done ? ARGB.color(220, 60, 120, 96) : ARGB.color(230, 95, 232, 176));
        }

        if (this.kind == MinigameKind.HOLD && this.holdStart >= 0) {
            int hx = x + this.holdStart * BAR_WIDTH / FishingMinigame.DURATION_TICKS;
            int hw = Math.max(1, (this.elapsed - this.holdStart) * BAR_WIDTH / FishingMinigame.DURATION_TICKS);
            guiGraphics.fill(hx, y + 2, hx + hw, y + BAR_HEIGHT - 2, ARGB.color(200, 255, 226, 120));
        }

        float progress = Math.min(this.elapsed + partialTick, FishingMinigame.DURATION_TICKS);
        int markerX = x + (int) (progress * BAR_WIDTH / FishingMinigame.DURATION_TICKS);
        guiGraphics.fill(markerX - 1, y - 3, markerX + 1, y + BAR_HEIGHT + 3, ARGB.color(255, 240, 246, 250));

        Component prompt = Component.translatable("nautec.fishing_minigame.prompt." + this.kind.name().toLowerCase(java.util.Locale.ROOT));
        guiGraphics.text(this.font, prompt,
                (this.width - this.font.width(prompt)) / 2, y - 16, ARGB.opaque(0xE8F4F0), true);
    }
}
