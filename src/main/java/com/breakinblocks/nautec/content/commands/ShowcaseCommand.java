package com.breakinblocks.nautec.content.commands;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.showcase.ShowcaseBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class ShowcaseCommand {
    private ShowcaseCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(Nautec.MODID)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("showcase")
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ShowcaseCommand::showcase)));
    }

    private static int showcase(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        BlockPos feet = BlockPos.containing(source.getPosition());
        Direction facing = Direction.fromYRot(source.getRotation().y);

        BoundingBox bounds = ShowcaseBuilder.bounds(feet, facing);
        if (level.isOutsideBuildHeight(bounds.minY()) || level.isOutsideBuildHeight(bounds.maxY())) {
            source.sendFailure(Component.translatableWithFallback("nautec.showcase.height",
                    "There is not enough build height here for the showcase"));
            return 0;
        }

        try {
            ShowcaseBuilder.Result result = ShowcaseBuilder.build(level, feet, facing);
            source.sendSuccess(() -> Component.translatableWithFallback("nautec.showcase.done",
                    "Built the NauTec showcase: %s blocks and %s items over %s by %s blocks",
                    result.blocks(), result.items(), result.width(), result.depth()), true);
            return 1;
        } catch (Exception e) {
            Nautec.LOGGER.error("Could not build the NauTec showcase", e);
            source.sendFailure(Component.translatableWithFallback("nautec.showcase.failed",
                    "Could not build the showcase: %s", String.valueOf(e.getMessage())));
            return 0;
        }
    }
}
