package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class IndustrialBioReactorScreen extends AbstractBioReactorScreen<IndustrialBioReactorBlockEntity> {
    public IndustrialBioReactorScreen(NTMachineMenu<IndustrialBioReactorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BioReactorLayout.INDUSTRIAL);
    }
}
