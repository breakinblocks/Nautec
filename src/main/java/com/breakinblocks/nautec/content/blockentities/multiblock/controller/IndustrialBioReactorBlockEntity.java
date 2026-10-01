package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.content.menus.IndustrialBioReactorMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IndustrialBioReactorBlockEntity extends AbstractBioReactorBlockEntity {
    public static final int COLONIES = 9;
    public static final int NUTRIENT_SLOTS = 3;
    public static final int UPGRADE_SLOTS = 4;
    private static final Set<Direction> LASER_INPUTS = Set.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public IndustrialBioReactorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.INDUSTRIAL_BIO_REACTOR.get(), blockPos, blockState, COLONIES, NUTRIENT_SLOTS, UPGRADE_SLOTS);
    }

    @Override
    protected int basePower() {
        return NTConfig.industrialBioReactorPowerBase;
    }

    @Override
    protected int powerPerColony() {
        return NTConfig.industrialBioReactorPowerPerColony;
    }

    @Override
    protected double baseSpeed() {
        return NTConfig.industrialBioReactorBaseSpeed;
    }

    @Override
    protected boolean canRun() {
        return isFormed();
    }

    @Override
    public Multiblock multiblock() {
        return NTMultiblocks.INDUSTRIAL_BIO_REACTOR.get();
    }

    @Override
    protected Block partBlock() {
        return NTBlocks.INDUSTRIAL_BIO_REACTOR_PART.get();
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return LASER_INPUTS;
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IndustrialBioReactorMenu(containerId, playerInventory, this);
    }
}
