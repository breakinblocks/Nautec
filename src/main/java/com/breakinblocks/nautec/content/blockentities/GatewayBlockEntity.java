package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.api.blockentities.multiblock.MultiblockEntity;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayEffects;
import com.breakinblocks.nautec.api.gateways.GatewayFarEnd;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.utils.MachineSounds;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GatewayBlockEntity extends LaserBlockEntity implements MultiblockEntity {
    public static final int EVENT_RIPPLE = 1;
    public static final int RING_VERSION = 1;
    public static final int ENERGY_CAPACITY = 5000;
    public static final int ENERGY_PER_TICK = 100;
    public static final double OPEN_RADIUS = 12.0;
    public static final double STAY_RADIUS = 16.0;

    private static final int AMBIENT_PERIOD = 120;
    private static final int PRESENCE_PERIOD = 10;
    private static final int LINK_PERIOD = 20;
    private static final int SYNC_PERIOD = 20;
    private static final int HEAL_PERIOD = 100;
    private static final int LINGER_TICKS = 60;
    private static final int ARRIVAL_WAKE_TICKS = 100;
    private static final int MAX_TARGET_ATTEMPTS = 4;
    private static final double SCAN_DEPTH = 4.0;
    private static final double EXIT_GAP = 0.6;

    private GatewayAddress address = GatewayAddress.DEFAULT;
    private MultiblockData multiblockData = MultiblockData.EMPTY;
    private Direction front = Direction.SOUTH;
    private int ringVersion;
    private boolean wild;
    private boolean needsPower;
    private int energy;
    private int syncedEnergy;
    private boolean linked;
    private boolean open;
    private long awakeUntil;
    private boolean pendingBuild;
    private @Nullable HorizontalDirection preferredDirection;
    private @Nullable Direction preferredFront;
    private @Nullable BlockPos blockedAt;
    private final Int2ObjectMap<Vec3> lastCentres = new Int2ObjectOpenHashMap<>();

    private long linkChangedAt = Long.MIN_VALUE / 2;
    private boolean synced;
    private final List<Ripple> ripples = new ArrayList<>();

    public GatewayBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.GATEWAY.get(), blockPos, blockState);
    }

    public record Ripple(long startedAt, float x, float y) {
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return ObjectSet.of(Direction.values());
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return ObjectSet.of();
    }

    public GatewayAddress getAddress() {
        return address;
    }

    public void setAddress(GatewayAddress address) {
        this.address = address;
        if (level instanceof ServerLevel serverLevel && isFormed()) {
            GatewayIndex.get(serverLevel).put(worldPosition, address);
            refreshLink(serverLevel);
        }
        update();
    }

    public boolean isFormed() {
        BlockState state = getBlockState();
        return state.hasProperty(Multiblock.FORMED) && state.getValue(Multiblock.FORMED);
    }

    public boolean isOpen() {
        return open && isFormed();
    }

    public boolean isLinked() {
        return linked && isFormed();
    }

    public boolean isWild() {
        return wild;
    }

    public boolean needsPower() {
        return needsPower;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isRedstoneLocked() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public Direction getFront() {
        return front;
    }

    public @Nullable BlockPos getBlockedAt() {
        return blockedAt;
    }

    public long getLinkChangedAt() {
        return linkChangedAt;
    }

    public List<Ripple> getRipples() {
        return ripples;
    }

    public void markPlacedByPlayer() {
        this.ringVersion = RING_VERSION;
        this.needsPower = true;
        setChanged();
    }

    public void unpack(PackedGateway packed, @Nullable Player placer) {
        this.ringVersion = RING_VERSION;
        this.needsPower = true;
        this.wild = packed.wild();
        this.pendingBuild = true;
        if (placer != null) {
            Direction facing = placer.getDirection();
            this.preferredDirection = facing.getAxis() == Direction.Axis.Z ? HorizontalDirection.NORTH : HorizontalDirection.EAST;
            this.preferredFront = facing.getOpposite();
        }
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            build(serverLevel);
        }
    }

    public void configureFarEnd(GatewayAddress address) {
        this.address = address;
        this.ringVersion = RING_VERSION;
        this.wild = true;
        this.needsPower = false;
        setChanged();
    }

    public void setEnergy(int energy) {
        this.energy = Math.clamp(energy, 0, ENERGY_CAPACITY);
        setChanged();
    }

    public void wake(int ticks) {
        if (level != null) {
            awakeUntil = Math.max(awakeUntil, level.getGameTime() + ticks);
        }
    }

    public boolean needsSelfHeal() {
        return ringVersion < RING_VERSION && !isFormed();
    }

    public boolean isWaitingToBuild() {
        return pendingBuild && !isFormed();
    }

    @Override
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    @Override
    public void setMultiblockData(MultiblockData data) {
        this.multiblockData = data;
        setChanged();
    }

    public void onFormed(MultiblockData data, @Nullable Player player) {
        this.multiblockData = data;
        this.ringVersion = RING_VERSION;
        this.blockedAt = null;
        this.pendingBuild = false;
        Direction.Axis axis = GatewayRing.normalAxis(data.direction());
        if (player != null) {
            this.front = facingToward(axis, player.position());
        } else if (preferredFront != null && preferredFront.getAxis() == axis) {
            this.front = preferredFront;
        } else {
            this.front = openestSide(axis);
        }
        this.preferredFront = null;
        this.preferredDirection = null;
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).put(worldPosition, address);
            refreshLink(serverLevel);
        }
        update();
    }

    public void onUnformed() {
        this.multiblockData = MultiblockData.EMPTY;
        this.linked = false;
        this.open = false;
        this.lastCentres.clear();
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).remove(worldPosition);
        }
        update();
    }

    public void unformRing() {
        if (!isFormed() || level == null || level.isClientSide() || !MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            return;
        }
        try {
            MultiblockHelper.unform(NTMultiblocks.GATEWAY.get(), worldPosition, level);
        } finally {
            MultiblockEntity.UNFORMING.set(false);
        }
    }

    public @Nullable ItemStack pack() {
        if (!(level instanceof ServerLevel serverLevel) || !isFormed() || !MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
            return null;
        }
        ItemStack packed = new ItemStack(NTBlocks.GATEWAY.get());
        packed.set(NTDataComponents.GATEWAY_ADDRESS.get(), address);
        packed.set(NTDataComponents.GATEWAY_PACKED.get(), new PackedGateway(wild));
        try {
            GatewayIndex.get(serverLevel).remove(worldPosition);
            for (BlockPos cell : GatewayRing.ringCells(worldPosition, multiblockData.direction())) {
                BlockState state = serverLevel.getBlockState(cell);
                if (state.is(NTBlocks.GATEWAY_RING_PART.get())) {
                    serverLevel.setBlock(cell, emptied(state), 3);
                }
            }
            serverLevel.setBlock(worldPosition, emptied(getBlockState()), 3);
        } finally {
            MultiblockEntity.UNFORMING.set(false);
        }
        serverLevel.playSound(null, worldPosition.above(GatewayRing.CENTRE), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
        return packed;
    }

    private static BlockState emptied(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState();
    }

    private Direction facingToward(Direction.Axis axis, Vec3 point) {
        Vec3 offset = point.subtract(GatewayRing.centre(worldPosition));
        double along = axis == Direction.Axis.X ? offset.x : offset.z;
        return Direction.fromAxisAndDirection(axis, along >= 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
    }

    private Direction openestSide(Direction.Axis axis) {
        Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        return openness(positive) >= openness(positive.getOpposite()) ? positive : positive.getOpposite();
    }

    private int openness(Direction side) {
        if (level == null) {
            return 0;
        }
        int open = 0;
        BlockPos centre = worldPosition.above(GatewayRing.CENTRE).relative(side, 3);
        Direction across = side.getClockWise();
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                if (level.getBlockState(centre.relative(across, a).above(b)).canBeReplaced()) {
                    open++;
                }
            }
        }
        return open;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            if (isFormed()) {
                GatewayIndex.get(serverLevel).put(worldPosition, address);
            } else {
                GatewayIndex.get(serverLevel).remove(worldPosition);
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).remove(pos);
            if (state.hasProperty(Multiblock.FORMED) && state.getValue(Multiblock.FORMED)
                    && MultiblockEntity.UNFORMING.compareAndSet(false, true)) {
                try {
                    MultiblockHelper.unform(NTMultiblocks.GATEWAY.get(), pos, level);
                } finally {
                    MultiblockEntity.UNFORMING.set(false);
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            long now = level.getGameTime();
            ripples.removeIf(ripple -> now - ripple.startedAt() > 40);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long now = serverLevel.getGameTime();

        if (needsPower && getPower() > 0 && energy < ENERGY_CAPACITY) {
            energy = Math.min(ENERGY_CAPACITY, energy + getPower());
            setChanged();
        }

        if (!isFormed()) {
            if ((needsSelfHeal() || pendingBuild) && (now + worldPosition.asLong()) % HEAL_PERIOD == 0) {
                build(serverLevel);
            }
            syncEnergy(now);
            return;
        }

        if (now % PRESENCE_PERIOD == 0 && someoneNear(serverLevel, open ? STAY_RADIUS : OPEN_RADIUS)) {
            wake(LINGER_TICKS);
        }
        if (now % LINK_PERIOD == Math.floorMod(worldPosition.asLong(), LINK_PERIOD)) {
            refreshLink(serverLevel);
        }

        boolean powered = !needsPower || energy >= ENERGY_PER_TICK;
        setOpen(serverLevel, linked && now < awakeUntil && powered && !isRedstoneLocked());

        if (open) {
            if (needsPower) {
                energy -= ENERGY_PER_TICK;
                setChanged();
            }
            MachineSounds.interval(level, worldPosition.above(GatewayRing.CENTRE), NTSounds.GATEWAY_AMBIENT, AMBIENT_PERIOD, 0.5f, 0.8f);
            scanCrossings(serverLevel);
        } else {
            lastCentres.clear();
        }
        syncEnergy(now);
    }

    private void syncEnergy(long now) {
        if (now % SYNC_PERIOD == 0 && energy != syncedEnergy) {
            syncedEnergy = energy;
            update();
        }
    }

    public static boolean wakesGateway(Entity entity) {
        return (entity instanceof Player player && !player.isSpectator()) || entity instanceof SubmarineEntity;
    }

    private boolean someoneNear(ServerLevel level, double radius) {
        Vec3 centre = GatewayRing.centre(worldPosition);
        AABB box = new AABB(centre, centre).inflate(radius);
        double radiusSqr = radius * radius;
        boolean near = false;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box, GatewayBlockEntity::wakesGateway)) {
            if (entity.getBoundingBox().getCenter().distanceToSqr(centre) <= radiusSqr) {
                near = true;
                if (entity instanceof ServerPlayer player) {
                    NTCriteriaTriggers.GATEWAY_FOUND.get().trigger(player);
                }
            }
        }
        return near;
    }

    private void setOpen(ServerLevel level, boolean nowOpen) {
        if (nowOpen == open) {
            return;
        }
        open = nowOpen;
        BlockPos centre = worldPosition.above(GatewayRing.CENTRE);
        if (open) {
            MachineSounds.play(level, centre, NTSounds.GATEWAY_TRAVEL, 1.0f, 0.6f);
            GatewayEffects.opened(level, GatewayRing.centre(worldPosition));
        } else {
            MachineSounds.play(level, centre, NTSounds.GATEWAY_UNLINKED, 0.8f, 0.7f);
        }
        update();
    }

    private void refreshLink(ServerLevel level) {
        boolean partner = GatewayIndex.get(level).findNearest(level, worldPosition, address) != null;
        boolean nowLinked = isFormed() && (partner || wild);
        if (nowLinked != linked) {
            linked = nowLinked;
            update();
        }
    }

    private Vec3 normal() {
        return GatewayRing.normal(front);
    }

    private void scanCrossings(ServerLevel level) {
        Vec3 centre = GatewayRing.centre(worldPosition);
        Vec3 n = normal();
        double r = GatewayRing.OPENING_RADIUS;
        Vec3 extent = new Vec3(Math.abs(n.x) * SCAN_DEPTH + Math.abs(n.z) * r, r, Math.abs(n.z) * SCAN_DEPTH + Math.abs(n.x) * r);
        AABB box = new AABB(centre.subtract(extent), centre.add(extent));

        IntSet seen = new IntOpenHashSet();
        for (Entity entity : level.getEntitiesOfClass(Entity.class, box, GatewayBlockEntity::canTravel)) {
            Vec3 current = entity.getBoundingBox().getCenter();
            seen.add(entity.getId());
            Vec3 previous = lastCentres.put(entity.getId(), current);
            if (previous == null) {
                continue;
            }
            double before = previous.subtract(centre).dot(n);
            double after = current.subtract(centre).dot(n);
            if ((before > 0) == (after > 0) || before == after) {
                continue;
            }
            double t = before / (before - after);
            Vec3 hit = previous.add(current.subtract(previous).scale(t));
            Vec3 offset = hit.subtract(centre);
            double across = offset.dot(GatewayRing.right(n));
            if (across * across + offset.y * offset.y > r * r) {
                continue;
            }
            Vec3 motion = current.subtract(previous);
            if (travel(level, entity, hit, motion, after > before ? n : n.reverse())) {
                lastCentres.remove(entity.getId());
                seen.remove(entity.getId());
            }
        }
        lastCentres.keySet().retainAll(seen);
    }

    private static boolean canTravel(Entity entity) {
        return !entity.isSpectator()
                && !entity.isOnPortalCooldown()
                && entity.isAlive()
                && !entity.isPassenger();
    }

    private boolean travel(ServerLevel level, Entity root, Vec3 hit, Vec3 motion, Vec3 entering) {
        BlockPos target = resolveTarget(level);
        if (target == null && wild) {
            target = GatewayFarEnd.create(level, worldPosition, address, level.getRandom());
        }
        if (target == null || !(level.getBlockEntity(target) instanceof GatewayBlockEntity partner) || !partner.isFormed()) {
            return false;
        }

        Vec3 centre = GatewayRing.centre(worldPosition);
        Vec3 offset = hit.subtract(centre);
        Vec3 enterRight = GatewayRing.right(entering);
        double across = offset.dot(enterRight);
        double up = offset.y;

        Vec3 velocity = root instanceof Player || root.getControllingPassenger() instanceof Player ? motion : root.getDeltaMovement();
        double forward = Math.max(velocity.dot(entering), 0.05);
        double sideways = velocity.dot(enterRight);

        for (Vec3 exit : new Vec3[]{partner.normal(), partner.normal().reverse()}) {
            Vec3 exitRight = GatewayRing.right(exit);
            double half = root.getBbWidth() / 2.0;
            Vec3 exitCentre = GatewayRing.centre(target)
                    .add(exitRight.scale(across))
                    .add(0, up, 0)
                    .add(exit.scale(half + EXIT_GAP));
            Vec3 feet = exitCentre.subtract(0, root.getBbHeight() / 2.0, 0);
            Vec3 shift = feet.subtract(root.position());
            if (root.getSelfAndPassengers().anyMatch(part -> !level.noCollision(part, part.getBoundingBox().move(shift)))) {
                continue;
            }

            float turn = GatewayRing.yawOf(exit) - GatewayRing.yawOf(entering);
            Vec3 exitVelocity = exit.scale(forward).add(exitRight.scale(sideways)).add(0, velocity.y, 0);

            MachineSounds.play(level, worldPosition.above(GatewayRing.CENTRE), NTSounds.GATEWAY_TRAVEL, 0.9f, 0.9f);
            GatewayEffects.travel(level, hit);
            ripple(level, worldPosition, front, hit);

            List<Entity> riders = root.getPassengers().stream().toList();
            root.teleport(new TeleportTransition(level, feet, exitVelocity, root.getYRot() + turn, root.getXRot(),
                    Set.of(), TeleportTransition.DO_NOTHING));
            for (Entity rider : riders) {
                if (rider instanceof ServerPlayer player && turn != 0F) {
                    player.setYRot(player.getYRot() + turn);
                    player.connection.send(new ClientboundPlayerRotationPacket(turn, true, 0F, true));
                }
            }

            partner.wake(ARRIVAL_WAKE_TICKS);
            MachineSounds.play(level, target.above(GatewayRing.CENTRE), NTSounds.GATEWAY_TRAVEL, 0.9f, 1.1f);
            GatewayEffects.travel(level, exitCentre);
            ripple(level, target, partner.front, exitCentre);

            for (Entity part : root.getSelfAndPassengers().toList()) {
                part.setPortalCooldown(NTConfig.gatewayCooldown);
                if (part instanceof ServerPlayer player) {
                    NTCriteriaTriggers.GATEWAY_TRAVEL.get().trigger(player);
                }
            }
            return true;
        }
        return false;
    }

    private static void ripple(ServerLevel level, BlockPos core, Direction front, Vec3 point) {
        Vec3 offset = point.subtract(GatewayRing.centre(core));
        double across = offset.dot(GatewayRing.right(GatewayRing.normal(front)));
        int x = Math.clamp(Math.round((float) (across + 8.0) * 16F), 0, 255);
        int y = Math.clamp(Math.round((float) (offset.y + 8.0) * 16F), 0, 255);
        level.blockEvent(core, NTBlocks.GATEWAY.get(), EVENT_RIPPLE, x << 8 | y);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_RIPPLE) {
            if (level != null && level.isClientSide()) {
                float x = ((param >> 8) & 0xFF) / 16F - 8F;
                float y = (param & 0xFF) / 16F - 8F;
                ripples.add(new Ripple(level.getGameTime(), x, y));
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private @Nullable BlockPos resolveTarget(ServerLevel level) {
        GatewayIndex index = GatewayIndex.get(level);
        for (int attempt = 0; attempt < MAX_TARGET_ATTEMPTS; attempt++) {
            BlockPos target = index.findNearest(level, worldPosition, address);
            if (target == null) {
                return null;
            }
            if (!level.isLoaded(target)) {
                ChunkPos chunk = ChunkPos.containing(target);
                level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunk, 2);
                level.getChunk(chunk.x(), chunk.z());
            }
            if (!(level.getBlockEntity(target) instanceof GatewayBlockEntity partner) || !partner.isFormed()) {
                index.remove(target);
                continue;
            }
            if (partner.getAddress().equals(address)) {
                return target;
            }
            index.put(target, partner.getAddress());
        }
        return null;
    }

    private void build(ServerLevel level) {
        if (needsSelfHeal() && !pendingBuild && !wild && !needsPower) {
            if (address.equals(GatewayAddress.DEFAULT)) {
                wild = true;
                address = wildAddress(level);
            } else {
                needsPower = true;
            }
        }
        HorizontalDirection[] directions = pendingBuild && preferredDirection != null
                ? new HorizontalDirection[]{preferredDirection}
                : new HorizontalDirection[]{HorizontalDirection.NORTH, HorizontalDirection.EAST};
        BlockPos firstBlocked = null;
        for (HorizontalDirection direction : directions) {
            List<BlockPos> cells = GatewayRing.ringCells(worldPosition, direction);
            BlockPos blocked = null;
            for (BlockPos cell : cells) {
                if (!level.isLoaded(cell)) {
                    return;
                }
                if (!clearable(level.getBlockState(cell))) {
                    blocked = cell;
                    break;
                }
            }
            if (blocked != null) {
                if (firstBlocked == null) {
                    firstBlocked = blocked;
                }
                continue;
            }
            for (BlockPos cell : cells) {
                level.setBlock(cell, NTBlocks.GATEWAY_RING.get().defaultBlockState(), 3);
            }
            if (MultiblockHelper.form(NTMultiblocks.GATEWAY.get(), worldPosition, level)) {
                return;
            }
        }
        blockedAt = firstBlocked;
        update();
    }

    private static GatewayAddress wildAddress(ServerLevel level) {
        GatewayIndex index = GatewayIndex.get(level);
        GatewayAddress candidate = GatewayAddress.DEFAULT;
        for (int attempt = 0; attempt < 64; attempt++) {
            candidate = GatewayAddress.unpack(level.getRandom().nextInt(GatewayAddress.addressCount()));
            if (!candidate.equals(GatewayAddress.DEFAULT) && !index.isAddressUsed(candidate)) {
                return candidate;
            }
        }
        return candidate;
    }

    private static boolean clearable(BlockState state) {
        if (state.is(NTBlocks.GATEWAY_RING.get())) {
            return true;
        }
        if (state.hasBlockEntity()) {
            return false;
        }
        return state.canBeReplaced() || state.is(NTTags.Blocks.GATEWAY_RING_CLEARABLE);
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.address = in.read("address", GatewayAddress.CODEC).orElse(GatewayAddress.DEFAULT);
        this.ringVersion = in.getIntOr("ringVersion", 0);
        this.wild = in.getBooleanOr("wild", false);
        this.needsPower = in.getBooleanOr("needsPower", false);
        this.energy = in.getIntOr("energy", 0);
        this.pendingBuild = in.getBooleanOr("pendingBuild", false);
        this.preferredDirection = in.getInt("preferredDirection").map(i -> HorizontalDirection.values()[Math.floorMod(i, 4)]).orElse(null);
        this.preferredFront = in.getInt("preferredFront").map(Direction::from3DDataValue).orElse(null);
        this.front = Direction.from3DDataValue(in.getIntOr("front", Direction.SOUTH.get3DDataValue()));
        this.multiblockData = in.read("multiblockData", CompoundTag.CODEC).map(this::loadMBData).orElse(MultiblockData.EMPTY);
        this.blockedAt = in.getLong("blockedAt").map(BlockPos::of).orElse(null);
        this.linked = in.getBooleanOr("linked", false);
        boolean nowOpen = in.getBooleanOr("open", false);
        if (synced && nowOpen != open && level != null && level.isClientSide()) {
            linkChangedAt = level.getGameTime();
        }
        this.open = nowOpen;
        this.synced = level != null;
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("address", GatewayAddress.CODEC, this.address);
        out.putInt("ringVersion", this.ringVersion);
        out.putBoolean("wild", this.wild);
        out.putBoolean("needsPower", this.needsPower);
        out.putInt("energy", this.energy);
        out.putBoolean("pendingBuild", this.pendingBuild);
        if (preferredDirection != null) {
            out.putInt("preferredDirection", preferredDirection.ordinal());
        }
        if (preferredFront != null) {
            out.putInt("preferredFront", preferredFront.get3DDataValue());
        }
        out.putInt("front", this.front.get3DDataValue());
        out.store("multiblockData", CompoundTag.CODEC, saveMBData());
        out.putBoolean("linked", this.linked);
        out.putBoolean("open", this.open);
        if (blockedAt != null) {
            out.putLong("blockedAt", blockedAt.asLong());
        }
    }
}
