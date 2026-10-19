package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.worldgen.DeeperOceans;
import com.breakinblocks.nautec.worldgen.MonumentLowering;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/RandomState;create(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)Lnet/minecraft/world/level/levelgen/RandomState;"
            )
    )
    private RandomState nautec$perDimensionOceans(NoiseGeneratorSettings settings, HolderGetter<NormalNoise.NoiseParameters> noises, long seed,
                                                  Operation<RandomState> original, @Local(argsOnly = true) ServerLevel level) {
        RandomState state = DeeperOceans.applyIn(level.dimension(), level.registryAccess(), () -> original.call(settings, noises, seed));
        ((MonumentLowering.Flag) (Object) state).nautec$setLowersMonuments(MonumentLowering.enabledIn(level.dimension()));
        return state;
    }
}
