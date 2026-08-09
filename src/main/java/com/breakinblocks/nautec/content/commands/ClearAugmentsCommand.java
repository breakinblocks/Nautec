package com.breakinblocks.nautec.content.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.network.ClearAugmentPayload;
import com.breakinblocks.nautec.network.SyncAugmentPayload;
import com.breakinblocks.nautec.utils.AugmentHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

// /nautec augments clear
public class ClearAugmentsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> nautecCommand = Commands.literal(Nautec.MODID)
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()));

        dispatcher.register(nautecCommand
                .then(Commands.literal("augments")
                        .then(Commands.literal("clear").executes(ClearAugmentsCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = (ServerPlayer) ctx.getSource().getPlayer();
        if (player == null) return 0;
        
        Map<AugmentSlot, Augment> currentAugments = new HashMap<>(AugmentHelper.getAugments(player));
        
        for (Map.Entry<AugmentSlot, Augment> entry : currentAugments.entrySet()) {
            Augment augment = entry.getValue();
            if (augment != null) {
                augment.onRemoved(player);
            }
        }
        
        player.setData(NTDataAttachments.AUGMENTS, new HashMap<>());
        player.setData(NTDataAttachments.AUGMENTS_EXTRA_DATA, new HashMap<>());
        
        for (AugmentSlot slot : currentAugments.keySet()) {
            PacketDistributor.sendToPlayer(player, new ClearAugmentPayload(slot));
        }
        
        player.sendSystemMessage(Component.literal("Cleared all player augments"));
        return 1;
    }
}
