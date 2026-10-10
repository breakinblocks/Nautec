package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.ResonancePylonMenu;
import com.breakinblocks.nautec.network.ResonanceActionPayload;
import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonancePylonScreen extends ResonanceNetworkScreen<ResonancePylonMenu> {
    public ResonancePylonScreen(ResonancePylonMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected int chunkState() {
        return this.menu.getChunkState();
    }

    @Override
    protected void addHeaderWidgets(int x, int y) {
        addRenderableWidget(new PanelButton(x + IMAGE_WIDTH - 72, y + 4, 64, 14,
                () -> Component.translatable(this.menu.isSendMode() ? "nautec.resonance.mode.send" : "nautec.resonance.mode.receive"),
                () -> this.menu.isSendMode() ? SEND_COLOR : RECEIVE_COLOR,
                () -> this.menu.isSendMode() ? SEND_HOVER : RECEIVE_HOVER,
                () -> Component.translatable(this.menu.isSendMode() ? "nautec.resonance.mode.send.desc" : "nautec.resonance.mode.receive.desc")
                        .append("\n").append(Component.translatable("nautec.resonance.mode.click").withStyle(ChatFormatting.GRAY)),
                () -> send(ResonanceActionPayload.MODE, null, "")));
    }

    @Override
    protected void extractReadoutBackground(GuiGraphics graphics, int rx, int ry, int rw) {
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
    }

    @Override
    protected void extractReadout(GuiGraphics graphics, int tx, int ty, ResonanceSyncPayload.@Nullable NetworkView view) {
        graphics.drawString(this.font, Component.translatable("nautec.resonance.flow", number(this.menu.getFlow())), tx, ty, READOUT, false);
        graphics.drawString(this.font, Component.translatable("nautec.resonance.buffer", compact(this.menu.getEnergy()), compact(this.menu.getCapacity())),
                tx, ty + 11, READOUT_DIM, false);
        Component tier = Component.translatable(this.menu.isInterdimensional() ? "nautec.resonance.tier.abyssal" : "nautec.resonance.tier.basic");
        graphics.drawString(this.font, tier, IMAGE_WIDTH - 13 - this.font.width(tier), ty, READOUT_DIM, false);
        if (view != null) {
            Component pylons = Component.translatable("nautec.resonance.pylons", view.pylons());
            graphics.drawString(this.font, pylons, IMAGE_WIDTH - 13 - this.font.width(pylons), ty + 11, READOUT_DIM, false);
        }
    }

    @Override
    protected boolean readoutTooltip(List<Component> lines, int mouseX, int mouseY, int rx, int ry) {
        if (inside(mouseX, mouseY, rx, ry, IMAGE_WIDTH - 16, 12)) {
            lines.add(Component.translatable("nautec.resonance.buffer", number(this.menu.getEnergy()), number(this.menu.getCapacity())));
            lines.add(Component.translatable("nautec.resonance.buffer.desc").withStyle(ChatFormatting.GRAY));
            return true;
        }
        if (inside(mouseX, mouseY, rx, ry + 12, IMAGE_WIDTH - 16, READOUT_HEIGHT - 12)) {
            lines.add(Component.translatable("nautec.resonance.flow", number(this.menu.getFlow())));
            int throughput = this.menu.isInterdimensional() ? NTConfig.abyssalPylonThroughput : NTConfig.resonancePylonThroughput;
            lines.add(Component.translatable("nautec.resonance.flow.desc", number(throughput)).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(this.menu.isInterdimensional() ? "nautec.resonance.tier.abyssal.desc" : "nautec.resonance.tier.basic.desc",
                    Math.round(NTConfig.resonanceSameDimensionLoss * 100), Math.round(NTConfig.resonanceCrossDimensionLoss * 100))
                    .withStyle(ChatFormatting.GRAY));
            return true;
        }
        return false;
    }
}
