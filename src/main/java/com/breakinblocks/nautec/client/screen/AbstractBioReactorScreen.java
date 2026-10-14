package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.bacteria.Bacteria;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.slots.SlotBacteriaStorage;
import com.breakinblocks.nautec.client.BacteriaLookup;
import com.breakinblocks.nautec.utils.BacteriaHelper;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.AbstractBioReactorBlockEntity;
import com.breakinblocks.nautec.content.menus.BioReactorLayout;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractBioReactorScreen<T extends AbstractBioReactorBlockEntity> extends NTMachineScreen<T> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/bio_reactor/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/bio_reactor/progress_arrow_off");
    private static final int VITALITY_FILL = 0xFF4FE0C8;
    private static final int STARVING_FILL = 0xFFB8483E;
    private static final int PROGRESS_FILL = 0xFF7FD9A0;

    private final BioReactorLayout layout;

    protected AbstractBioReactorScreen(NTMachineMenu<T> menu, Inventory playerInventory, Component title, BioReactorLayout layout) {
        super(menu, playerInventory, title, layout.imageWidth(), layout.imageHeight());
        this.layout = layout;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        T reactor = this.menu.blockEntity;
        int x0 = this.leftPos;
        int y0 = this.topPos;
        for (int i = 0; i < this.layout.nutrients().length; i++) {
            if (reactor.getItemStackHandler().getStackInSlot(reactor.nutrientSlot(i)).isEmpty()) {
                PanelStyle.icon(guiGraphics, PanelStyle.ICON_NUTRIENT, x0 + this.layout.nutrients()[i][0], y0 + this.layout.nutrients()[i][1], 16, 16);
            }
        }
        for (int i = 0; i < this.layout.upgrades().length; i++) {
            if (reactor.getItemStackHandler().getStackInSlot(reactor.upgradeSlot(i)).isEmpty()) {
                PanelStyle.icon(guiGraphics, PanelStyle.ICON_UPGRADE, x0 + this.layout.upgrades()[i][0], y0 + this.layout.upgrades()[i][1], 16, 16);
            }
        }
        if (this.layout.progressArrows() != null) {
            for (int[] arrow : this.layout.progressArrows()) {
                NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, x0 + arrow[0], y0 + arrow[1],
                        BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT);
            }
        }
        if (this.layout.summaryArrow() != null) {
            NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, x0 + this.layout.summaryArrow()[0], y0 + this.layout.summaryArrow()[1],
                    BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT);
        }
        for (int[] bar : this.layout.vitalityBars()) {
            guiGraphics.fill(x0 + bar[0] - 1, y0 + bar[1] - 1, x0 + bar[0] + bar[2] + 1, y0 + bar[1] + bar[3] + 1, PanelStyle.SLOT_EDGE);
            guiGraphics.fill(x0 + bar[0], y0 + bar[1], x0 + bar[0] + bar[2], y0 + bar[1] + bar[3], PanelStyle.SCREEN_FILL);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        T reactor = this.menu.blockEntity;
        int colonies = reactor.getColonySlots();
        float activeProgress = 0;
        int activeCount = 0;

        for (int i = 0; i < colonies; i++) {
            float progress = Math.max(0, Math.min(100, reactor.getProgress(i))) / 100f;
            BacteriaInstance colony = reactor.getBacteriaStorage().getBacteria(i);
            if (!colony.isEmpty()) {
                activeProgress += progress;
                activeCount++;
            }

            if (this.layout.progressArrows() != null && i < this.layout.progressArrows().length) {
                int[] arrow = this.layout.progressArrows()[i];
                int width = (int) (progress * BioReactorLayout.ARROW_WIDTH);
                NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT,
                        0, 0, this.leftPos + arrow[0], this.topPos + arrow[1], width, BioReactorLayout.ARROW_HEIGHT);
            }

            if (this.layout.progressBars() != null && i < this.layout.progressBars().length) {
                int[] bar = this.layout.progressBars()[i];
                int left = this.leftPos + bar[0];
                int top = this.topPos + bar[1];
                guiGraphics.fill(left, top, left + bar[2], top + bar[3], PanelStyle.SLOT_EDGE);
                guiGraphics.fill(left, top, left + Math.round(progress * bar[2]), top + bar[3], PROGRESS_FILL);
            }

            if (i < this.layout.vitalityBars().length) {
                int[] bar = this.layout.vitalityBars()[i];
                int left = this.leftPos + bar[0];
                int top = this.topPos + bar[1];
                if (!colony.isEmpty()) {
                    float capacity = reactor.getVitalityCapacity(i);
                    float fraction = capacity <= 0 ? 0 : Math.min(1, reactor.getVitality(i) / capacity);
                    int filled = Math.round(fraction * bar[3]);
                    if (filled > 0) {
                        guiGraphics.fill(left, top + bar[3] - filled, left + bar[2], top + bar[3], VITALITY_FILL);
                    } else {
                        guiGraphics.fill(left, top + bar[3] - 1, left + bar[2], top + bar[3], STARVING_FILL);
                    }
                }
                if (isHovering(bar[0] - 1, bar[1] - 1, bar[2] + 2, bar[3] + 2, mouseX, mouseY) && !colony.isEmpty()) {
                    guiGraphics.renderComponentTooltip(this.font, vitalityTooltip(reactor, i), mouseX, mouseY);
                }
            }
        }

        if (this.layout.summaryArrow() != null) {
            int[] arrow = this.layout.summaryArrow();
            float average = activeCount == 0 ? 0 : activeProgress / activeCount;
            int width = (int) (average * BioReactorLayout.ARROW_WIDTH);
            NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT,
                    0, 0, this.leftPos + arrow[0], this.topPos + arrow[1], width, BioReactorLayout.ARROW_HEIGHT);
            if (isHovering(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, mouseX, mouseY)) {
                guiGraphics.renderComponentTooltip(this.font, RecipeAreaHost.withRecipeHint(statusTooltip(reactor)), mouseX, mouseY);
            }
        }
        if (this.layout.progressArrows() != null) {
            for (int[] arrow : this.layout.progressArrows()) {
                if (isHovering(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, mouseX, mouseY)) {
                    guiGraphics.renderComponentTooltip(this.font, RecipeAreaHost.withRecipeHint(statusTooltip(reactor)), mouseX, mouseY);
                }
            }
        }
        extractWarnings(guiGraphics);
        if (this.menu.getCarried().isEmpty()) {
            for (int i = 0; i < this.layout.nutrients().length; i++) {
                int[] slot = this.layout.nutrients()[i];
                if (isHovering(slot[0], slot[1], 16, 16, mouseX, mouseY)
                        && reactor.getItemStackHandler().getStackInSlot(reactor.nutrientSlot(i)).isEmpty()) {
                    guiGraphics.renderComponentTooltip(this.font, nutrientTooltip(reactor), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        List<RecipeArea> areas = new ArrayList<>();
        if (this.layout.progressArrows() != null) {
            for (int[] arrow : this.layout.progressArrows()) {
                areas.add(new RecipeArea(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, true));
            }
        }
        if (this.layout.summaryArrow() != null) {
            int[] arrow = this.layout.summaryArrow();
            areas.add(new RecipeArea(arrow[0], arrow[1], BioReactorLayout.ARROW_WIDTH, BioReactorLayout.ARROW_HEIGHT, true));
        }
        return areas;
    }

    private List<Component> nutrientTooltip(T reactor) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.bio_reactor.nutrient").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("nautec.bio_reactor.nutrient.desc").withStyle(ChatFormatting.GRAY));
        ClientLevel level = Minecraft.getInstance().level;
        boolean any = false;
        if (level != null) {
            for (int i = 0; i < reactor.getColonySlots(); i++) {
                BacteriaInstance colony = reactor.getBacteriaStorage().getBacteria(i);
                if (colony.isEmpty()) {
                    continue;
                }
                any = true;
                List<ItemStack> eats = BacteriaLookup.feedingItems(colony);
                lines.add(Component.translatable("nautec.bio_reactor.nutrient.colony", i + 1, colonyName(level, colony),
                                eats.isEmpty() ? Component.translatable("nautec.bio_reactor.nutrient.nothing") : BacteriaLookup.itemList(eats))
                        .withStyle(eats.isEmpty() ? ChatFormatting.RED : ChatFormatting.WHITE));
            }
        }
        if (!any) {
            lines.add(Component.translatable("nautec.bio_reactor.nutrient.no_colony").withStyle(ChatFormatting.GRAY));
        }
        return RecipeAreaHost.withRecipeHint(lines);
    }

    private ColonyWarning warning(T reactor, int colony) {
        BacteriaInstance bacteria = reactor.getBacteriaStorage().getBacteria(colony);
        if (bacteria.isEmpty()) {
            return ColonyWarning.NONE;
        }
        boolean stocked = false;
        for (int i = 0; i < reactor.getNutrientSlotCount() && !stocked; i++) {
            stocked = BacteriaLookup.feeds(bacteria, reactor.getItemStackHandler().getStackInSlot(reactor.nutrientSlot(i)));
        }
        if (bacteria.isSenescent()) {
            return stocked || reactor.getVitality(colony) > 0 ? ColonyWarning.SENESCENT : ColonyWarning.SHRINKING;
        }
        if (stocked) {
            return ColonyWarning.NONE;
        }
        return reactor.getVitality(colony) > 0 ? ColonyWarning.LOW : ColonyWarning.STARVING;
    }

    @Override
    protected List<Component> bacteriaTooltip(SlotBacteriaStorage slot, BacteriaInstance bacteria) {
        List<Component> lines = new ArrayList<>(super.bacteriaTooltip(slot, bacteria));
        ColonyWarning warning = warning(this.menu.blockEntity, slot.getSlot());
        if (warning != ColonyWarning.NONE) {
            lines.add(Component.translatable("nautec.bio_reactor.warning." + warning.name().toLowerCase(Locale.ROOT)).withStyle(warning.format));
            List<ItemStack> eats = BacteriaLookup.feedingItems(bacteria);
            if (warning != ColonyWarning.SENESCENT && !eats.isEmpty()) {
                lines.add(Component.translatable("nautec.bio_reactor.warning.eats", BacteriaLookup.itemList(eats)).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    private void extractWarnings(GuiGraphics guiGraphics) {
        T reactor = this.menu.blockEntity;
        for (SlotBacteriaStorage slot : this.menu.getBacteriaStorageSlots()) {
            ColonyWarning warning = warning(reactor, slot.getSlot());
            if (warning != ColonyWarning.NONE) {
                guiGraphics.drawString(this.font, "!", this.leftPos + slot.getX() + slot.getWidth() - 5, this.topPos + slot.getY() - 1, warning.color, true);
            }
        }
    }

    private enum ColonyWarning {
        NONE(0, ChatFormatting.WHITE),
        LOW(0xFFFFD24A, ChatFormatting.YELLOW),
        STARVING(0xFFFF5A4A, ChatFormatting.RED),
        SENESCENT(0xFFFFD24A, ChatFormatting.YELLOW),
        SHRINKING(0xFFFF5A4A, ChatFormatting.RED);

        private final int color;
        private final ChatFormatting format;

        ColonyWarning(int color, ChatFormatting format) {
            this.color = color;
            this.format = format;
        }
    }

    private static Component colonyName(ClientLevel level, BacteriaInstance colony) {
        Bacteria bacteria = BacteriaHelper.getBacteria(level.registryAccess(), colony.getBacteria());
        if (bacteria != null) {
            Item item = bacteria.resource().resolve();
            if (item != Items.AIR) {
                return new ItemStack(item).getHoverName();
            }
        }
        return Component.literal(colony.getBacteria().location().getPath());
    }

    private static List<Component> vitalityTooltip(AbstractBioReactorBlockEntity reactor, int colony) {
        List<Component> lines = new ArrayList<>();
        float vitality = reactor.getVitality(colony);
        if (vitality > 0) {
            float seconds = vitality / Math.max(0.0001f, reactor.getVitalityCost()) / 20f;
            lines.add(Component.translatable("nautec.bio_reactor.vitality", String.format(Locale.ROOT, "%.1f", seconds))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            lines.add(Component.translatable("nautec.bio_reactor.starving").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private static List<Component> statusTooltip(AbstractBioReactorBlockEntity reactor) {
        List<Component> lines = new ArrayList<>();
        ChatFormatting powerColor = reactor.getPower() >= reactor.getRequiredPower() ? ChatFormatting.GREEN : ChatFormatting.RED;
        lines.add(Component.translatable("nautec.bio_reactor.power", reactor.getPower(), reactor.getRequiredPower()).withStyle(powerColor));
        if (reactor.beamSpeed() > 0F) {
            lines.add(Component.translatable("nautec.bio_reactor.beam_speed",
                    String.format(Locale.ROOT, "%.2f", reactor.beamSpeed()),
                    String.format(Locale.ROOT, "%.2f", reactor.beamSpeed() * reactor.getSpeedMultiplier())).withStyle(ChatFormatting.AQUA));
        }
        if (reactor.hasUpgrades()) {
            lines.add(Component.translatable("nautec.bio_reactor.upgrades",
                    String.format(Locale.ROOT, "%.2f", reactor.getSpeedMultiplier()),
                    reactor.getItemsPerCycle(),
                    Math.round(reactor.getVitalityCost() * 100)).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
