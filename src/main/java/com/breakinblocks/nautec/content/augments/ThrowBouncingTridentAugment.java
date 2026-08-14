package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.content.entities.ThrownBouncingTrident;
import com.breakinblocks.nautec.network.KeyPressedPayload;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTKeybinds;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ThrowBouncingTridentAugment extends Augment {
    public ThrowBouncingTridentAugment(AugmentSlot augmentSlot) {
        super(NTAugments.THROWN_BOUNCING_TRIDENT_AUGMENT.get(), augmentSlot);
    }

    @Override
    public boolean replaceBodyPart() {
        return true;
    }

    @Override
    public void clientTick(PlayerTickEvent.Post event) {
        if (NTKeybinds.THROW_TRIDENT_KEYBIND.get().consumeClick() && !isOnCooldown()) {
            ClientPacketDistributor.sendToServer(new KeyPressedPayload(augmentSlot));
            handleKeybindPress();
        }
    }

    @Override
    public void handleKeybindPress() {
        if (!player.level().isClientSide()) {
            ThrownBouncingTrident trident = new ThrownBouncingTrident(player.level(), player, Items.TRIDENT.getDefaultInstance(), 1);
            trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 0.0f);
            player.level().addFreshEntity(trident);
        }
        setCooldown(20);
    }
}
