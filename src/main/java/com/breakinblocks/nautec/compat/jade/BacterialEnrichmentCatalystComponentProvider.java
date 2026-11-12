package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.BacterialEnrichmentCatalystBlockEntity;
import com.breakinblocks.nautec.content.enrichment.EnrichmentRules;
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

import java.text.NumberFormat;
import java.util.Locale;

public enum BacterialEnrichmentCatalystComponentProvider
        implements StreamServerDataProvider<BlockAccessor, BacterialEnrichmentCatalystComponentProvider.Data> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("bacterial_enrichment_catalyst");
    private static final String STATUS_KEY = "nautec.enrichment_catalyst.status.";
    private static final String[] STATUS_NAMES = {
            "no_crystal", "wild_crystal", "shared", "too_impure", "no_source", "low_power", "no_colony", "small_colony", "overreach",
            "at_target", "at_cap", "paused", "running"
    };

    public record Data(int status, float crystalPurity, float sourcePurity, float chance, long progress, long cycle) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.FLOAT, Data::crystalPurity,
                ByteBufCodecs.FLOAT, Data::sourcePurity,
                ByteBufCodecs.FLOAT, Data::chance,
                ByteBufCodecs.VAR_LONG, Data::progress,
                ByteBufCodecs.VAR_LONG, Data::cycle,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        BacterialEnrichmentCatalystBlockEntity catalyst = (BacterialEnrichmentCatalystBlockEntity) accessor.getBlockEntity();
        float crystal = catalyst.getShownCrystalPurity();
        float source = catalyst.getShownSourcePurity();
        return new Data(catalyst.getStatus(), crystal, source, EnrichmentRules.regressionChance(crystal, source), catalyst.getProgress(),
                EnrichmentRules.cycleAp(crystal));
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
            BacterialEnrichmentCatalystComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                int status = Math.clamp(data.status(), 0, STATUS_NAMES.length - 1);
                boolean running = status == BacterialEnrichmentCatalystBlockEntity.STATUS_RUNNING;
                long cycle = Math.max(1L, data.cycle());
                int percent = (int) Math.min(100L, data.progress() * 100L / cycle);
                Component text = running
                        ? Component.translatable(STATUS_KEY + STATUS_NAMES[status], percent)
                        : Component.translatable(STATUS_KEY + STATUS_NAMES[status]);
                tooltip.add(text.copy().withStyle(running ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                if (data.crystalPurity() <= 0F) {
                    return;
                }
                tooltip.add(Component.translatable("nautec.jade.enrichment_catalyst.crystal", String.format(Locale.ROOT, "%.2f", data.crystalPurity())));
                if (data.sourcePurity() > 0F) {
                    tooltip.add(Component.translatable("nautec.jade.enrichment_catalyst.source", String.format(Locale.ROOT, "%.2f", data.sourcePurity())));
                    tooltip.add(Component.translatable("nautec.jade.enrichment_catalyst.chance", String.format(Locale.ROOT, "%.1f", data.chance() * 100F)));
                }
                NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
                tooltip.add(Component.translatable("nautec.jade.enrichment_catalyst.progress", percent, format.format(data.progress()), format.format(cycle))
                        .withStyle(ChatFormatting.GRAY));
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
