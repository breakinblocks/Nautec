package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.CrystalPurity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.BacterialEnrichmentCatalystBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.content.enrichment.EnrichmentRules;
import com.breakinblocks.nautec.content.menus.BacterialEnrichmentCatalystMenu;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.utils.DataSlots;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.util.GeckoLibUtil;

import java.util.Map;
import java.util.Set;

public class BacterialEnrichmentCatalystBlockEntity extends LaserBlockEntity implements MenuProvider, GeoBlockEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WORKING_ANIMATION = RawAnimation.begin().thenLoop("working");
    public static final DataTicket<Boolean> WORKING = DataTicket.create("nautec:enrichment_catalyst_working", Boolean.class);

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);

    public static final int STATUS_NO_CRYSTAL = 0;
    public static final int STATUS_WILD_CRYSTAL = 1;
    public static final int STATUS_SHARED = 2;
    public static final int STATUS_TOO_IMPURE = 3;
    public static final int STATUS_NO_SOURCE = 4;
    public static final int STATUS_LOW_POWER = 5;
    public static final int STATUS_NO_COLONY = 6;
    public static final int STATUS_SMALL_COLONY = 7;
    public static final int STATUS_OVERREACH = 8;
    public static final int STATUS_AT_TARGET = 9;
    public static final int STATUS_AT_CAP = 10;
    public static final int STATUS_PAUSED = 11;
    public static final int STATUS_RUNNING = 12;

    public static final int OUTCOME_NONE = 0;
    public static final int OUTCOME_SUCCESS = 1;
    public static final int OUTCOME_REGRESSION = 2;

    public static final int BUTTON_TARGET_DOWN = 0;
    public static final int BUTTON_TARGET_UP = 1;
    public static final int BUTTON_TARGET_DOWN_FINE = 2;
    public static final int BUTTON_TARGET_UP_FINE = 3;

    public static final int DATA_STATUS = 0;
    public static final int DATA_PROGRESS = 1;
    public static final int DATA_CYCLE_AP = 3;
    public static final int DATA_POWER = 5;
    public static final int DATA_CRYSTAL_PURITY = 7;
    public static final int DATA_SOURCE_PURITY = 8;
    public static final int DATA_TARGET_PURITY = 9;
    public static final int DATA_CHANCE = 10;
    public static final int DATA_COLONY_COST = 11;
    public static final int DATA_COUNT = 13;

    public static final int DISH_IN = 0;
    public static final int DISH_OUT = 1;
    public static final int DISH_EMPTY_OUT = 2;
    private static final int[] LOAD_SLOTS = {0};
    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{DISH_IN}, new int[]{DISH_OUT, DISH_EMPTY_OUT});

    private static final int OWNER_CHECK_INTERVAL = 20;
    private static final float TARGET_STEP = 0.5f;
    private static final float TARGET_STEP_FINE = 0.1f;

    private long progress;
    private float targetPurity = CrystalPurity.MAX;
    private boolean paused;
    private int status = STATUS_NO_CRYSTAL;
    private int lastOutcome = OUTCOME_NONE;
    private long outcomeTick;
    private float shownCrystalPurity;
    private float shownSourcePurity;
    private @Nullable BlockPos targetCore;
    private boolean owner = true;
    private int ownerCheck;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            float crystal = shownCrystalPurity;
            float source = shownSourcePurity;
            return switch (index) {
                case DATA_STATUS -> status;
                case DATA_PROGRESS -> DataSlots.low(clampInt(progress));
                case DATA_PROGRESS + 1 -> DataSlots.high(clampInt(progress));
                case DATA_CYCLE_AP -> DataSlots.low(clampInt(EnrichmentRules.cycleAp(crystal)));
                case DATA_CYCLE_AP + 1 -> DataSlots.high(clampInt(EnrichmentRules.cycleAp(crystal)));
                case DATA_POWER -> DataSlots.low(getPower());
                case DATA_POWER + 1 -> DataSlots.high(getPower());
                case DATA_CRYSTAL_PURITY -> Math.round(crystal * 100f);
                case DATA_SOURCE_PURITY -> Math.round(source * 100f);
                case DATA_TARGET_PURITY -> Math.round(targetPurity * 100f);
                case DATA_CHANCE -> Math.round(EnrichmentRules.regressionChance(crystal, source) * 1000f);
                case DATA_COLONY_COST -> DataSlots.low(clampInt(colonyCost(crystal, source)));
                case DATA_COLONY_COST + 1 -> DataSlots.high(clampInt(colonyCost(crystal, source)));
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public BacterialEnrichmentCatalystBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.BACTERIAL_ENRICHMENT_CATALYST.get(), blockPos, blockState);
        addBacteriaStorage(1);
        addItemHandler(3, 1, (slot, stack) -> slot == DISH_IN && DishPort.isDish(stack));
        ownerCheck = Math.floorMod(blockPos.hashCode(), OWNER_CHECK_INTERVAL);
    }

    private static int clampInt(long value) {
        return (int) Math.clamp(value, 0L, Integer.MAX_VALUE);
    }

    public Direction facing() {
        return getBlockState().getValue(BacterialEnrichmentCatalystBlock.FACING);
    }

    public BlockPos behind() {
        return worldPosition.relative(facing().getOpposite());
    }

    public @Nullable PrismarineCrystalBlockEntity targetCrystal() {
        return level == null ? null : PrismarineCrystalBlock.findCrystal(level, behind());
    }

    public @Nullable BlockPos targetCore() {
        return targetCore;
    }

    public ContainerData getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

    public long getProgress() {
        return progress;
    }

    public float getTargetPurity() {
        return targetPurity;
    }

    public void setTargetPurity(float purity) {
        float clamped = CrystalPurity.snap(Math.clamp(purity, EnrichmentRules.minPurity(), EnrichmentRules.maxPurity()));
        if (clamped != targetPurity) {
            targetPurity = clamped;
            setChanged();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            this.paused = paused;
            setChanged();
        }
    }

    public int getLastOutcome() {
        return lastOutcome;
    }

    public long getOutcomeTick() {
        return outcomeTick;
    }

    public float getShownCrystalPurity() {
        return shownCrystalPurity;
    }

    public float getShownSourcePurity() {
        return shownSourcePurity;
    }

    public boolean isRunning() {
        return status == STATUS_RUNNING;
    }

    public void pressButton(int id) {
        switch (id) {
            case BUTTON_TARGET_DOWN -> setTargetPurity(targetPurity - TARGET_STEP);
            case BUTTON_TARGET_UP -> setTargetPurity(targetPurity + TARGET_STEP);
            case BUTTON_TARGET_DOWN_FINE -> setTargetPurity(targetPurity - TARGET_STEP_FINE);
            case BUTTON_TARGET_UP_FINE -> setTargetPurity(targetPurity + TARGET_STEP_FINE);
            default -> {
            }
        }
    }

    @Override
    public int getRequiredPower() {
        return NTConfig.enrichmentMinPower;
    }

    @Override
    public float beamSpeed() {
        int required = getRequiredPower();
        if (getPower() < required) {
            return 0F;
        }
        return required <= 0 ? 1F : (float) getPower() / required;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.UP, Direction.DOWN);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected boolean acceptsNow(int slot, ItemResource resource) {
        if (slot != DISH_IN) {
            return true;
        }
        return DishPort.accepts(this, resource, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::unloadSlot, BacterialEnrichmentCatalystBlockEntity::loadable);
    }

    private static boolean loadable(BacteriaInstance colony) {
        return colony.is(NTBacterias.PRISMATOPHILES);
    }

    private int unloadSlot() {
        return getBacteriaStorage().getBacteria(0).isEmpty() ? DishPort.NONE : 0;
    }

    public boolean acceptsSource(BlockPos originPos) {
        if (level == null || !(level.getBlockEntity(originPos) instanceof PrismarineCrystalPartBlockEntity part)) {
            return false;
        }
        return targetCore == null || !targetCore.equals(part.getCrystalPos());
    }

    @Override
    public void acceptBeam(int amount, float beamPurity, Direction direction, BlockPos originPos, LongSet trail) {
        if (acceptsSource(originPos)) {
            super.acceptBeam(amount, beamPurity, direction, originPos, trail);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            paused = level.hasNeighborSignal(worldPosition);
        }
    }

    private boolean ownsTarget(BlockPos core) {
        long self = worldPosition.asLong();
        for (int offset = -3; offset <= 2; offset++) {
            BlockPos column = core.above(offset);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos other = column.relative(side);
                if (other.asLong() >= self || !level.isLoaded(other)) {
                    continue;
                }
                if (level.getBlockEntity(other) instanceof BacterialEnrichmentCatalystBlockEntity catalyst
                        && core.equals(catalyst.targetCorePos())) {
                    return false;
                }
            }
        }
        return true;
    }

    private @Nullable BlockPos targetCorePos() {
        PrismarineCrystalBlockEntity crystal = targetCrystal();
        return crystal == null ? null : crystal.getBlockPos();
    }

    private long colonyCost(float crystalPurity, float sourcePurity) {
        return EnrichmentRules.colonyCost(crystalPurity, sourcePurity, EnrichmentRules.resistanceRatio(getBacteriaStorage().getBacteria(0)));
    }

    public int evaluate(@Nullable PrismarineCrystalBlockEntity crystal) {
        if (crystal == null) {
            return STATUS_NO_CRYSTAL;
        }
        if (!crystal.isCultivated()) {
            return STATUS_WILD_CRYSTAL;
        }
        if (!owner) {
            return STATUS_SHARED;
        }
        float purity = crystal.getCrystalPurity();
        if (!EnrichmentRules.enrichable(purity)) {
            return STATUS_TOO_IMPURE;
        }
        if (purity >= EnrichmentRules.maxPurity() - 0.0001f) {
            return STATUS_AT_CAP;
        }
        if (purity >= targetPurity - 0.0001f) {
            return STATUS_AT_TARGET;
        }
        if (paused) {
            return STATUS_PAUSED;
        }
        if (getPower() <= 0) {
            return STATUS_NO_SOURCE;
        }
        if (getPower() < NTConfig.enrichmentMinPower) {
            return STATUS_LOW_POWER;
        }
        float source = getPurity();
        if (EnrichmentRules.overreached(purity, source)) {
            return STATUS_OVERREACH;
        }
        BacteriaInstance colony = getBacteriaStorage().getBacteria(0);
        if (colony.isEmpty()) {
            return STATUS_NO_COLONY;
        }
        if (colony.getSize() < colonyCost(purity, source)) {
            return STATUS_SMALL_COLONY;
        }
        return STATUS_RUNNING;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        PrismarineCrystalBlockEntity crystal = targetCrystal();
        targetCore = crystal == null ? null : crystal.getBlockPos();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (server.getGameTime() % DishPort.INTERVAL == 0) {
            DishPort.tick(this, DISH_IN, DISH_EMPTY_OUT, DISH_OUT, LOAD_SLOTS, this::unloadSlot, slot -> {
            });
        }
        if (++ownerCheck >= OWNER_CHECK_INTERVAL) {
            ownerCheck = 0;
            owner = targetCore == null || ownsTarget(targetCore);
        }

        int evaluated = evaluate(crystal);
        float crystalPurity = crystal == null ? 0f : crystal.getCrystalPurity();
        float sourcePurity = getPower() > 0 ? getPurity() : 0f;
        boolean changed = evaluated != status
                || Math.abs(crystalPurity - shownCrystalPurity) > 0.004f
                || Math.abs(sourcePurity - shownSourcePurity) > 0.004f;
        status = evaluated;
        shownCrystalPurity = crystalPurity;
        shownSourcePurity = sourcePurity;

        boolean running = status == STATUS_RUNNING;
        if (getBlockState().getValue(BacterialEnrichmentCatalystBlock.ACTIVE) != running) {
            server.setBlock(worldPosition, getBlockState().setValue(BacterialEnrichmentCatalystBlock.ACTIVE, running), Block.UPDATE_CLIENTS);
        }
        if (running) {
            progress += getPower();
            long needed = EnrichmentRules.cycleAp(crystalPurity);
            if (progress >= needed) {
                progress = Math.min(progress - needed, needed);
                completeCycle(server, crystal, server.getRandom().nextFloat());
                changed = true;
            }
            setChanged();
        }
        if (changed) {
            update();
        }
    }

    public boolean completeCycle(ServerLevel server, PrismarineCrystalBlockEntity crystal, float roll) {
        float purity = crystal.getCrystalPurity();
        float source = getPurity();
        long cost = colonyCost(purity, source);
        if (cost > 0) {
            getBacteriaStorage().extractBacteria(0, cost, false);
        }
        boolean regressed = roll < EnrichmentRules.regressionChance(purity, source);
        float result = regressed ? EnrichmentRules.regress(purity) : EnrichmentRules.success(purity, source, targetPurity);
        crystal.setCrystalPurity(result);
        crystal.update();
        lastOutcome = regressed ? OUTCOME_REGRESSION : OUTCOME_SUCCESS;
        outcomeTick = server.getGameTime();
        shownCrystalPurity = crystal.getCrystalPurity();
        BlockPos core = crystal.getBlockPos();
        if (regressed) {
            server.playSound(null, core, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1.6F, 0.6F);
            server.sendParticles(ParticleTypes.END_ROD, core.getX() + 0.5, core.getY() + 0.5, core.getZ() + 0.5, 30, 0.5, 2.2, 0.5, 0.08);
        } else {
            server.playSound(null, core, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.4F, 1.4F);
        }
        setChanged();
        return !regressed;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<BacterialEnrichmentCatalystBlockEntity>("catalyst", 8,
                state -> state.setAndContinue(state.getDataOrDefault(WORKING, false) ? WORKING_ANIMATION : IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BacterialEnrichmentCatalystMenu(containerId, inventory, this);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putLong("progress", progress);
        out.putFloat("target_purity", targetPurity);
        out.putBoolean("paused", paused);
        out.putInt("status", status);
        out.putInt("last_outcome", lastOutcome);
        out.putLong("outcome_tick", outcomeTick);
        out.putFloat("shown_crystal_purity", shownCrystalPurity);
        out.putFloat("shown_source_purity", shownSourcePurity);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        progress = Math.max(0L, in.getLongOr("progress", 0L));
        targetPurity = CrystalPurity.clamp(in.getFloatOr("target_purity", CrystalPurity.MAX));
        paused = in.getBooleanOr("paused", false);
        status = in.getIntOr("status", STATUS_NO_CRYSTAL);
        lastOutcome = in.getIntOr("last_outcome", OUTCOME_NONE);
        outcomeTick = in.getLongOr("outcome_tick", 0L);
        shownCrystalPurity = in.getFloatOr("shown_crystal_purity", 0f);
        shownSourcePurity = in.getFloatOr("shown_source_purity", 0f);
    }
}
