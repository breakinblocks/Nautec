package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.FluidTankRenderer;
import com.breakinblocks.nautec.content.menus.ResonantCisternMenu;
import com.breakinblocks.nautec.content.menus.ResonantStorageMenu;
import com.breakinblocks.nautec.content.resonantstorage.ResonantCisternBlockEntity;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class ResonantCisternScreen extends ResonantStorageScreen<ResonantCisternMenu> {
    private static final int GAUGE_X = 56;
    private static final int GAUGE_Y = CONTENT_Y;
    private static final int GAUGE_WIDTH = 64;
    private static final int GAUGE_HEIGHT = 72;
    private static final ItemStack EXPANSION_GHOST = new ItemStack(NTItems.RESONANT_EXPANSION.get());
    private FluidTankRenderer gauge;
    private int gaugeCapacity;

    public ResonantCisternScreen(ResonantCisternMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private ResonantCisternBlockEntity cistern() {
        return this.menu.blockEntity;
    }

    private FluidTankRenderer gauge() {
        int capacity = Math.max(1, cistern().capacity());
        if (gauge == null || capacity != gaugeCapacity) {
            gaugeCapacity = capacity;
            gauge = new FluidTankRenderer(capacity, true, GAUGE_WIDTH - 2, GAUGE_HEIGHT - 2);
        }
        return gauge;
    }

    @Override
    protected void extractStorage(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = this.leftPos + GAUGE_X;
        int y = this.topPos + GAUGE_Y;
        PanelStyle.tank(graphics, x, y, GAUGE_WIDTH, GAUGE_HEIGHT);
        gauge().render(graphics, x + 1, y + 1, cistern().fluid());
        PanelStyle.tankTicks(graphics, x, y, GAUGE_WIDTH, GAUGE_HEIGHT);
        int ux = this.leftPos + ResonantStorageMenu.UPGRADE_X;
        int uy = this.topPos + ResonantStorageMenu.UPGRADE_Y;
        PanelStyle.slot(graphics, ux, uy);
        if (cistern().upgrades() == 0 && this.menu.getSlot(0).getItem().isEmpty()) {
            graphics.item(EXPANSION_GHOST, ux, uy);
            graphics.nextStratum();
            graphics.fill(ux, uy, ux + 16, uy + 16, PanelStyle.GHOST_FADE);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (tab == Tab.STORAGE && PanelStyle.inside(mouseX, mouseY, this.leftPos + GAUGE_X, this.topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            FluidStack fluid = cistern().fluid();
            graphics.setComponentTooltipForNextFrame(this.font, gauge().getTooltip(fluid), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (tab == Tab.STORAGE && !this.menu.getCarried().isEmpty()
                && PanelStyle.inside(event.x(), event.y(), this.leftPos + GAUGE_X, this.topPos + GAUGE_Y, GAUGE_WIDTH, GAUGE_HEIGHT)) {
            press(ResonantCisternMenu.GAUGE_BUTTON);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
