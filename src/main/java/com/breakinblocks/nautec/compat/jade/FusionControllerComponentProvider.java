package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionControllerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.fusion.FusionStructure;
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

import java.util.Locale;

public enum FusionControllerComponentProvider implements StreamServerDataProvider<BlockAccessor, FusionControllerComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("fusion_controller");

    public record Data(int status, int problem, int heat, int output, int fuel) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.VAR_INT, Data::problem,
                ByteBufCodecs.VAR_INT, Data::heat,
                ByteBufCodecs.VAR_INT, Data::output,
                ByteBufCodecs.VAR_INT, Data::fuel,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        FusionControllerBlockEntity controller = (FusionControllerBlockEntity) accessor.getBlockEntity();
        return new Data(controller.getStatus().ordinal(), controller.getStructure().problem().ordinal(), controller.heatPermille(),
                controller.getOutput(), controller.getFuelTank().getFluidAmount());
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
            FusionControllerComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                FusionControllerBlockEntity.Status status = FusionControllerBlockEntity.Status.byId(data.status());
                ChatFormatting color = status.running() ? ChatFormatting.GREEN
                        : status == FusionControllerBlockEntity.Status.IGNITING ? ChatFormatting.GOLD : ChatFormatting.RED;
                tooltip.add(Component.translatable(status.translationKey()).withStyle(color));
                if (status == FusionControllerBlockEntity.Status.INCOMPLETE) {
                    tooltip.add(Component.translatable(FusionStructure.Problem.byId(data.problem()).translationKey()).withStyle(ChatFormatting.YELLOW));
                    return;
                }
                tooltip.add(Component.translatable("nautec.jade.fusion.heat", data.heat() / 10));
                tooltip.add(Component.translatable("nautec.jade.fusion.output", String.format(Locale.ROOT, "%,d", data.output())));
                tooltip.add(Component.translatable("nautec.jade.fusion.fuel", String.format(Locale.ROOT, "%,d", data.fuel())));
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
