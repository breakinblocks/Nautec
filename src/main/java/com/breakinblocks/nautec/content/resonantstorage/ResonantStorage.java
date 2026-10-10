package com.breakinblocks.nautec.content.resonantstorage;



import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.breakinblocks.nautec.Nautec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import com.breakinblocks.nautec.utils.SavedDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResonantStorage extends SavedData {
    public static final Codec<ResonantStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VaultStore.CODEC.listOf().optionalFieldOf("vaults", List.of()).forGetter(storage -> kept(storage.vaults.values())),
            CisternStore.CODEC.listOf().optionalFieldOf("cisterns", List.of()).forGetter(storage -> kept(storage.cisterns.values()))
    ).apply(instance, ResonantStorage::new));

    public static final SavedDataType<ResonantStorage> TYPE =
            new SavedDataType<>(Nautec.rl("resonant_storage"), ResonantStorage::new, CODEC);

    private final Map<ResonantChannel, VaultStore> vaults = new HashMap<>();
    private final Map<ResonantChannel, CisternStore> cisterns = new HashMap<>();

    public ResonantStorage() {
    }

    private ResonantStorage(List<VaultStore> vaults, List<CisternStore> cisterns) {
        for (VaultStore store : vaults) {
            store.attach(this::setDirty);
            this.vaults.put(store.channel(), store);
        }
        for (CisternStore store : cisterns) {
            store.attach(this::setDirty);
            this.cisterns.put(store.channel(), store);
        }
    }

    private static <S extends ResonantStore> List<S> kept(Collection<S> stores) {
        List<S> kept = new ArrayList<>(stores.size());
        for (S store : stores) {
            if (!store.isBlank()) {
                kept.add(store);
            }
        }
        return kept;
    }

    public static ResonantStorage get(MinecraftServer server) {
        return TYPE.get(server.overworld().getDataStorage());
    }

    public VaultStore vault(ResonantChannel channel) {
        VaultStore store = vaults.get(channel);
        if (store == null) {
            store = new VaultStore(channel);
            store.attach(this::setDirty);
            vaults.put(channel, store);
        }
        return store;
    }

    public CisternStore cistern(ResonantChannel channel) {
        CisternStore store = cisterns.get(channel);
        if (store == null) {
            store = new CisternStore(channel);
            store.attach(this::setDirty);
            cisterns.put(channel, store);
        }
        return store;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return TYPE.save(this, tag, registries);
    }
}
