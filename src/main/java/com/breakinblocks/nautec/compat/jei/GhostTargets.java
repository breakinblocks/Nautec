package com.breakinblocks.nautec.compat.jei;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;

public final class GhostTargets {
    private GhostTargets() {
    }

    public static <I> IGhostIngredientHandler.Target<I> of(Rect2i area, Runnable action) {
        return new IGhostIngredientHandler.Target<>() {
            @Override
            public Rect2i getArea() {
                return area;
            }

            @Override
            public void accept(I value) {
                action.run();
            }
        };
    }
}
