package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.DrainPartBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

public enum DrainComponentProvider implements StreamServerDataProvider<BlockAccessor, DrainComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("drain");

    public record Data(int status, int power, int blocker) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.VAR_INT, Data::power,
                ByteBufCodecs.VAR_INT, Data::blocker,
                Data::new
        );
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        DrainBlockEntity drain = controller(accessor);
        if (drain == null) {
            return null;
        }
        DrainBlockEntity.Status blocker = drain.isFormed() ? drain.pumpBlocker() : null;
        return new Data(drain.getStatus().ordinal(), drain.getPower(), blocker != null ? blocker.ordinal() : -1);
    }

    private static @Nullable DrainBlockEntity controller(BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof DrainBlockEntity drain) {
            return drain;
        }
        if (blockEntity instanceof DrainPartBlockEntity part) {
            BlockPos controllerPos = part.getActualBlockEntityPos();
            if (controllerPos != null && accessor.getLevel().getBlockEntity(controllerPos) instanceof DrainBlockEntity drain) {
                return drain;
            }
        }
        return null;
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
            DrainComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                DrainBlockEntity.Status status = DrainBlockEntity.Status.byId(data.status());
                tooltip.add(Component.translatable(status.translationKey()).withStyle(status.isGood() ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                if (!status.isGood()) {
                    tooltip.add(Component.translatable(status.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
                }
                if (data.blocker() >= 0 && (status == DrainBlockEntity.Status.CLOSED || status == DrainBlockEntity.Status.OPENING)) {
                    DrainBlockEntity.Status blocker = DrainBlockEntity.Status.byId(data.blocker());
                    tooltip.add(Component.translatable("nautec.jade.drain.blocked", Component.translatable(blocker.translationKey()))
                            .withStyle(ChatFormatting.RED));
                    tooltip.add(Component.translatable(blocker.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
                }
                tooltip.add(Component.translatable("nautec.jade.drain.power", data.power(), NTConfig.drainPower)
                        .withStyle(data.power() > NTConfig.drainPower ? ChatFormatting.WHITE : ChatFormatting.RED));
                if (data.power() > NTConfig.drainPower) {
                    String rate = status == DrainBlockEntity.Status.PUMPING ? "nautec.jade.drain.rate" : "nautec.jade.drain.rate_idle";
                    tooltip.add(Component.translatable(rate, DrainBlockEntity.saltWaterPerSecond(data.power()))
                            .withStyle(ChatFormatting.AQUA));
                }
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
