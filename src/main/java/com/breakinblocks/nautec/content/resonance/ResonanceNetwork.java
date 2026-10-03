package com.breakinblocks.nautec.content.resonance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ResonanceNetwork {
    public static final int MAX_NAME_LENGTH = 24;

    private static final Codec<Map<UUID, String>> TRUSTED_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING);

    public static final Codec<ResonanceNetwork> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(ResonanceNetwork::id),
            Codec.STRING.fieldOf("name").forGetter(ResonanceNetwork::name),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(ResonanceNetwork::owner),
            Codec.STRING.fieldOf("owner_name").forGetter(ResonanceNetwork::ownerName),
            TRUSTED_CODEC.optionalFieldOf("trusted", Map.of()).forGetter(ResonanceNetwork::trusted),
            Codec.BOOL.optionalFieldOf("team_access", false).forGetter(ResonanceNetwork::teamAccess)
    ).apply(instance, ResonanceNetwork::new));

    private final UUID id;
    private String name;
    private final UUID owner;
    private String ownerName;
    private final Map<UUID, String> trusted;
    private boolean teamAccess;

    public ResonanceNetwork(UUID id, String name, UUID owner, String ownerName, Map<UUID, String> trusted, boolean teamAccess) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.ownerName = ownerName;
        this.trusted = new LinkedHashMap<>(trusted);
        this.teamAccess = teamAccess;
    }

    public static String cleanName(String name) {
        String trimmed = name == null ? "" : name.strip();
        return trimmed.length() > MAX_NAME_LENGTH ? trimmed.substring(0, MAX_NAME_LENGTH) : trimmed;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UUID owner() {
        return owner;
    }

    public String ownerName() {
        return ownerName;
    }

    public Map<UUID, String> trusted() {
        return trusted;
    }

    public boolean teamAccess() {
        return teamAccess;
    }

    void setName(String name) {
        this.name = name;
    }

    void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    void setTeamAccess(boolean teamAccess) {
        this.teamAccess = teamAccess;
    }

    public boolean isOwner(UUID player) {
        return owner.equals(player);
    }

    public boolean canAccess(UUID player) {
        return isOwner(player) || trusted.containsKey(player) || (teamAccess && TeamAccess.sameTeam(owner, player));
    }
}
