package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.ThermalVentTapBlockEntity;
import com.breakinblocks.nautec.content.blockentities.generators.TidalRotorBlockEntity;
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

public enum GeneratorComponentProvider implements StreamServerDataProvider<BlockAccessor, GeneratorComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("fe_generator");

    public static final int ROTOR = 0;
    public static final int TAP = 1;
    public static final int DYNAMO = 2;

    public record Data(int kind, int status, int output, int first, int second) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::kind,
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.VAR_INT, Data::output,
                ByteBufCodecs.VAR_INT, Data::first,
                ByteBufCodecs.VAR_INT, Data::second,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof TidalRotorBlockEntity rotor) {
            return new Data(ROTOR, rotor.getStatus().ordinal(), rotor.getRate(), Math.round(rotor.getOpenWater() * 100), rotor.getDepth());
        }
        if (accessor.getBlockEntity() instanceof CombustionDynamoBlockEntity dynamo) {
            return new Data(DYNAMO, dynamo.getStatus().ordinal(), dynamo.getRate(), dynamo.getFluidTank().getFluidAmount(),
                    dynamo.getSecondaryFluidTank().getFluidAmount());
        }
        ThermalVentTapBlockEntity tap = (ThermalVentTapBlockEntity) accessor.getBlockEntity();
        return new Data(TAP, tap.getStatus().ordinal(), tap.rate(), tap.getHeat(), tap.getFuelTank().getFluidAmount());
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
            GeneratorComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                if (data.kind() == DYNAMO) {
                    CombustionDynamoBlockEntity.Status status = CombustionDynamoBlockEntity.Status.byId(data.status());
                    tooltip.add(Component.translatable(status.translationKey())
                            .withStyle(status == CombustionDynamoBlockEntity.Status.RUNNING ? ChatFormatting.GREEN : ChatFormatting.RED));
                    if (data.output() > 0) {
                        tooltip.add(Component.translatable("nautec.jade.generator.output", data.output()));
                    }
                    tooltip.add(Component.translatable("nautec.jade.combustion_dynamo.oil", data.first(), NTConfig.combustionDynamoTankCapacity));
                    tooltip.add(Component.translatable("nautec.jade.combustion_dynamo.water", data.second(), NTConfig.combustionDynamoTankCapacity));
                    return;
                }
                if (data.kind() == ROTOR) {
                    TidalRotorBlockEntity.Status status = TidalRotorBlockEntity.Status.byId(data.status());
                    tooltip.add(Component.translatable(status.translationKey())
                            .withStyle(status == TidalRotorBlockEntity.Status.RUNNING ? ChatFormatting.GREEN : ChatFormatting.RED));
                    if (data.output() > 0) {
                        tooltip.add(Component.translatable("nautec.jade.generator.output", data.output()));
                        tooltip.add(Component.translatable("nautec.jade.tidal_rotor.water", data.first(), data.second(), TidalRotorBlockEntity.MAX_DEPTH));
                    }
                    return;
                }
                ThermalVentTapBlockEntity.Status status = ThermalVentTapBlockEntity.Status.byId(data.status());
                tooltip.add(Component.translatable(status.translationKey())
                        .withStyle(status == ThermalVentTapBlockEntity.Status.RUNNING ? ChatFormatting.GREEN : ChatFormatting.RED));
                if (data.output() > 0) {
                    tooltip.add(Component.translatable("nautec.jade.generator.output", data.output()));
                }
                tooltip.add(Component.translatable("nautec.jade.vent_tap.heat", data.first(), ThermalVentTapBlockEntity.HEAT_SPOTS));
                tooltip.add(Component.translatable("nautec.jade.vent_tap.fuel", data.second(), ThermalVentTapBlockEntity.FUEL_CAPACITY));
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
