package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.augments.AugmentType;
import com.breakinblocks.nautec.network.KeyPressedPayload;
import com.breakinblocks.nautec.registries.NTKeybinds;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public abstract class ToggleableMagnetAugment extends Augment {
    private boolean enabled = true;

    protected ToggleableMagnetAugment(AugmentType<?> augmentType, AugmentSlot augmentSlot) {
        super(augmentType, augmentSlot);
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected boolean isPulling() {
        return enabled && !player.isCrouching();
    }

    @Override
    public void clientTick(PlayerTickEvent.Post event) {
        if (player.isLocalPlayer() && NTKeybinds.TOGGLE_MAGNET_KEYBIND.get().consumeClick()) {
            PacketDistributor.sendToServer(new KeyPressedPayload(augmentSlot));
        }
    }

    @Override
    public void handleKeybindPress() {
        if (player.level().isClientSide()) {
            return;
        }
        boolean target = !enabled;
        for (Augment installed : AugmentHelper.getAugments(player).values()) {
            if (installed instanceof ToggleableMagnetAugment magnet) {
                magnet.enabled = target;
                if (magnet != this) {
                    AugmentHelper.syncAugment(player, magnet);
                }
            }
        }
        player.displayClientMessage(Component.translatable(target ? "nautec.augment.magnet.on" : "nautec.augment.magnet.off")
                .withStyle(target ? ChatFormatting.GREEN : ChatFormatting.GRAY), true);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
        this.enabled = !nbt.contains("enabled") || nbt.getBoolean("enabled");
    }
}
