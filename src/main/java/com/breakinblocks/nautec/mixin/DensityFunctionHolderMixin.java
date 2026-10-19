package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.worldgen.DeeperOceans;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.levelgen.DensityFunctions$HolderHolder")
public abstract class DensityFunctionHolderMixin {
    @WrapOperation(
            method = "mapAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;value()Ljava/lang/Object;")
    )
    private Object nautec$deeperOceans(Holder<DensityFunction> holder, Operation<Object> original) {
        return original.call(DeeperOceans.resolve(holder));
    }
}
