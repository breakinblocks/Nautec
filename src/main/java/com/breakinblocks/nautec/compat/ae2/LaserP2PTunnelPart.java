package com.breakinblocks.nautec.compat.ae2;

import appeng.api.parts.IPartItem;
import appeng.parts.p2p.P2PTunnelPart;
import com.breakinblocks.nautec.api.blockentities.LaserBlockEntity;
import com.breakinblocks.nautec.capabilities.beam.IBeamReceiver;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LaserP2PTunnelPart extends P2PTunnelPart<LaserP2PTunnelPart> implements IBeamReceiver {
    private boolean clientOutput;

    public LaserP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
    }

    public boolean receivesBeams() {
        return !(isClientSide() ? clientOutput : isOutput());
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(isOutput());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        boolean output = data.readBoolean();
        changed |= output != clientOutput;
        clientOutput = output;
        return changed;
    }

    @Override
    public void onTunnelConfigChange() {
        syncOutput();
    }

    @Override
    public void onTunnelNetworkChange() {
        syncOutput();
    }

    private void syncOutput() {
        if (!isClientSide()) {
            getHost().markForUpdate();
        }
    }

    @Override
    public void receiveBeam(int power, float purity, Direction travel, BlockPos origin, LongSet trail) {
        if (isClientSide() || isOutput() || !getMainNode().isActive()) {
            return;
        }
        List<LaserP2PTunnelPart> outputs = getOutputs();
        LaserP2PTunnelPart[] senders = new LaserP2PTunnelPart[outputs.size()];
        LaserBlockEntity[] targets = new LaserBlockEntity[outputs.size()];
        int count = 0;
        for (LaserP2PTunnelPart output : outputs) {
            LaserBlockEntity target = output.target(trail);
            if (target != null) {
                senders[count] = output;
                targets[count] = target;
                count++;
            }
        }
        if (count == 0) {
            return;
        }
        int share = power / count;
        int extra = power % count;
        for (int i = 0; i < count; i++) {
            LaserP2PTunnelPart sender = senders[i];
            targets[i].acceptBeam(share + (i < extra ? 1 : 0), purity, sender.getSide(), sender.getBlockEntity().getBlockPos(), trail);
        }
    }

    private @Nullable LaserBlockEntity target(LongSet trail) {
        Direction side = getSide();
        BlockEntity host = getBlockEntity();
        Level level = host.getLevel();
        if (side == null || level == null || !getMainNode().isActive()) {
            return null;
        }
        BlockPos at = host.getBlockPos().relative(side);
        if (trail.contains(at.asLong()) || !level.isLoaded(at)) {
            return null;
        }
        return level.getBlockEntity(at) instanceof LaserBlockEntity laser && laser.getLaserInputs().contains(side.getOpposite()) ? laser : null;
    }
}
