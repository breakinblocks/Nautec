package com.breakinblocks.nautec.content.blockentities.multiblock.controller;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.google.common.collect.ImmutableMap;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.multiblock.part.DrainPartBlock;
import com.breakinblocks.nautec.content.multiblocks.DrainMultiblock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.BlockUtils;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;

import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class DrainBlockEntity extends LaserBlockEntity implements MultiblockEntity {
    private static final int DRAIN_INTERVAL_TICKS = 20;
    private static final int VALVE_TRAVEL_TICKS = 12;
    private static final int VALVE_SPEED = 30;
    private static final int LID_OPEN_TRAVEL_TICKS = 72;
    private static final int LID_OPEN_SPEED = 3;
    private static final int LID_CLOSE_TRAVEL_TICKS = 36;
    private static final int LID_CLOSE_SPEED = -6;
    private static final int VALVE_TO_LID_DELAY = 60;
    private static final int LID_TO_VALVE_DELAY = 30;
    private static final int WATER_COLUMN_SCAN = 64;
    private static final Pattern WORD_SPLIT = Pattern.compile("[/_.-]");
    private static final Map<TagKey<Biome>, Boolean> OCEAN_TAGS = new ConcurrentHashMap<>();
    private static final int OCEAN_CHECK_INTERVAL = 200;
    private boolean oceanKnown;
    private boolean ocean;
    private long oceanCheckedAt;

    private MultiblockData multiblockData;

    private final Rotator valve = new Rotator();
    private final Rotator lid = new Rotator();

    private boolean closing;
    private int valveLidInterval;

    public DrainBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.DRAIN.get(), blockPos, blockState);
        addFluidTank(NTConfig.drainCapacity);
        this.multiblockData = MultiblockData.EMPTY;
    }

    public boolean hasOperatingPower() {
        return getPower() > NTConfig.drainPower;
    }

    public int saltWaterPerSecond() {
        return saltWaterPerSecond(getPower());
    }

    public static int saltWaterPerSecond(int power) {
        if (power <= NTConfig.drainPower) {
            return 0;
        }
        if (NTConfig.drainPower <= 0) {
            return NTConfig.drainSaltWaterAmount;
        }
        double scaled = NTConfig.drainSaltWaterAmount * Math.sqrt(power / (double) NTConfig.drainPower);
        return (int) Math.min(Integer.MAX_VALUE, Math.round(scaled));
    }

    public @Nullable Component open() {
        if (!isFormed()) {
            return Component.translatable("nautec.drain.message.not_formed");
        }
        if (!hasOperatingPower()) {
            return Component.translatable("nautec.drain.message.no_power", NTConfig.drainPower, getPower());
        }

        this.closing = false;
        this.valve.start(VALVE_TRAVEL_TICKS, VALVE_SPEED);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1, 1f);
        setOpen(true);
        update();

        Status blocker = pumpBlocker();
        return blocker != null ? Component.translatable("nautec.drain.message.opened_idle", Component.translatable(blocker.translationKey())) : null;
    }

    public void close() {
        this.lid.start(LID_CLOSE_TRAVEL_TICKS, LID_CLOSE_SPEED);
        this.closing = true;
        this.valveLidInterval = 0;
        update();
    }

    public boolean isMoving() {
        return lid.isMoving() || valve.isMoving() || valveLidInterval > 0;
    }

    public boolean isClosing() {
        return closing;
    }

    public boolean isFormed() {
        BlockState state = getBlockState();
        return state.hasProperty(DrainMultiblock.FORMED) && state.getValue(DrainMultiblock.FORMED);
    }

    public boolean isOpen() {
        BlockState state = getBlockState();
        return state.hasProperty(DrainPartBlock.OPEN) && state.getValue(DrainPartBlock.OPEN);
    }

    public Status getStatus() {
        if (!isFormed()) {
            return Status.NOT_FORMED;
        }
        if (!isOpen()) {
            return hasOperatingPower() ? Status.CLOSED : Status.NO_POWER;
        }
        if (isMoving()) {
            return closing ? Status.CLOSING : Status.OPENING;
        }
        if (!hasOperatingPower()) {
            return Status.NO_POWER;
        }
        Status blocker = pumpBlocker();
        return blocker != null ? blocker : Status.PUMPING;
    }

    public @Nullable Status pumpBlocker() {
        if (!hasWater()) {
            return Status.NO_WATER;
        }
        if (!isOceanBiome()) {
            return Status.NOT_OCEAN;
        }
        if (getFluidTank().getFluidAmount() >= getFluidTank().getCapacity()) {
            return Status.FULL;
        }
        return null;
    }

    private void setOpen(boolean value) {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos selfPos = worldPosition;
        BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelf3x3(selfPos);
        for (BlockPos blockPos : aroundSelf) {
            BlockState state = level.getBlockState(blockPos);
            if (state.hasProperty(DrainPartBlock.OPEN)) {
                level.setBlockAndUpdate(blockPos, state.setValue(DrainPartBlock.OPEN, value));
            }
        }
        BlockState selfState = level.getBlockState(selfPos);
        if (selfState.hasProperty(DrainPartBlock.OPEN)) {
            level.setBlockAndUpdate(selfPos, selfState.setValue(DrainPartBlock.OPEN, value));
        }
    }

    private boolean hasWater() {
        BlockPos selfPos = worldPosition.above();
        BlockPos[] aroundSelf = BlockUtils.getBlocksAroundSelf3x3(selfPos);
        for (BlockPos blockPos : aroundSelf) {
            if (!level.getBlockState(blockPos).getFluidState().is(FluidTags.WATER))
                return false;
        }
        return level.getBlockState(selfPos).getFluidState().is(FluidTags.WATER);
    }

    private boolean isOceanBiome() {
        if (!NTConfig.drainRequiresOcean) {
            return true;
        }
        long now = level.getGameTime();
        if (!oceanKnown || now - oceanCheckedAt >= OCEAN_CHECK_INTERVAL || now < oceanCheckedAt) {
            oceanKnown = true;
            oceanCheckedAt = now;
            ocean = scanOceanBiome();
        }
        return ocean;
    }

    private boolean scanOceanBiome() {
        if (isOcean(level.getBiome(worldPosition))) {
            return true;
        }
        BlockPos.MutableBlockPos cursor = worldPosition.above().mutable();
        for (int i = 0; i < WATER_COLUMN_SCAN && level.getBlockState(cursor.above()).getFluidState().is(FluidTags.WATER); i++) {
            cursor.move(Direction.UP);
        }
        return isOcean(level.getBiome(cursor));
    }

    public static boolean isOcean(Holder<Biome> biome) {
        return biome.tags().anyMatch(tag -> OCEAN_TAGS.computeIfAbsent(tag, DrainBlockEntity::isOceanTag));
    }

    private static boolean isOceanTag(TagKey<Biome> tag) {
        String path = tag.location().getPath();
        String name = path.substring(path.lastIndexOf('/') + 1);
        if (isOceanWord(name)) {
            return true;
        }
        if (!name.startsWith("is_")) {
            return false;
        }
        for (String word : WORD_SPLIT.split(name)) {
            if (isOceanWord(word)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOceanWord(String word) {
        return word.equals("ocean") || word.equals("oceans") || word.equals("sea") || word.equals("seas");
    }

    @Override
    public Set<Direction> getLaserInputs() {
        if (isFormed()) {
            return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        }
        return ObjectSet.of();
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();

        if (!level.isClientSide()) {
            refreshPowerState();
        }

        performRotation();

        performDraining();
    }

    private void refreshPowerState() {
        boolean hasPower = hasOperatingPower();
        BlockState selfState = getBlockState();
        if (!selfState.hasProperty(DrainPartBlock.HAS_POWER) || selfState.getValue(DrainPartBlock.HAS_POWER) == hasPower) {
            return;
        }
        for (BlockPos pos : BlockUtils.getBlocksAroundSelfHorizontal(worldPosition)) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(DrainPartBlock.HAS_POWER)) {
                level.setBlockAndUpdate(pos, state.setValue(DrainPartBlock.HAS_POWER, hasPower));
            }
        }
        level.setBlockAndUpdate(worldPosition, selfState.setValue(DrainPartBlock.HAS_POWER, hasPower));
        updateBubbleColumns();
    }

    private void performDraining() {
        if (level.isClientSide() || level.getGameTime() % DRAIN_INTERVAL_TICKS != 0) {
            return;
        }
        if (isFormed() && isOpen() && !isMoving() && hasOperatingPower() && pumpBlocker() == null) {
            getFluidTank().fill(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), saltWaterPerSecond()));
        }
    }

    private void performRotation() {
        boolean server = !level.isClientSide();
        if (valve.tick()) {
            if (!closing) {
                this.valveLidInterval = VALVE_TO_LID_DELAY;
            } else {
                this.closing = false;
                setOpen(false);
            }
            if (server) {
                update();
            }
        }

        if (valveLidInterval > 0) {
            valveLidInterval--;

            if (valveLidInterval == 0) {
                if (!closing) {
                    lid.start(LID_OPEN_TRAVEL_TICKS, LID_OPEN_SPEED);
                } else {
                    valve.start(VALVE_TRAVEL_TICKS, -VALVE_SPEED);
                }
                if (server) {
                    update();
                }
            }
        }

        if (lid.tick()) {
            if (closing) {
                this.valveLidInterval = LID_TO_VALVE_DELAY;
            } else if (server) {
                updateBubbleColumns();
            }
            if (server) {
                update();
            }
        }
    }

    private void updateBubbleColumns() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos selfPos = worldPosition;
        for (BlockPos blockPos : BlockUtils.getBlocksAroundSelfHorizontal(selfPos)) {
            BubbleColumnBlock.updateColumn(level, blockPos.above(), level.getBlockState(blockPos));
        }
        BubbleColumnBlock.updateColumn(level, selfPos.above(), level.getBlockState(selfPos));
    }

    public float getValveIndependentAngle(float partialTicks) {
        return valve.renderAngle(partialTicks);
    }

    public float getLidIndependentAngle(float partialTicks) {
        return lid.renderAngle(partialTicks);
    }

    private static final class Rotator {
        private float independentAngle;
        private float chasingVelocity;
        private int ticksRemaining;
        private int speed;

        void start(int ticks, int speed) {
            this.ticksRemaining = ticks;
            this.speed = speed;
        }

        boolean isMoving() {
            return ticksRemaining > 0;
        }

        boolean tick() {
            chasingVelocity += ((speed * 10 / 3f) - chasingVelocity) * .25f;
            independentAngle += chasingVelocity;

            if (ticksRemaining > 0 && --ticksRemaining == 0) {
                this.speed = 0;
                return true;
            }
            return false;
        }

        float renderAngle(float partialTicks) {
            return (independentAngle + partialTicks * chasingVelocity) / 360;
        }

        void save(ValueOutput out) {
            out.putFloat("angle", independentAngle);
            out.putFloat("velocity", chasingVelocity);
            out.putInt("ticks", ticksRemaining);
            out.putInt("speed", speed);
        }

        void load(ValueInput in) {
            independentAngle = in.getFloatOr("angle", independentAngle);
            chasingVelocity = in.getFloatOr("velocity", 0);
            ticksRemaining = in.getIntOr("ticks", 0);
            speed = in.getIntOr("speed", 0);
        }
    }

    @Override
    public <T> ImmutableMap<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        if (capability == TransferCapabilities.Fluid.BLOCK) {
            return ImmutableMap.of(
                    Direction.DOWN, Pair.of(IOActions.EXTRACT, new int[]{0})
            );
        }
        return ImmutableMap.of();
    }

    @Override
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            try {
                MultiblockHelper.unform(NTMultiblocks.DRAIN.get(), pos, level, null);
            } finally {
                MultiblockEntity.UNFORMING.set(false);
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
        out.putFloat("angle", this.lid.independentAngle);
        lid.save(out.child("lid"));
        valve.save(out.child("valve"));
        out.putBoolean("closing", closing);
        out.putInt("valveLidInterval", valveLidInterval);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.multiblockData = loadMBData(in.read("multiblockData", CompoundTag.CODEC).orElseGet(CompoundTag::new));
        this.lid.independentAngle = in.getFloatOr("angle", 0);
        in.child("lid").ifPresent(lid::load);
        in.child("valve").ifPresent(valve::load);
        this.closing = in.getBooleanOr("closing", false);
        this.valveLidInterval = in.getIntOr("valveLidInterval", 0);
    }

    public enum Status {
        NOT_FORMED(false),
        NO_POWER(false),
        CLOSED(false),
        OPENING(true),
        CLOSING(false),
        NO_WATER(false),
        NOT_OCEAN(false),
        FULL(false),
        PUMPING(true);

        private final boolean good;

        Status(boolean good) {
            this.good = good;
        }

        public boolean isGood() {
            return good;
        }

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : NOT_FORMED;
        }

        public String translationKey() {
            return "nautec.drain.status." + name().toLowerCase(Locale.ROOT);
        }
    }
}
