package com.breakinblocks.nautec.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class NTPanelButton extends AbstractButton {
    private final Font font;
    private final Supplier<Component> label;
    private final Supplier<Integer> color;
    private final Supplier<Integer> hover;
    private final Supplier<Component> tooltip;
    private final Runnable action;
    private @Nullable Component lastTooltip;

    public NTPanelButton(Font font, int x, int y, int width, int height, Supplier<Component> label, Supplier<Integer> color,
                         Supplier<Integer> hover, Supplier<Component> tooltip, Runnable action) {
        super(x, y, width, height, label.get());
        this.font = font;
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
        graphics.fill(x, y, x + getWidth(), y + getHeight(), PanelStyle.OUTLINE);
        int fill = !this.active ? PanelStyle.SLOT_EDGE : isHoveredOrFocused() ? hover.get() : color.get();
        graphics.fill(x + 1, y + 1, x + getWidth() - 1, y + getHeight() - 1, fill);
        Component message = getMessage();
        graphics.text(this.font, message, x + (getWidth() - this.font.width(message)) / 2, y + (getHeight() - 8) / 2,
                this.active ? 0xFFFFFFFF : 0xFF808080, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
