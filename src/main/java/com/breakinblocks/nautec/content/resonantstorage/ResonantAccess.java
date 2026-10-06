package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.content.resonance.TeamAccess;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ResonantAccess {
    private ResonantAccess() {
    }

    public static boolean canUse(Player player, ResonantChannel channel) {
        return switch (channel.access()) {
            case PUBLIC -> true;
            case PRIVATE -> channel.owner().equals(player.getUUID()) || admin(player);
            case TEAM -> TeamAccess.isMember(channel.owner(), player.getUUID()) || admin(player);
        };
    }

    public static boolean admin(Player player) {
        return player instanceof ServerPlayer server && Commands.LEVEL_GAMEMASTERS.check(server.createCommandSourceStack().permissions());
    }

    public static @Nullable ResonantLink key(ServerPlayer player, ChannelAccess access, GatewayAddress address) {
        return switch (access) {
            case PUBLIC -> new ResonantLink(new ResonantChannel(ChannelAccess.PUBLIC, ResonantChannel.NOBODY, address), "");
            case PRIVATE -> new ResonantLink(new ResonantChannel(ChannelAccess.PRIVATE, player.getUUID(), address), player.getGameProfile().name());
            case TEAM -> {
                UUID team = TeamAccess.teamOf(player.getUUID());
                yield team == null ? null : new ResonantLink(new ResonantChannel(ChannelAccess.TEAM, team, address), TeamAccess.teamName(team));
            }
        };
    }
}
