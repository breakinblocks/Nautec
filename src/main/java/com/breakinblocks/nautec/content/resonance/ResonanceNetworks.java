package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ResonanceNetworks extends SavedData {
    public static final Codec<ResonanceNetworks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResonanceNetwork.CODEC.listOf().fieldOf("networks").forGetter(networks -> List.copyOf(networks.networks.values()))
    ).apply(instance, ResonanceNetworks::new));

    public static final SavedDataType<ResonanceNetworks> TYPE =
            new SavedDataType<>(Nautec.rl("resonance_networks"), ResonanceNetworks::new, CODEC);

    private final Map<UUID, ResonanceNetwork> networks = new LinkedHashMap<>();

    public ResonanceNetworks() {
    }

    private ResonanceNetworks(List<ResonanceNetwork> loaded) {
        for (ResonanceNetwork network : loaded) {
            this.networks.put(network.id(), network);
        }
    }

    public static ResonanceNetworks get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public @Nullable ResonanceNetwork get(@Nullable UUID id) {
        return id == null ? null : networks.get(id);
    }

    public List<ResonanceNetwork> accessibleTo(UUID player) {
        List<ResonanceNetwork> result = new ArrayList<>();
        for (ResonanceNetwork network : networks.values()) {
            if (network.canAccess(player)) {
                result.add(network);
            }
        }
        result.sort(Comparator.comparing((ResonanceNetwork network) -> !network.isOwner(player)).thenComparing(ResonanceNetwork::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public int ownedBy(UUID player) {
        int count = 0;
        for (ResonanceNetwork network : networks.values()) {
            if (network.isOwner(player)) {
                count++;
            }
        }
        return count;
    }

    public Result create(ServerPlayer owner, String name) {
        String clean = ResonanceNetwork.cleanName(name);
        if (clean.isEmpty()) {
            return Result.fail("nautec.resonance.error.name");
        }
        if (ownedBy(owner.getUUID()) >= NTConfig.resonanceMaxNetworksPerPlayer) {
            return Result.fail("nautec.resonance.error.too_many");
        }
        ResonanceNetwork network = new ResonanceNetwork(UUID.randomUUID(), clean, owner.getUUID(),
                owner.getGameProfile().name(), Map.of(), false);
        networks.put(network.id(), network);
        setDirty();
        return Result.ok(network);
    }

    public static boolean canManage(ServerPlayer player, ResonanceNetwork network) {
        return network.isOwner(player.getUUID()) || Commands.LEVEL_GAMEMASTERS.check(player.createCommandSourceStack().permissions());
    }

    public static boolean canUse(ServerPlayer player, ResonanceNetwork network) {
        return network.canAccess(player.getUUID()) || canManage(player, network);
    }

    public Result rename(ServerPlayer player, UUID id, String name) {
        ResonanceNetwork network = networks.get(id);
        if (network == null || !canManage(player, network)) {
            return Result.fail("nautec.resonance.error.not_owner");
        }
        String clean = ResonanceNetwork.cleanName(name);
        if (clean.isEmpty()) {
            return Result.fail("nautec.resonance.error.name");
        }
        network.setName(clean);
        setDirty();
        return Result.ok(network);
    }

    public Result trust(ServerPlayer player, UUID id, UUID target, String targetName) {
        ResonanceNetwork network = networks.get(id);
        if (network == null || !canManage(player, network)) {
            return Result.fail("nautec.resonance.error.not_owner");
        }
        if (network.isOwner(target)) {
            return Result.fail("nautec.resonance.error.already_owner");
        }
        network.trusted().put(target, targetName);
        setDirty();
        return Result.ok(network);
    }

    public Result untrust(ServerPlayer player, UUID id, UUID target) {
        ResonanceNetwork network = networks.get(id);
        if (network == null || !canManage(player, network)) {
            return Result.fail("nautec.resonance.error.not_owner");
        }
        network.trusted().remove(target);
        setDirty();
        return Result.ok(network);
    }

    public Result setTeamAccess(ServerPlayer player, UUID id, boolean teamAccess) {
        ResonanceNetwork network = networks.get(id);
        if (network == null || !canManage(player, network)) {
            return Result.fail("nautec.resonance.error.not_owner");
        }
        network.setTeamAccess(teamAccess);
        setDirty();
        return Result.ok(network);
    }

    public Result delete(ServerPlayer player, UUID id) {
        ResonanceNetwork network = networks.get(id);
        if (network == null || !canManage(player, network)) {
            return Result.fail("nautec.resonance.error.not_owner");
        }
        networks.remove(id);
        setDirty();
        return Result.ok(network);
    }

    public void refreshOwnerName(ServerPlayer player) {
        boolean changed = false;
        for (ResonanceNetwork network : networks.values()) {
            if (network.isOwner(player.getUUID()) && !network.ownerName().equals(player.getGameProfile().name())) {
                network.setOwnerName(player.getGameProfile().name());
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public record Result(@Nullable ResonanceNetwork network, @Nullable String error) {
        static Result ok(ResonanceNetwork network) {
            return new Result(network, null);
        }

        static Result fail(String error) {
            return new Result(null, error);
        }

        public boolean success() {
            return error == null;
        }
    }
}
