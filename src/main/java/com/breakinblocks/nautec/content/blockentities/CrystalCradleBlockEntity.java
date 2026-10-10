package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.api.sides.SlotRoles;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTItems;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapability;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CrystalCradleBlockEntity extends LaserBlockEntity {
    public static final int SEED_SLOT = 0;
    public static final int CRYSTAL_OFFSET = 4;
    private static final int SPACE_CHECK_INTERVAL = 20;
    private static final int SYNC_INTERVAL = 10;
    private static final int SAVE_INTERVAL = 100;
    private static final int PERMILLE = 1000;

    private long growth;
    private int permille;
    private int syncedPermille = -1;
    private long lastSync;
    private boolean spaceClear = true;
    private boolean spaceKnown;
    private Status status = Status.EMPTY;

    private static final SlotRoles ITEM_ROLES = SlotRoles.of(new int[]{SEED_SLOT}, new int[0]);

    public CrystalCradleBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.CRYSTAL_CRADLE.get(), pos, state);
        addItemHandler(1, 1, (slot, stack) -> stack.is(NTItems.PRISMARINE_CRYSTAL_SEED.get()));
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
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

    public boolean hasSeed() {
        return !getItemStackHandler().getStackInSlot(SEED_SLOT).isEmpty();
    }

    public boolean insertSeed(ItemStack stack) {
        if (hasSeed() || !stack.is(NTItems.PRISMARINE_CRYSTAL_SEED.get())) {
            return false;
        }
        getItemStackHandler().setStackInSlot(SEED_SLOT, stack.copyWithCount(1));
        return true;
    }

    public ItemStack removeSeed() {
        if (!hasSeed() || growth > 0) {
            return ItemStack.EMPTY;
        }
        ItemStack seed = getItemStackHandler().getStackInSlot(SEED_SLOT).copy();
        getItemStackHandler().setStackInSlot(SEED_SLOT, ItemStack.EMPTY);
        return seed;
    }

    public void restore(boolean seeded, long growth) {
        getItemStackHandler().setStackInSlot(SEED_SLOT, seeded ? new ItemStack(NTItems.PRISMARINE_CRYSTAL_SEED.get()) : ItemStack.EMPTY);
        this.growth = seeded ? Math.max(0, growth) : 0;
        this.permille = permilleOf(this.growth);
        update();
    }

    public long getGrowth() {
        return growth;
    }

    public void setGrowth(long growth) {
        this.growth = Math.max(0, growth);
        this.permille = permilleOf(this.growth);
    }

    public float getProgress() {
        return permille / (float) PERMILLE;
    }

    public Status getStatus() {
        return status;
    }

    public BlockPos getCrystalCore() {
        return worldPosition.above(CRYSTAL_OFFSET);
    }

    @Override
    protected void onItemsChanged(int slot) {
        if (!hasSeed()) {
            growth = 0;
            permille = 0;
        }
    }

    @Override
    public void drop() {
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long gameTime = serverLevel.getGameTime();
        if (!hasSeed()) {
            status = Status.EMPTY;
            return;
        }
        if (!spaceKnown || gameTime % SPACE_CHECK_INTERVAL == 0) {
            spaceClear = PrismarineCrystalBlock.canBuild(serverLevel, getCrystalCore());
            spaceKnown = true;
        }
        int power = getPower();
        if (!spaceClear) {
            status = Status.BLOCKED;
        } else if (power <= 0) {
            status = Status.NO_POWER;
        } else if (getPurity() < NTConfig.crystalGrowthPurity) {
            status = Status.LOW_PURITY;
        } else {
            status = Status.GROWING;
            growth += power;
            permille = permilleOf(growth);
            if (growth >= NTConfig.crystalGrowthPower) {
                complete(serverLevel);
                return;
            }
        }

        if (permille != syncedPermille && gameTime - lastSync >= SYNC_INTERVAL) {
            syncedPermille = permille;
            lastSync = gameTime;
            update();
        } else if (status == Status.GROWING && gameTime % SAVE_INTERVAL == 0) {
            setChanged();
        }
    }

    private void complete(ServerLevel serverLevel) {
        BlockPos core = getCrystalCore();
        growth = 0;
        permille = 0;
        getItemStackHandler().setStackInSlot(SEED_SLOT, ItemStack.EMPTY);
        PrismarineCrystalBlock.build(serverLevel, core, true);
        serverLevel.playSound(null, core, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 2.0F, 0.6F);
        serverLevel.playSound(null, core, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.3F);
        serverLevel.sendParticles(ParticleTypes.END_ROD, core.getX() + 0.5, core.getY() + 0.5, core.getZ() + 0.5, 60, 0.6, 2.5, 0.6, 0.05);
        status = Status.EMPTY;
        syncedPermille = 0;
        update();
    }

    private static int permilleOf(long growth) {
        long target = Math.max(1, NTConfig.crystalGrowthPower);
        return (int) Math.min(PERMILLE, growth * PERMILLE / target);
    }

    @Override
    protected void saveData(ValueOutput out) {
        out.putLong("growth", growth);
        out.putInt("permille", permille);
    }

    @Override
    protected void loadData(ValueInput in) {
        growth = in.getLongOr("growth", 0);
        permille = in.getIntOr("permille", 0);
    }

    public enum Status {
        EMPTY,
        BLOCKED,
        NO_POWER,
        LOW_PURITY,
        GROWING;

        public static Status byId(int id) {
            Status[] values = values();
            return id >= 0 && id < values.length ? values[id] : EMPTY;
        }

        public String translationKey() {
            return "nautec.crystal_cradle.status." + name().toLowerCase(Locale.ROOT);
        }
    }
}
