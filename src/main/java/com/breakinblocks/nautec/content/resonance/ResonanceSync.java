package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.network.ResonanceSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ResonanceSync {
    private ResonanceSync() {
    }

    private static int pylonCount(ResonanceNetwork network, ResonancePylonBlockEntity pylon) {
        List<ResonancePylonBlockEntity> members = ResonanceGrid.members(network.id());
        boolean pending = network.id().equals(pylon.getNetworkId()) && !members.contains(pylon);
        return members.size() + (pending ? 1 : 0);
    }

    public static void send(ServerPlayer player, ResonancePylonBlockEntity pylon) {
        ResonanceNetworks networks = ResonanceNetworks.get(player.level().getServer());
        networks.refreshOwnerName(player);
        List<ResonanceNetwork> visible = new ArrayList<>(networks.accessibleTo(player.getUUID()));
        ResonanceNetwork current = pylon.getNetwork();
        if (current != null && !visible.contains(current)) {
            visible.add(0, current);
        }
        List<ResonanceSyncPayload.NetworkView> views = new ArrayList<>();
        for (ResonanceNetwork network : visible) {
            List<ResonanceSyncPayload.Member> trusted = new ArrayList<>();
            for (Map.Entry<UUID, String> entry : network.trusted().entrySet()) {
                trusted.add(new ResonanceSyncPayload.Member(entry.getKey(), entry.getValue()));
            }
            views.add(new ResonanceSyncPayload.NetworkView(network.id(), network.name(), network.ownerName(),
                    network.isOwner(player.getUUID()), ResonanceNetworks.canManage(player, network), trusted,
                    network.teamAccess(), pylonCount(network, pylon)));
        }
        PacketDistributor.sendToPlayer(player, new ResonanceSyncPayload(pylon.getBlockPos(),
                Optional.ofNullable(pylon.getNetworkId()), views, TeamAccess.available()));
    }
}
