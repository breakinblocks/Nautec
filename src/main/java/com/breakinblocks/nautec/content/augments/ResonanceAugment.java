package com.breakinblocks.nautec.content.augments;

import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.content.resonance.ResonanceBinding;
import com.breakinblocks.nautec.content.resonance.ResonanceCharmItem;
import com.breakinblocks.nautec.content.resonance.SatelliteGrid;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.utils.AugmentHelper;
import com.breakinblocks.nautec.utils.valueio.TagValueInput;
import com.breakinblocks.nautec.utils.valueio.TagValueOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResonanceAugment extends Augment {
    private @Nullable ResonanceBinding binding;
    private int priority;

    public ResonanceAugment(AugmentSlot augmentSlot) {
        super(NTAugments.RESONANCE.get(), augmentSlot);
    }

    public static @Nullable ResonanceAugment installed(Player player) {
        for (Augment augment : AugmentHelper.getAugments(player).values()) {
            if (augment instanceof ResonanceAugment resonance) {
                return resonance;
            }
        }
        return null;
    }

    public @Nullable ResonanceBinding getBinding() {
        return binding;
    }

    public int getPriority() {
        return priority;
    }

    public void bind(@Nullable ResonanceBinding binding) {
        this.binding = binding;
        AugmentHelper.syncAugment(player, this);
    }

    public void setPriority(int priority) {
        this.priority = priority;
        AugmentHelper.syncAugment(player, this);
    }

    @Override
    public void readParts(List<ItemStack> parts) {
        for (ItemStack part : parts) {
            if (part.getItem() instanceof ResonanceCharmItem) {
                binding = part.get(NTDataComponents.RESONANCE_BINDING.get());
                priority = ResonanceCharmItem.priority(part);
                return;
            }
        }
    }

    @Override
    public void serverTick(PlayerTickEvent.Post event) {
        if (binding == null || !(player instanceof ServerPlayer serverPlayer)
                || serverPlayer.level().getGameTime() % (ResonanceCharmItem.INTERVAL / 2) != 0
                || !ResonanceCharmItem.equipped(serverPlayer).isEmpty()) {
            return;
        }
        SatelliteGrid.charm(serverPlayer, binding.network());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        if (binding != null) {
            TagValueOutput.wrap(provider, tag).store("binding", ResonanceBinding.CODEC, binding);
        }
        tag.putInt("priority", priority);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        binding = TagValueInput.create(provider, tag).read("binding", ResonanceBinding.CODEC).orElse(null);
        priority = tag.getInt("priority");
    }
}
