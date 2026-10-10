package com.breakinblocks.nautec.api.client.renderer.augments;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.PlayerModel;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ModelPartGetter {
    @Nullable
    ModelPart getModelPart(PlayerModel<?> renderer);
}
