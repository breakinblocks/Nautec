package com.breakinblocks.nautec.registries;

import com.mojang.brigadier.CommandDispatcher;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.commands.BacteriaGenerateCommand;
import com.breakinblocks.nautec.content.commands.ClearAugmentsCommand;
import com.breakinblocks.nautec.content.commands.GetAugmentCommand;
import com.breakinblocks.nautec.content.commands.GetAugmentCooldownCommand;
import com.breakinblocks.nautec.content.commands.RemoveAugmentCommand;
import com.breakinblocks.nautec.content.commands.SetAugmentCommand;
import com.breakinblocks.nautec.content.commands.SetAugmentCooldownCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

@EventBusSubscriber(modid = Nautec.MODID)
public final class NTCommands {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        GetAugmentCommand.register(dispatcher);
        SetAugmentCommand.register(dispatcher);
        ClearAugmentsCommand.register(dispatcher);
        RemoveAugmentCommand.register(dispatcher);
        GetAugmentCooldownCommand.register(dispatcher);
        SetAugmentCooldownCommand.register(dispatcher);
        BacteriaGenerateCommand.register(dispatcher, event.getBuildContext());
        ConfigCommand.register(dispatcher);
    }
}
