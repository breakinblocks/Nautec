package com.breakinblocks.nautec.api.augments;

import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.data.NTDataAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public abstract class Augment {
    protected final AugmentType<?> augmentType;
    protected Player player;
    protected final AugmentSlot augmentSlot;

    private static final int COOLDOWN_SAVE_INTERVAL = 20;

    private int cooldown;
    private List<ItemStack> installedParts = List.of();

    public Augment(AugmentType<?> augmentType, AugmentSlot augmentSlot) {
        this.augmentType = augmentType;
        this.augmentSlot = augmentSlot;
    }

    public Augment copyForRender() {
        Augment copy = augmentType.create(augmentSlot);
        copy.setPlayer(player);
        copy.deserializeNBT(player.level().registryAccess(), serializeNBT(player.level().registryAccess()));
        return copy;
    }

    public boolean replaceBodyPart() {
        return false;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public AugmentType<?> getAugmentType() {
        return augmentType;
    }

    public Player getPlayer() {
        return player;
    }

    public AugmentSlot getAugmentSlot() {
        return augmentSlot;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
        setChanged();
    }

    public void onAdded(Player player) {
    }

    public void onRemoved(Player player) {
    }

    public void readParts(List<ItemStack> parts) {
    }

    public final void setInstalledParts(List<ItemStack> parts) {
        List<ItemStack> copies = new ArrayList<>(parts.size());
        for (ItemStack part : parts) {
            if (!part.isEmpty()) {
                copies.add(part.copyWithCount(1));
            }
        }
        this.installedParts = List.copyOf(copies);
    }

    public final List<ItemStack> getInstalledParts() {
        List<ItemStack> copies = new ArrayList<>(installedParts.size());
        for (ItemStack part : installedParts) {
            copies.add(part.copy());
        }
        return copies;
    }

    public void commonTick(PlayerTickEvent.Post event) {
        if (player == null) return;
        if (isOnCooldown()) {
            cooldown--;
            if (cooldown == 0 || cooldown % COOLDOWN_SAVE_INTERVAL == 0) {
                setChanged();
            }
        }
        if (player.level().isClientSide()) {
            clientTick(event);
        } else {
            serverTick(event);
        }
    }

    @Deprecated
    public void clientTick(PlayerTickEvent.Post event) {

    }

    @Deprecated
    public void serverTick(PlayerTickEvent.Post event) {

    }

    public void fall(LivingFallEvent event) {

    }

    public boolean canActivate() {
        return player != null && player.isAlive() && !player.isSpectator() && !isOnCooldown();
    }

    public void handleKeybindPress() {
    }

    public boolean isOnCooldown() {
        return getCooldown() > 0;
    }

    protected final void setChanged() {
        player.setData(NTDataAttachments.AUGMENT_DATA_CHANGED, NTRegistries.AUGMENT_SLOT.getId(augmentSlot));
    }

    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cooldown", cooldown);
        if (!installedParts.isEmpty()) {
            tag.store("installed_parts", ItemStack.CODEC.listOf(), provider.createSerializationContext(NbtOps.INSTANCE), installedParts);
        }
        return tag;
    }

    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        this.cooldown = nbt.getIntOr("cooldown", 0);
        this.installedParts = List.copyOf(nbt.read("installed_parts", ItemStack.CODEC.listOf(), provider.createSerializationContext(NbtOps.INSTANCE))
                .orElse(List.of()));
    }
}
