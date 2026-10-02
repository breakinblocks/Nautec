package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.DrainPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.ConfinedSpawnerBlock;
import com.breakinblocks.nautec.content.blocks.CrystalCradleBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.controller.DrainBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalPartBlock;
import com.breakinblocks.nautec.content.blocks.MixerBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AquaticCatalystComponentProvider.INSTANCE, AquaticCatalystBlockEntity.class);
        registration.registerBlockDataProvider(ConfinedSpawnerComponentProvider.INSTANCE, ConfinedSpawnerBlockEntity.class);
        registration.registerBlockDataProvider(DrainComponentProvider.INSTANCE, DrainBlockEntity.class);
        registration.registerBlockDataProvider(CrystalCradleComponentProvider.INSTANCE, CrystalCradleBlockEntity.class);
        registration.registerBlockDataProvider(DrainComponentProvider.INSTANCE, DrainPartBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AquaticCatalystComponentProvider.Client.INSTANCE, AquaticCatalystBlock.class);
        registration.registerBlockComponent(LaserJunctionComponentProvider.INSTANCE, LaserJunctionBlock.class);
        registration.registerBlockComponent(MixerComponentProvider.INSTANCE, MixerBlock.class);
        registration.registerBlockComponent(ConfinedSpawnerComponentProvider.Client.INSTANCE, ConfinedSpawnerBlock.class);
        registration.registerBlockComponent(DrainComponentProvider.Client.INSTANCE, DrainBlock.class);
        registration.registerBlockComponent(CrystalCradleComponentProvider.Client.INSTANCE, CrystalCradleBlock.class);
        registration.registerBlockComponent(PrismarineCrystalComponentProvider.INSTANCE, PrismarineCrystalBlock.class);
        registration.registerBlockComponent(PrismarineCrystalComponentProvider.INSTANCE, PrismarineCrystalPartBlock.class);
        registration.registerBlockComponent(DrainComponentProvider.Client.INSTANCE, DrainPartBlock.class);
    }
}