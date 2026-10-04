package com.breakinblocks.nautec.content.blockentities.multiblock.part;

import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockPartEntity;
import com.breakinblocks.nautec.api.sides.SideMode;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.item.DelegatingItemHandler;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.IndustrialBioReactorBlockEntity;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.content.multiblocks.IndustrialBioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class IndustrialBioReactorPartBlockEntity extends LaserBlockEntity implements MultiblockPartEntity {
    private static final int CELLS = IndustrialBioReactorMultiblock.SIZE * IndustrialBioReactorMultiblock.SIZE;
    private static final Set<Direction>[] HATCH_INPUTS = hatchInputs();
    private static final Set<Direction>[][] OUTWARD = outwardFaces();

    private final ResourceHandler<ItemResource> controllerItems = new DelegatingItemHandler(() -> {
        IndustrialBioReactorBlockEntity reactor = controller();
        return reactor == null ? null : reactor.getItemHandler();
    });
    private BlockPos controllerPos;

    public IndustrialBioReactorPartBlockEntity(BlockPos pos, BlockState blockState) {
        super(NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR_PART.get(), pos, blockState);
    }

    @SuppressWarnings("unchecked")
    private static Set<Direction>[] hatchInputs() {
        Set<Direction>[] inputs = new Set[CELLS];
        for (int cell = 0; cell < CELLS; cell++) {
            Set<Direction> faces = EnumSet.of(Direction.UP);
            for (Direction direction : IndustrialBioReactorMultiblock.outwardFaces(IndustrialBioReactorMultiblock.HEIGHT - 1, cell)) {
                if (direction.getAxis().isHorizontal()) {
                    faces.add(direction);
                }
            }
            inputs[cell] = Collections.unmodifiableSet(faces);
        }
        return inputs;
    }

    @SuppressWarnings("unchecked")
    private static Set<Direction>[][] outwardFaces() {
        Set<Direction>[][] faces = new Set[IndustrialBioReactorMultiblock.HEIGHT][CELLS];
        for (int layer = 0; layer < IndustrialBioReactorMultiblock.HEIGHT; layer++) {
            for (int cell = 0; cell < CELLS; cell++) {
                faces[layer][cell] = Collections.unmodifiableSet(IndustrialBioReactorMultiblock.outwardFaces(layer, cell));
            }
        }
        return faces;
    }

    public int getLayer() {
        BlockState state = getBlockState();
        return state.hasProperty(IndustrialBioReactorMultiblock.LAYER) ? state.getValue(IndustrialBioReactorMultiblock.LAYER) : 0;
    }

    public int getCell() {
        BlockState state = getBlockState();
        return state.hasProperty(IndustrialBioReactorMultiblock.CELL) ? state.getValue(IndustrialBioReactorMultiblock.CELL) : 0;
    }

    public boolean isHatch() {
        BlockState state = getBlockState();
        return state.hasProperty(BioReactorMultiblock.HATCH) && state.getValue(BioReactorMultiblock.HATCH)
                && IndustrialBioReactorMultiblock.isHatchCandidate(getLayer(), getCell());
    }

    @Override
    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    @Override
    public void setControllerPos(BlockPos blockPos) {
        this.controllerPos = blockPos;
        if (level != null && !level.isClientSide()) {
            update();
        }
    }

    private @Nullable IndustrialBioReactorBlockEntity controller() {
        if (level == null || controllerPos == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof IndustrialBioReactorBlockEntity reactor ? reactor : null;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return isHatch() ? HATCH_INPUTS[getCell()] : Set.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandler() {
        return this.controllerItems;
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        if (direction == null) {
            return getItemHandler();
        }
        if (!OUTWARD[getLayer()][getCell()].contains(direction)) {
            return null;
        }
        IndustrialBioReactorBlockEntity reactor = controller();
        if (reactor != null && reactor.itemMode(direction) == SideMode.NONE) {
            return null;
        }
        return new DelegatingItemHandler(() -> {
            IndustrialBioReactorBlockEntity current = controller();
            return current == null ? null : current.getItemHandlerOnSide(direction);
        });
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (getPower() > 0 && isHatch()) {
            IndustrialBioReactorBlockEntity reactor = controller();
            if (reactor != null) {
                reactor.forwardPower(getPower());
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        BlockPos controller = getControllerPos();
        if (controller != null && level.getBlockEntity(controller) instanceof MultiblockEntity
                && MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get(), controller, level);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        if (in.getBooleanOr("hasControllerPos", false)) {
            this.controllerPos = BlockPos.of(in.getLongOr("controllerPos", 0));
        }
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putBoolean("hasControllerPos", this.controllerPos != null);
        if (this.controllerPos != null) {
            out.putLong("controllerPos", this.controllerPos.asLong());
        }
    }
}
