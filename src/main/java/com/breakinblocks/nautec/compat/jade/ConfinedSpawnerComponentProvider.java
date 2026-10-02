package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.content.spawner.SpawnerSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.Optional;

public enum ConfinedSpawnerComponentProvider implements StreamServerDataProvider<BlockAccessor, ConfinedSpawnerComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("confined_spawner");

    public record Data(int power, int status, boolean active, Optional<Identifier> mob) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::power,
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.BOOL, Data::active,
                ByteBufCodecs.optional(Identifier.STREAM_CODEC), Data::mob,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        ConfinedSpawnerBlockEntity spawner = (ConfinedSpawnerBlockEntity) accessor.getBlockEntity();
        SpawnerSettings settings = spawner.getSettings();
        Optional<Identifier> mob = settings == null ? Optional.empty() : settings.displayEntity().read("id", Identifier.CODEC);
        return new Data(spawner.getBufferedPower(), spawner.getStatus().ordinal(), spawner.isActive(), mob);
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
            ConfinedSpawnerComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                data.mob().flatMap(BuiltInRegistries.ENTITY_TYPE::getOptional).map(EntityType::getDescription).ifPresent(name ->
                        tooltip.add(Component.translatable("nautec.jade.confined_spawner.mob", name)));
                ConfinedSpawnerBlockEntity.Status status = ConfinedSpawnerBlockEntity.Status.byId(data.status());
                tooltip.add(Component.translatable(data.active() ? "nautec.jade.confined_spawner.running" : "nautec.jade.confined_spawner.stopped")
                        .withStyle(data.active() ? ChatFormatting.GREEN : ChatFormatting.RED));
                if (status != ConfinedSpawnerBlockEntity.Status.RUNNING) {
                    tooltip.add(Component.translatable(status.translationKey()).withStyle(ChatFormatting.YELLOW));
                }
                tooltip.add(Component.translatable("nautec.confined_spawner.power", data.power(), NTConfig.confinedSpawnerPowerBuffer));
            });
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
