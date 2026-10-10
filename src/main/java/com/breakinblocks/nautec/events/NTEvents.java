package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.content.items.DivingSuitArmorItem;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.GatewayBlock;
import com.breakinblocks.nautec.content.blocks.GatewayRingPartBlock;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.events.helper.ItemEtching;
import com.breakinblocks.nautec.events.helper.ItemInfusion;
import com.breakinblocks.nautec.registries.NTAttachmentTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;

public final class NTEvents {
    @EventBusSubscriber(modid = Nautec.MODID)
    public static class Game {
        @SubscribeEvent
        public static void onItemEntityTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof ItemEntity itemEntity) || itemEntity.level().isClientSide()) {
                return;
            }
            Level level = itemEntity.level();
            BlockPos pos = itemEntity.blockPosition();
            FluidState fluid = level.getFluidState(pos);
            FluidType fluidType = fluid.getFluidType();

            if (fluidType == NTFluids.ETCHING_ACID.getFluidType().get()) {
                ItemEtching.processItemEtching(itemEntity, level);
            }

            if (itemEntity.getItem().getItem() instanceof IPowerItem
                    && (fluidType == NTFluids.EAS.getFluidType().get() || level.getFluidState(pos.below()).is(NTFluids.EAS.getStillFluid()))) {
                ItemInfusion.processPowerItemInfusion(itemEntity, level);
            } else {
                ItemInfusion.cancel(itemEntity);
            }
        }

        @SubscribeEvent
        public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                ItemEtching.onEntityLeave(itemEntity);
                ItemInfusion.cancel(itemEntity);
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            AugmentHelper.restoreAugments(player);

            if (ModList.get().isLoaded("guideme")) {
                if (!player.getData(NTAttachmentTypes.HAS_NAUTEC_GUIDE.get()) && NTConfig.spawnBookInInventory) {
                    ItemUtils.giveItemToPlayer(player, NTItems.NAUTEC_GUIDE.toStack());
                    player.setData(NTAttachmentTypes.HAS_NAUTEC_GUIDE.get(), true);
                }
            }
        }

        @SubscribeEvent
        public static void onHarvestCrystal(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
                return;
            }
            Player player = event.getEntity();
            ItemStack mainHandItem = player.getMainHandItem();
            if (player.hasInfiniteMaterials() || !mainHandItem.is(NTItems.AQUARINE_PICKAXE) || !Boolean.TRUE.equals(mainHandItem.get(NTDataComponents.ABILITY_ENABLED))) {
                return;
            }

            Level level = player.level();
            BlockPos pos = event.getPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            PrismarineCrystalBlockEntity be = null;
            if (blockEntity instanceof PrismarineCrystalPartBlockEntity partBlockEntity) {
                if (level.getBlockEntity(partBlockEntity.getCrystalPos()) instanceof PrismarineCrystalBlockEntity crystal) {
                    be = crystal;
                }
            } else if (blockEntity instanceof PrismarineCrystalBlockEntity crystal) {
                be = crystal;
            }

            if (be == null || be.isBreaking() || be.isCultivated()) {
                return;
            }
            be.playBreakAnimation();
            if (level.isClientSide()) {
                return;
            }

            ItemUtils.giveItemToPlayer(player, NTItems.PRISMARINE_CRYSTAL_SHARD.toStack(level.getRandom().nextInt(1, 3)));
            if (level.getRandom().nextInt(0, 4) == 0) {
                BlockPos crystalPos = be.getBlockPos();
                BlockState crystalState = be.getBlockState();
                PrismarineCrystalBlock.removeCrystal(level, player, crystalPos);
                level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, crystalPos.above(), Block.getId(crystalState));
                level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 4, 0.75f);
            } else {
                level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.5f);
            }
        }

        @SubscribeEvent
        public static void onWrenchCrystal(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            ItemStack stack = event.getItemStack();
            if (!player.isSecondaryUseActive() || !stack.is(Tags.Items.TOOLS_WRENCH)) {
                return;
            }
            Level level = event.getLevel();
            PrismarineCrystalBlockEntity crystal = PrismarineCrystalBlock.findCrystal(level, event.getPos());
            if (crystal == null) {
                return;
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            if (level.isClientSide()) {
                return;
            }
            if (!crystal.isCultivated()) {
                player.sendOverlayMessage(Component.translatable("nautec.cultivated_crystal.natural").withStyle(ChatFormatting.GOLD));
                return;
            }
            if (!level.mayInteract(player, crystal.getBlockPos())) {
                return;
            }
            ItemUtils.giveItemToPlayer(player, PrismarineCrystalBlock.pickUp(level, crystal));
        }

        @SubscribeEvent
        public static void onWrenchGateway(PlayerInteractEvent.RightClickBlock event) {
            Player player = event.getEntity();
            if (!player.isSecondaryUseActive() || !event.getItemStack().is(Tags.Items.TOOLS_WRENCH)) {
                return;
            }
            Level level = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            GatewayBlockEntity gateway = state.getBlock() instanceof GatewayRingPartBlock
                    ? GatewayRingPartBlock.core(level, state, pos)
                    : level.getBlockEntity(pos) instanceof GatewayBlockEntity core ? core : null;
            if (gateway == null) {
                return;
            }
            event.setCanceled(true);
            if (!level.mayInteract(player, gateway.getBlockPos())) {
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            event.setCancellationResult(GatewayBlock.useWrench(level, gateway, player));
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            int changedIndex = player.getData(NTDataAttachments.AUGMENT_DATA_CHANGED);
            if (changedIndex != -1) {
                Map<AugmentSlot, Augment> augments = AugmentHelper.getAugments(player);
                Map<AugmentSlot, CompoundTag> augmentsExtraData = AugmentHelper.getAugmentsData(player);
                AugmentSlot changedSlot = NTRegistries.AUGMENT_SLOT.byId(changedIndex);
                Augment changed = augments.get(changedSlot);
                if (changed != null) {
                    CompoundTag tag = changed.serializeNBT(player.level().registryAccess());
                    AugmentHelper.setAugmentExtraData(player, changedSlot, tag);
                }
                player.setData(NTDataAttachments.AUGMENT_DATA_CHANGED, -1);
            }
        }

        @SubscribeEvent
        public static void onBreakBlock(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
            event.setNewSpeed(DivingSuitArmorItem.withoutWaterPenalties(player, event.getNewSpeed()));
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof IPowerItem powerItem) {
                IPowerStorage powerStorage = stack.getCapability(NTCapabilities.PowerStorage.ITEM);
                if (powerStorage.getPowerStored() <= 0) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onHitEntity(AttackEntityEvent event) {
            if (event.getEntity().getMainHandItem().getItem() instanceof IPowerItem powerItem) {
                IPowerStorage powerStorage = event.getEntity().getMainHandItem().getCapability(NTCapabilities.PowerStorage.ITEM);
                if (powerStorage.getPowerStored() <= 0 && event.getTarget() instanceof LivingEntity) {
                    event.setCanceled(true);
                    event.getEntity().sendOverlayMessage(Component.translatable("nautec.tool.no_power"));
                }
            }
        }

        @SubscribeEvent
        public static void onRightClick(PlayerInteractEvent.RightClickItem event) {
            if (event.getItemStack().getItem() instanceof IPowerItem powerItem) {
                ItemStack stack = event.getItemStack();
                if (stack.has(NTDataComponents.ABILITY_ENABLED) && event.getEntity().isShiftKeyDown()) {
                    if (stack.is(NTItems.PRISMATIC_BATTERY)) {
                        NTDataComponentsUtils.setAbilityStatus(stack, !NTDataComponentsUtils.isAbilityEnabled(stack));
                        return;
                    }
                    if (NTDataComponentsUtils.isInfused(stack)) {
                        boolean enabled = NTDataComponentsUtils.isAbilityEnabled(stack);
                        NTDataComponentsUtils.setAbilityStatus(stack, !enabled);
                        event.getEntity().sendOverlayMessage(Component.translatable(enabled ? "nautec.tool.ability_disabled" : "nautec.tool.ability_enabled").withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN));
                        if (event.getLevel().isClientSide()) {
                            Player player = event.getEntity();
                            Level level = event.getLevel();
                            if (enabled) {
                                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.4f, 0.01f);
                            } else {
                                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.4f, 0.09f);
                            }
                        }
                    } else {
                        if (event.getLevel().isClientSide()) {
                            event.getEntity().sendSystemMessage(Component.translatable("nautec.tool.infuse-me").withStyle(ChatFormatting.RED));
                        }
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

}
