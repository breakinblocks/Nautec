package com.breakinblocks.nautec.content.resonantstorage;

import com.breakinblocks.nautec.transfer.TransferCapabilities;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.menus.ResonantVaultMenu;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.MachineSounds;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;

import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.ResourceHandlerUtil;
import com.breakinblocks.nautec.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class ResonantVaultBlockEntity extends ResonantStorageBlockEntity implements GeoBlockEntity {
    public static final DataTicket<Boolean> OPEN = new DataTicket<>("nautec:resonant_vault_open", Boolean.class);
    private static final int OPENERS_EVENT = 1;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation OPENING = RawAnimation.begin().thenPlay("open").thenLoop("opened");
    private static final RawAnimation CLOSING = RawAnimation.begin().thenPlay("close").thenLoop("idle");

    private final AnimatableInstanceCache animatableCache = GeckoLibUtil.createInstanceCache(this);
    @SuppressWarnings("unchecked")
    private final BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction>[] neighbours = new BlockCapabilityCache[6];
    private int openers;
    private boolean everOpened;

    public ResonantVaultBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlockEntityTypes.RESONANT_VAULT.get(), pos, state);
    }

    @Override
    protected ResonantStore createStore(ResonantStorage storage, ResonantChannel channel) {
        return storage.vault(channel);
    }

    public @Nullable VaultStore vault() {
        return (VaultStore) store();
    }

    public @Nullable ResourceHandler<ItemResource> itemHandler(@Nullable Direction side) {
        if (side != null && !face(side).exposed()) {
            return null;
        }
        VaultStore vault = vault();
        return vault != null ? vault.view() : null;
    }

    @Override
    protected void transfer(ServerLevel level, Direction face, FaceMode mode) {
        VaultStore vault = vault();
        if (vault == null) {
            return;
        }
        BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> cache = neighbours[face.ordinal()];
        if (cache == null) {
            cache = BlockCapabilityCache.create(TransferCapabilities.Item.BLOCK, level, worldPosition.relative(face), face.getOpposite());
            neighbours[face.ordinal()] = cache;
        }
        ResourceHandler<ItemResource> neighbour = cache.getCapability();
        if (neighbour == null) {
            return;
        }
        BlockEntity other = level.getBlockEntity(worldPosition.relative(face));
        if (other != null && sameChannel(other)) {
            return;
        }
        int amount = NTConfig.resonantItemsPerTransfer;
        if (mode == FaceMode.PUSH) {
            ResourceHandlerUtil.moveStacking(vault.view(), neighbour, resource -> true, amount, null);
        } else {
            ResourceHandlerUtil.moveStacking(neighbour, vault.view(), resource -> true, amount, null);
        }
    }

    public void startOpen(Player player) {
        if (player.isSpectator() || level == null) {
            return;
        }
        openers++;
        if (openers == 1) {
            MachineSounds.play(level, worldPosition, NTSounds.RESONANT_VAULT_OPEN, 0.7f, 1.0f);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), OPENERS_EVENT, openers);
    }

    public void stopOpen(Player player) {
        if (player.isSpectator() || level == null || openers <= 0) {
            return;
        }
        openers--;
        if (openers == 0) {
            MachineSounds.play(level, worldPosition, NTSounds.RESONANT_VAULT_CLOSE, 0.7f, 1.0f);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), OPENERS_EVENT, openers);
    }

    public boolean isOpen() {
        return openers > 0;
    }

    @Override
    public boolean triggerEvent(int id, int value) {
        if (id == OPENERS_EVENT) {
            openers = value;
            return true;
        }
        return super.triggerEvent(id, value);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResonantVaultMenu(containerId, inventory, this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ResonantVaultBlockEntity>(this, "hatch", 3, state -> {
            if (isOpen()) {
                everOpened = true;
                return state.setAndContinue(OPENING);
            }
            return state.setAndContinue(everOpened ? CLOSING : IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }
}
