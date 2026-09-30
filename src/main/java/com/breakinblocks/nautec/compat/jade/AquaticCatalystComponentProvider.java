package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum AquaticCatalystComponentProvider implements StreamServerDataProvider<BlockAccessor, AquaticCatalystComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("aquatic_catalyst");

    public record Data(boolean active, ItemStack queued, int remainingDuration, int transferring) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Data::active,
                ItemStack.OPTIONAL_STREAM_CODEC, Data::queued,
                ByteBufCodecs.VAR_INT, Data::remainingDuration,
                ByteBufCodecs.VAR_INT, Data::transferring,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor blockAccessor) {
        AquaticCatalystBlockEntity blockEntity = (AquaticCatalystBlockEntity) blockAccessor.getBlockEntity();
        return new Data(
                blockEntity.isActive(),
                blockEntity.getProcessingItem().copy(),
                blockEntity.getRemainingDuration(),
                blockEntity.getPowerToTransfer());
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
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            AquaticCatalystComponentProvider.INSTANCE.decodeFromData(blockAccessor).ifPresent(data -> {
                if (data.active()) {
                    iTooltip.add(Component.translatable("nautec.jade.status.active"));
                    if (!data.queued().isEmpty()) {
                        iTooltip.add(Component.translatable("nautec.jade.processing",
                                data.queued().getCount(),
                                data.queued().getHoverName()));
                    }
                    iTooltip.add(Component.translatable("nautec.jade.remaining_duration", data.remainingDuration()));
                    iTooltip.add(Component.translatable("nautec.jade.transferring", data.transferring()));
                } else {
                    iTooltip.add(Component.translatable("nautec.jade.status.inactive"));
                }
            });
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
