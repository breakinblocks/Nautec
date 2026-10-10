package com.breakinblocks.nautec.content.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.IOActions;
import com.breakinblocks.nautec.content.blocks.OxygenDiffuserBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OxygenDiffuserBlockEntity extends LaserBlockEntity {
    public static final int INTERVAL = 20;
    public static final int EFFECT_TICKS = 100;
    private static final int LASER_GRACE = 40;

    private int laserHold;

    public OxygenDiffuserBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.OXYGEN_DIFFUSER.get(), pos, state);
    }

    public boolean isRunning() {
        return getPower() >= NTConfig.oxygenDiffuserPower || laserHold > 0;
    }

    public static int radius() {
        return NTConfig.oxygenDiffuserRadius;
    }

    @Override
    public Set<Direction> getLaserInputs() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public Set<Direction> getLaserOutputs() {
        return Set.of();
    }

    @Override
    public void commonTick() {
        super.commonTick();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (getPower() >= NTConfig.oxygenDiffuserPower) {
            laserHold = LASER_GRACE;
        } else if (laserHold > 0) {
            laserHold--;
        }
        boolean running = isRunning();
        if (getBlockState().getValue(OxygenDiffuserBlock.ACTIVE) != running) {
            serverLevel.setBlock(worldPosition, getBlockState().setValue(OxygenDiffuserBlock.ACTIVE, running), Block.UPDATE_CLIENTS);
        }
        if (running && serverLevel.getGameTime() % INTERVAL == 0) {
            breathe(serverLevel);
        }
    }

    public int breathe(ServerLevel serverLevel) {
        int radius = radius();
        double reach = (radius + 0.5) * (radius + 0.5);
        AABB area = new AABB(worldPosition).inflate(radius);
        int count = 0;
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area, player -> player.isAlive() && !player.isSpectator())) {
            if (player.distanceToSqr(worldPosition.getCenter()) > reach) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, EFFECT_TICKS, 0, true, false, true));
            player.setAirSupply(player.getMaxAirSupply());
            count++;
        }
        return count;
    }

    @Override
    public <T> Map<Direction, Pair<IOActions, int[]>> getSidedInteractions(BlockCapability<T, @Nullable Direction> capability) {
        return Map.of();
    }

    @Override
    protected void saveData(ValueOutput out) {
        super.saveData(out);
        out.putInt("laser_hold", laserHold);
    }

    @Override
    protected void loadData(ValueInput in) {
        super.loadData(in);
        this.laserHold = in.getIntOr("laser_hold", 0);
    }
}
