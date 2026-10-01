package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.LaserJunctionBlockEntity;
import com.breakinblocks.nautec.content.blocks.LaserJunctionBlock;
import com.breakinblocks.nautec.content.recipes.AquaticCatalystChannelingRecipe;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public final class ShowcaseParts {
    public static final int CLEAR_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
    public static final ItemStack CATALYST_FUEL = new ItemStack(Items.PRISMARINE_SHARD);
    public static final int FUEL_STACKS = 9;

    private ShowcaseParts() {
    }

    public static void prepareArea(ServerLevel level, ShowcaseFrame frame, int x0, int z0, int x1, int z1, int height) {
        AABB area = frame.aabb(x0, 0, z0, x1, height, z1).inflate(1);
        discardLoose(level, area);

        for (BlockPos pos : BlockPos.betweenClosed(frame.at(x0, 1, z0), frame.at(x1, height, z1))) {
            if (level.getBlockEntity(pos) instanceof GatewayBlockEntity) {
                GatewayIndex.get(level).remove(pos.immutable());
            }
        }

        for (int y = height; y >= 1; y--) {
            for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    level.setBlock(frame.at(x, y, z), Blocks.AIR.defaultBlockState(), CLEAR_FLAGS);
                }
            }
        }

        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                boolean light = Math.floorMod(x, 6) == 0 && Math.floorMod(z, 6) == 0;
                BlockState floor = light ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.DARK_PRISMARINE.defaultBlockState();
                level.setBlock(frame.at(x, 0, z), floor, CLEAR_FLAGS);
            }
        }

        discardLoose(level, area);
    }

    private static void discardLoose(ServerLevel level, AABB area) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area,
                e -> e instanceof ItemEntity || e instanceof HangingEntity || e instanceof ExperienceOrb)) {
            entity.discard();
        }
    }

    public static void place(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    public static void pillar(ServerLevel level, ShowcaseFrame frame, int x, int z, int fromY, int toY) {
        for (int y = fromY; y <= toY; y++) {
            place(level, frame.at(x, y, z), NTBlocks.DARK_PRISMARINE_PILLAR.get().defaultBlockState());
        }
    }

    public static int catalystPower(ServerLevel level) {
        return level.recipeAccess()
                .getRecipeFor(AquaticCatalystChannelingRecipe.Type.INSTANCE, new SingleRecipeInput(CATALYST_FUEL.copy()), level)
                .map(holder -> holder.value().powerAmount() / Math.max(1, holder.value().duration()))
                .orElse(0);
    }

    public static BlockPos fedCatalyst(ServerLevel level, ShowcaseFrame frame, int x, int y, int z, Direction beam) {
        BlockPos pos = frame.at(x, y, z);
        place(level, pos, NTBlocks.AQUATIC_CATALYST.get().defaultBlockState()
                .setValue(BlockStateProperties.FACING, frame.dir(beam.getOpposite())));
        hopperWithChest(level, frame, x, y + 1, z, Direction.DOWN, CATALYST_FUEL, FUEL_STACKS);
        return pos;
    }

    public static BlockPos hopperWithChest(ServerLevel level, ShowcaseFrame frame, int x, int y, int z, Direction output, ItemStack contents, int stacks) {
        BlockPos hopperPos = frame.at(x, y, z);
        place(level, hopperPos, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, frame.dir(output)));
        if (level.getBlockEntity(hopperPos) instanceof HopperBlockEntity hopper) {
            hopper.setItem(0, contents.copyWithCount(contents.getMaxStackSize()));
            hopper.setChanged();
        }
        chest(level, frame.at(x, y + 1, z), frame.dir(Direction.NORTH), contents, stacks);
        return hopperPos;
    }

    public static void chest(ServerLevel level, BlockPos pos, Direction facing, ItemStack contents, int stacks) {
        place(level, pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            int slots = Math.min(stacks, chest.getContainerSize());
            for (int slot = 0; slot < slots; slot++) {
                chest.setItem(slot, contents.copyWithCount(contents.getMaxStackSize()));
            }
            chest.setChanged();
        }
    }

    public static BlockPos junction(ServerLevel level, ShowcaseFrame frame, int x, int y, int z, Set<Direction> inputs, Set<Direction> outputs) {
        BlockPos pos = frame.at(x, y, z);
        BlockState state = NTBlocks.LASER_JUNCTION.get().defaultBlockState();
        for (Direction local : inputs) {
            state = state.setValue(LaserJunctionBlock.CONNECTION[frame.dir(local).ordinal()], LaserJunctionBlock.ConnectionType.INPUT);
        }
        for (Direction local : outputs) {
            state = state.setValue(LaserJunctionBlock.CONNECTION[frame.dir(local).ordinal()], LaserJunctionBlock.ConnectionType.OUTPUT);
        }
        place(level, pos, state);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LaserJunctionBlockEntity junction) {
            for (Direction local : inputs) {
                junction.getLaserInputs().add(frame.dir(local));
            }
            for (Direction local : outputs) {
                junction.getLaserOutputs().add(frame.dir(local));
            }
            junction.setChanged();
        }
        return pos;
    }

    public static void sign(ServerLevel level, ShowcaseFrame frame, int x, int y, int z, Component... lines) {
        BlockPos pos = frame.at(x, y, z);
        int rotation = RotationSegment.convertToSegment(frame.forward().toYRot() + 180.0F);
        place(level, pos, Blocks.WARPED_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, rotation));
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            SignText text = new SignText();
            for (int i = 0; i < Math.min(lines.length, 4); i++) {
                text = text.setMessage(i, lines[i]);
            }
            sign.setText(text, true);
            sign.setWaxed(true);
        }
    }

    public static Component title(String key, String fallback) {
        return Component.translatableWithFallback(key, fallback);
    }

    public static Component apLine(int ap) {
        return Component.translatableWithFallback("nautec.showcase.sign.ap", "%s AP", ap);
    }
}
