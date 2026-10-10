package com.breakinblocks.nautec.content.resonantstorage;


import com.breakinblocks.nautec.api.blockentities.NTBlockEntity;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class ResonantStorageBlockEntity extends NTBlockEntity implements MenuProvider, ResonantStore.Listener {
    protected static final Direction[] DIRECTIONS = Direction.values();
    private static final Codec<List<FaceMode>> FACES_CODEC = FaceMode.CODEC.listOf(6, 6);

    protected ResonantChannel channel = ResonantChannel.PUBLIC_DEFAULT;
    protected String ownerName = "";
    protected final FaceMode[] faces = new FaceMode[DIRECTIONS.length];
    protected int clientUpgrades;
    private @Nullable ResonantStore store;
    private boolean syncPending;
    private long nextTransfer;

    protected ResonantStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(faces, FaceMode.OPEN);
    }

    protected abstract ResonantStore createStore(ResonantStorage storage, ResonantChannel channel);

    protected abstract void transfer(ServerLevel level, Direction face, FaceMode mode);

    protected void writeClient(CompoundTag tag, HolderLookup.Provider registries) {
    }

    protected void readClient(ValueInput input) {
    }

    public @Nullable ResonantStore store() {
        if (store == null && level instanceof ServerLevel server && !isRemoved()) {
            store = createStore(ResonantStorage.get(server.getServer()), channel);
            store.addListener(this);
        }
        return store;
    }

    private void release() {
        if (store != null) {
            store.removeListener(this);
            store = null;
        }
    }

    public ResonantChannel channel() {
        return channel;
    }

    public String ownerName() {
        return ownerName;
    }

    public FaceMode face(Direction direction) {
        return faces[direction.ordinal()];
    }

    public int upgrades() {
        ResonantStore current = store();
        return current != null ? current.upgrades() : clientUpgrades;
    }

    public boolean canUse(Player player) {
        return ResonantAccess.canUse(player, channel);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel) {
            store();
            ensureTicking();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        release();
    }

    public void link(ResonantLink link) {
        if (link.channel().equals(channel) && link.ownerName().equals(ownerName)) {
            return;
        }
        release();
        channel = link.channel();
        ownerName = link.ownerName();
        store();
        invalidateCapabilities();
        setChanged();
        syncNow();
        if (level != null && !level.isClientSide()) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public void setFace(Direction direction, FaceMode mode) {
        if (faces[direction.ordinal()] == mode) {
            return;
        }
        faces[direction.ordinal()] = mode;
        invalidateCapabilities();
        setChanged();
        syncNow();
        ensureTicking();
    }

    @Override
    public void storeChanged() {
        if (level == null || level.isClientSide() || syncPending) {
            return;
        }
        syncPending = true;
        schedule(2);
    }

    private void schedule(int delay) {
        Block block = getBlockState().getBlock();
        if (level != null && !level.getBlockTicks().hasScheduledTick(worldPosition, block)) {
            level.scheduleTick(worldPosition, block, delay);
        }
    }

    public void ensureTicking() {
        if (level instanceof ServerLevel && hasAutomaticFace()) {
            schedule(1);
        }
    }

    public boolean hasAutomaticFace() {
        for (FaceMode mode : faces) {
            if (mode.automatic()) {
                return true;
            }
        }
        return false;
    }

    public void serverTick(ServerLevel level) {
        if (syncPending) {
            syncPending = false;
            syncNow();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
        if (!hasAutomaticFace()) {
            return;
        }
        long time = level.getGameTime();
        if (time >= nextTransfer) {
            for (Direction direction : DIRECTIONS) {
                FaceMode mode = faces[direction.ordinal()];
                if (mode.automatic()) {
                    transfer(level, direction, mode);
                }
            }
            nextTransfer = time + NTConfig.resonantTransferInterval;
        }
        schedule((int) Math.max(1, nextTransfer - time));
    }

    protected boolean sameChannel(BlockEntity neighbour) {
        return neighbour instanceof ResonantStorageBlockEntity other && other.getType() == getType() && other.channel.equals(channel);
    }

    protected void syncNow() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public int comparatorSignal() {
        ResonantStore current = store();
        return current != null ? current.comparatorSignal() : 0;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("channel", ResonantChannel.CODEC, channel);
        output.putString("owner_name", ownerName);
        output.store("faces", FACES_CODEC, List.of(faces));
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        ResonantChannel loaded = input.read("channel", ResonantChannel.CODEC).orElse(ResonantChannel.PUBLIC_DEFAULT);
        if (!loaded.equals(channel)) {
            release();
            channel = loaded;
        }
        ownerName = input.getStringOr("owner_name", "");
        List<FaceMode> loadedFaces = input.read("faces", FACES_CODEC).orElse(null);
        for (int i = 0; i < faces.length; i++) {
            faces[i] = loadedFaces != null ? loadedFaces.get(i) : FaceMode.OPEN;
        }
        clientUpgrades = input.getIntOr("client_upgrades", 0);
        readClient(input);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.putInt("client_upgrades", upgrades());
        writeClient(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.@NotNull DataComponentInput components) {
        super.applyImplicitComponents(components);
        ResonantLink link = components.get(NTDataComponents.RESONANT_LINK.get());
        if (link != null) {
            release();
            channel = link.channel();
            ownerName = link.ownerName();
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder components) {
        super.collectImplicitComponents(components);
        components.set(NTDataComponents.RESONANT_LINK.get(), new ResonantLink(channel, ownerName));
    }

    @Override
    public void removeComponentsFromTag(@NotNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("channel");
        output.discard("owner_name");
    }
}
