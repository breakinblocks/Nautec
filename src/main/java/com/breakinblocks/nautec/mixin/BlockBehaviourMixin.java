package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.api.blockentities.RemovalAware;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    @Inject(method = "onRemove", at = @At("HEAD"))
    private void nautec$preRemoveSideEffects(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if (!level.isClientSide() && state.hasBlockEntity() && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RemovalAware removalAware) {
            removalAware.preRemoveSideEffects(pos, state);
        }
    }
}
