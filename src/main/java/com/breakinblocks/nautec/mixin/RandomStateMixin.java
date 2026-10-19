package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.worldgen.MonumentLowering;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RandomState.class)
public abstract class RandomStateMixin implements MonumentLowering.Flag {
    @Unique
    private boolean nautec$keepsMonumentHeight;

    @Override
    public boolean nautec$lowersMonuments() {
        return !nautec$keepsMonumentHeight;
    }

    @Override
    public void nautec$setLowersMonuments(boolean lowers) {
        nautec$keepsMonumentHeight = !lowers;
    }
}
