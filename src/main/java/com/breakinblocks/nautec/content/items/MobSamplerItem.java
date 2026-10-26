package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = Nautec.MODID)
public class MobSamplerItem extends Item {
    public MobSamplerItem(Properties properties) {
        super(properties);
    }

    // Handle the precise interaction before a mob opens its own menu or trades.
    @SubscribeEvent
    public static void onSampleEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() instanceof MobSamplerItem && event.getTarget() instanceof LivingEntity target) {
            event.setCanceled(true);
            event.setCancellationResult(sample(event.getItemStack(), event.getEntity(), target, event.getHand()));
        }
    }

    @SubscribeEvent
    public static void onSampleEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof MobSamplerItem && event.getTarget() instanceof LivingEntity target) {
            event.setCanceled(true);
            event.setCancellationResult(sample(event.getItemStack(), event.getEntity(), target, event.getHand()));
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return sample(stack, player, target, hand);
    }

    public static InteractionResult sample(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.isSpectator() || !player.getAbilities().mayBuild || !target.isAlive()
                || !(target instanceof Mob) || !MobSampleItem.supports(target.getType())) {
            if (!player.level().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("nautec.mob_sampler.unsupported").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack sample = new ItemStack(NTItems.MOB_SAMPLE.get());
        sample.set(NTDataComponents.MOB_SAMPLE.get(), target.getType());
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            player.setItemInHand(hand, sample);
        } else if (!player.getInventory().add(sample)) {
            player.drop(sample, false);
        }
        player.level().playSound(null, target.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.7F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("nautec.mob_sampler.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
