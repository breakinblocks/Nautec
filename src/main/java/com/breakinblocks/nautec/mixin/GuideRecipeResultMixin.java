package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.content.recipes.display.MachineRecipeDisplay;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Include secondary items and fluid buckets in GuideMe's 1.21 recipe lookup. */
@Pseudo
@Mixin(targets = "guideme.internal.util.Platform", remap = false)
public abstract class GuideRecipeResultMixin {
    @Inject(method = "recipeHasResult", at = @At("HEAD"), cancellable = true)
    private static void nautec$machineResults(RecipeHolder<?> recipe, Item item, CallbackInfoReturnable<Boolean> cir) {
        if (MachineRecipeDisplay.supports(recipe.value()) && MachineRecipeDisplay.of(recipe.value()).hasResult(item)) {
            cir.setReturnValue(true);
        }
    }
}
