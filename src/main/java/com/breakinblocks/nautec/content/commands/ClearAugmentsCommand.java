package com.breakinblocks.nautec.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ClearAugmentsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> nautecCommand = Commands.literal(Nautec.MODID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS));

        dispatcher.register(nautecCommand
                .then(Commands.literal("augments")
                        .then(Commands.literal("clear").executes(ClearAugmentsCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        List<AugmentSlot> slots = new ArrayList<>(AugmentHelper.getAugments(player).keySet());
        for (AugmentSlot slot : slots) {
            AugmentHelper.removeAugment(player, slot);
        }

        player.sendSystemMessage(Component.literal("Cleared all player augments"));
        return 1;
    }
}
