package com.breakinblocks.nautec.content.fluids;

import com.breakinblocks.nautec.api.fluids.NTFluid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4i;

public class SaltWaterFluid extends NTFluid {
    public SaltWaterFluid(String name) {
        super(name);
        this.fluidType = registerFluidType(FluidType.Properties.create()
                .isWaterLike(true)
                .fallDistanceModifier(0.0F)
                .canExtinguish(true)
                .supportsBoating(true)
                .density(1025)
                .viscosity(1025)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH),
                new Vector4i(174, 227, 227, 176), FluidTemplates.WATER);
    }

    @Override
    public BaseFlowingFluid.Properties fluidProperties() {
        return super.fluidProperties().block(this.block).bucket(this.deferredBucket);
    }

    @Override
    public BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.WATER);
    }
}
