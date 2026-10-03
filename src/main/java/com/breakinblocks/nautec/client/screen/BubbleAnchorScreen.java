package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlock;
import com.breakinblocks.nautec.content.bubble.BubbleAnchorBlockEntity;
import com.breakinblocks.nautec.content.menus.BubbleAnchorMenu;
import com.breakinblocks.nautec.network.BubbleAnchorTogglePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class BubbleAnchorScreen extends NTMachineScreen<BubbleAnchorBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/bubble_anchor.png");
    private static final int BAR_X = 46;
    private static final int BAR_Y = 36;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 32;
    private static final int OUTLINE = 0xFF1E2221;
    private static final int BAR_EMPTY = 0xFF45504A;
    private static final int FUEL_FILL = 0xFF5FE8B0;
    private static final int LASER_FILL = 0xFF4CCBE0;
    private static final int RUNNING = 0xFF2E7D4F;
    private static final int STOPPED = 0xFF8C2F2F;

    private Button enabled;
    private Button fill;
    private Button above;

    public BubbleAnchorScreen(NTMachineMenu<BubbleAnchorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private BubbleAnchorMenu anchor() {
        return (BubbleAnchorMenu) this.menu;
    }

    @Override
    protected void init() {
        super.init();
        enabled = addRenderableWidget(toggle(60, 18, BubbleAnchorTogglePayload.ENABLED));
        fill = addRenderableWidget(toggle(60, 38, BubbleAnchorTogglePayload.FILL));
        above = addRenderableWidget(toggle(60, 58, BubbleAnchorTogglePayload.ABOVE));
    }

    private Button toggle(int x, int y, int which) {
        return Button.builder(Component.empty(), button -> ClientPacketDistributor.sendToServer(new BubbleAnchorTogglePayload(this.menu.containerId, which)))
                .bounds(leftPos + x, topPos + y, 108, 16).build();
    }

    private static void label(Button button, Component text, Component tooltip) {
        button.setMessage(text);
        button.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        BubbleAnchorMenu anchor = anchor();
        label(enabled, Component.translatable(anchor.isEnabled() ? "nautec.bubble_anchor.button.on" : "nautec.bubble_anchor.button.off"),
                Component.translatable("nautec.bubble_anchor.button.enabled.desc"));
        label(fill, Component.translatable(anchor.fillsWater() ? "nautec.bubble_anchor.button.water" : "nautec.bubble_anchor.button.air"),
                Component.translatable(anchor.fillsWater() ? "nautec.bubble_anchor.button.water.desc" : "nautec.bubble_anchor.button.air.desc"));
        label(above, Component.translatable(anchor.isAbove() ? "nautec.bubble_anchor.button.above" : "nautec.bubble_anchor.button.centred"),
                Component.translatable("nautec.bubble_anchor.button.position.desc"));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        extractSlotFrame(graphics, 26, 52);
        int x = leftPos + BAR_X;
        int y = topPos + BAR_Y;
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, OUTLINE);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_EMPTY);
        BubbleAnchorMenu anchor = anchor();
        int filled;
        int color;
        if (anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_LASER) {
            filled = BAR_HEIGHT;
            color = LASER_FILL;
        } else {
            filled = Math.round(BAR_HEIGHT * Math.min(1F, anchor.getBurn() / (float) Math.max(1, anchor.getBurnTotal())));
            color = FUEL_FILL;
        }
        if (filled > 0) {
            graphics.fill(x, y + BAR_HEIGHT - filled, x + BAR_WIDTH, y + BAR_HEIGHT, color);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        int status = anchor().getStatus();
        boolean running = status == BubbleAnchorBlockEntity.STATUS_FUEL || status == BubbleAnchorBlockEntity.STATUS_LASER;
        Component text = Component.translatable(BubbleAnchorBlock.statusKey(status));
        graphics.text(this.font, text, this.imageWidth - 8 - this.font.width(text), this.titleLabelY, running ? RUNNING : STOPPED, false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + BAR_X - 1 && mouseX < leftPos + BAR_X + BAR_WIDTH + 1 && mouseY >= topPos + BAR_Y - 1 && mouseY < topPos + BAR_Y + BAR_HEIGHT + 1) {
            BubbleAnchorMenu anchor = anchor();
            int size = anchor.getRadius() * 2 + 1;
            Supplier<Component> fuelLine = () -> anchor.getStatus() == BubbleAnchorBlockEntity.STATUS_LASER
                    ? Component.translatable("nautec.bubble_anchor.status.laser")
                    : Component.translatable("nautec.bubble_anchor.time", anchor.getBurn() / 20);
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    fuelLine.get(),
                    Component.translatable("nautec.bubble_anchor.size", size, size, size).withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.bubble_anchor.fuel.desc").withStyle(ChatFormatting.GRAY),
                    Component.translatable("nautec.bubble_anchor.laser.desc", NTConfig.bubbleAnchorLaserPower).withStyle(ChatFormatting.DARK_GRAY)
            ), mouseX, mouseY);
        }
    }

    @Override
    public @NotNull Identifier getBackgroundTexture() {
        return TEXTURE;
    }
}
