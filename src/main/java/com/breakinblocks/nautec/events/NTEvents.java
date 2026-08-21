package com.breakinblocks.nautec.events;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.NTRegistries;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.compat.modonomicon.ModonomiconCompat;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.semi.PrismarineCrystalPartBlockEntity;
import com.breakinblocks.nautec.content.blocks.multiblock.semi.PrismarineCrystalBlock;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.events.helper.ItemEtching;
import com.breakinblocks.nautec.events.helper.ItemInfusion;
import com.breakinblocks.nautec.network.SyncAugmentPayload;
import com.breakinblocks.nautec.registries.NTAttachmentTypes;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.ParticleUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import com.breakinblocks.nautec.utils.ItemUtils;

public final class NTEvents {
    @EventBusSubscriber(modid = Nautec.MODID)
    public static class Game {
        @SubscribeEvent
        public static void onItemEntityTick(EntityTickEvent.Post event) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                Level level = itemEntity.level();

                if (level.getFluidState(itemEntity.blockPosition()).getFluidType() == NTFluids.ETCHING_ACID.getFluidType().get()) {
                    ItemEtching.processItemEtching(itemEntity, level);
                }

                if (level.getFluidState(itemEntity.blockPosition()).getFluidType() == NTFluids.EAS.getFluidType().get() || level.getBlockState(itemEntity.blockPosition().below()).getFluidState().is(NTFluids.EAS.getStillFluid())) {
                    ItemInfusion.processPowerItemInfusion(itemEntity, level);
                }
            }
        }

        @SubscribeEvent
        public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
            if (event.getEntity() instanceof ItemEntity itemEntity) {
                ItemEtching.onEntityLeave(itemEntity);
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Player player = event.getEntity();
            Map<AugmentSlot, Augment> augments = player.getData(NTDataAttachments.AUGMENTS);
            Map<AugmentSlot, CompoundTag> augmentsExtraData = player.getData(NTDataAttachments.AUGMENTS_EXTRA_DATA);
            for (AugmentSlot augmentSlot : augments.keySet()) {
                Augment augment = augments.get(augmentSlot);
                augment.setPlayer(player);
                CompoundTag nbt = augmentsExtraData.get(augmentSlot);
                if (nbt != null) {
                    augment.deserializeNBT(player.level().registryAccess(), nbt);
                }
                PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncAugmentPayload(augment, nbt != null ? nbt : new CompoundTag()));
            }

            if (ModList.get().isLoaded("modonomicon")) {
                if (!player.getData(NTAttachmentTypes.HAS_NAUTEC_GUIDE.get()) && NTConfig.spawnBookInInventory) {
                    ItemUtils.giveItemToPlayer(player, ModonomiconCompat.getItemStack());
                    player.setData(NTAttachmentTypes.HAS_NAUTEC_GUIDE.get(), true);
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();

            Level level = player.level();
            BlockPos pos = event.getPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);

            ItemStack mainHandItem = player.getMainHandItem();
            if (!player.hasInfiniteMaterials() && mainHandItem.is(NTItems.AQUARINE_PICKAXE) && mainHandItem.get(NTDataComponents.ABILITY_ENABLED)) {
                PrismarineCrystalBlockEntity be = null;
                if (blockEntity instanceof PrismarineCrystalPartBlockEntity partBlockEntity) {
                    be = (PrismarineCrystalBlockEntity) level.getBlockEntity(partBlockEntity.getCrystalPos());
                } else if (blockEntity instanceof PrismarineCrystalBlockEntity blockEntity1) {
                    be = blockEntity1;
                }

                if (be != null && !be.isBreaking()) {
                    be.playBreakAnimation();
                    ItemUtils.giveItemToPlayer(player, NTItems.PRISMARINE_CRYSTAL_SHARD.toStack(level.getRandom().nextInt(1, 3)));
                    if (level.getRandom().nextInt(0, 4) == 0) {
                        PrismarineCrystalBlock.removeCrystal(level, player, be.getBlockPos());
                        if (level.isClientSide()) {
                            ParticleUtils.spawnBreakParticle(be.getBlockPos(), be.getBlockState().getBlock(), 50, level);
                        }
                        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 4, 0.75f);
                    } else {
                        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 0.5f);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            Player player = event.getEntity();
            int changedIndex = player.getData(NTDataAttachments.AUGMENT_DATA_CHANGED);
            if (changedIndex != -1) {
                Map<AugmentSlot, Augment> augments = AugmentHelper.getAugments(player);
                Map<AugmentSlot, CompoundTag> augmentsExtraData = AugmentHelper.getAugmentsData(player);
                AugmentSlot changedSlot = NTRegistries.AUGMENT_SLOT.byId(changedIndex);
                CompoundTag tag = augments.get(changedSlot).serializeNBT(player.level().registryAccess());
                AugmentHelper.setAugmentExtraData(player, changedSlot, tag);
                player.setData(NTDataAttachments.AUGMENT_DATA_CHANGED, -1);
            }
        }

        @SubscribeEvent
        public static void onBreakBlock(PlayerEvent.BreakSpeed event) {
            Player player = event.getEntity();
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
