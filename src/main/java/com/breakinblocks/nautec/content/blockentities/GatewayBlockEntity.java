package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayEffects;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GatewayBlockEntity extends ContainerBlockEntity {
    private static final int AMBIENT_PERIOD = 120;
    private static final int UNLINKED_PERIOD = 60;
    private static final int SWEEP_PERIOD = 3;
    private static final int MAX_TARGET_ATTEMPTS = 4;

    private GatewayAddress address = GatewayAddress.DEFAULT;
    private final Set<UUID> arrivals = new HashSet<>();

    public GatewayBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(NTBlockEntityTypes.GATEWAY.get(), blockPos, blockState);
    }

    public GatewayAddress getAddress() {
        return address;
    }

    public void setAddress(GatewayAddress address) {
        this.address = address;
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).put(worldPosition, address);
        }
        update();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).put(worldPosition, address);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            GatewayIndex.get(serverLevel).remove(pos);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public void commonTick() {
        super.commonTick();

        MachineSounds.interval(level, worldPosition, NTSounds.GATEWAY_AMBIENT, AMBIENT_PERIOD, 0.5f, 0.8f);

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameTime() % SWEEP_PERIOD == 0) {
            GatewayEffects.sweep(serverLevel, worldPosition);
        }

        if (serverLevel.getGameTime() % 10 != 0) {
            return;
        }

        List<Entity> present = serverLevel.getEntitiesOfClass(Entity.class, padBox());
        if (!arrivals.isEmpty()) {
            Set<UUID> stillHere = new HashSet<>();
            for (Entity entity : present) {
                stillHere.add(entity.getUUID());
            }
            arrivals.retainAll(stillHere);
        }

        List<Entity> riders = present.stream()
                .filter(GatewayBlockEntity::canTravel)
                .filter(entity -> !arrivals.contains(entity.getUUID()))
                .toList();
        if (riders.isEmpty()) {
            return;
        }

        BlockPos target = resolveTarget(serverLevel);
        if (target == null) {
            if (serverLevel.getGameTime() % UNLINKED_PERIOD == 0) {
                MachineSounds.play(serverLevel, worldPosition, NTSounds.GATEWAY_UNLINKED, 0.6f, 0.6f);
                GatewayEffects.unlinked(serverLevel, worldPosition);
            }
            return;
        }

        for (Entity entity : riders) {
            send(serverLevel, entity, target);
        }
    }

    private AABB padBox() {
        return new AABB(worldPosition.above()).inflate(0.5);
    }

    public boolean isWaitingForStepOff(Entity entity) {
        return arrivals.contains(entity.getUUID());
    }

    private void markArrived(Entity root) {
        root.getSelfAndPassengers().forEach(part -> arrivals.add(part.getUUID()));
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
            if (!(level.getBlockEntity(target) instanceof GatewayBlockEntity partner)) {
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

    private static boolean canTravel(Entity entity) {
        return !entity.isSpectator()
                && !entity.isOnPortalCooldown()
                && entity.isAlive()
                && !entity.isPassenger();
    }

    private void send(ServerLevel level, Entity entity, BlockPos target) {
        Entity root = entity.getRootVehicle();
        if (root.isOnPortalCooldown()) {
            return;
        }

        Vec3 destination = Vec3.atBottomCenterOf(target.above());
        Vec3 offset = destination.subtract(root.position());
        if (root.getSelfAndPassengers().anyMatch(part -> !level.noCollision(part, part.getBoundingBox().move(offset)))) {
            return;
        }
        MachineSounds.play(level, worldPosition, NTSounds.GATEWAY_TRAVEL, 0.9f, 0.9f);
        GatewayEffects.travel(level, worldPosition);

        root.teleport(new TeleportTransition(level, destination, Vec3.ZERO, root.getYRot(), root.getXRot(),
                Set.of(), TeleportTransition.DO_NOTHING));

        MachineSounds.play(level, target, NTSounds.GATEWAY_TRAVEL, 0.9f, 1.1f);
        GatewayEffects.travel(level, target);

        for (Entity part : root.getSelfAndPassengers().toList()) {
            part.setPortalCooldown(NTConfig.gatewayCooldown);
        }

        if (level.getBlockEntity(target) instanceof GatewayBlockEntity arrivalPad) {
            arrivalPad.markArrived(root);
        }
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.address = in.read("address", GatewayAddress.CODEC).orElse(GatewayAddress.DEFAULT);
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.store("address", GatewayAddress.CODEC, this.address);
    }
}
