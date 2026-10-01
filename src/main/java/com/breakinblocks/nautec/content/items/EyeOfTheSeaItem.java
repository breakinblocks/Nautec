package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.EyeOfTheSeaEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.events.LuckyFishingZoneEvents;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class EyeOfTheSeaItem extends Item {
    public EyeOfTheSeaItem(Properties properties) {
        super(properties);
    }

    public static SeaEyeTarget targetOf(ItemStack stack) {
        return stack.getOrDefault(NTDataComponents.SEA_EYE_TARGET.get(), SeaEyeTarget.CRYSTAL_GEODES);
    }

    public static SeaEyeTarget cycle(ItemStack stack) {
        SeaEyeTarget next = targetOf(stack).next();
        stack.set(NTDataComponents.SEA_EYE_TARGET.get(), next);
        return next;
    }

    public static @Nullable BlockPos locate(ServerLevel level, BlockPos from, SeaEyeTarget target) {
        return level.findNearestMapStructure(target.structures(), from, NTConfig.eyeOfTheSeaSearchRadius, false);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                SeaEyeTarget next = cycle(stack);
                player.sendOverlayMessage(Component.translatable("nautec.eye_of_the_sea.seeking", next.displayName())
                        .withStyle(ChatFormatting.AQUA));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F + 0.1F * next.ordinal());
            }
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        SeaEyeTarget target = targetOf(stack);
        BlockPos found = locate(serverLevel, player.blockPosition(), target);
        player.getCooldowns().addCooldown(stack, NTConfig.eyeOfTheSeaCooldownTicks);
        if (found == null) {
            player.sendOverlayMessage(Component.translatable("nautec.eye_of_the_sea.not_found", target.displayName())
                    .withStyle(ChatFormatting.RED));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CONDUIT_DEACTIVATE, SoundSource.PLAYERS, 0.6F, 1.4F);
            return InteractionResult.FAIL;
        }

        EyeOfTheSeaEntity eye = new EyeOfTheSeaEntity(level, player.getX(), player.getY(0.5), player.getZ());
        eye.setItem(stack);
        eye.signalTo(Vec3.atLowerCornerOf(found));
        level.gameEvent(GameEvent.PROJECTILE_SHOOT, eye.position(), GameEvent.Context.of(player));
        level.addFreshEntity(eye);

        float pitch = Mth.lerp(level.getRandom().nextFloat(), 0.33F, 0.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, pitch);
        LuckyFishingZoneEvents.boost(player, NTConfig.eyeOfTheSeaLuckyBoostSeconds * 20);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        Tooltips.tt(tooltipComponents, Component.translatable("nautec.eye_of_the_sea.seeking", targetOf(stack).displayName()),
                ChatFormatting.AQUA);
        Tooltips.trans(tooltipComponents, "nautec.eye_of_the_sea.cycle_hint", ChatFormatting.GRAY);
    }
}
