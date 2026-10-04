package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ResonanceChunkLoading {
    public static final int OFF = 0;
    public static final int ON = 1;
    public static final int DISABLED = 2;

    private static final TicketController CONTROLLER = new TicketController(Nautec.rl("resonance"), ResonanceChunkLoading::validate);

    private ResonanceChunkLoading() {
    }

    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    private static void validate(ServerLevel level, TicketHelper helper) {
        if (!NTConfig.resonanceChunkLoading) {
            for (BlockPos owner : List.copyOf(helper.getBlockTickets().keySet())) {
                helper.removeAllTickets(owner);
            }
        }
    }

    public static int state(boolean wanted) {
        if (!NTConfig.resonanceChunkLoading) {
            return DISABLED;
        }
        return wanted ? ON : OFF;
    }

    public static final class Ticket {
        private @Nullable Boolean held;

        public void update(ServerLevel level, BlockPos pos, boolean wanted) {
            boolean want = wanted && NTConfig.resonanceChunkLoading;
            if (held != null && held == want) {
                return;
            }
            force(level, pos, want);
            held = want;
        }

        public void release(ServerLevel level, BlockPos pos) {
            if (held == null || held) {
                force(level, pos, false);
            }
            held = false;
        }

        private static void force(ServerLevel level, BlockPos pos, boolean add) {
            CONTROLLER.forceChunk(level, pos, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), add, true);
        }
    }
}
