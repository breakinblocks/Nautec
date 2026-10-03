package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.Nautec;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.UUID;

public final class TeamAccess {
    private static boolean resolved;
    private static @Nullable Method api;
    private static @Nullable Method managerLoaded;
    private static @Nullable Method manager;
    private static @Nullable Method sameTeam;

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
            Object instance = api.invoke(null);
            if (!(boolean) managerLoaded.invoke(instance)) {
                return false;
            }
            return (boolean) sameTeam.invoke(manager.invoke(instance), first, second);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
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
            api = teams.getMethod("api");
            managerLoaded = apiType.getMethod("isManagerLoaded");
            manager = apiType.getMethod("getManager");
            sameTeam = managerType.getMethod("arePlayersInSameTeam", UUID.class, UUID.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            Nautec.LOGGER.warn("Could not reach the FTB Teams API, Resonance Network team access is off", e);
            sameTeam = null;
        }
    }
}
