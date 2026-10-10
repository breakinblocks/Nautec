package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.content.menus.ResonantStorageMenu;
import com.breakinblocks.nautec.content.menus.ResonantVaultMenu;
import com.breakinblocks.nautec.content.resonantstorage.ResonantStore;
import com.breakinblocks.nautec.content.resonantstorage.VaultStore;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ResonantVaultScreen extends ResonantStorageScreen<ResonantVaultMenu> {
    private static final int PAGE_Y = ResonantVaultMenu.GRID_Y + 3 * 18 + 3;
    private static final int PAGE_WIDTH = 18;
    private static final ItemStack EXPANSION_GHOST = new ItemStack(NTItems.RESONANT_EXPANSION.get());

    public ResonantVaultScreen(ResonantVaultMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void initStorage() {
        for (int page = 0; page <= ResonantStore.MAX_UPGRADES; page++) {
            int index = page;
            NTPanelButton button = new NTPanelButton(this.font, this.leftPos + 8 + page * (PAGE_WIDTH + 2), this.topPos + PAGE_Y, PAGE_WIDTH, 13,
                    () -> Component.literal(String.valueOf(index + 1)),
                    () -> this.menu.page() == index ? PanelStyle.RECEIVE_COLOR : PanelStyle.NEUTRAL,
                    () -> this.menu.page() == index ? PanelStyle.RECEIVE_HOVER : PanelStyle.NEUTRAL_HOVER,
                    () -> index < this.menu.pages()
                            ? Component.translatable("nautec.resonant_storage.page", index + 1)
                            : Component.translatable("nautec.resonant_storage.page_locked"),
                    () -> {
                        if (index < this.menu.pages()) {
                            this.menu.selectPage(index);
                            press(index);
                        }
                    });
            storageWidgets.add(addRenderableWidget(button));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int page = 0; page < storageWidgets.size(); page++) {
            storageWidgets.get(page).active = page < this.menu.pages();
        }
        if (this.menu.page() >= this.menu.pages()) {
            this.menu.selectPage(0);
        }
    }

    @Override
    protected void extractStorage(GuiGraphics graphics, int mouseX, int mouseY) {
        VaultStore vault = this.menu.vault();
        int used = 0;
        int capacity = vault != null ? vault.capacity() : VaultStore.PAGE;
        if (vault != null) {
            for (int slot = 0; slot < capacity; slot++) {
                if (!vault.stackAt(slot).isEmpty()) {
                    used++;
                }
            }
        }
        int x = this.leftPos + ResonantStorageMenu.UPGRADE_X;
        int y = this.topPos + ResonantStorageMenu.UPGRADE_Y;
        PanelStyle.slot(graphics, x, y);
        if (vault != null && vault.upgrades() == 0) {
            graphics.renderItem(EXPANSION_GHOST, x, y);
            NTGui.pushOverItems(graphics);
            graphics.fill(x, y, x + 16, y + 16, PanelStyle.GHOST_FADE);
            NTGui.popOverItems(graphics);
        }
        Component readout = Component.translatable("nautec.resonant_storage.slots_used", used, capacity);
        int readoutX = this.leftPos + WIDTH - 8 - this.font.width(readout);
        graphics.drawString(this.font, readout, readoutX, this.topPos + PAGE_Y + 3, PanelStyle.LABEL, false);
    }
}
