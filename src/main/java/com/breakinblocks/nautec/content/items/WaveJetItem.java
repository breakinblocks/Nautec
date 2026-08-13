package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTSounds;
import com.breakinblocks.nautec.utils.ItemUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class WaveJetItem extends Item implements IPowerItem {
    private static final int USE_DURATION = 72000;

    public WaveJetItem(Properties properties) {
        super(properties.component(NTDataComponents.POWER, ComponentPowerStorage.withCapacity(6000)));
    }

    @Override
    public int getMaxInput() {
        return 128;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ItemUtils.POWER_BAR_COLOR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ItemUtils.powerForDurabilityBar(stack);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isInWater() || !hasCharge(stack, player)) {
            return InteractionResult.PASS;
        }

        player.startUsingItem(hand);
        playAt(level, player, NTSounds.WAVE_JET_START.get(), 0.6f, 1.3f);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (!player.isInWater() || !hasCharge(stack, player)) {
            player.stopUsingItem();
            return;
        }

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(NTConfig.waveJetThrust)));

        player.setSwimming(true);
        player.setPose(Pose.SWIMMING);

        if (!player.getAbilities().instabuild) {
            drain(stack, NTConfig.waveJetPowerUsage);
        }

        if (level.isClientSide()) {
            Vec3 wake = player.position().subtract(look.scale(0.6));
            level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, wake.x, wake.y + 0.4, wake.z,
                    -look.x * 0.2, -look.y * 0.2, -look.z * 0.2);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof Player player && !player.isInWater()) {
            player.setSwimming(false);
        }
        playAt(level, entity, NTSounds.WAVE_JET_STOP.get(), 0.5f, 1.4f);
        return false;
    }

    private static void playAt(Level level, LivingEntity entity, SoundEvent sound, float volume, float pitch) {
        if (level.isClientSide()) {
            return;
        }
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static boolean hasCharge(ItemStack stack, Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        IPowerStorage storage = storage(stack);
        return storage != null && storage.getPowerStored() >= NTConfig.waveJetPowerUsage;
    }

    private static void drain(ItemStack stack, int amount) {
        IPowerStorage storage = storage(stack);
        if (storage != null) {
            storage.tryDrainPower(amount, false);
        }
    }

    private static @Nullable IPowerStorage storage(ItemStack stack) {
        return stack.getCapability(NTCapabilities.PowerStorage.ITEM);
    }
}
