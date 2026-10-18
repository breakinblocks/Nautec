package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.OsmosisFilterBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum OsmosisFilterComponentProvider implements StreamServerDataProvider<BlockAccessor, OsmosisFilterComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("osmosis_filter");

    public record Data(int water, int limit, long rate) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::water,
                ByteBufCodecs.VAR_INT, Data::limit,
                ByteBufCodecs.VAR_LONG, Data::rate,
                Data::new
        );
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof OsmosisFilterBlockEntity filter) {
            return new Data(filter.getWaterAmount(), filter.getFillLimit(), Math.round(filter.getRate()));
        }
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            OsmosisFilterComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                boolean full = data.water() >= data.limit();
                tooltip.add(Component.translatable("nautec.jade.osmosis_filter.water",
                        String.format("%,d", data.water()), String.format("%,d", data.limit())).withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.translatable(full ? "nautec.jade.osmosis_filter.full" : "nautec.jade.osmosis_filter.rate",
                        String.format("%,d", data.rate())).withStyle(full ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
            });
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
