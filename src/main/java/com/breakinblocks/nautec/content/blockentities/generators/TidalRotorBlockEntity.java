package com.breakinblocks.nautec.content.blockentities.generators;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Locale;

public class TidalRotorBlockEntity extends FeGeneratorBlockEntity {
    public static final int BUFFER = 10_000;
    public static final int MAX_DEPTH = 24;
    private static final int SCAN_INTERVAL = 100;

    private Status status = Status.DRY;
    private int rate;
    private float openWater;
    private int depth;

    public TidalRotorBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.TIDAL_ROTOR.get(), pos, state, BUFFER);
    }

    public Status getStatus() {
        return status;
    }

    public int getRate() {
        return rate;
    }

    public float getOpenWater() {
        return openWater;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    protected void serverTick(ServerLevel level) {
        if ((level.getGameTime() + worldPosition.hashCode()) % SCAN_INTERVAL == 0 || rate == 0 && status == Status.DRY && level.getGameTime() % 20 == 0) {
            scan(level);
        }
        if (rate <= 0) {
            return;
        }
        generate(rate);
        status = getOutput() > 0 ? Status.RUNNING : Status.BUFFER_FULL;
    }

    public void scan(ServerLevel level) {
        if (!getBlockState().getValue(BlockStateProperties.WATERLOGGED)) {
            status = Status.DRY;
            rate = 0;
            return;
        }
        if (NTConfig.drainRequiresOcean && !DrainBlockEntity.isOcean(level.getBiome(worldPosition))) {
            status = Status.NOT_OCEAN;
            rate = 0;
            return;
        }
        int water = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    cursor.setWithOffset(worldPosition, x, y, z);
                    if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                        water++;
                    }
                }
            }
        }
        int above = 0;
        cursor.set(worldPosition);
        for (int i = 1; i <= MAX_DEPTH; i++) {
            cursor.move(0, 1, 0);
            if (!level.getFluidState(cursor).is(FluidTags.WATER)) {
                break;
            }
            above++;
        }
        this.openWater = water / 26F;
        this.depth = above;
        double quality = Math.min(1.0, 0.5 * openWater + 0.5 * (above / (double) MAX_DEPTH));
        int min = NTConfig.tidalRotorMinOutput;
        int max = Math.max(min, NTConfig.tidalRotorMaxOutput);
        this.rate = (int) Math.round(min + (max - min) * quality);
        this.status = Status.RUNNING;
    }

    public enum Status {
        RUNNING,
        DRY,
        NOT_OCEAN,
        BUFFER_FULL;

        public String translationKey() {
            return "nautec.tidal_rotor.status." + name().toLowerCase(Locale.ROOT);
        }

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : DRY;
        }
    }
}
