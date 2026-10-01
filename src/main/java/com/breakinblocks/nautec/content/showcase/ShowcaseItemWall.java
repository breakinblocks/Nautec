package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ShowcaseItemWall {
    private ShowcaseItemWall() {
    }

    public static List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (Nautec.MODID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) {
                items.add(item);
            }
        }
        return items;
    }

    public static int rows(int columns) {
        return (items().size() + columns - 1) / columns;
    }

    public static List<ItemFrame> build(ServerLevel level, ShowcaseFrame frame, int columns) {
        List<Item> items = items();
        int rows = (items.size() + columns - 1) / columns;
        BlockState wall = NTBlocks.POLISHED_PRISMARINE.get().defaultBlockState();
        BlockState trim = Blocks.SEA_LANTERN.defaultBlockState();

        for (int x = -1; x <= columns; x++) {
            for (int y = 1; y <= rows + 1; y++) {
                boolean edge = y == rows + 1 || x == -1 || x == columns;
                ShowcaseParts.place(level, frame.at(x, y, 1), edge ? trim : wall);
            }
        }

        Direction facing = frame.dir(Direction.NORTH);
        List<ItemFrame> frames = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            BlockPos pos = frame.at(i % columns, rows - i / columns, 0);
            GlowItemFrame itemFrame = new GlowItemFrame(level, pos, facing);
            itemFrame.setItem(new ItemStack(items.get(i)), false);
            if (level.addFreshEntity(itemFrame)) {
                frames.add(itemFrame);
            }
        }
        return frames;
    }
}
