package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.NTGui;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.client.screen.NTMachineScreen;
import com.breakinblocks.nautec.api.client.screen.RecipeAreaHost;
import com.breakinblocks.nautec.api.menu.NTMachineMenu;
import com.breakinblocks.nautec.client.BacteriaLookup;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.recipes.BacteriaMutationRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MutatorScreen extends NTMachineScreen<MutatorBlockEntity> {
    public static final ResourceLocation PROGRESS_ARROW = Nautec.rl("container/mutator/progress_arrow");
    public static final ResourceLocation PROGRESS_ARROW_OFF = Nautec.rl("container/mutator/progress_arrow_off");
    private static final int BOOSTER_X = 99;
    private static final int BOOSTER_Y = 61;
    private static final int CATALYST_X = 79;
    private static final int CATALYST_Y = 61;
    private static final int ARROW_X = 56;
    private static final int ARROW_Y = 36;
    private static final int ARROW_WIDTH = 62;
    private static final int ARROW_HEIGHT = 14;

    public MutatorScreen(NTMachineMenu<MutatorBlockEntity> menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;

        int progress = menu.blockEntity.getProgress();

        int j1 = Mth.ceil(((float) progress / NTConfig.mutatorCraftingSpeed) * 62f);

        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW_OFF, i + ARROW_X, j + ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT);
        NTGui.blitSprite(guiGraphics, PROGRESS_ARROW, 62, 14, 0, 0, i + ARROW_X, j + ARROW_Y, j1, 14);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        MutatorBlockEntity mutator = menu.blockEntity;
        boolean emptyHand = this.menu.getCarried().isEmpty();
        if (emptyHand && isHovering(BOOSTER_X, BOOSTER_Y, 16, 16, mouseX, mouseY)
                && mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.BOOSTER).isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, List.of(
                    Component.translatable("nautec.mutator.slot.booster"),
                    Component.translatable("nautec.mutator.slot.booster.desc", String.format(Locale.ROOT, "%.0f", NTConfig.mutatorBoosterMultiplier))
                            .withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
        }
        if (emptyHand && isHovering(CATALYST_X, CATALYST_Y, 16, 16, mouseX, mouseY)
                && mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.CATALYST).isEmpty()) {
            guiGraphics.renderComponentTooltip(this.font, catalystTooltip(mutator), mouseX, mouseY);
        }
        if (isHovering(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, mouseX, mouseY)) {
            guiGraphics.renderComponentTooltip(this.font, RecipeAreaHost.withRecipeHint(statusTooltip(mutator)), mouseX, mouseY);
        }
    }

    private static List<Component> catalystTooltip(MutatorBlockEntity mutator) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("nautec.mutator.slot.catalyst").withStyle(ChatFormatting.AQUA));
        BacteriaInstance colony = mutator.getBacteriaStorage().getBacteria(0);
        if (colony.isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.slot.catalyst.no_colony").withStyle(ChatFormatting.GRAY));
            return lines;
        }
        List<Component> rows = BacteriaLookup.mutationRows(colony, mutator.isBoosted());
        if (rows.isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.slot.catalyst.none", BacteriaLookup.strainName(colony.getBacteria()))
                    .withStyle(ChatFormatting.RED));
            return lines;
        }
        lines.add(Component.translatable("nautec.mutator.slot.catalyst.options", BacteriaLookup.strainName(colony.getBacteria()))
                .withStyle(ChatFormatting.GRAY));
        lines.addAll(rows);
        return lines;
    }

    private static List<Component> statusTooltip(MutatorBlockEntity mutator) {
        List<Component> lines = new ArrayList<>();
        BacteriaInstance colony = mutator.getBacteriaStorage().getBacteria(0);
        ItemStack catalyst = mutator.getItemStackHandler().getStackInSlot(MutatorBlockEntity.CATALYST);
        Optional<BacteriaMutationRecipe> recipe = BacteriaLookup.mutation(colony, catalyst);
        boolean powered = mutator.getPower() >= mutator.getRequiredPower();

        if (colony.isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.status.no_colony").withStyle(ChatFormatting.YELLOW));
        } else if (catalyst.isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.status.no_catalyst").withStyle(ChatFormatting.YELLOW));
        } else if (recipe.isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.status.wrong_catalyst", catalyst.getHoverName(),
                    BacteriaLookup.strainName(colony.getBacteria())).withStyle(ChatFormatting.RED));
        } else if (!mutator.getBacteriaStorage().getBacteria(1).isEmpty()) {
            lines.add(Component.translatable("nautec.mutator.status.output_full").withStyle(ChatFormatting.YELLOW));
        } else if (!powered) {
            lines.add(Component.translatable("nautec.mutator.status.no_power").withStyle(ChatFormatting.RED));
        } else {
            lines.add(Component.translatable("nautec.mutator.status.running").withStyle(ChatFormatting.GREEN));
        }

        lines.add(Component.translatable("nautec.mutator.status.power", mutator.getPower(), mutator.getRequiredPower())
                .withStyle(powered ? ChatFormatting.GREEN : ChatFormatting.RED));

        if (recipe.isPresent()) {
            BacteriaMutationRecipe found = recipe.get();
            boolean boosted = mutator.isBoosted();
            lines.add(found.refines()
                    ? Component.translatable("nautec.mutator.status.refines", BacteriaLookup.strainName(found.inputBacteria())).withStyle(ChatFormatting.AQUA)
                    : Component.translatable("nautec.mutator.status.mutates", BacteriaLookup.strainName(found.inputBacteria()),
                    BacteriaLookup.strainName(found.resultBacteria())).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable(boosted ? "nautec.mutator.status.chance_boosted" : "nautec.mutator.status.chance",
                    BacteriaLookup.percent(MutatorBlockEntity.computeSuccessChance(found, colony, boosted))).withStyle(ChatFormatting.WHITE));
            long shrink = MutatorBlockEntity.computeFailureShrink(colony, boosted);
            lines.add(shrink > 0
                    ? Component.translatable("nautec.mutator.status.failure_cost", BacteriaLookup.percent((float) shrink / Math.max(1, colony.getSize())))
                    .withStyle(ChatFormatting.GRAY)
                    : Component.translatable("nautec.mutator.status.failure_free").withStyle(ChatFormatting.GRAY));
            if (mutator.getFailedAttempts() > 0) {
                lines.add(Component.translatable("nautec.mutator.status.attempts", mutator.getFailedAttempts()).withStyle(ChatFormatting.GRAY));
            }
            if (!boosted) {
                lines.add(Component.translatable("nautec.mutator.status.booster_hint",
                        String.format(Locale.ROOT, "%.0f", NTConfig.mutatorBoosterMultiplier)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (mutator.beamSpeed() > 1F) {
            lines.add(Component.translatable("nautec.machine.beam_speed", String.format(Locale.ROOT, "%.2f", mutator.beamSpeed()))
                    .withStyle(ChatFormatting.AQUA));
        }
        return lines;
    }

    @Override
    public List<RecipeArea> recipeAreas() {
        return List.of(new RecipeArea(ARROW_X, ARROW_Y, ARROW_WIDTH, ARROW_HEIGHT, true));
    }
}
