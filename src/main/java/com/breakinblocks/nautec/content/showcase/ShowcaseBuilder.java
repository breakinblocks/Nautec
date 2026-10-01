package com.breakinblocks.nautec.content.showcase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class ShowcaseBuilder {
    public static final int GRID_COLUMNS = 13;
    public static final int WALL_COLUMNS = 24;
    public static final int HALF_WIDTH = 15;
    public static final int START_Z = 2;
    public static final int HEIGHT = 12;

    private ShowcaseBuilder() {
    }

    public record Result(int blocks, int items, int width, int depth, BoundingBox bounds,
                         ShowcaseLaserPower.Layout laser, ShowcaseIronProduction.Layout iron) {
    }

    public static int depth() {
        return wallZ() + 1 - START_Z + 1;
    }

    private static int gridEndZ() {
        return 6 + ShowcaseBlockGrid.PITCH * (ShowcaseBlockGrid.rows(GRID_COLUMNS) - 1);
    }

    private static int multiblockZ() {
        return gridEndZ() + 6;
    }

    private static int powerZ() {
        return multiblockZ() + 9;
    }

    private static int wallZ() {
        return powerZ() + 7;
    }

    public static BoundingBox bounds(BlockPos feet, Direction facing) {
        ShowcaseFrame frame = ShowcaseFrame.facing(feet.below(), facing);
        return frame.box(-HALF_WIDTH, 0, START_Z, HALF_WIDTH, HEIGHT, wallZ() + 1);
    }

    public static Result build(ServerLevel level, BlockPos feet, Direction facing) {
        ShowcaseFrame frame = ShowcaseFrame.facing(feet.below(), facing);
        int wallZ = wallZ();
        ShowcaseParts.prepareArea(level, frame, -HALF_WIDTH, START_Z, HALF_WIDTH, wallZ + 1, HEIGHT);

        ShowcaseParts.sign(level, frame, 0, 1, 4, ShowcaseParts.title("nautec.showcase.sign.blocks", "Blocks"));
        int blocks = ShowcaseBlockGrid.build(level, frame.shift(-12, 0, 6), GRID_COLUMNS).size();

        int multiblockZ = multiblockZ();
        ShowcaseParts.sign(level, frame, 0, 1, gridEndZ() + 2, ShowcaseParts.title("nautec.showcase.sign.multiblocks", "Multiblocks"));
        ShowcaseMultiblocks.build(level, frame.shift(0, 0, multiblockZ));

        int powerZ = powerZ();
        ShowcaseLaserPower.Layout laser = ShowcaseLaserPower.build(level, frame.shift(-9, 0, powerZ));
        ShowcaseIronProduction.Layout iron = ShowcaseIronProduction.build(level, frame.shift(6, 0, powerZ));

        ShowcaseParts.sign(level, frame, 0, 1, wallZ - 2, ShowcaseParts.title("nautec.showcase.sign.items", "Items"));
        int items = ShowcaseItemWall.build(level, frame.shift(-WALL_COLUMNS / 2, 0, wallZ), WALL_COLUMNS).size();

        BoundingBox bounds = frame.box(-HALF_WIDTH, 0, START_Z, HALF_WIDTH, HEIGHT, wallZ + 1);
        return new Result(blocks, items, HALF_WIDTH * 2 + 1, depth(), bounds, laser, iron);
    }
}
