package com.breakinblocks.nautec.api.fluids;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4i;

public class ViscousFluidType extends BaseFluidType {
    public ViscousFluidType(Identifier stillTexture, Identifier flowingTexture, Identifier overlayTexture, Vector4i color, FluidType.Properties properties) {
        super(stillTexture, flowingTexture, overlayTexture, color, properties);
    }

    @Override
    public boolean move(LivingEntity entity, Vec3 movementVector, double gravity) {
        boolean falling = entity.getDeltaMovement().y <= 0.0;
        double startY = entity.getY();

        entity.moveRelative(0.02F, movementVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        if (entity.getFluidHeight(this) <= entity.getFluidJumpThreshold()) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
            entity.setDeltaMovement(entity.getFluidFallingAdjustedMovement(gravity, falling, entity.getDeltaMovement()));
        } else {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5));
        }

        if (gravity != 0.0) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, -gravity / 4.0, 0.0));
        }

        Vec3 movement = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.isFree(movement.x, movement.y + 0.6F - entity.getY() + startY, movement.z)) {
            entity.setDeltaMovement(movement.x, 0.3F, movement.z);
        }

        return true;
    }
}
