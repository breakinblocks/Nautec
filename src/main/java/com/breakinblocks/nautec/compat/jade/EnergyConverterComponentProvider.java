package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;
import com.breakinblocks.nautec.content.blocks.EnergyConverterBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum EnergyConverterComponentProvider implements StreamServerDataProvider<BlockAccessor, EnergyConverterComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("energy_converter");

    public record Data(int sending, int beams, int fe, int rate, int capacity) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::sending,
                ByteBufCodecs.VAR_INT, Data::beams,
                ByteBufCodecs.VAR_INT, Data::fe,
                ByteBufCodecs.VAR_INT, Data::rate,
                ByteBufCodecs.VAR_INT, Data::capacity,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        EnergyConverterBlockEntity converter = (EnergyConverterBlockEntity) accessor.getBlockEntity();
        return new Data(converter.getSending(), converter.getBeams(), converter.getFeStored(), converter.getRate(), EnergyConverterBlockEntity.maxFe());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    public enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            EnergyConverterComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                EnergyConverterBlock.lines(data.sending(), data.beams(), data.rate()).forEach(tooltip::add);
                tooltip.add(Component.translatable("nautec.energy_converter.fe", String.format("%,d", data.fe()),
                        String.format("%,d", data.capacity())).withStyle(ChatFormatting.GRAY));
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
