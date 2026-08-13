package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GatewayBlockEntity extends ContainerBlockEntity {
    private static final int AMBIENT_PERIOD = 120;
    private static final int UNLINKED_PERIOD = 60;

    private GatewayAddress address = GatewayAddress.DEFAULT;

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

        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 10 != 0) {
            return;
        }

        AABB box = new AABB(worldPosition.above()).inflate(0.5);
        List<Entity> riders = serverLevel.getEntitiesOfClass(Entity.class, box, GatewayBlockEntity::canTravel);
        if (riders.isEmpty()) {
            return;
        }

        BlockPos target = GatewayIndex.get(serverLevel).findNearest(serverLevel, worldPosition, address);
        if (target == null) {
            if (serverLevel.getGameTime() % UNLINKED_PERIOD == 0) {
                MachineSounds.play(serverLevel, worldPosition, NTSounds.GATEWAY_UNLINKED, 0.6f, 0.6f);
            }
            return;
        }

        for (Entity entity : riders) {
            send(serverLevel, entity, target);
        }
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
        MachineSounds.play(level, worldPosition, NTSounds.GATEWAY_TRAVEL, 0.9f, 0.9f);

        root.teleport(new TeleportTransition(level, destination, Vec3.ZERO, root.getYRot(), root.getXRot(),
                Set.of(), TeleportTransition.DO_NOTHING));

        MachineSounds.play(level, target, NTSounds.GATEWAY_TRAVEL, 0.9f, 1.1f);

        for (Entity part : root.getSelfAndPassengers().toList()) {
            part.setPortalCooldown(NTConfig.gatewayCooldown);
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
