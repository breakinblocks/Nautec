package com.breakinblocks.nautec.content.items.submarine;

import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.utils.Tooltips;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }

        if (!player.isInWater()) {
            player.sendOverlayMessage(Component.translatable("nautec.submarine.module.teleport.needs_water")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            TeleportAnchor anchor = new TeleportAnchor(GlobalPos.of(level.dimension(), player.blockPosition()), player.getYRot());
            stack.set(NTDataComponents.TELEPORT_ANCHOR, anchor);
            player.sendOverlayMessage(Component.translatable("nautec.submarine.module.teleport.bound",
                    anchor.pos().pos().toShortString()).withStyle(ChatFormatting.AQUA));
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        TeleportAnchor anchor = anchorOf(stack);
        if (anchor == null) {
            Tooltips.trans(tooltipComponents, "nautec.submarine.module.teleport.unbound", ChatFormatting.GRAY);
            return;
        }

        Tooltips.tt(tooltipComponents, Component.translatable("nautec.submarine.module.teleport.destination",
                anchor.pos().pos().toShortString(), anchor.pos().dimension().identifier().toString()), ChatFormatting.AQUA);
    }
}
