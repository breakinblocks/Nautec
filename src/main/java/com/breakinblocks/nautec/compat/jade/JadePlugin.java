package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blocks.AquaticCatalystBlock;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
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
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AquaticCatalystComponentProvider.Client.INSTANCE, AquaticCatalystBlock.class);
        registration.registerBlockComponent(LaserJunctionComponentProvider.INSTANCE, LaserJunctionBlock.class);
        registration.registerBlockComponent(MixerComponentProvider.INSTANCE, MixerBlock.class);
    }
}