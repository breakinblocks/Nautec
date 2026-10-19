package com.breakinblocks.nautec.content.enchanter;

import com.breakinblocks.nautec.capabilities.beam.IBeamReceiver;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class AbyssalEnchanterPartBlockEntity extends BlockEntity {
    private final IBeamReceiver beamReceiver = (power, purity, travel, origin, trail) -> {
        AbyssalEnchanterBlockEntity core = core();
        if (core != null) {
            core.receiveBeam(power, purity, travel, origin, trail);
        }
    };

    public AbyssalEnchanterPartBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.ABYSSAL_ENCHANTER_PART.get(), pos, state);
    }

    public int cell() {
        BlockState state = getBlockState();
        return state.hasProperty(AbyssalEnchanterPartBlock.CELL) ? state.getValue(AbyssalEnchanterPartBlock.CELL) : EnchanterStructure.CORE_CELL;
    }

    public @Nullable AbyssalEnchanterBlockEntity core() {
        if (level == null) {
            return null;
        }
        return level.getBlockEntity(EnchanterStructure.coreOf(worldPosition, cell())) instanceof AbyssalEnchanterBlockEntity core ? core : null;
    }

    private boolean outward(@Nullable Direction side) {
        return side == null || EnchanterStructure.isOutward(cell(), side);
    }

    public @Nullable IBeamReceiver beamReceiver(@Nullable Direction side) {
        return side != null && outward(side) && core() != null ? beamReceiver : null;
    }

    public @Nullable ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        AbyssalEnchanterBlockEntity core = core();
        return core == null || !outward(side) ? null : core.getItemHandlerOnSide(side);
    }

    public @Nullable ResourceHandler<FluidResource> fluidHandler(@Nullable Direction side) {
        AbyssalEnchanterBlockEntity core = core();
        return core == null || !outward(side) ? null : core.getFluidHandlerOnSide(side);
    }
}
