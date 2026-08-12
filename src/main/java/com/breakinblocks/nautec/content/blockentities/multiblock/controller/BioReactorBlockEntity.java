package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.menus.BioReactorMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class BioReactorBlockEntity extends LaserBlockEntity implements MenuProvider, MultiblockEntity {
    private MultiblockData multiblockData;
    private final float[] progress;

    public BioReactorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BIO_REACTOR.get(), blockPos, blockState);
        addItemHandler(3);
        addBacteriaStorage(3);
        this.multiblockData = MultiblockData.EMPTY;
        this.progress = new float[3];
    }

    public int getActiveColonies() {
        IBacteriaStorage storage = getBacteriaStorage();
        int active = 0;
        for (int i = 0; i < this.progress.length; i++) {
            if (!storage.getBacteria(i).isEmpty()) {
                active++;
            }
        }
        return active;
    }

    public int getRequiredPower() {
        return NTConfig.bioReactorPowerBase + NTConfig.bioReactorPowerPerColony * getActiveColonies();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (getPower() >= getRequiredPower()) {
            IBacteriaStorage storage = getBacteriaStorage();
            for (int i = 0; i < this.progress.length; i++) {
                BacteriaInstance bacteria = storage.getBacteria(i);
                if (bacteria.isEmpty()) {
                    this.progress[i] = 0;
                    continue;
                }

                this.progress[i] += productionPerTick(bacteria);
                bacteria.addAge(1);

                if (this.progress[i] >= 100) {
                    this.progress[i] -= 100;

                    if (!level.isClientSide()) {
                        produce(i, bacteria, storage);
                    }
                }
            }
        } else {
            Arrays.fill(this.progress, 0);
        }
    }

    private void produce(int slot, BacteriaInstance bacteria, IBacteriaStorage storage) {
        Bacteria definition = BacteriaHelper.getBacteria(level.registryAccess(), bacteria.getBacteria());

        Item produced = definition.resource().resolve();
        if (produced != Items.AIR) {
            getItemStackHandler().insertItem(slot, produced.getDefaultInstance(), false);
        }

        if (bacteria.isSenescent()) {
            long decay = Math.max(1, (long) Math.ceil(bacteria.getSize() * NTConfig.bioReactorDecayFraction));
            storage.extractBacteria(slot, decay, false);
        }
    }

    public static float productionPerTick(BacteriaInstance bacteria) {
        long cap = Math.max(1, NTConfig.bacteriaColonySizeCap);
        float sizeFactor = 0.5f + 0.5f * ((float) Math.min(bacteria.getSize(), cap) / cap);

        return (float) (bacteria.getStats().productionRate() * sizeFactor * NTConfig.bioReactorBaseSpeed);
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Bio Reactor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BioReactorMenu(containerId, playerInventory, this);
    }

    @Override
    public MultiblockData getMultiblockData() {
        return this.multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(NTMultiblocks.BIO_REACTOR.get(), pos, level);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
        for (int i = 0; i < this.progress.length; i++) {
            out.putFloat("progress" + i, this.progress[i]);
        }
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.multiblockData = loadMBData(in.read("multiblockData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        int[] legacyProgress = in.getIntArray("progress").orElse(new int[0]);
        for (int i = 0; i < this.progress.length; i++) {
            float legacy = i < legacyProgress.length ? legacyProgress[i] : 0;
            this.progress[i] = in.getFloatOr("progress" + i, legacy);
        }
    }

    public float getProgress(int i) {
        return progress[i];
    }
}
