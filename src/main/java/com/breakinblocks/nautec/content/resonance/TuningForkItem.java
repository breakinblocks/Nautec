package com.breakinblocks.nautec.content.resonance;


import java.util.List;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Locale;

public class TuningForkItem extends Item {
    public TuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(context.getPlayer() instanceof Player player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }
        ItemStack fork = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        GlobalPos tuned = fork.get(NTDataComponents.TUNED_EMITTER.get());
        boolean linkingEmitter = tuned != null && !serverPlayer.isSecondaryUseActive()
                && tuned.dimension().equals(serverLevel.dimension()) && !tuned.pos().equals(pos)
                && serverLevel.getBlockEntity(tuned.pos()) instanceof PrismaticEmitterBlockEntity;

        if (!linkingEmitter && serverLevel.getBlockEntity(pos) instanceof PrismaticEmitterBlockEntity emitter) {
            if (!emitter.canTune(serverPlayer)) {
                message(serverPlayer, Component.translatable("nautec.tuning_fork.not_yours", emitter.getOwnerName()).withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
            if (serverPlayer.isSecondaryUseActive()) {
                emitter.clearLinks();
                message(serverPlayer, Component.translatable("nautec.tuning_fork.cleared"));
                return InteractionResult.SUCCESS;
            }
            fork.set(NTDataComponents.TUNED_EMITTER.get(), GlobalPos.of(serverLevel.dimension(), pos.immutable()));
            message(serverPlayer, Component.translatable("nautec.tuning_fork.tuned", emitter.getLinks().size()));
            ring(serverLevel, pos, 1.6F);
            return InteractionResult.SUCCESS;
        }

        if (tuned == null) {
            return InteractionResult.PASS;
        }
        if (!tuned.dimension().equals(serverLevel.dimension()) || !serverLevel.isLoaded(tuned.pos())
                || !(serverLevel.getBlockEntity(tuned.pos()) instanceof PrismaticEmitterBlockEntity emitter)) {
            message(serverPlayer, Component.translatable("nautec.tuning_fork.lost").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!emitter.canTune(serverPlayer)) {
            message(serverPlayer, Component.translatable("nautec.tuning_fork.not_yours", emitter.getOwnerName()).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        PrismaticEmitterBlockEntity.LinkResult result = emitter.toggle(pos, context.getClickedFace());
        ChatFormatting color = result == PrismaticEmitterBlockEntity.LinkResult.LINKED || result == PrismaticEmitterBlockEntity.LinkResult.UNLINKED
                ? ChatFormatting.AQUA : ChatFormatting.RED;
        message(serverPlayer, Component.translatable("nautec.tuning_fork." + result.name().toLowerCase(Locale.ROOT),
                serverLevel.getBlockState(pos).getBlock().getName(), emitter.getLinks().size()).withStyle(color));
        if (result == PrismaticEmitterBlockEntity.LinkResult.LINKED) {
            ring(serverLevel, pos, 2.0F);
            NTCriteriaTriggers.EMITTER_LINKED.get().trigger(serverPlayer);
        } else if (result == PrismaticEmitterBlockEntity.LinkResult.UNLINKED) {
            ring(serverLevel, pos, 1.2F);
        }
        return result == PrismaticEmitterBlockEntity.LinkResult.LINKED || result == PrismaticEmitterBlockEntity.LinkResult.UNLINKED
                ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack fork = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && fork.has(NTDataComponents.TUNED_EMITTER.get())) {
            if (!level.isClientSide()) {
                fork.remove(NTDataComponents.TUNED_EMITTER.get());
                player.displayClientMessage(Component.translatable("nautec.tuning_fork.reset"), true);
            }
            return InteractionResultHolder.success(fork);
        }
        return InteractionResultHolder.pass(fork);
    }

    private static void message(ServerPlayer player, Component text) {
        player.displayClientMessage(text, true);
    }

    private static void ring(ServerLevel level, BlockPos pos, float pitch) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8F, pitch);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GlobalPos tuned = stack.get(NTDataComponents.TUNED_EMITTER.get());
        if (tuned != null) {
            tooltip.add(Component.translatable("nautec.tuning_fork.tooltip.tuned", tuned.pos().getX(), tuned.pos().getY(), tuned.pos().getZ())
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("nautec.tuning_fork.tooltip.untuned").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("nautec.tuning_fork.tooltip.usage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
