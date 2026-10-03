package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.api.client.screen.SideConfigHost;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public final class SideConfigJeiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> screen) {
        if (screen instanceof SideConfigHost host) {
            Rect2i area = host.sideConfigArea();
            if (area != null) {
                return List.of(area);
            }
        }
        return List.of();
    }
}
