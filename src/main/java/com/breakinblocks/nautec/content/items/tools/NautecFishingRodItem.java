package com.breakinblocks.nautec.content.items.tools;

import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class NautecFishingRodItem extends FishingRodItem {
    public NautecFishingRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack rod = player.getItemInHand(hand);

        if (player.fishing != null) {
            if (!level.isClientSide()) {
                int damage = player.fishing.retrieve(rod);
                ItemStack original = rod.copy();
                rod.hurtAndBreak(damage, player, hand.asEquipmentSlot());
                if (rod.isEmpty()) {
                    net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, original, hand);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE,
                    SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            rod.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW,
                    SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (level instanceof ServerLevel serverLevel) {
                int lureSpeed = (int) (EnchantmentHelper.getFishingTimeReduction(serverLevel, rod, player) * 20.0F);
                int luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, rod, player);
                Projectile.spawnProjectile(new NautecFishingHook(player, level, luck, lureSpeed), serverLevel, rod);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            rod.causeUseVibration(player, GameEvent.ITEM_INTERACT_START);
        }

        return InteractionResult.SUCCESS;
    }
}
