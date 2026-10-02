package com.breakinblocks.nautec.content.blockentities.fusion;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blocks.fusion.LaserInjectorBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record FusionStructure(Problem problem, @Nullable BlockPos problemPos, @Nullable BlockPos core, int radius,
                              List<BlockPos> injectors, List<BlockPos> ports, int coils,
                              @Nullable BlockPos topCollector, @Nullable BlockPos bottomCollector) {
    public static final int MIN_RADIUS = 2;
    public static final int MAX_RADIUS = 5;
    public static final int FLOOR_DROP = 4;
    public static final int CEILING_RISE = 3;
    public static final int MAX_INJECTORS = 4;

    private static final int[] CHAMBER_CONTAINMENT = {0, 0, 20_000, 50_000, 100_000, 200_000};

    public static final FusionStructure UNSCANNED = failed(Problem.NO_CRYSTAL, null, null, 0);

    public boolean formed() {
        return problem == Problem.NONE;
    }

    public int interiorWidth() {
        return radius * 2 - 1;
    }

    public int ceiling() {
        if (!formed()) {
            return 0;
        }
        long total = (long) CHAMBER_CONTAINMENT[radius] + (long) coils * NTConfig.fusionCoilContainment;
        return (int) Math.min(NTConfig.fusionMaxOutput, total);
    }

    public static int chamberContainment(int radius) {
        return radius >= MIN_RADIUS && radius <= MAX_RADIUS ? CHAMBER_CONTAINMENT[radius] : 0;
    }

    public boolean contains(BlockPos pos) {
        if (core == null) {
            return false;
        }
        return Math.abs(pos.getX() - core.getX()) <= radius
                && Math.abs(pos.getZ() - core.getZ()) <= radius
                && pos.getY() >= core.getY() - FLOOR_DROP
                && pos.getY() <= core.getY() + CEILING_RISE;
    }

    private static FusionStructure failed(Problem problem, @Nullable BlockPos at, @Nullable BlockPos core, int radius) {
        return new FusionStructure(problem, at, core, radius, List.of(), List.of(), 0, null, null);
    }

    public static FusionStructure scan(Level level, BlockPos controller, Direction front) {
        Direction inward = front.getOpposite();
        Direction lateral = inward.getClockWise();
        PrismarineCrystalBlockEntity crystal = null;
        int radius = 0;
        search:
        for (int distance = MIN_RADIUS; distance <= MAX_RADIUS; distance++) {
            BlockPos ahead = controller.relative(inward, distance);
            for (int offset = -(distance - 1); offset <= distance - 1; offset++) {
                BlockPos column = ahead.relative(lateral, offset);
                if (!level.isLoaded(column)) {
                    continue;
                }
                PrismarineCrystalBlockEntity found = PrismarineCrystalBlock.findCrystal(level, column);
                if (found != null) {
                    crystal = found;
                    radius = distance;
                    break search;
                }
            }
        }
        if (crystal == null) {
            return failed(Problem.NO_CRYSTAL, null, null, 0);
        }

        BlockPos core = crystal.getBlockPos();
        int minX = core.getX() - radius;
        int maxX = core.getX() + radius;
        int minY = core.getY() - FLOOR_DROP;
        int maxY = core.getY() + CEILING_RISE;
        int minZ = core.getZ() - radius;
        int maxZ = core.getZ() + radius;

        if (controller.getY() <= minY || controller.getY() >= maxY) {
            return failed(Problem.CONTROLLER, controller, core, radius);
        }
        if (!level.hasChunksAt(minX, minZ, maxX, maxZ)) {
            return failed(Problem.UNLOADED, null, core, radius);
        }

        List<BlockPos> injectors = new ArrayList<>();
        List<BlockPos> ports = new ArrayList<>();
        int coils = 0;
        BlockPos top = null;
        BlockPos bottom = null;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    boolean onX = x == minX || x == maxX;
                    boolean onY = y == minY || y == maxY;
                    boolean onZ = z == minZ || z == maxZ;
                    int faces = (onX ? 1 : 0) + (onY ? 1 : 0) + (onZ ? 1 : 0);

                    if (faces == 0) {
                        boolean column = x == core.getX() && z == core.getZ();
                        if (column) {
                            if (!state.is(NTBlocks.PRISMARINE_CRYSTAL.get()) && !state.is(NTBlocks.PRISMARINE_CRYSTAL_PART.get())) {
                                return failed(Problem.INTERIOR, cursor.immutable(), core, radius);
                            }
                        } else if (!state.isAir() && !state.is(Blocks.WATER)) {
                            return failed(Problem.INTERIOR, cursor.immutable(), core, radius);
                        }
                        continue;
                    }

                    if (faces >= 2) {
                        if (!state.is(NTBlocks.FUSION_CASING.get())) {
                            return failed(Problem.FRAME, cursor.immutable(), core, radius);
                        }
                        continue;
                    }

                    if (cursor.equals(controller)) {
                        continue;
                    }
                    if (state.is(NTBlocks.FUSION_CASING.get()) || state.is(NTBlocks.AQUAMARINE_STRUCTURAL_GLASS.get())) {
                        continue;
                    }
                    if (state.is(NTBlocks.CONTAINMENT_COIL.get())) {
                        coils++;
                        continue;
                    }
                    if (state.is(NTBlocks.FUSION_PORT.get())) {
                        ports.add(cursor.immutable());
                        continue;
                    }
                    if (state.is(NTBlocks.LASER_INJECTOR.get())) {
                        boolean slot = y == core.getY() && ((onX && z == core.getZ()) || (onZ && x == core.getX()));
                        Direction toCore = onX ? (x == minX ? Direction.EAST : Direction.WEST) : (z == minZ ? Direction.SOUTH : Direction.NORTH);
                        if (!slot || state.getValue(LaserInjectorBlock.FACING) != toCore) {
                            return failed(Problem.INJECTOR, cursor.immutable(), core, radius);
                        }
                        injectors.add(cursor.immutable());
                        continue;
                    }
                    if (state.is(NTBlocks.FUSION_COLLECTOR.get()) && onY && x == core.getX() && z == core.getZ()) {
                        if (y == maxY) {
                            top = cursor.immutable();
                        } else {
                            bottom = cursor.immutable();
                        }
                        continue;
                    }
                    return failed(Problem.SHELL, cursor.immutable(), core, radius);
                }
            }
        }

        if (!crystal.isCultivated()) {
            return failed(Problem.WILD_CRYSTAL, core, core, radius);
        }
        if (top == null || bottom == null) {
            return failed(Problem.NO_COLLECTOR, top == null ? new BlockPos(core.getX(), maxY, core.getZ()) : new BlockPos(core.getX(), minY, core.getZ()), core, radius);
        }
        if (injectors.isEmpty()) {
            return failed(Problem.NO_INJECTOR, null, core, radius);
        }
        return new FusionStructure(Problem.NONE, null, core, radius, List.copyOf(injectors), List.copyOf(ports), coils, top, bottom);
    }

    public enum Problem {
        NONE,
        NO_CRYSTAL,
        CONTROLLER,
        UNLOADED,
        INTERIOR,
        FRAME,
        SHELL,
        INJECTOR,
        WILD_CRYSTAL,
        NO_COLLECTOR,
        NO_INJECTOR;

        public String translationKey() {
            return "nautec.fusion.problem." + name().toLowerCase(Locale.ROOT);
        }

        public static Problem byId(int id) {
            Problem[] values = values();
            return id >= 0 && id < values.length ? values[id] : NO_CRYSTAL;
        }
    }
}
