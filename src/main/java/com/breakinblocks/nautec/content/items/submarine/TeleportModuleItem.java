package com.breakinblocks.nautec.content.items.submarine;



import net.minecraft.world.item.Item;
import java.util.List;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TeleportModuleItem extends SubmarineModuleItem {
    public TeleportModuleItem(Properties properties) {
        super(SubmarineModuleType.TELEPORT, properties);
    }

    public static @Nullable TeleportAnchor anchorOf(ItemStack stack) {
        return stack.get(NTDataComponents.TELEPORT_ANCHOR);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (!player.isInWater()) {
            player.displayClientMessage(Component.translatable("nautec.submarine.module.teleport.needs_water")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            TeleportAnchor anchor = new TeleportAnchor(GlobalPos.of(level.dimension(), player.blockPosition()), player.getYRot());
            stack.set(NTDataComponents.TELEPORT_ANCHOR, anchor);
            player.displayClientMessage(Component.translatable("nautec.submarine.module.teleport.bound",
                    anchor.pos().pos().toShortString()).withStyle(ChatFormatting.AQUA), true);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        TeleportAnchor anchor = anchorOf(stack);
        if (anchor == null) {
            Tooltips.trans(tooltipComponents, "nautec.submarine.module.teleport.unbound", ChatFormatting.GRAY);
            return;
        }

        Tooltips.tt(tooltipComponents, Component.translatable("nautec.submarine.module.teleport.destination",
                anchor.pos().pos().toShortString(), anchor.pos().dimension().location().toString()), ChatFormatting.AQUA);
    }
}
