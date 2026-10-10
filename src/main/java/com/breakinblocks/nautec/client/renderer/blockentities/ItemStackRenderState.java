package com.breakinblocks.nautec.client.renderer.blockentities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ItemStackRenderState {
    private ItemStack stack = ItemStack.EMPTY;
    private ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    private @Nullable Level level;
    private int seed;

    public void update(ItemStack stack, ItemDisplayContext displayContext, @Nullable Level level, int seed) {
        this.stack = stack;
        this.displayContext = displayContext;
        this.level = level;
        this.seed = seed;
    }

    public void clear() {
        this.stack = ItemStack.EMPTY;
        this.level = null;
    }

    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    public ItemStack stack() {
        return this.stack;
    }

    public void submit(PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, int outlineColor) {
        if (this.stack.isEmpty()) {
            return;
        }
        Minecraft.getInstance().getItemRenderer().renderStatic(this.stack, this.displayContext, packedLight, packedOverlay, poseStack, buffers, this.level, this.seed);
    }
}
