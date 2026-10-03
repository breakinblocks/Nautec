package com.breakinblocks.nautec.content.resonance;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.api.items.ICurioItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ResonanceCharmItem extends Item implements ICurioItem {
    public static final int INTERVAL = 10;

    public ResonanceCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(NTDataComponents.RESONANCE_BINDING.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof ResonancePylonBlockEntity pylon)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        ResonanceNetwork network = pylon.getNetwork();
        if (network == null) {
            player.sendOverlayMessage(Component.translatable("nautec.resonance_charm.no_network").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!ResonanceNetworks.canUse(player, network)) {
            player.sendOverlayMessage(Component.translatable("nautec.resonance.error.no_access").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        context.getItemInHand().set(NTDataComponents.RESONANCE_BINDING.get(), new ResonanceBinding(network.id(), network.name()));
        player.sendOverlayMessage(Component.translatable("nautec.resonance_charm.bound", network.name()).withStyle(ChatFormatting.AQUA));
        level.playSound(null, context.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack charm = player.getItemInHand(hand);
        if (player.isSecondaryUseActive() && charm.has(NTDataComponents.RESONANCE_BINDING.get())) {
            if (!level.isClientSide()) {
                charm.remove(NTDataComponents.RESONANCE_BINDING.get());
                player.sendOverlayMessage(Component.translatable("nautec.resonance_charm.unbound"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void curioTick(ItemStack stack, SlotContext slotContext) {
        if (slotContext.entity() instanceof ServerPlayer player && player.tickCount % INTERVAL == 0) {
            charge(player, stack);
        }
    }

    public static int charge(ServerPlayer player, ItemStack charm) {
        ResonanceBinding binding = charm.get(NTDataComponents.RESONANCE_BINDING.get());
        if (binding == null) {
            return 0;
        }
        ServerLevel level = player.level();
        ResonanceNetwork network = ResonanceNetworks.get(level.getServer()).get(binding.network());
        if (network == null || !ResonanceNetworks.canUse(player, network)) {
            return 0;
        }
        List<Source> sources = sources(player, network);
        if (sources.isEmpty()) {
            return 0;
        }

        int budget = NTConfig.charmTransferRate * INTERVAL;
        int delivered = 0;
        for (ItemStack target : targets(player, charm)) {
            if (budget - delivered <= 0) {
                break;
            }
            delivered += fill(target, sources, budget - delivered);
        }
        if (delivered > 0) {
            player.getInventory().setChanged();
            level.sendParticles(NTParticles.CRYSTAL_MOTE.get(), player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.35, 0.5, 0.35, 0.0);
        }
        return delivered;
    }

    private record Source(ResonancePylonBlockEntity pylon, double factor) {
    }

    private static List<Source> sources(ServerPlayer player, ResonanceNetwork network) {
        List<Source> sources = new ArrayList<>();
        for (ResonancePylonBlockEntity pylon : ResonanceGrid.members(network.id())) {
            if (!pylon.sending() || pylon.isRemoved()) {
                continue;
            }
            double factor;
            if (pylon.dimension().equals(player.level().dimension())) {
                factor = 1.0 - NTConfig.resonanceSameDimensionLoss;
            } else if (pylon.interdimensional()) {
                factor = 1.0 - NTConfig.resonanceCrossDimensionLoss;
            } else {
                continue;
            }
            sources.add(new Source(pylon, factor));
        }
        sources.sort(Comparator.comparingDouble(Source::factor).reversed());
        return sources;
    }

    private static List<ItemStack> targets(ServerPlayer player, ItemStack charm) {
        List<ItemStack> targets = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack != charm) {
                targets.add(stack);
            }
        }
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(handler -> {
            for (int slot = 0; slot < handler.getEquippedCurios().getSlots(); slot++) {
                ItemStack stack = handler.getEquippedCurios().getStackInSlot(slot);
                if (!stack.isEmpty() && stack != charm) {
                    targets.add(stack);
                }
            }
        });
        return targets;
    }

    private static int fill(ItemStack target, List<Source> sources, int limit) {
        EnergyHandler energy = target.getCount() == 1 ? ItemAccess.forStack(target).getCapability(Capabilities.Energy.ITEM) : null;
        if (energy != null) {
            int demand;
            try (Transaction simulation = Transaction.openRoot()) {
                demand = energy.insert(limit, simulation);
            }
            int drawn = draw(sources, demand);
            if (drawn > 0) {
                try (Transaction tx = Transaction.openRoot()) {
                    energy.insert(drawn, tx);
                    tx.commit();
                }
            }
            return drawn;
        }
        IPowerStorage power = power(target);
        if (power != null) {
            int demand = power.tryFillPower(limit, true);
            int drawn = draw(sources, demand);
            if (drawn > 0) {
                power.tryFillPower(drawn, false);
            }
            return drawn;
        }
        return 0;
    }

    private static @Nullable IPowerStorage power(ItemStack stack) {
        return stack.getCount() == 1 ? stack.getCapability(NTCapabilities.PowerStorage.ITEM) : null;
    }

    private static int draw(List<Source> sources, int demand) {
        int delivered = 0;
        for (Source source : sources) {
            int remaining = demand - delivered;
            if (remaining <= 0) {
                break;
            }
            int needed = (int) Math.min(Integer.MAX_VALUE, (long) Math.ceil(remaining / source.factor()));
            int taken = Math.min(needed, source.pylon().sendable());
            if (taken <= 0) {
                continue;
            }
            int arriving = Math.min(remaining, (int) Math.floor(taken * source.factor()));
            source.pylon().send(taken);
            delivered += arriving;
        }
        return delivered;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ResonanceBinding binding = stack.get(NTDataComponents.RESONANCE_BINDING.get());
        if (binding != null) {
            tooltip.accept(Component.translatable("nautec.resonance_charm.tooltip.bound", binding.name()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.accept(Component.translatable("nautec.resonance_charm.tooltip.unbound").withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable("nautec.resonance_charm.tooltip.monocle").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("nautec.resonance_charm.tooltip.usage").withStyle(ChatFormatting.DARK_GRAY));
    }
}
