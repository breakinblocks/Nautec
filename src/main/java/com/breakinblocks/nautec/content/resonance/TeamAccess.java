package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class TeamAccess {
    private static boolean resolved;
    private static @Nullable Method api;
    private static @Nullable Method managerLoaded;
    private static @Nullable Method manager;
    private static @Nullable Method sameTeam;
    private static @Nullable Method teamForPlayer;
    private static @Nullable Method teamById;
    private static @Nullable Method teamId;
    private static @Nullable Method teamName;
    private static @Nullable Method teamMembers;

    private TeamAccess() {
    }

    public static boolean available() {
        resolve();
        return sameTeam != null;
    }

    public static boolean sameTeam(UUID first, UUID second) {
        resolve();
        if (sameTeam == null || first.equals(second)) {
            return first.equals(second);
        }
        try {
            Object teams = manager();
            return teams != null && (boolean) sameTeam.invoke(teams, first, second);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    public static @Nullable UUID teamOf(UUID player) {
        Object team = team(teamForPlayer, player);
        if (team == null) {
            return null;
        }
        try {
            return (UUID) teamId.invoke(team);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static String teamName(UUID team) {
        Object found = team(teamById, team);
        if (found == null) {
            return "";
        }
        try {
            return ((Component) teamName.invoke(found)).getString();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return "";
        }
    }

    public static boolean isMember(UUID team, UUID player) {
        Object found = team(teamById, team);
        if (found == null) {
            return false;
        }
        try {
            return ((Collection<?>) teamMembers.invoke(found)).contains(player);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static @Nullable Object manager() throws ReflectiveOperationException {
        Object instance = api.invoke(null);
        return (boolean) managerLoaded.invoke(instance) ? manager.invoke(instance) : null;
    }

    private static @Nullable Object team(@Nullable Method lookup, UUID id) {
        resolve();
        if (lookup == null) {
            return null;
        }
        try {
            Object teams = manager();
            if (teams == null) {
                return null;
            }
            return ((Optional<?>) lookup.invoke(teams, id)).orElse(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (!ModList.get().isLoaded("ftbteams")) {
            return;
        }
        try {
            Class<?> teams = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            Class<?> apiType = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI$API");
            Class<?> managerType = Class.forName("dev.ftb.mods.ftbteams.api.TeamManager");
            Class<?> teamType = Class.forName("dev.ftb.mods.ftbteams.api.Team");
            api = teams.getMethod("api");
            managerLoaded = apiType.getMethod("isManagerLoaded");
            manager = apiType.getMethod("getManager");
            sameTeam = managerType.getMethod("arePlayersInSameTeam", UUID.class, UUID.class);
            teamForPlayer = managerType.getMethod("getTeamForPlayerID", UUID.class);
            teamById = managerType.getMethod("getTeamByID", UUID.class);
            teamId = teamType.getMethod("getId");
            teamName = teamType.getMethod("getName");
            teamMembers = teamType.getMethod("getMembers");
        } catch (ReflectiveOperationException | LinkageError e) {
            Nautec.LOGGER.warn("Could not reach the FTB Teams API, team access is off", e);
            sameTeam = null;
            teamForPlayer = null;
            teamById = null;
        }
    }
}
