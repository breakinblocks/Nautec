package com.breakinblocks.nautec.client.sound;

import com.breakinblocks.nautec.client.renderer.blockentities.ExtradimensionalStorageBERenderer;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlock;
import com.breakinblocks.nautec.content.extradimensional.ExtradimensionalStorageBlockEntity;
import com.breakinblocks.nautec.registries.NTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class ExtradimensionalHumSound extends AbstractTickableSoundInstance {
    private static final float FADE = 0.02F;

    private final ExtradimensionalStorageBlockEntity storage;

    public ExtradimensionalHumSound(ExtradimensionalStorageBlockEntity storage) {
        super(NTSounds.EXTRADIMENSIONAL_STORAGE_HUM.get(), SoundSource.BLOCKS, RandomSource.create());
        this.storage = storage;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
        this.x = storage.getBlockPos().getX() + 0.5;
        this.z = storage.getBlockPos().getZ() + 0.5;
        place();
    }

    private int tier() {
        BlockState state = this.storage.getBlockState();
        return state.hasProperty(ExtradimensionalStorageBlock.TIER) ? state.getValue(ExtradimensionalStorageBlock.TIER) : 1;
    }

    private void place() {
        int tier = tier();
        this.y = this.storage.getBlockPos().getY() + ExtradimensionalStorageBERenderer.height(tier);
        this.pitch = 1.0F - 0.05F * (tier - 1);
    }

    public void end() {
        if (!isStopped()) {
            stop();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.storage.isRemoved() || this.storage.getLevel() != Minecraft.getInstance().level) {
            stop();
            return;
        }
        place();
        float target = Math.min(1F, 0.45F + 0.1F * tier() + 0.1F * this.storage.fill());
        this.volume = Mth.approach(this.volume, target, FADE);
    }
}
