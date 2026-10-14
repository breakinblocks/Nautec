package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.client.BacteriaLookup;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncubatorScreen extends NTMachineScreen<IncubatorBlockEntity> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/incubator/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/incubator/progress_arrow_off");
    private static final int NUTRIENT_X = 80;
    private static final int NUTRIENT_Y = 49;
    private static final int ARROW_X = 65;
    private static final int ARROW_Y = 18;
    private static final int ARROW_WIDTH = 46;
    private static final int ARROW_HEIGHT = 29;

    public IncubatorScreen(NTMachineMenu<IncubatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;

        int progress = menu.blockEntity.getProgress();

        int j1 = (int) Math.ceil(((float) progress / Math.max(1, NTConfig.incubatorCraftingSpeed)) * 29f);

        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, i + ARROW_X, j + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT);
        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, 46, 29, 0, 29 - j1, i + ARROW_X, j + 47 - j1, 46, j1);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        IncubatorBlockEntity incubator = this.menu.blockEntity;
        if (this.menu.getCarried().isEmpty() && isHovering(NUTRIENT_X, NUTRIENT_Y, 16, 16, mouseX, mouseY)
                && incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.NUTRIENT_SLOT).isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, nutrientTooltip(incubator), mouseX, mouseY);
        }
        if (isHovering(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, RecipeAreaHost.withRecipeHint(statusTooltip(incubator)), mouseX, mouseY);
        }
    }

    private static List<Component> nutrientTooltip(IncubatorBlockEntity incubator) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.incubator.slot.nutrient").withStyle(ChatFormatting.AQUA));
        BacteriaInstance colony = incubator.getBacteriaStorage().getBacteria(0);
        if (colony.isEmpty()) {
            lines.add(Component.translatable("nautec.incubator.slot.nutrient.no_colony").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        List<ItemStack> eats = BacteriaLookup.incubationItems(colony.getBacteria());
        lines.add(eats.isEmpty()
                ? Component.translatable("nautec.incubator.slot.nutrient.none", BacteriaLookup.strainName(colony.getBacteria())).withStyle(ChatFormatting.RED)
                : Component.translatable("nautec.incubator.slot.nutrient.eats", BacteriaLookup.strainName(colony.getBacteria()),
                BacteriaLookup.itemList(eats)).withStyle(ChatFormatting.WHITE));
        return lines;
    }

    private static List<Component> statusTooltip(IncubatorBlockEntity incubator) {
        List<Component> lines = new ArrayList<>();
        BacteriaInstance colony = incubator.getBacteriaStorage().getBacteria(0);
        boolean powered = incubator.getPower() >= incubator.getRequiredPower();
        if (colony.isEmpty()) {
            lines.add(Component.translatable("nautec.incubator.status.no_colony").withStyle(ChatFormatting.YELLOW));
        } else if (incubator.getItemStackHandler().getStackInSlot(IncubatorBlockEntity.NUTRIENT_SLOT).isEmpty()) {
            lines.add(Component.translatable("nautec.incubator.status.no_nutrient").withStyle(ChatFormatting.YELLOW));
        } else if (!incubator.isActive()) {
            lines.add(Component.translatable("nautec.incubator.status.wrong_nutrient", BacteriaLookup.strainName(colony.getBacteria()))
                    .withStyle(ChatFormatting.RED));
        } else if (!powered) {
            lines.add(Component.translatable("nautec.mutator.status.no_power").withStyle(ChatFormatting.RED));
        } else {
            lines.add(Component.translatable("nautec.incubator.status.running").withStyle(ChatFormatting.GREEN));
        }
        lines.add(Component.translatable("nautec.mutator.status.power", incubator.getPower(), incubator.getRequiredPower())
                .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (colony.isAnalyzed()) {
            lines.add(Component.translatable("nautec.incubator.status.size", colony.getSize(), NTConfig.bacteriaColonySizeCap)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (incubator.beamSpeed() > 1F) {
            lines.add(Component.translatable("nautec.machine.beam_speed", String.format(Locale.ROOT, "%.2f", incubator.beamSpeed()))
                    .withStyle(ChatFormatting.AQUA));
        }
        return lines;
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, true));
    }
}
