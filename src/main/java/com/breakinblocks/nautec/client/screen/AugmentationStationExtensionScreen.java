package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.screen.NTAbstractContainerScreen;
import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.AugmentationStationExtensionBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class AugmentationStationExtensionScreen extends NTAbstractContainerScreen<AugmentationStationExtensionBlockEntity> {
    public static final Identifier TEXTURE = Nautec.rl("textures/gui/augment_station_extension.png");

    public AugmentationStationExtensionScreen(NTAbstractContainerMenu<AugmentationStationExtensionBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.titleLabelY = 4;
    }

    @Override
    public @NotNull Identifier getBackgroundTexture() {
        return TEXTURE;
    }
}
