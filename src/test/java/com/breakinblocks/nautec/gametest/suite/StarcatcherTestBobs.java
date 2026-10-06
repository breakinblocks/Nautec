package com.breakinblocks.nautec.gametest.suite;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.bobentity.FishingBobEntity;
import com.wdiscute.starcatcher.registry.SCItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class StarcatcherTestBobs {
    private StarcatcherTestBobs() {
    }

    static Entity cast(ServerLevel level, Player owner) {
        ItemStack rod = SCItems.ROD.toStack();
        owner.setItemInHand(InteractionHand.MAIN_HAND, rod);
        return new FishingBobEntity(level, owner, rod, Starcatcher.TACKLE_SKIN_REGISTRY.getValue(Starcatcher.BASE));
    }

    static int ticksInFluid(Entity bob) {
        return ((FishingBobEntity) bob).ticksInFluid;
    }
}
