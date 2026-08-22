package com.breakinblocks.nautec.content.fluids;

import com.breakinblocks.nautec.api.fluids.NTFluid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4i;

public class OilFluid extends NTFluid {
    public OilFluid(String name) {
        super(name);
        this.fluidType = registerViscousFluidType(FluidType.Properties.create()
                .canSwim(false)
                .motionScale(0.0023)
                .pathType(PathType.LAVA)
                .adjacentPathType(null)
                .density(900)
                .viscosity(6000)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY),
                new Vector4i(20, 20, 20, 255), FluidTemplates.WATER);
    }

    @Override
    public BaseFlowingFluid.Properties fluidProperties() {
        return super.fluidProperties().block(this.block).bucket(this.deferredBucket);
    }

    @Override
    public BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA);
    }
}
