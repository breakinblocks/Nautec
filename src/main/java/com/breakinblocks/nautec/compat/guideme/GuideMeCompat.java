package com.breakinblocks.nautec.compat.guideme;

import com.breakinblocks.nautec.Nautec;
import guideme.GuidesCommon;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class GuideMeCompat {
    public static final Identifier GUIDE_ID = Nautec.rl("guide");

    private GuideMeCompat() {
    }

    public static void openGuide(Player player) {
        GuidesCommon.openGuide(player, GUIDE_ID);
    }
}
