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
import net.minecraft.world.entity.player.Player;

public class GetAugmentCooldownCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> nautecCommand = Commands.literal(Nautec.MODID)
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()));

        dispatcher.register(nautecCommand
                .then(Commands.literal("augments")
                        .then(Commands.literal("cooldown")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("slot", AugmentSlotArgumentType.getInstance())
                                                .executes(GetAugmentCooldownCommand::execute))))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        Player player = ctx.getSource().getPlayer();
        AugmentSlot augmentSlot = ctx.getArgument("slot", AugmentSlot.class);
        Augment augmentBySlot = AugmentHelper.getAugmentBySlot(player, augmentSlot);
        int cooldown = 0;
        if (augmentBySlot != null) {
            cooldown = augmentBySlot.getCooldown();
            player.sendSystemMessage(Component.literal("Augment cooldown for slot '" + augmentSlot.getName() + "': " + cooldown));
        }
        return 1;
    }
}
