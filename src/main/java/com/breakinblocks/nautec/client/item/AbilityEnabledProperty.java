package com.breakinblocks.nautec.client.item;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AbilityEnabledProperty() implements ClampedItemPropertyFunction {
    public static final ResourceLocation ID = Nautec.rl("ability_enabled");

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return NTDataComponentsUtils.isAbilityEnabledNBT(stack) > 0 ? 1.0F : 0.0F;
    }
}
