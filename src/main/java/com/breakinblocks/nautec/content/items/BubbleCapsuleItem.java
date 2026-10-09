package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.bubble.AirPocketBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class BubbleCapsuleItem extends Item {
    public BubbleCapsuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        if (!player.isEyeInFluid(FluidTags.WATER) || !level.getBlockState(eyes).is(Blocks.WATER)) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("nautec.bubble_capsule.not_underwater").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        serverLevel.setBlock(eyes, NTBlocks.AIR_POCKET.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(serverLevel.getBlockEntity(eyes) instanceof AirPocketBlockEntity pocket)) {
            return InteractionResult.FAIL;
        }
        pocket.start(serverLevel, NTConfig.bubbleCapsuleRadius, NTConfig.bubbleCapsuleSeconds * 20);
        player.setAirSupply(player.getMaxAirSupply());
        serverLevel.playSound(null, eyes, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverLevel.sendParticles(ParticleTypes.BUBBLE, eyes.getX() + 0.5, eyes.getY() + 0.5, eyes.getZ() + 0.5, 40,
                NTConfig.bubbleCapsuleRadius * 0.5, NTConfig.bubbleCapsuleRadius * 0.5, NTConfig.bubbleCapsuleRadius * 0.5, 0.1);
        ItemStack stack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(stack, 20);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        int size = NTConfig.bubbleCapsuleRadius * 2 + 1;
        tooltip.accept(Component.translatable("nautec.bubble_capsule.tooltip", size, size, size, NTConfig.bubbleCapsuleSeconds)
                .withStyle(ChatFormatting.GRAY));
    }
}
