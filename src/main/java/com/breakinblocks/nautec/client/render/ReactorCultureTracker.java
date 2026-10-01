package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class ReactorCultureTracker {
    public static final float FLASH_TICKS = 14.0F;
    private static final int PULSE_GAP_TICKS = 10;
    private static final int RESYNC_GAP_TICKS = 40;
    private static final float PROGRESS_DROP = 40.0F;
    private static final float ACTIVE_FADE_TICKS = 16.0F;
    private static final int DEFAULT_TINT = 0x38E4FF;
    private static final Map<AbstractBioReactorBlockEntity, ReactorCultureTracker> TRACKERS = new WeakHashMap<>();

    private final int colonies;
    private final ResourceKey<?>[] keys;
    private final int[] colors;
    private final float[] lastProgress;
    private final int[] lastOutput;
    private final long[] pulseTick;
    private final int[] palette;
    private final float[] flash;
    private int paletteSize;
    private int averageColor = DEFAULT_TINT;
    private long lastSampleTick = Long.MIN_VALUE;
    private float lastFrameTime = Float.NaN;
    private float activeLevel;
    private float peakFlash;
    private int peakFlashColor = DEFAULT_TINT;
    private @Nullable AABB bounds;

    private ReactorCultureTracker(int colonies) {
        this.colonies = colonies;
        this.keys = new ResourceKey<?>[colonies];
        this.colors = new int[colonies];
        this.lastProgress = new float[colonies];
        this.lastOutput = new int[colonies];
        this.pulseTick = new long[colonies];
        this.palette = new int[colonies];
        this.flash = new float[colonies];
        for (int i = 0; i < colonies; i++) {
            this.pulseTick[i] = -1000L;
        }
    }

    public static ReactorCultureTracker of(AbstractBioReactorBlockEntity blockEntity) {
        ReactorCultureTracker tracker = TRACKERS.get(blockEntity);
        if (tracker == null || tracker.colonies != blockEntity.getColonySlots()) {
            tracker = new ReactorCultureTracker(blockEntity.getColonySlots());
            TRACKERS.put(blockEntity, tracker);
        }
        return tracker;
    }

    public AABB bounds(AbstractBioReactorBlockEntity blockEntity, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        AABB box = this.bounds;
        if (box == null) {
            double x = blockEntity.getBlockPos().getX();
            double y = blockEntity.getBlockPos().getY();
            double z = blockEntity.getBlockPos().getZ();
            box = new AABB(x + minX, y + minY, z + minZ, x + maxX, y + maxY, z + maxZ);
            this.bounds = box;
        }
        return box;
    }

    public void update(AbstractBioReactorBlockEntity blockEntity, Level level, float partialTick) {
        long gameTime = level.getGameTime();
        float now = gameTime + partialTick;
        float dt = Float.isNaN(this.lastFrameTime) ? 0.0F : Mth.clamp(now - this.lastFrameTime, 0.0F, 10.0F);
        this.lastFrameTime = now;

        float target = blockEntity.isActive() ? 1.0F : 0.0F;
        float step = dt / ACTIVE_FADE_TICKS;
        this.activeLevel = this.activeLevel < target
                ? Math.min(target, this.activeLevel + step)
                : Math.max(target, this.activeLevel - step);

        if (gameTime != this.lastSampleTick) {
            sample(blockEntity, level, gameTime);
        }

        this.peakFlash = 0.0F;
        for (int i = 0; i < this.colonies; i++) {
            float age = now - this.pulseTick[i];
            float value = 0.0F;
            if (age >= 0.0F && age < FLASH_TICKS && this.keys[i] != null) {
                float left = 1.0F - age / FLASH_TICKS;
                value = left * left;
            }
            this.flash[i] = value;
            if (value > this.peakFlash) {
                this.peakFlash = value;
                this.peakFlashColor = this.colors[i];
            }
        }
    }

    private void sample(AbstractBioReactorBlockEntity blockEntity, Level level, long gameTime) {
        boolean resync = this.lastSampleTick == Long.MIN_VALUE || gameTime - this.lastSampleTick > RESYNC_GAP_TICKS || gameTime < this.lastSampleTick;
        this.lastSampleTick = gameTime;
        IBacteriaStorage storage = blockEntity.getBacteriaStorage();
        boolean paletteDirty = false;
        for (int i = 0; i < this.colonies; i++) {
            BacteriaInstance bacteria = storage.getBacteria(i);
            ResourceKey<Bacteria> key = bacteria.isEmpty() ? null : bacteria.getBacteria();
            if (key != this.keys[i] && (key == null || !key.equals(this.keys[i]))) {
                this.keys[i] = key;
                this.colors[i] = key == null ? DEFAULT_TINT : colorOf(level, key);
                paletteDirty = true;
            }

            float progress = blockEntity.getProgress(i);
            int output = blockEntity.getItemStackHandler().getStackInSlot(blockEntity.outputSlot(i)).getCount();
            if (!resync && key != null && gameTime - this.pulseTick[i] > PULSE_GAP_TICKS) {
                boolean wrapped = progress > 0.0F && progress + PROGRESS_DROP < this.lastProgress[i];
                if (wrapped || output > this.lastOutput[i]) {
                    this.pulseTick[i] = gameTime;
                }
            }
            this.lastProgress[i] = progress;
            this.lastOutput[i] = output;
        }
        if (paletteDirty) {
            rebuildPalette();
        }
    }

    private void rebuildPalette() {
        int size = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < this.colonies; i++) {
            if (this.keys[i] == null) {
                continue;
            }
            int color = this.colors[i];
            this.palette[size++] = color;
            red += (color >> 16) & 0xFF;
            green += (color >> 8) & 0xFF;
            blue += color & 0xFF;
        }
        this.paletteSize = size;
        this.averageColor = size == 0 ? DEFAULT_TINT : ReactorFx.brighten(((red / size) << 16) | ((green / size) << 8) | (blue / size));
    }

    private static int colorOf(Level level, ResourceKey<Bacteria> key) {
        Bacteria bacteria = BacteriaHelper.findBacteria(level.registryAccess(), key);
        if (bacteria == null) {
            return DEFAULT_TINT;
        }
        return ReactorFx.brighten(bacteria.stats().color() & 0xFFFFFF);
    }

    public int colonies() {
        return this.colonies;
    }

    public boolean hasColony(int colony) {
        return this.keys[colony] != null;
    }

    public int colonyColor(int colony) {
        return this.colors[colony];
    }

    public float flash(int colony) {
        return this.flash[colony];
    }

    public float peakFlash() {
        return this.peakFlash;
    }

    public int peakFlashColor() {
        return this.peakFlashColor;
    }

    public float activeLevel() {
        return this.activeLevel;
    }

    public int averageColor() {
        return this.averageColor;
    }

    public int paletteSize() {
        return this.paletteSize;
    }

    public int paletteColor(int index) {
        return this.palette[index];
    }
}
