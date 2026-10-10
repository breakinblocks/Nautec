package com.breakinblocks.nautec.content.blocks.multiblock.semi;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.LaserBlock;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrismarineCrystalBlock extends LaserBlock {
    public PrismarineCrystalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean waterloggable() {
        return true;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.PRISMARINE_CRYSTAL.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PrismarineCrystalBlock::new);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        build(level, pos, Boolean.TRUE.equals(stack.get(NTDataComponents.CULTIVATED)));
    }

    public static void build(Level level, BlockPos core, boolean cultivated) {
        BlockPos firstPos = core.above(2);
        for (int i = 0; i < 6; i++) {
            BlockPos curPos = firstPos.below(i);
            boolean water = level.getFluidState(curPos).getType() == Fluids.WATER;
            if (i == 2) {
                level.setBlockAndUpdate(curPos, NTBlocks.PRISMARINE_CRYSTAL.get().defaultBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, water));
            } else {
                level.setBlockAndUpdate(curPos, NTBlocks.PRISMARINE_CRYSTAL_PART.get().defaultBlockState()
                        .setValue(PrismarineCrystalPartBlock.INDEX, i)
                        .setValue(BlockStateProperties.WATERLOGGED, water));
            }
        }
        if (level.getBlockEntity(core) instanceof PrismarineCrystalBlockEntity crystal) {
            crystal.setCultivated(cultivated);
        }
    }

    public static boolean canBuild(Level level, BlockPos core) {
        BlockPos firstPos = core.above(2);
        for (int i = 0; i < 6; i++) {
            BlockPos curPos = firstPos.below(i);
            if (level.isOutsideBuildHeight(curPos) || !level.getBlockState(curPos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable PrismarineCrystalBlockEntity findCrystal(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PrismarineCrystalBlockEntity crystal) {
            return crystal;
        }
        if (level.getBlockEntity(pos) instanceof PrismarineCrystalPartBlockEntity part
                && level.getBlockEntity(part.getCrystalPos()) instanceof PrismarineCrystalBlockEntity crystal) {
            return crystal;
        }
        return null;
    }

    public static ItemStack cultivatedItem() {
        ItemStack stack = new ItemStack(NTBlocks.PRISMARINE_CRYSTAL.get());
        stack.set(NTDataComponents.CULTIVATED, true);
        return stack;
    }

    public static ItemStack pickUp(Level level, PrismarineCrystalBlockEntity crystal) {
        if (!crystal.isCultivated()) {
            return ItemStack.EMPTY;
        }
        BlockPos topPos = crystal.getBlockPos().above(2);
        for (int i = 0; i < 6; i++) {
            level.removeBlock(topPos.below(i), false);
        }
        level.playSound(null, crystal.getBlockPos(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.5F, 0.7F);
        return cultivatedItem();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) {
            return;
        }
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 0.5 + random.nextDouble() * 0.9;
        level.addParticle(NTParticles.CRYSTAL_MOTE.get(),
                pos.getX() + 0.5 + Math.cos(angle) * distance,
                pos.getY() - 2.7 + random.nextDouble() * 4.5,
                pos.getZ() + 0.5 + Math.sin(angle) * distance,
                0.0, 0.0, 0.0);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        removeCrystal(level, player, pos);
        return true;
    }

    public static void removeCrystal(Level level, Player player, BlockPos thisPos) {
        if (thisPos != null) {
            BlockPos topPos = thisPos.above(2);
            for (int i = 0; i < 6; i++) {
                BlockPos curPos = topPos.below(i);
                level.removeBlock(curPos, false);
            }

            ItemStack mainHandItem = player.getMainHandItem();
            IPowerStorage capability = mainHandItem.getCapability(NTCapabilities.PowerStorage.ITEM);
            if (mainHandItem.is(NTItems.AQUARINE_PICKAXE.get())
                    && Boolean.TRUE.equals(mainHandItem.get(NTDataComponents.ABILITY_ENABLED))
                    && capability.getPowerStored() >= 100
                    && !player.hasInfiniteMaterials()) {
                Containers.dropItemStack(level, thisPos.getX(), thisPos.getY(), thisPos.getZ(), new ItemStack(NTItems.PRISMARINE_CRYSTAL_SHARD.get(), level.getRandom().nextInt(3, 8)));
                capability.tryDrainPower(100, false);
                level.playSound(null, thisPos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS);
            }
        }
    }

}
