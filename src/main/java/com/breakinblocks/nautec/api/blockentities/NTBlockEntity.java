package com.breakinblocks.nautec.api.blockentities;

import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NTBlockEntity extends BlockEntity implements RemovalAware {
    public NTBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadAdditional(TagValueInput.create(registries, tag));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveAdditional(TagValueOutput.wrap(registries, tag));
    }

    protected void loadAdditional(ValueInput input) {
    }

    protected void saveAdditional(ValueOutput output) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        HolderLookup.Provider registries = level != null ? level.registryAccess() : RegistryAccess.EMPTY;
        removeComponentsFromTag(TagValueOutput.wrap(registries, tag));
    }

    public void removeComponentsFromTag(ValueOutput output) {
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    }
}
