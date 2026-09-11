package com.breakinblocks.nautec.events.helper;

import com.breakinblocks.nautec.api.items.IPowerItem;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.registries.NTFluids;
import java.util.Optional;
import com.breakinblocks.nautec.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;


public class ItemInfusion {

    private int infusionProgress;
    private final BlockPos originalFluidPos;

    public ItemInfusion(BlockPos originalFluidPos) {
        this.infusionProgress = 0;
        this.originalFluidPos = originalFluidPos;
    }

    public int getInfusionProgress() {
        return infusionProgress;
    }

    public void incrementInfusionProgress() {
        this.infusionProgress++;
    }

    public BlockPos getOriginalFluidPos() {
        return originalFluidPos;
    }

    private static final int MAX_INFUSION_TIME = 150;
    private static final int PARTICLE_INTERVAL = 5;
    private static final int SOUND_INTERVAL = 50;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    public static void cancel(ItemEntity itemEntity) {
        itemEntity.removeData(NTDataAttachments.ITEM_INFUSION);
    }

    public static void processPowerItemInfusion(ItemEntity itemEntity, Level level) {
        ItemStack stack = itemEntity.getItem();
        if (!(stack.getItem() instanceof IPowerItem) || NTDataComponentsUtils.isInfused(stack)) {
            cancel(itemEntity);
            return;
        }

        Optional<ItemInfusion> active = itemEntity.getData(NTDataAttachments.ITEM_INFUSION);
        if (active.isEmpty()) {
            BlockPos originalFluidPos = itemEntity.blockPosition();
            if (!level.getFluidState(originalFluidPos).is(NTFluids.EAS.getStillFluid())) {
                originalFluidPos = originalFluidPos.below();
            }
            itemEntity.setData(NTDataAttachments.ITEM_INFUSION, Optional.of(new ItemInfusion(originalFluidPos)));
        } else {
            ItemInfusion infusionData = active.get();

            if (infusionData.getInfusionProgress() >= MAX_INFUSION_TIME) {
                NTDataComponentsUtils.setInfusedStatus(stack, true);

                spawnCompletionEffects(itemEntity, level);

                cancel(itemEntity);

                BlockPos originalFluidPos = infusionData.getOriginalFluidPos();
                if (!level.isClientSide() && level.getFluidState(originalFluidPos).is(NTFluids.EAS.getStillFluid())
                        && level.getFluidState(originalFluidPos).isSource()) {
                    level.setBlock(originalFluidPos, Blocks.AIR.defaultBlockState(), 11);
                }
            } else {
                infusionData.incrementInfusionProgress();

                if (infusionData.getInfusionProgress() % PARTICLE_INTERVAL == 0 && level.isClientSide()) {
                    ParticleUtils.spawnParticlesAroundItem(itemEntity, level, ParticleTypes.ENCHANT);
                }

                if (infusionData.getInfusionProgress() % SOUND_INTERVAL == 0) {
                    level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
                }
            }
        }
    }

    private static void spawnCompletionEffects(ItemEntity itemEntity, Level level) {
        if (level.isClientSide()) {
            ParticleUtils.spawnParticlesAroundItem(itemEntity, level, PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
        }

        level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }
}
