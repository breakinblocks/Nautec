package com.breakinblocks.nautec.content.menus;

import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.registries.NTMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class IndustrialBioReactorMenu extends AbstractBioReactorMenu<IndustrialBioReactorBlockEntity> {
    public IndustrialBioReactorMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, (IndustrialBioReactorBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public IndustrialBioReactorMenu(int containerId, @NotNull Inventory inv, @NotNull IndustrialBioReactorBlockEntity blockEntity) {
        super(NTMenuTypes.INDUSTRIAL_BIO_REACTOR.get(), containerId, inv, blockEntity, BioReactorLayout.INDUSTRIAL);
    }
}
