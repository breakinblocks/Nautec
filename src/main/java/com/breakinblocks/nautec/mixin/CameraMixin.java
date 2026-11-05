package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private Vec3 position;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    private void nautec$raiseSubmarineView(float partialTicks, CallbackInfo ci) {
        if (this.entity.getVehicle() instanceof SubmarineEntity submarine) {
            setPosition(this.position.add(submarine.viewLift(partialTicks)));
        }
    }
}
