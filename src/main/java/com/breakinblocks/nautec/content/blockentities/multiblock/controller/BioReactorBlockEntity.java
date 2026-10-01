package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.menus.BioReactorMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BioReactorBlockEntity extends AbstractBioReactorBlockEntity {
    public static final int COLONIES = 3;
    public static final int NUTRIENT_SLOTS = 2;
    public static final int UPGRADE_SLOTS = 2;

    public BioReactorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BIO_REACTOR.get(), blockPos, blockState, COLONIES, NUTRIENT_SLOTS, UPGRADE_SLOTS);
    }

    @Override
    protected int basePower() {
        return NTConfig.bioReactorPowerBase;
    }

    @Override
    protected int powerPerColony() {
        return NTConfig.bioReactorPowerPerColony;
    }

    @Override
    protected double baseSpeed() {
        return NTConfig.bioReactorBaseSpeed;
    }

    @Override
    public Multiblock multiblock() {
        return NTMultiblocks.BIO_REACTOR.get();
    }

    @Override
    protected Block partBlock() {
        return NTBlocks.BIO_REACTOR_PART.get();
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
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BioReactorMenu(containerId, playerInventory, this);
    }
}
