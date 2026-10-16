package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class EmiRecipeAreas {
    private EmiRecipeAreas() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EmiRecipeAreas::onMouseClicked);
        NeoForge.EVENT_BUS.addListener(EmiRecipeAreas::onRender);
    }

    private static @Nullable RecipeAreaHost.RecipeArea hovered(Screen screen, double mouseX, double mouseY) {
        if (!(screen instanceof AbstractContainerScreen<?> container) || !(screen instanceof RecipeAreaHost host)) {
            return null;
        }
        double x = mouseX - container.getGuiLeft();
        double y = mouseY - container.getGuiTop();
        for (RecipeAreaHost.RecipeArea area : host.recipeAreas()) {
            if (x >= area.x() && x < area.x() + area.width() && y >= area.y() && y < area.y() + area.height()) {
                return area;
            }
        }
        return null;
    }

    private static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0 || hovered(event.getScreen(), event.getMouseX(), event.getMouseY()) == null) {
            return;
        }
        ItemStack catalyst = ((RecipeAreaHost) event.getScreen()).recipeCatalyst();
        if (catalyst.isEmpty()) {
            return;
        }
        show(EmiStack.of(catalyst));
        event.setCanceled(true);
    }

    private static void show(EmiStack catalyst) {
        EmiRecipeManager manager = EmiApi.getRecipeManager();
        List<EmiRecipeCategory> categories = new ArrayList<>();
        for (EmiRecipeCategory category : manager.getCategories()) {
            for (EmiIngredient workstation : manager.getWorkstations(category)) {
                if (contains(workstation, catalyst)) {
                    categories.add(category);
                    break;
                }
            }
        }
        if (categories.size() == 1) {
            EmiApi.displayRecipeCategory(categories.get(0));
        } else {
            EmiApi.displayUses(catalyst);
        }
    }

    private static boolean contains(EmiIngredient workstation, EmiStack catalyst) {
        for (EmiStack stack : workstation.getEmiStacks()) {
            if (stack.isEqual(catalyst)) {
                return true;
            }
        }
        return false;
    }

    private static void onRender(ScreenEvent.Render.Post event) {
        RecipeAreaHost.RecipeArea area = hovered(event.getScreen(), event.getMouseX(), event.getMouseY());
        if (area == null || area.ownTooltip()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        event.getGuiGraphics().renderComponentTooltip(minecraft.font,
                List.of(Component.translatable("nautec.jei.show_recipes").withStyle(ChatFormatting.DARK_GRAY)), event.getMouseX(), event.getMouseY());
    }
}
