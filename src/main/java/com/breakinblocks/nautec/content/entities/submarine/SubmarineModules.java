package com.breakinblocks.nautec.content.entities.submarine;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.network.SubmarineCooldownPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class SubmarineModules {
    private final SubmarineEntity submarine;
    private final long[] readyAt = new long[SubmarineEntity.MODULE_SLOTS];
    private final int[] cooldownTicks = new int[SubmarineEntity.MODULE_SLOTS];
    private final int[] activeTicks = new int[SubmarineEntity.MODULE_SLOTS];

    public SubmarineModules(SubmarineEntity submarine) {
        this.submarine = submarine;
    }

    public void tickServer() {
    }

    public boolean isReady(int slot) {
        return this.submarine.level().getGameTime() >= this.readyAt[slot];
    }

    public int remainingCooldown(int slot) {
        return (int) Math.max(0L, this.readyAt[slot] - this.submarine.level().getGameTime());
    }

    public void startCooldown(int slot, int cooldown, int active) {
        this.readyAt[slot] = this.submarine.level().getGameTime() + cooldown;
        this.cooldownTicks[slot] = cooldown;
        this.activeTicks[slot] = active;
        broadcast(new SubmarineCooldownPayload(this.submarine.getId(), slot, cooldown, active));
    }

    public void clearCooldowns() {
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            this.readyAt[slot] = 0L;
            this.cooldownTicks[slot] = 0;
            this.activeTicks[slot] = 0;
        }
    }

    public void sendCooldownSnapshot(ServerPlayer player) {
        for (int slot = 0; slot < SubmarineEntity.MODULE_SLOTS; slot++) {
            int remaining = remainingCooldown(slot);
            if (remaining <= 0) {
                continue;
            }

            int elapsed = this.cooldownTicks[slot] - remaining;
            int activeLeft = Math.max(0, this.activeTicks[slot] - elapsed);
            PacketDistributor.sendToPlayer(player,
                    new SubmarineCooldownPayload(this.submarine.getId(), slot, remaining, activeLeft));
        }
    }

    public void activate(int slot, Player pilot) {
        if (slot < 0 || slot >= SubmarineEntity.MODULE_SLOTS) {
            return;
        }

        SubmarineModuleType type = this.submarine.getModuleType(slot);
        if (type == null || type.isPassive()) {
            return;
        }

        if (!isReady(slot)) {
            refuse(pilot, "cooldown");
            return;
        }

        if (!hasPowerFor(type, pilot)) {
            refuse(pilot, "no_power");
            return;
        }

        if (!applyAbility(type, slot, pilot)) {
            return;
        }

        startCooldown(slot, type.cooldownTicks(), type.activeTicks());
    }

    public boolean hasPowerFor(SubmarineModuleType type, Player pilot) {
        return pilot.gameMode().isCreative() || this.submarine.getPowerStored() >= type.powerCost();
    }

    private boolean applyAbility(SubmarineModuleType type, int slot, Player pilot) {
        return true;
    }

    private void refuse(Player pilot, String reason) {
        pilot.sendOverlayMessage(Component.translatable("nautec.submarine.ability." + reason)
                .withStyle(ChatFormatting.RED));
    }

    private void broadcast(SubmarineCooldownPayload payload) {
        for (Entity passenger : this.submarine.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
