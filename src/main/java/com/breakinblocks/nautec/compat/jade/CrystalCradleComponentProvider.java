package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.CrystalCradleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.text.NumberFormat;
import java.util.Locale;

public enum CrystalCradleComponentProvider implements StreamServerDataProvider<BlockAccessor, CrystalCradleComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("crystal_cradle");

    public record Data(int status, long growth, long target, int power, float purity, float requiredPurity) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.VAR_LONG, Data::growth,
                ByteBufCodecs.VAR_LONG, Data::target,
                ByteBufCodecs.VAR_INT, Data::power,
                ByteBufCodecs.FLOAT, Data::purity,
                ByteBufCodecs.FLOAT, Data::requiredPurity,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        CrystalCradleBlockEntity cradle = (CrystalCradleBlockEntity) accessor.getBlockEntity();
        return new Data(cradle.getStatus().ordinal(), cradle.getGrowth(), NTConfig.crystalGrowthPower, cradle.getPower(),
                cradle.getPurity(), (float) NTConfig.crystalGrowthPurity);
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
            CrystalCradleComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                CrystalCradleBlockEntity.Status status = CrystalCradleBlockEntity.Status.byId(data.status());
                boolean growing = status == CrystalCradleBlockEntity.Status.GROWING;
                tooltip.add(Component.translatable(status.translationKey()).withStyle(growing ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                if (!growing) {
                    tooltip.add(Component.translatable(status.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
                }
                if (status == CrystalCradleBlockEntity.Status.EMPTY) {
                    return;
                }
                NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
                long target = Math.max(1, data.target());
                tooltip.add(Component.translatable("nautec.jade.crystal_cradle.growth",
                        String.format(Locale.ROOT, "%.1f", data.growth() * 100.0 / target), format.format(data.growth()), format.format(target)));
                tooltip.add(Component.translatable("nautec.jade.crystal_cradle.beam", data.power(),
                                String.format(Locale.ROOT, "%.2f", data.purity()), String.format(Locale.ROOT, "%.1f", data.requiredPurity()))
                        .withStyle(data.purity() >= data.requiredPurity() ? ChatFormatting.WHITE : ChatFormatting.RED));
                if (growing && data.power() > 0) {
                    long ticks = (target - data.growth()) / data.power();
                    tooltip.add(Component.translatable("nautec.jade.crystal_cradle.remaining", format.format(ticks / 1200)).withStyle(ChatFormatting.GRAY));
                }
            });
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
