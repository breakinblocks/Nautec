package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.bacteria.IBacteriaStorage;
import com.breakinblocks.nautec.data.NTDataMaps;
import com.breakinblocks.nautec.data.maps.BacteriaObtainValue;
import com.breakinblocks.nautec.registries.NTCriteriaTriggers;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class GraftingToolItem extends Item {
    public GraftingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getHand() == InteractionHand.OFF_HAND) return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockState = level.getBlockState(pos);
        BacteriaObtainValue data = blockState.typeHolder().getData(NTDataMaps.BACTERIA_OBTAINING);
        Player player = context.getPlayer();
        if (player != null && data != null && level.getBiome(pos).is(data.biome())) {
            ItemStack offhandItem = player.getOffhandItem();
            if (offhandItem.is(NTItems.PETRI_DISH.get())) {
                IBacteriaStorage bacteriaStorage = offhandItem.getCapability(NTCapabilities.BacteriaStorage.ITEM);
                if (bacteriaStorage == null) {
                    return super.useOn(context);
                }
                if (!canGraftInto(bacteriaStorage)) {
                    if (!level.isClientSide()) {
                        player.sendOverlayMessage(Component.translatable("nautec.grafting_tool.dish_occupied").withStyle(ChatFormatting.RED));
                    }
                    return InteractionResult.FAIL;
                }
                if (level.getRandom().nextFloat() <= data.chance()) {
                    graftInto(bacteriaStorage, BacteriaInstance.roll(data.bacteria(), level.registryAccess()));
                    if (player instanceof ServerPlayer serverPlayer) {
                        NTCriteriaTriggers.BACTERIA_GRAFTED.get().trigger(serverPlayer);
                    }
                }
                ItemStack itemInHand = context.getItemInHand();
                itemInHand.hurtAndBreak(1, player, player.getEquipmentSlotForItem(itemInHand));
                level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.PLAYERS);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    public static boolean canGraftInto(IBacteriaStorage dish) {
        return dish.getBacteria(0).isEmpty();
    }

    public static boolean graftInto(IBacteriaStorage dish, BacteriaInstance colony) {
        if (!canGraftInto(dish)) {
            return false;
        }
        dish.setBacteria(0, colony);
        return true;
    }
}
