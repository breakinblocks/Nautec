package com.breakinblocks.nautec.content.blockentities;

import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import com.breakinblocks.nautec.registries.NTFluids;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.capabilities.item.ItemStackHandler;
import com.breakinblocks.nautec.content.menus.ConfinedSpawnerMenu;
import com.breakinblocks.nautec.content.spawner.SpawnerFilter;
import com.breakinblocks.nautec.content.spawner.SpawnerFilterEntry;
import com.breakinblocks.nautec.content.spawner.SpawnerLootSimulator;
import com.breakinblocks.nautec.content.spawner.SpawnerSettings;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConfinedSpawnerBlockEntity extends LaserBlockEntity implements MenuProvider {
    public static final int SLOTS = 54;
    public static final int DATA_POWER = 0;
    public static final int DATA_STATUS = 1;
    public static final int DATA_PROGRESS = 2;
    public static final int DATA_CYCLE = 3;
    public static final int DATA_XP_LOW = 4;
    public static final int DATA_XP_HIGH = 5;
    public static final int DATA_XP_CAPACITY_LOW = 6;
    public static final int DATA_XP_CAPACITY_HIGH = 7;
    public static final int DATA_XP_FLUID = 8;
    public static final int DATA_COUNT = 9;

    private static final int ACTIVE_WINDOW = 40;
    private static final int SAVE_INTERVAL = 100;
    private static final float IDLE_SPIN = 3.0F;
    private static final float ACTIVE_SPIN = 18.0F;

    private @Nullable CompoundTag spawnerTag;
    private BlockState spawnerState = Blocks.SPAWNER.defaultBlockState();
    private @Nullable SpawnerSettings settings;
    private final SpawnerFilter filter = new SpawnerFilter();
    private final SpawnerLootSimulator simulator = new SpawnerLootSimulator();
    private final ObjectArrayList<ItemStack> cycleDrops = new ObjectArrayList<>();
    private int cycleExperience;

    private int bufferedPower;
    private int progress;
    private int cycleLength;
    private int ticksSinceRun = ACTIVE_WINDOW;
    private boolean active;
    private Status status = Status.NO_MOB;
    private CompoundTag displayEntityTag = new CompoundTag();

    private boolean freeSlotKnown;
    private boolean hasFreeSlot = true;
    private boolean batching;
    private boolean pendingSave;

    private @Nullable Entity displayEntity;
    private boolean displayEntityFailed;
    private float spin;
    private float oSpin;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_POWER -> bufferedPower;
                case DATA_STATUS -> status.ordinal();
                case DATA_PROGRESS -> progress;
                case DATA_CYCLE -> cycleLength;
                case DATA_XP_LOW -> getFluidTank().getFluidAmount() & 0xFFFF;
                case DATA_XP_HIGH -> getFluidTank().getFluidAmount() >>> 16;
                case DATA_XP_CAPACITY_LOW -> getFluidTank().getCapacity() & 0xFFFF;
                case DATA_XP_CAPACITY_HIGH -> getFluidTank().getCapacity() >>> 16;
                case DATA_XP_FLUID -> BuiltInRegistries.FLUID.getId(storedExperienceFluid());
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

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[0], SlotRoles.range(0, SLOTS));
    private static final SlotRoles FLUID_ROLES = SlotRoles.of(new int[0], new int[]{0});

    public ConfinedSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.CONFINED_SPAWNER.get(), pos, state);
        addItemHandler(SLOTS, (slot, stack) -> false);
        addFluidTank(NTConfig.confinedSpawnerXpCapacity);
    }

    public static Fluid experienceFluid() {
        for (String id : NTConfig.confinedSpawnerXpFluids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                Optional<Fluid> fluid = BuiltInRegistries.FLUID.getOptional(key);
                if (fluid.isPresent() && fluid.get() != Fluids.EMPTY) {
                    return fluid.get();
                }
            }
        }
        return NTFluids.EXPERIENCE_ALGAE.getStillFluid();
    }

    private Fluid storedExperienceFluid() {
        FluidStack stored = getFluidTank().getFluid();
        return stored.isEmpty() ? experienceFluid() : stored.getFluid();
    }

    private void storeExperience(int points) {
        int amount = points * NTConfig.confinedSpawnerXpRatio;
        if (amount > 0) {
            getFluidTank().fill(new FluidStack(experienceFluid(), amount));
        }
    }

    @Override
    public SlotRoles fluidRoles() {
        return FLUID_ROLES;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.values());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    public SlotRoles itemRoles() {
        return ITEM_ROLES;
    }

    public void confine(BlockState originalState, CompoundTag originalData) {
        this.spawnerState = originalState;
        this.spawnerTag = originalData;
        this.settings = level != null ? SpawnerSettings.read(originalData, level.registryAccess()) : null;
        this.displayEntityTag = settings != null ? settings.displayEntity().copy() : new CompoundTag();
        this.displayEntity = null;
        this.displayEntityFailed = false;
        this.simulator.clear();
        this.progress = 0;
        this.cycleLength = 0;
        setChanged();
        sync();
    }

    public boolean hasContents() {
        if (getFluidTank().getFluidAmount() > 0) {
            return true;
        }
        for (int slot = 0; slot < getItemStackHandler().getSlots(); slot++) {
            if (!getItemStackHandler().getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void drop() {
    }

    public boolean release() {
        if (level == null || level.isClientSide() || spawnerTag == null) {
            return false;
        }
        Level world = level;
        BlockPos pos = worldPosition;
        BlockState original = spawnerState;
        CompoundTag data = spawnerTag.copy();
        if (!world.setBlock(pos, original, Block.UPDATE_ALL)) {
            return false;
        }
        BlockEntity restored = world.getBlockEntity(pos);
        if (restored != null) {
            restored.loadWithComponents(data, world.registryAccess());
            restored.setChanged();
            world.sendBlockUpdated(pos, original, original, Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level == null) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverTick(serverLevel);
        } else {
            clientTick();
        }
    }

    private void serverTick(ServerLevel serverLevel) {
        int capacity = NTConfig.confinedSpawnerPowerBuffer;
        int incoming = getPower();
        if (incoming > 0 && bufferedPower < capacity) {
            bufferedPower = Math.min(capacity, bufferedPower + incoming);
        } else if (bufferedPower > capacity) {
            bufferedPower = capacity;
        }

        status = evaluate();
        if (status == Status.RUNNING) {
            bufferedPower -= NTConfig.confinedSpawnerPowerPerTick;
            ticksSinceRun = 0;
            RandomSource random = serverLevel.getRandom();
            if (cycleLength <= 0) {
                cycleLength = settings.nextDelay(random);
            }
            if (++progress >= cycleLength) {
                progress = 0;
                cycleLength = settings.nextDelay(random);
                runCycle(serverLevel, random);
            }
        } else if (ticksSinceRun < ACTIVE_WINDOW) {
            ticksSinceRun++;
        }

        boolean nowActive = ticksSinceRun < ACTIVE_WINDOW;
        if (nowActive != active) {
            active = nowActive;
            sync();
        }

        if (pendingSave || serverLevel.getGameTime() % SAVE_INTERVAL == 0) {
            pendingSave = false;
            setChanged();
        }
    }

    private Status evaluate() {
        if (settings == null || !settings.hasMob()) {
            return Status.NO_MOB;
        }
        if (filter.blocksEverything()) {
            return Status.FILTERED;
        }
        if (!hasFreeSlot()) {
            return Status.FULL;
        }
        if (bufferedPower < NTConfig.confinedSpawnerPowerPerTick) {
            return Status.NO_POWER;
        }
        return Status.RUNNING;
    }

    private void runCycle(ServerLevel serverLevel, RandomSource random) {
        int spawnCount = settings.spawnCount();
        SpawnData[] kinds = new SpawnData[spawnCount];
        int[] times = new int[spawnCount];
        int distinct = 0;
        for (int i = 0; i < spawnCount; i++) {
            SpawnData data = settings.pick(random);
            if (data == null) {
                continue;
            }
            int kind = 0;
            while (kind < distinct && kinds[kind] != data) {
                kind++;
            }
            if (kind == distinct) {
                kinds[distinct++] = data;
            }
            times[kind]++;
        }

        cycleExperience = 0;
        batching = true;
        try {
            for (int kind = 0; kind < distinct; kind++) {
                simulator.roll(serverLevel, worldPosition, kinds[kind], times[kind], this::collect, xp -> cycleExperience += xp);
            }
            for (int i = 0; i < cycleDrops.size(); i++) {
                store(cycleDrops.get(i));
            }
            storeExperience(cycleExperience);
        } finally {
            batching = false;
            cycleDrops.clear();
        }
        if (pendingSave) {
            pendingSave = false;
            setChanged();
        }
    }

    private void collect(ItemStack generated) {
        if (generated.isEmpty()) {
            return;
        }
        for (int i = 0; i < cycleDrops.size(); i++) {
            ItemStack collected = cycleDrops.get(i);
            if (ItemStack.isSameItemSameComponents(collected, generated)) {
                collected.grow(generated.getCount());
                return;
            }
        }
        cycleDrops.add(generated.copy());
    }

    private void store(ItemStack generated) {
        if (generated.isEmpty() || !filter.allows(generated)) {
            return;
        }
        ItemStackHandler handler = getItemStackHandler();
        ItemStack remaining = generated.copy();
        int maxStack = remaining.getMaxStackSize();
        for (int slot = 0; slot < SLOTS && !remaining.isEmpty(); slot++) {
            ItemStack existing = handler.getStackInSlot(slot);
            if (!existing.isEmpty() && existing.getCount() < maxStack && ItemStack.isSameItemSameComponents(existing, remaining)) {
                int moved = Math.min(maxStack - existing.getCount(), remaining.getCount());
                handler.setStackInSlot(slot, existing.copyWithCount(existing.getCount() + moved));
                remaining.shrink(moved);
            }
        }
        for (int slot = 0; slot < SLOTS && !remaining.isEmpty(); slot++) {
            if (handler.getStackInSlot(slot).isEmpty()) {
                int moved = Math.min(maxStack, remaining.getCount());
                handler.setStackInSlot(slot, remaining.copyWithCount(moved));
                remaining.shrink(moved);
            }
        }
    }

    private boolean hasFreeSlot() {
        if (!freeSlotKnown) {
            ItemStackHandler handler = getItemStackHandler();
            hasFreeSlot = false;
            for (int slot = 0; slot < SLOTS; slot++) {
                if (handler.getStackInSlot(slot).isEmpty()) {
                    hasFreeSlot = true;
                    break;
                }
            }
            freeSlotKnown = true;
        }
        return hasFreeSlot;
    }

    @Override
    public void update() {
        if (batching) {
            pendingSave = true;
        } else {
            setChanged();
        }
    }

    @Override
    protected void onItemsChanged(int slot) {
        freeSlotKnown = false;
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void clientTick() {
        oSpin = spin;
        spin = (spin + (active ? ACTIVE_SPIN : IDLE_SPIN)) % 360.0F;
        if (active && level.getGameTime() % 6 == 0) {
            RandomSource random = level.getRandom();
            level.addParticle(ParticleTypes.GLOW,
                    worldPosition.getX() + 0.15 + random.nextDouble() * 0.7,
                    worldPosition.getY() + 0.2 + random.nextDouble() * 0.6,
                    worldPosition.getZ() + 0.15 + random.nextDouble() * 0.7,
                    0.0, 0.02, 0.0);
        }
    }

    public @Nullable Entity getOrCreateDisplayEntity() {
        if (displayEntity == null && !displayEntityFailed && level != null && !displayEntityTag.isEmpty()) {
            displayEntity = EntityType.loadEntityRecursive(displayEntityTag.copy(), level, entity -> entity);
            displayEntityFailed = displayEntity == null;
        }
        return displayEntity;
    }

    public float getSpin(float partialTick) {
        float delta = spin - oSpin;
        if (delta < 0) {
            delta += 360.0F;
        }
        return oSpin + delta * partialTick;
    }

    public boolean isActive() {
        return active;
    }

    public Status getStatus() {
        return status;
    }

    public int getBufferedPower() {
        return bufferedPower;
    }

    public void setBufferedPower(int bufferedPower) {
        this.bufferedPower = Math.max(0, Math.min(NTConfig.confinedSpawnerPowerBuffer, bufferedPower));
    }

    public int getProgress() {
        return progress;
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public @Nullable SpawnerSettings getSettings() {
        return settings;
    }

    public @Nullable CompoundTag getSpawnerTag() {
        return spawnerTag;
    }

    public BlockState getSpawnerState() {
        return spawnerState;
    }

    public SpawnerFilter getFilter() {
        return filter;
    }

    public ContainerData getData() {
        return data;
    }

    public void setFilterEntry(int slot, @Nullable SpawnerFilterEntry entry) {
        if (slot < 0 || slot >= SpawnerFilter.SIZE) {
            return;
        }
        filter.set(slot, entry);
        setChanged();
        sync();
    }

    public void toggleWhitelist() {
        filter.setWhitelist(!filter.isWhitelist());
        setChanged();
        sync();
    }

    @Override
    public void saveSettings(ValueOutput out) {
        filter.save(out.child("filter"));
    }

    @Override
    public boolean loadSettings(ValueInput in, ServerPlayer player) {
        Optional<ValueInput> copied = in.child("filter");
        if (copied.isEmpty()) {
            return false;
        }
        filter.load(copied.get());
        setChanged();
        sync();
        return true;
    }

    @Override
    protected void saveData(ValueOutput out) {
        if (spawnerTag != null) {
            out.store("spawner", CompoundTag.CODEC, spawnerTag);
            out.store("spawner_state", BlockState.CODEC, spawnerState);
        }
        out.putInt("buffered_power", bufferedPower);
        out.putInt("progress", progress);
        out.putInt("cycle_length", cycleLength);
        writeClientData(out);
    }

    @Override
    protected void loadData(ValueInput in) {
        in.read("spawner", CompoundTag.CODEC).ifPresent(tag -> {
            spawnerTag = tag;
            settings = SpawnerSettings.read(tag, in.lookup());
            simulator.clear();
        });
        in.read("spawner_state", BlockState.CODEC).ifPresent(state -> spawnerState = state);
        bufferedPower = in.getIntOr("buffered_power", bufferedPower);
        progress = in.getIntOr("progress", progress);
        cycleLength = in.getIntOr("cycle_length", cycleLength);
        in.child("filter").ifPresent(filter::load);
        active = in.getBooleanOr("active", false);
        CompoundTag display = in.read("display_entity", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        if (!display.equals(displayEntityTag)) {
            displayEntityTag = display;
            displayEntity = null;
            displayEntityFailed = false;
        }
    }

    private void writeClientData(ValueOutput out) {
        filter.save(out.child("filter"));
        out.putBoolean("active", active);
        out.store("display_entity", CompoundTag.CODEC, displayEntityTag);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, registries);
            writeClientData(out);
            getSideConfig().save(out.child("side_config"));
            return out.buildResult();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        simulator.clear();
        displayEntity = null;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ConfinedSpawnerMenu(containerId, inventory, this);
    }

    public enum Status {
        RUNNING,
        NO_POWER,
        FULL,
        NO_MOB,
        FILTERED;

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : NO_MOB;
        }

        public String translationKey() {
            return "nautec.confined_spawner.status." + name().toLowerCase(Locale.ROOT);
        }
    }
}
