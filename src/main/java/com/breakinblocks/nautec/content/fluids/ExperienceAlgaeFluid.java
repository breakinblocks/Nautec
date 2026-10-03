package com.breakinblocks.nautec.content.fluids;

import com.breakinblocks.nautec.api.fluids.NTFluid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4i;

public class ExperienceAlgaeFluid extends NTFluid {
    public ExperienceAlgaeFluid(String name) {
        super(name);
        this.fluidType = registerFluidType(FluidType.Properties.create()
                .lightLevel(10)
                .density(800)
                .viscosity(1500)
                .canExtinguish(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY),
                new Vector4i(255, 255, 255, 255), FluidTemplates.EXPERIENCE_ALGAE);
    }

    @Override
    public BaseFlowingFluid.Properties fluidProperties() {
        return super.fluidProperties().block(this.block).bucket(this.deferredBucket);
    }

    @Override
    public BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).lightLevel(state -> 10);
    }
}
