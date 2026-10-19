package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlockEntity;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AbyssalEnchanterScreen extends NTMachineScreen<AbyssalEnchanterBlockEntity> {
    private static final int READOUT_X = 54;
    private static final int READOUT_Y = 17;
    private static final int READOUT_WIDTH = 88;
    private static final int READOUT_HEIGHT = 52;
    private static final int BUTTON_SIZE = 11;
    private static final int LEVEL_Y = READOUT_Y + 15;
    private static final int BAR_X = READOUT_X + 5;
    private static final int BAR_Y = READOUT_Y + 30;
    private static final int BAR_WIDTH = READOUT_WIDTH - 10;
    private static final int BAR_HEIGHT = 4;
    private static final int BAR_FILL = 0xFF2AA3BE;
    private static final int BAR_SHINE = 0xFF86E7F3;
    private static final int OVERENCHANT_COLOR = 0xFFE8B84A;

    public AbyssalEnchanterScreen(NTMachineMenu<AbyssalEnchanterBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    private AbyssalEnchanterBlockEntity enchanter() {
        return menu.blockEntity;
    }

    private void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new NTPanelButton(this.font, this.leftPos + READOUT_X + 3, this.topPos + LEVEL_Y - 2, BUTTON_SIZE, BUTTON_SIZE,
                () -> Component.literal("<"), () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.abyssal_enchanter.screen.level_down"),
                () -> press(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_DOWN)));
        addRenderableWidget(new NTPanelButton(this.font, this.leftPos + READOUT_X + READOUT_WIDTH - 3 - BUTTON_SIZE, this.topPos + LEVEL_Y - 2,
                BUTTON_SIZE, BUTTON_SIZE,
                () -> Component.literal(">"), () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.abyssal_enchanter.screen.level_up"),
                () -> press(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_UP)));
        addRenderableWidget(new NTPanelButton(this.font, this.leftPos + READOUT_X + READOUT_WIDTH - 3 - BUTTON_SIZE, this.topPos + READOUT_Y + 2,
                BUTTON_SIZE, BUTTON_SIZE,
                () -> Component.literal("»"),
                () -> enchanter().getRecipeCount() > 1 ? PanelStyle.SEND_COLOR : PanelStyle.NEUTRAL,
                () -> enchanter().getRecipeCount() > 1 ? PanelStyle.SEND_HOVER : PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable("nautec.abyssal_enchanter.screen.next_recipe", enchanter().getRecipeCount()),
                () -> press(AbyssalEnchanterBlockEntity.BUTTON_NEXT_RECIPE)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        AbyssalEnchanterBlockEntity enchanter = enchanter();
        int x = leftPos + READOUT_X;
        int y = topPos + READOUT_Y;
        PanelStyle.screen(guiGraphics, x, y, READOUT_WIDTH, READOUT_HEIGHT);

        Holder<Enchantment> enchantment = enchanter.displayedEnchantment();
        int textWidth = READOUT_WIDTH - 8 - BUTTON_SIZE;
        if (enchantment != null) {
            String name = font.plainSubstrByWidth(enchantment.value().description().getString(), textWidth);
            guiGraphics.text(font, name, x + 4, y + 4, PanelStyle.READOUT, false);
            Component level = Component.translatable("nautec.abyssal_enchanter.screen.level",
                    Component.translatable("enchantment.level." + enchanter.getEnchantLevel()),
                    Component.translatable("enchantment.level." + enchanter.getLevelCap()));
            int levelColor = enchanter.getEnchantLevel() > enchanter.getMaxLevel() ? OVERENCHANT_COLOR : PanelStyle.READOUT;
            guiGraphics.text(font, level, x + READOUT_WIDTH / 2 - font.width(level) / 2, topPos + LEVEL_Y, levelColor, false);
        } else {
            guiGraphics.text(font, font.plainSubstrByWidth(Component.translatable("nautec.abyssal_enchanter.screen.idle").getString(), textWidth),
                    x + 4, y + 4, PanelStyle.READOUT_DIM, false);
        }

        guiGraphics.fill(leftPos + BAR_X - 1, topPos + BAR_Y - 1, leftPos + BAR_X + BAR_WIDTH + 1, topPos + BAR_Y + BAR_HEIGHT + 1, PanelStyle.SLOT_EDGE);
        guiGraphics.fill(leftPos + BAR_X, topPos + BAR_Y, leftPos + BAR_X + BAR_WIDTH, topPos + BAR_Y + BAR_HEIGHT, PanelStyle.SCREEN_FILL);
        int filled = Math.round(BAR_WIDTH * enchanter.progressFraction());
        if (filled > 0) {
            guiGraphics.fill(leftPos + BAR_X, topPos + BAR_Y, leftPos + BAR_X + filled, topPos + BAR_Y + BAR_HEIGHT, BAR_FILL);
            guiGraphics.fill(leftPos + BAR_X, topPos + BAR_Y, leftPos + BAR_X + filled, topPos + BAR_Y + 1, BAR_SHINE);
        }

        Component status = statusLine(enchanter);
        guiGraphics.text(font, font.plainSubstrByWidth(status.getString(), READOUT_WIDTH - 8), x + 4, y + READOUT_HEIGHT - 12,
                enchanter.status() == AbyssalEnchanterBlockEntity.Status.WORKING ? PanelStyle.READOUT : 0xFFE0A060, false);
    }

    private static Component statusLine(AbyssalEnchanterBlockEntity enchanter) {
        String key = "nautec.abyssal_enchanter.status." + enchanter.status().name().toLowerCase(Locale.ROOT);
        return switch (enchanter.status()) {
            case NEED_REAGENT -> Component.translatable(key, enchanter.getReagentNeeded());
            case NEED_XP -> Component.translatable(key, String.format(Locale.ROOT, "%,d", enchanter.getExperienceMb()));
            case LOW_POWER -> Component.translatable(key, String.format(Locale.ROOT, "%,d", enchanter.getRequiredPower()));
            case WORKING -> Component.translatable(key, Math.round(enchanter.progressFraction() * 100F));
            default -> Component.translatable(key);
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (PanelStyle.inside(mouseX, mouseY, leftPos + READOUT_X, topPos + READOUT_Y + 26, READOUT_WIDTH, READOUT_HEIGHT - 26)) {
            guiGraphics.setComponentTooltipForNextFrame(font, RecipeAreaHost.withRecipeHint(details(enchanter())), mouseX, mouseY);
        }
    }

    private static List<Component> details(AbyssalEnchanterBlockEntity enchanter) {
        List<Component> lines = new ArrayList<>();
        lines.add(statusLine(enchanter));
        if (enchanter.getMaxLevel() > 0) {
            boolean powered = enchanter.getPower() >= enchanter.getRequiredPower() && enchanter.getPower() > 0;
            lines.add(Component.translatable("nautec.abyssal_enchanter.screen.sustained",
                    String.format(Locale.ROOT, "%,d", enchanter.getPower()), String.format(Locale.ROOT, "%,d", enchanter.getRequiredPower()))
                    .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED));
            lines.add(Component.translatable("nautec.abyssal_enchanter.screen.total",
                    String.format(Locale.ROOT, "%,d", enchanter.getProgress()), String.format(Locale.ROOT, "%,d", enchanter.getTotal()))
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.abyssal_enchanter.screen.experience", String.format(Locale.ROOT, "%,d", enchanter.getExperienceMb()))
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("nautec.abyssal_enchanter.screen.reagent", enchanter.getReagentNeeded()).withStyle(ChatFormatting.GRAY));
            if (enchanter.getEnchantLevel() > enchanter.getMaxLevel()) {
                Holder<Enchantment> enchantment = enchanter.displayedEnchantment();
                double factor = enchantment == null ? 1.0 : EnchantingRules.overFactor(enchantment, enchanter.getEnchantLevel());
                lines.add(Component.translatable("nautec.abyssal_enchanter.screen.overenchant",
                        Component.translatable("enchantment.level." + enchanter.getMaxLevel()), String.format(Locale.ROOT, "%,.0f", factor))
                        .withStyle(ChatFormatting.GOLD));
            }
            if (enchanter.isBookCapped()) {
                lines.add(Component.translatable("nautec.abyssal_enchanter.screen.book_capped").withStyle(ChatFormatting.GRAY));
            }
        }
        float stability = enchanter.stability();
        lines.add(Component.translatable("nautec.abyssal_enchanter.screen.stability", String.format(Locale.ROOT, "%.2f", enchanter.getPurity()),
                Math.round(stability * 100F)).withStyle(stability >= 1F ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
        float curse = enchanter.curseChance();
        if (curse > 0F) {
            lines.add(Component.translatable("nautec.abyssal_enchanter.screen.curse", String.format(Locale.ROOT, "%.1f", curse * 100F))
                    .withStyle(ChatFormatting.DARK_RED));
        }
        return lines;
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(READOUT_X, READOUT_Y + 26, READOUT_WIDTH, READOUT_HEIGHT - 26, true));
    }
}
