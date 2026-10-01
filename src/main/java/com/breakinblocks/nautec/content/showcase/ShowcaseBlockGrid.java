package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ShowcaseBlockGrid {
    public static final int PITCH = 2;
    private static final int SEARCH_HEIGHT = 6;

    private ShowcaseBlockGrid() {
    }

    public static List<Block> blocks() {
        Set<Block> blocks = new LinkedHashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BlockItem blockItem && Nautec.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                blocks.add(blockItem.getBlock());
            }
        }
        return new ArrayList<>(blocks);
    }

    public static int rows(int columns) {
        int count = blocks().size();
        return (count + columns - 1) / columns;
    }

    public static Map<Block, BlockPos> build(ServerLevel level, ShowcaseFrame frame, int columns) {
        List<Block> blocks = blocks();
        Map<Block, BlockPos> placed = new LinkedHashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            BlockPos pos = frame.at((i % columns) * PITCH, 1, (i / columns) * PITCH);
            placeOne(level, frame, pos, block);
            BlockPos found = find(level, pos, block);
            if (found != null) {
                placed.put(block, found);
            }
        }
        return placed;
    }

    private static void placeOne(ServerLevel level, ShowcaseFrame frame, BlockPos pos, Block block) {
        if (needsWater(block)) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                ShowcaseParts.place(level, pos.relative(side), Blocks.GLASS.defaultBlockState());
            }
            ShowcaseParts.place(level, pos, Blocks.WATER.defaultBlockState());
        } else if (block == NTBlocks.CREATIVE_POWER_SOURCE.get()) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                ShowcaseParts.place(level, pos.relative(side), Blocks.GLASS.defaultBlockState());
            }
            ShowcaseParts.place(level, pos.above(), Blocks.GLASS.defaultBlockState());
        }

        ItemStack stack = new ItemStack(block.asItem());
        if (stack.getItem() instanceof BlockItem blockItem) {
            try {
                blockItem.place(new ShowcasePlaceContext(level, pos, frame.forward(), stack));
            } catch (RuntimeException e) {
                Nautec.LOGGER.warn("Showcase could not place {} through its item, using its default state", BuiltInRegistries.BLOCK.getKey(block), e);
            }
        }

        if (find(level, pos, block) == null && block.defaultBlockState().canSurvive(level, pos)) {
            ShowcaseParts.place(level, pos, block.defaultBlockState());
        }
    }

    private static boolean needsWater(Block block) {
        return !block.defaultBlockState().getFluidState().isEmpty();
    }

    private static BlockPos find(ServerLevel level, BlockPos base, Block block) {
        for (int i = 0; i < SEARCH_HEIGHT; i++) {
            BlockPos pos = base.above(i);
            if (level.getBlockState(pos).is(block)) {
                return pos.immutable();
            }
        }
        return null;
    }
}
