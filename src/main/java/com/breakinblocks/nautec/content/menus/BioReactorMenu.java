package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class BioReactorMenu extends AbstractBioReactorMenu<BioReactorBlockEntity> {
    public BioReactorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (BioReactorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BioReactorMenu(int containerId, @NotNull Inventory inv, @NotNull BioReactorBlockEntity blockEntity) {
        super(NTMenuTypes.BIO_REACTOR.get(), containerId, inv, blockEntity, BioReactorLayout.BIO_REACTOR);
    }
}
