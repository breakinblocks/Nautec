package com.breakinblocks.nautec.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingHook.class)
public interface FishingHookAccessor {
    @Accessor("nibble")
    int nautec$getNibble();

    @Mutable
    @Accessor("luck")
    void nautec$setLuck(int luck);

    @Mutable
    @Accessor("lureSpeed")
    void nautec$setLureSpeed(int lureSpeed);
}
