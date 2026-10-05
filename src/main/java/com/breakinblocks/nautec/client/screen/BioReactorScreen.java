package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BioReactorScreen extends AbstractBioReactorScreen<BioReactorBlockEntity> {
    public BioReactorScreen(NTMachineMenu<BioReactorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BioReactorLayout.BIO_REACTOR);
    }
}
