package com.breakinblocks.nautec.content.biometank;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BiomeTankBlockEntity extends ContainerBlockEntity {
    private final BiomeTankType type;
    private int progress;

    public BiomeTankBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.BIOME_TANK.get(), pos, state);
        this.type = state.getBlock() instanceof BiomeTankBlock tank ? tank.getType() : BiomeTankType.KELP;
        addItemHandler(1, (slot, stack) -> false);
    }

    public BiomeTankType getTankType() {
        return type;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack output = getItemStackHandler().getStackInSlot(0);
        if (output.getCount() >= output.getMaxStackSize()) {
            return;
        }
        progress++;
        if (progress >= NTConfig.biomeTankTicks) {
            progress = 0;
            forceInsertItem(0, new ItemStack(type.plant()), false);
        }
    }

    public ItemStack takeOutput() {
        return forceExtractItem(0, 64, false);
    }

    @Override
    public void update() {
        setChanged();
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandlerOnSide(Direction direction) {
        return getItemHandler();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        drop();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("progress", progress);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.progress = in.getIntOr("progress", 0);
    }
}
