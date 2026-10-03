package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.network.ResonanceActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class ResonanceActions {
    private static final double REACH = 8.0;

    private ResonanceActions() {
    }

    public static void apply(ServerPlayer player, ResonanceActionPayload payload) {
        if (!(player.level().getBlockEntity(payload.pos()) instanceof ResonanceTunable pylon)
                || player.distanceToSqr(payload.pos().getCenter()) > REACH * REACH) {
            return;
        }
        ResonanceNetworks networks = ResonanceNetworks.get(player.level().getServer());
        ResonanceNetwork current = pylon.getNetwork();
        if (current != null && !ResonanceNetworks.canUse(player, current)) {
            fail(player, "nautec.resonance.error.no_access");
            return;
        }

        String error = switch (payload.action()) {
            case ResonanceActionPayload.SELECT -> select(player, pylon, networks, payload.network().orElse(null));
            case ResonanceActionPayload.CREATE -> {
                ResonanceNetworks.Result result = networks.create(player, payload.text());
                if (result.success()) {
                    pylon.setNetwork(result.network());
                }
                yield result.error();
            }
            case ResonanceActionPayload.MODE -> {
                if (pylon instanceof ResonancePylonBlockEntity sender) {
                    sender.setSendMode(!sender.isSendMode());
                }
                yield null;
            }
            case ResonanceActionPayload.TRUST -> current == null ? "nautec.resonance.error.no_network" : trust(player, networks, current, payload.text());
            case ResonanceActionPayload.UNTRUST -> current == null ? "nautec.resonance.error.no_network"
                    : untrust(player, networks, current, payload.text());
            case ResonanceActionPayload.TEAM -> current == null ? "nautec.resonance.error.no_network"
                    : networks.setTeamAccess(player, current.id(), !current.teamAccess()).error();
            case ResonanceActionPayload.RENAME -> current == null ? "nautec.resonance.error.no_network"
                    : networks.rename(player, current.id(), payload.text()).error();
            case ResonanceActionPayload.DELETE -> {
                if (current == null) {
                    yield "nautec.resonance.error.no_network";
                }
                ResonanceNetworks.Result result = networks.delete(player, current.id());
                if (result.success()) {
                    pylon.setNetwork(null);
                }
                yield result.error();
            }
            default -> null;
        };
        if (error != null) {
            fail(player, error);
        }
        ResonanceSync.send(player, pylon);
    }

    private static String select(ServerPlayer player, ResonanceTunable pylon, ResonanceNetworks networks, UUID id) {
        if (id == null) {
            pylon.setNetwork(null);
            return null;
        }
        ResonanceNetwork network = networks.get(id);
        if (network == null || !ResonanceNetworks.canUse(player, network)) {
            return "nautec.resonance.error.no_access";
        }
        pylon.setNetwork(network);
        return null;
    }

    private static String trust(ServerPlayer player, ResonanceNetworks networks, ResonanceNetwork network, String name) {
        ServerPlayer target = player.level().getServer().getPlayerList().getPlayerByName(name.strip());
        if (target == null) {
            return "nautec.resonance.error.player_offline";
        }
        return networks.trust(player, network.id(), target.getUUID(), target.getGameProfile().name()).error();
    }

    private static String untrust(ServerPlayer player, ResonanceNetworks networks, ResonanceNetwork network, String id) {
        try {
            return networks.untrust(player, network.id(), UUID.fromString(id)).error();
        } catch (IllegalArgumentException e) {
            return "nautec.resonance.error.player_offline";
        }
    }

    private static void fail(ServerPlayer player, String key) {
        player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
    }
}
