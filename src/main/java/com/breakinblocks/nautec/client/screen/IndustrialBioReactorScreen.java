package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class IndustrialBioReactorScreen extends AbstractBioReactorScreen<IndustrialBioReactorBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/industrial_bio_reactor.png");

    public IndustrialBioReactorScreen(NTMachineMenu<IndustrialBioReactorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BioReactorLayout.INDUSTRIAL, TEXTURE);
    }

    @Override
    protected boolean drawInputFrames() {
        return false;
    }
}
