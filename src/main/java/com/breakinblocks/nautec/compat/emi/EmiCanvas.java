package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.client.GuiUtils;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class EmiCanvas {
    public static final int PAD = 1;
    public static final int GRAY = 0xFF808080;
    public static final int DARK = 0xFF404040;
    public static final int WHITE = 0xFFFFFFFF;
    private static final ResourceLocation BACTERIA_SLOT_SPRITE = Nautec.rl("container/bacteria_slot");

    private final WidgetHolder widgets;
    private final EmiRecipe recipe;

    public EmiCanvas(WidgetHolder widgets, EmiRecipe recipe) {
        this.widgets = widgets;
        this.recipe = recipe;
    }

    public static int size(int layout) {
        return layout + 2 * PAD;
    }

    public static int lineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public SlotWidget slot(EmiIngredient ingredient, int x, int y) {
        return widgets.addSlot(ingredient, x - 1 + PAD, y - 1 + PAD);
    }

    public <T extends Widget> T add(T widget) {
        return widgets.add(widget);
    }

    public SlotWidget output(EmiStack stack, int x, int y) {
        return slot(stack, x, y).recipeContext(recipe);
    }

    public TankWidget tank(EmiIngredient ingredient, int x, int y, int width, int height, long capacity) {
        return widgets.add(new TankWidget(ingredient, x - 1 + PAD, y - 1 + PAD, width + 2, height + 2, Math.max(1L, capacity)));
    }

    public void sprite(ResourceLocation sprite, int x, int y, int width, int height) {
        widgets.addDrawable(x + PAD, y + PAD, width, height,
                (graphics, mouseX, mouseY, delta) -> NTGui.blitSprite(graphics, sprite, 0, 0, width, height));
    }

    public void fill(int x0, int y0, int x1, int y1, int color) {
        widgets.addDrawable(x0 + PAD, y0 + PAD, x1 - x0, y1 - y0,
                (graphics, mouseX, mouseY, delta) -> graphics.fill(0, 0, x1 - x0, y1 - y0, color));
    }

    public void arrow(int x, int y, int ticks) {
        widgets.addFillingArrow(x + PAD, y + PAD, Math.max(1, ticks) * 50);
    }

    public void text(Component text, int x, int y, int width, Align align, int color, boolean shadow) {
        text(text, x, y, width, lineHeight(), align, color, shadow);
    }

    public void text(Component text, int x, int y, int width, int height, Align align, int color, boolean shadow) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = height < font.lineHeight * 2 ? List.of(text.getVisualOrderText()) : font.split(text, Math.max(1, width));
        int count = Math.min(lines.size(), Math.max(1, height / font.lineHeight));
        for (int i = 0; i < count; i++) {
            FormattedCharSequence line = lines.get(i);
            int offset = switch (align) {
                case LEFT -> 0;
                case CENTER -> (width - font.width(line)) / 2;
                case RIGHT -> width - font.width(line);
            };
            widgets.addText(line, x + PAD + offset, y + PAD + i * font.lineHeight, color, shadow);
        }
    }

    public void bacteria(ResourceKey<Bacteria> bacteria, int x, int y) {
        BacteriaInstance instance = BacteriaInstance.withMaxStats(bacteria, Minecraft.getInstance().level.registryAccess());
        widgets.addDrawable(x + PAD, y + PAD, 18, 18, (graphics, mouseX, mouseY, delta) -> {
            NTGui.blitSprite(graphics, BACTERIA_SLOT_SPRITE, 0, 0, 18, 18);
            GuiUtils.renderBacteria(graphics, instance, 0, 0);
        }).tooltipText(instance.getTooltip());
    }

    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }
}
