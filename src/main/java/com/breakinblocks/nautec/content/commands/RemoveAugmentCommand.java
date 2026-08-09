package com.breakinblocks.nautec.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.content.commands.arguments.AugmentSlotArgumentType;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

// /nautec augments remove <slot>
public class RemoveAugmentCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> nautecCommand = Commands.literal(Nautec.MODID)
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()));

        dispatcher.register(nautecCommand
                .then(Commands.literal("augments")
                        .then(Commands.literal("remove")
                                .then(Commands.argument("slot", AugmentSlotArgumentType.getInstance())
                                        .executes(RemoveAugmentCommand::execute)))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        AugmentSlot slot = ctx.getArgument("slot", AugmentSlot.class);
        Augment currentAugment = AugmentHelper.getAugmentBySlot(player, slot);
        
        if (currentAugment == null) {
            player.sendSystemMessage(Component.literal("No augment found in slot: " + slot.getName()));
            return 0;
        }
        
        AugmentHelper.removeAugment(player, slot);
        
        player.sendSystemMessage(Component.literal("Removed augment from slot: " + slot.getName()));
        return 1;
    }
}