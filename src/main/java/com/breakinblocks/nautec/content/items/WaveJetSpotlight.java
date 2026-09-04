package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Nautec.MODID)
public final class WaveJetSpotlight {
    private record Lit(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private static final Map<UUID, Lit> LIT = new HashMap<>();
    private static final double BACK_OFF_STEP = 0.5D;
    private static final double BACK_OFF_LIMIT = 3.0D;

    public static boolean isLit(ItemStack stack) {
        return stack.is(NTItems.WAVE_JET.get()) && NTDataComponentsUtils.isAbilityEnabled(stack);
    }

    public static @Nullable ItemStack heldWaveJet(LivingEntity holder) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = holder.getItemInHand(hand);
            if (stack.is(NTItems.WAVE_JET.get())) {
                return stack;
            }
        }
        return null;
    }

    public static @Nullable ItemStack litWaveJet(LivingEntity holder) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = holder.getItemInHand(hand);
            if (isLit(stack)) {
                return stack;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack stack = litWaveJet(player);
        if (stack == null) {
            extinguish(player);
            return;
        }

        if (!WaveJetItem.spendLightPower(stack, player)) {
            NTDataComponentsUtils.setAbilityStatus(stack, false);
            extinguish(player);
            return;
        }

        aim(level, player);
    }

    public static void aim(ServerLevel level, LivingEntity holder) {
        if (NTConfig.waveJetLightLevel <= 0) {
            extinguish(holder);
            return;
        }

        BlockPos target = findSpot(level, holder);
        Lit current = LIT.get(holder.getUUID());
        if (current != null && current.dimension() == level.dimension() && current.pos().equals(target)) {
            return;
        }

        extinguish(holder);
        if (target == null) {
            return;
        }

        place(level, target);
        LIT.put(holder.getUUID(), new Lit(level.dimension(), target));
    }

    private static @Nullable BlockPos findSpot(ServerLevel level, LivingEntity holder) {
        Vec3 eyes = holder.getEyePosition();
        Vec3 end = eyes.add(holder.getLookAngle().scale(NTConfig.waveJetLightRange));
        BlockHitResult hit = level.clip(new ClipContext(eyes, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, holder));

        BlockPos candidate = hit.getType() == HitResult.Type.BLOCK
                ? hit.getBlockPos().relative(hit.getDirection())
                : BlockPos.containing(end);
        if (canHold(level, candidate)) {
            return candidate;
        }

        Vec3 impact = hit.getType() == HitResult.Type.BLOCK ? hit.getLocation() : end;
        Vec3 look = holder.getLookAngle();
        for (double back = BACK_OFF_STEP; back <= BACK_OFF_LIMIT; back += BACK_OFF_STEP) {
            BlockPos fallback = BlockPos.containing(impact.subtract(look.scale(back)));
            if (canHold(level, fallback)) {
                return fallback;
            }
        }
        return null;
    }

    private static boolean canHold(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.LIGHT)
                || (state.is(Blocks.WATER) && state.getFluidState().isSource());
    }

    private static void place(ServerLevel level, BlockPos pos) {
        boolean waterlogged = level.getBlockState(pos).is(Blocks.WATER);
        level.setBlock(pos, Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, NTConfig.waveJetLightLevel)
                .setValue(LightBlock.WATERLOGGED, waterlogged), Block.UPDATE_CLIENTS);
    }

    public static void extinguish(LivingEntity holder) {
        Lit lit = LIT.remove(holder.getUUID());
        MinecraftServer server = holder.level().getServer();
        if (lit == null || server == null) {
            return;
        }
        clear(server.getLevel(lit.dimension()), lit.pos());
    }

    private static void clear(@Nullable ServerLevel level, BlockPos pos) {
        if (level == null || !level.isLoaded(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.LIGHT)) {
            return;
        }
        level.setBlock(pos, state.getValue(LightBlock.WATERLOGGED)
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        extinguish(event.getEntity());
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        extinguish(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        extinguish(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        for (Map.Entry<UUID, Lit> entry : LIT.entrySet()) {
            Lit lit = entry.getValue();
            clear(event.getServer().getLevel(lit.dimension()), lit.pos());
        }
        LIT.clear();
    }

    private WaveJetSpotlight() {
    }
}
