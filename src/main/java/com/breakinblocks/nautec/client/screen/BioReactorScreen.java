package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class BioReactorScreen extends AbstractBioReactorScreen<BioReactorBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/bio_reactor.png");

    public BioReactorScreen(NTMachineMenu<BioReactorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BioReactorLayout.BIO_REACTOR, TEXTURE);
    }

    @Override
    protected boolean drawInputFrames() {
        return false;
    }
}
