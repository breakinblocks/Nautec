package com.breakinblocks.nautec.content.showcase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public record ShowcaseFrame(BlockPos origin, Rotation rotation) {
    public static ShowcaseFrame facing(BlockPos origin, Direction forward) {
        return new ShowcaseFrame(origin, rotationFor(forward));
    }

    public static Rotation rotationFor(Direction forward) {
        return switch (forward) {
            case WEST -> Rotation.CLOCKWISE_90;
            case NORTH -> Rotation.CLOCKWISE_180;
            case EAST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public BlockPos at(int x, int y, int z) {
        return origin.offset(new BlockPos(x, y, z).rotate(rotation));
    }

    public Direction dir(Direction local) {
        return rotation.rotate(local);
    }

    public Direction forward() {
        return dir(Direction.SOUTH);
    }

    public ShowcaseFrame shift(int x, int y, int z) {
        return new ShowcaseFrame(at(x, y, z), rotation);
    }

    public BoundingBox box(int x0, int y0, int z0, int x1, int y1, int z1) {
        return BoundingBox.fromCorners(at(x0, y0, z0), at(x1, y1, z1));
    }

    public AABB aabb(int x0, int y0, int z0, int x1, int y1, int z1) {
        return AABB.of(box(x0, y0, z0, x1, y1, z1));
    }
}
