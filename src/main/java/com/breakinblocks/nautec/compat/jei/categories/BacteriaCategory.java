package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.client.GuiUtils;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BacteriaCategory<T> extends AbstractRecipeCategory<T> {
    private static final Identifier BACTERIA_SLOT_SPRITE = Nautec.rl("container/bacteria_slot");
    private final Map<T, List<BacteriaSlot>> slots;
    private RegistryAccess slotsAccess;

    protected BacteriaCategory(IRecipeType<T> recipeType, Component title, IDrawable icon, int width, int height) {
        super(recipeType, title, icon, width, height);
        this.slots = new HashMap<>();
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        List<BacteriaSlot> slots = this.slots.get(recipe);
        if (slots != null) {
            for (BacteriaSlot slot : slots) {
                NTJeiUtil.blitSprite(guiGraphics, BACTERIA_SLOT_SPRITE, slot.x, slot.y, 18, 18);
                GuiUtils.renderBacteria(guiGraphics, slot.bacteria, slot.x, slot.y);
            }
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<BacteriaSlot> slots = this.slots.get(recipe);
        if (slots == null) return;
        for (BacteriaSlot slot : slots) {
            if (mouseX >= slot.x && mouseX < slot.x + 18 && mouseY >= slot.y && mouseY < slot.y + 18) {
                tooltip.addAll(slot.bacteria.getTooltip());
                return;
            }
        }
    }

    public void addBacteriaSlot(T recipe, int x, int y, ResourceKey<Bacteria> bacteria) {
        RegistryAccess access = Minecraft.getInstance().level.registryAccess();
        if (access != this.slotsAccess) {
            this.slots.clear();
            this.slotsAccess = access;
        }
        List<BacteriaSlot> recipeSlots = this.slots.computeIfAbsent(recipe, key -> new ArrayList<>());
        for (BacteriaSlot slot : recipeSlots) {
            if (slot.x == x && slot.y == y && slot.bacteria.getBacteria().equals(bacteria)) {
                return;
            }
        }
        recipeSlots.removeIf(slot -> slot.x == x && slot.y == y);
        recipeSlots.add(new BacteriaSlot(BacteriaInstance.withMaxStats(bacteria, access), x, y));
    }

    public record BacteriaSlot(BacteriaInstance bacteria, int x, int y) {
    }

}
