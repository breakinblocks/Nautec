package com.breakinblocks.nautec.content.entities.mobs;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class SwimmingMoveControl extends MoveControl {
    public SwimmingMoveControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        super.tick();
        if (operation == Operation.JUMPING && mob.isInLiquid()) {
            operation = Operation.WAIT;
        }
    }
}
