package com.breakinblocks.nautec.content.items;


import java.util.List;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.sides.SideConfig;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.breakinblocks.nautec.utils.valueio.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class ConfigurationCardItem extends Item {
    public static final String SIDE_CONFIG = "side_config";
    public static final String SETTINGS = "settings";

    public ConfigurationCardItem(Properties properties) {
        super(properties);
    }

    public static @Nullable MachineSettings copy(ContainerBlockEntity machine, ServerLevel level) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, level.registryAccess());
            if (machine.hasSideConfig()) {
                machine.getSideConfig().save(out.child(SIDE_CONFIG));
            }
            machine.saveSettings(out.child(SETTINGS));
            CompoundTag data = out.buildResult();
            if (data.getCompound(SETTINGS).isEmpty()) {
                data.remove(SETTINGS);
            }
            if (data.isEmpty()) {
                return null;
            }
            return new MachineSettings(BuiltInRegistries.BLOCK.getKey(machine.getBlockState().getBlock()),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType()), data);
        }
    }

    public static boolean paste(MachineSettings settings, ContainerBlockEntity machine, ServerPlayer player) {
        boolean applied = false;
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            ValueInput in = TagValueInput.create(reporter, player.level().registryAccess(), settings.data());
            Optional<ValueInput> sides = in.child(SIDE_CONFIG);
            if (sides.isPresent() && machine.hasSideConfig()) {
                SideConfig config = new SideConfig();
                config.load(sides.get());
                machine.getSideConfig().copyFrom(config);
                machine.sideConfigChanged();
                applied = true;
            }
            Optional<ValueInput> own = in.child(SETTINGS);
            ResourceLocation type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(machine.getType());
            if (own.isPresent() && settings.blockEntityType().equals(type) && machine.loadSettings(own.get(), player)) {
                applied = true;
            }
        }
        return applied;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(pos) instanceof ContainerBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack card = context.getItemInHand();
        Component name = machine.getBlockState().getBlock().getName();
        if (player.isSecondaryUseActive()) {
            MachineSettings settings = copy(machine, serverLevel);
            if (settings == null) {
                player.displayClientMessage(Component.translatable("nautec.configuration_card.nothing_to_copy", name).withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            card.set(NTDataComponents.MACHINE_SETTINGS.get(), settings);
            player.displayClientMessage(Component.translatable("nautec.configuration_card.copied", name).withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 1.2F);
            return InteractionResult.SUCCESS;
        }
        MachineSettings settings = card.get(NTDataComponents.MACHINE_SETTINGS.get());
        if (settings == null) {
            player.displayClientMessage(Component.translatable("nautec.configuration_card.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!level.mayInteract(player, pos)) {
            return InteractionResult.FAIL;
        }
        if (!paste(settings, machine, serverPlayer)) {
            player.displayClientMessage(Component.translatable("nautec.configuration_card.nothing_fits", name).withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        player.displayClientMessage(Component.translatable("nautec.configuration_card.pasted", name).withStyle(ChatFormatting.AQUA), true);
        level.playSound(null, pos, SoundEvents.UI_LOOM_TAKE_RESULT, SoundSource.PLAYERS, 0.8F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if (!player.isSecondaryUseActive() || !card.has(NTDataComponents.MACHINE_SETTINGS.get())) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        card.remove(NTDataComponents.MACHINE_SETTINGS.get());
        if (!level.isClientSide()) {
            player.displayClientMessage(Component.translatable("nautec.configuration_card.cleared").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(NTDataComponents.MACHINE_SETTINGS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        MachineSettings settings = stack.get(NTDataComponents.MACHINE_SETTINGS.get());
        if (settings == null) {
            tooltip.add(Component.translatable("nautec.configuration_card.tooltip.empty").withStyle(ChatFormatting.GRAY));
        } else {
            Component source = BuiltInRegistries.BLOCK.get(settings.block()).getName();
            tooltip.add(Component.translatable("nautec.configuration_card.tooltip.source", source).withStyle(ChatFormatting.AQUA));
            if (settings.data().contains(SIDE_CONFIG)) {
                tooltip.add(Component.translatable("nautec.configuration_card.tooltip.sides").withStyle(ChatFormatting.GRAY));
            }
            if (settings.data().contains(SETTINGS)) {
                tooltip.add(Component.translatable("nautec.configuration_card.tooltip.settings", source).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("nautec.configuration_card.tooltip.clear").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("nautec.configuration_card.tooltip.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
