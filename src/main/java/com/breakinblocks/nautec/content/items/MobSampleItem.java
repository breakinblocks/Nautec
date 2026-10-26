package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

import java.util.function.Consumer;

public class MobSampleItem extends Item {
    public MobSampleItem(Properties properties) {
        super(properties);
    }

    public static boolean supports(EntityType<?> type) {
        return type.canSummon() && !type.builtInRegistryHolder().is(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild
                || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(pos) instanceof ConfinedSpawnerBlockEntity confined)) {
            return fail(level, player, "nautec.mob_sample.confined_only");
        }
        EntityType<?> type = context.getItemInHand().get(NTDataComponents.MOB_SAMPLE.get());
        if (type == null || !supports(type)) {
            return fail(level, player, "nautec.mob_sample.unsupported");
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!confined.applySample(type)) {
            return fail(level, player, "nautec.mob_sample.unsupported");
        }
        context.getItemInHand().consume(1, player);
        player.sendOverlayMessage(Component.translatable("nautec.mob_sample.applied", type.getDescription()).withStyle(ChatFormatting.AQUA));
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.8F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fail(Level level, Player player, String key) {
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        EntityType<?> type = stack.get(NTDataComponents.MOB_SAMPLE.get());
        tooltip.accept(type == null
                ? Component.translatable("nautec.mob_sample.empty").withStyle(ChatFormatting.RED)
                : Component.translatable("nautec.mob_sample.mob", type.getDescription()).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("nautec.mob_sample.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
