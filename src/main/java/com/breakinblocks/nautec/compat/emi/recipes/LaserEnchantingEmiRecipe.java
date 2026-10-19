package com.breakinblocks.nautec.compat.emi.recipes;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.compat.emi.EmiCanvas;
import com.breakinblocks.nautec.compat.emi.NTEmiPlugin;
import com.breakinblocks.nautec.compat.emi.NTEmiRecipe;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlockEntity;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.breakinblocks.nautec.content.reservoir.ReservoirText;
import com.breakinblocks.nautec.tags.NTTags;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class LaserEnchantingEmiRecipe extends NTEmiRecipe<LaserEnchantingEmiRecipe.Selection> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 52;
    private static final int TOP = 10;
    private static final int TANK_X = 21;
    private static final int TANK_Y = TOP + 1;
    private static final int TANK_WIDTH = 12;
    private static final int TANK_HEIGHT = 36;
    private static final int ARROW_X = 38;
    private static final int ARROW_Y = TOP + 11;
    private static final int OUTPUT_X = 66;
    private static final int TEXT_X = 88;
    private static final int SELECTOR_X = 38;
    private static final int SELECTOR_Y = TOP + 31;
    private static final int SELECTOR_WIDTH = 46;
    private static final int BUTTON_SIZE = 11;
    private static final int ARROW_TICKS = 100;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int OVERENCHANT_COLOR = 0xFFB07A10;
    private static final long COMPACT_FROM = 1_000_000L;

    private final List<EmiStack> books = new ArrayList<>();
    private final List<ItemStack> targets = new ArrayList<>();
    private final EmiStack book = EmiStack.of(Items.BOOK);

    public LaserEnchantingEmiRecipe(ResourceLocation id, LaserEnchantingRecipe recipe, Holder<Enchantment> enchantment) {
        super(NTEmiPlugin.LASER_ENCHANTING, id, new Selection(recipe, enchantment), WIDTH, HEIGHT);
        for (int level = 1; level <= this.recipe.maxLevel(); level++) {
            books.add(EmiStack.of(book(level)));
        }
        for (Holder<Item> item : enchantment.value().getSupportedItems()) {
            targets.add(new ItemStack(item));
        }
        inputs.add(book);
        inputs.add(reagent());
        inputs.add(experience());
        outputs.addAll(books);
    }

    private ItemStack book(int level) {
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(recipe.enchantment, level));
    }

    private EmiIngredient target() {
        if (!recipe.overenchanted()) {
            return book;
        }
        List<EmiStack> stacks = new ArrayList<>();
        for (ItemStack target : targets) {
            stacks.add(EmiStack.of(target));
        }
        return EmiIngredient.of(stacks);
    }

    private EmiIngredient result() {
        int level = recipe.level();
        if (!recipe.overenchanted()) {
            return books.get(level - 1);
        }
        List<EmiStack> stacks = new ArrayList<>();
        for (ItemStack target : targets) {
            ItemStack result = target.copy();
            result.enchant(recipe.enchantment, level);
            stacks.add(EmiStack.of(result));
        }
        return EmiIngredient.of(stacks);
    }

    private EmiIngredient reagent() {
        return EmiIngredient.of(recipe.recipe.input().ingredient(), recipe.reagentCount());
    }

    private EmiIngredient experience() {
        List<EmiStack> stacks = new ArrayList<>();
        for (Holder<Fluid> fluid : BuiltInRegistries.FLUID.getTagOrEmpty(NTTags.Fluids.EXPERIENCE)) {
            if (fluid.value().isSource(fluid.value().defaultFluidState())) {
                stacks.add(EmiStack.of(fluid.value(), Math.max(1, recipe.experience())));
            }
        }
        return EmiIngredient.of(stacks);
    }

    @Override
    protected void addWidgets(EmiCanvas canvas) {
        recipe.setLevel(recipe.maxLevel());
        canvas.fill(TANK_X - 1, TANK_Y - 1, TANK_X + TANK_WIDTH + 1, TANK_Y + TANK_HEIGHT + 1, 0xFF1E2221);
        canvas.fill(TANK_X, TANK_Y, TANK_X + TANK_WIDTH, TANK_Y + TANK_HEIGHT, 0xFF16201F);
        canvas.add(new LevelSlot(book, this::target, 1, TOP + 1));
        canvas.add(new LevelSlot(reagent(), this::reagent, 1, TOP + 21));
        canvas.add(new LevelTank(experience(), this::experience, TANK_X, TANK_Y)).drawBack(false);
        canvas.add(new LevelSlot(books.get(books.size() - 1), this::result, OUTPUT_X, ARROW_Y)).recipeContext(this)
                .appendTooltip(Component.translatable("nautec.jei.laser_enchanting.stable",
                        String.format(Locale.ROOT, "%.1f", NTConfig.abyssalEnchanterStablePurity)).withStyle(ChatFormatting.GRAY));
        canvas.arrow(ARROW_X, ARROW_Y, ARROW_TICKS);
        canvas.add(new LevelText(recipe));
        canvas.add(new LevelSelector(recipe));
    }

    private static String format(long value) {
        return value < COMPACT_FROM ? String.format(Locale.ROOT, "%,d", value) : ReservoirText.compact(value);
    }

    private static String factor(double value) {
        return format(value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(value));
    }

    public static final class Selection {
        private final LaserEnchantingRecipe recipe;
        private final Holder<Enchantment> enchantment;
        private int level;

        private Selection(LaserEnchantingRecipe recipe, Holder<Enchantment> enchantment) {
            this.recipe = recipe;
            this.enchantment = enchantment;
            this.level = maxLevel();
        }

        public int level() {
            return Math.clamp(level, 1, levelCap());
        }

        private void setLevel(int level) {
            this.level = level;
        }

        public int maxLevel() {
            return EnchantingRules.maxLevel(enchantment);
        }

        public int levelCap() {
            return EnchantingRules.levelCap(enchantment);
        }

        public boolean overenchanted() {
            return level() > maxLevel();
        }

        public int sustained() {
            return EnchantingRules.sustainedPower(enchantment, level(), recipe.costMultiplier());
        }

        public long total() {
            return EnchantingRules.totalPower(enchantment, level(), recipe.costMultiplier());
        }

        public int experience() {
            return EnchantingRules.experienceMb(enchantment, level(), recipe.costMultiplier());
        }

        public int reagentCount() {
            return recipe.reagentCount(Math.min(level(), maxLevel()));
        }

        public List<RecipeTransfer.Entry> transfer() {
            Ingredient target = Ingredient.of(Items.BOOK);
            if (overenchanted()) {
                List<ItemStack> items = new ArrayList<>();
                for (Holder<Item> item : enchantment.value().getSupportedItems()) {
                    items.add(new ItemStack(item));
                }
                target = Ingredient.of(items.toArray(ItemStack[]::new));
            }
            return List.of(new RecipeTransfer.Entry(AbyssalEnchanterBlockEntity.TARGET_SLOT, target, 1),
                    new RecipeTransfer.Entry(AbyssalEnchanterBlockEntity.REAGENT_SLOT, recipe.input().ingredient(), reagentCount()));
        }
    }

    private static final class LevelSlot extends SlotWidget {
        private final Supplier<EmiIngredient> shown;

        private LevelSlot(EmiIngredient stack, Supplier<EmiIngredient> shown, int x, int y) {
            super(stack, x - 1 + EmiCanvas.PAD, y - 1 + EmiCanvas.PAD);
            this.shown = shown;
        }

        @Override
        public EmiIngredient getStack() {
            return shown.get();
        }
    }

    private static final class LevelTank extends TankWidget {
        private final Supplier<EmiIngredient> shown;

        private LevelTank(EmiIngredient stack, Supplier<EmiIngredient> shown, int x, int y) {
            super(stack, x - 1 + EmiCanvas.PAD, y - 1 + EmiCanvas.PAD, TANK_WIDTH + 2, TANK_HEIGHT + 2, 1);
            this.shown = shown;
        }

        @Override
        public EmiIngredient getStack() {
            return shown.get();
        }
    }

    private static final class LevelText extends Widget {
        private static final Bounds NONE = new Bounds(0, 0, 0, 0);

        private final Selection selection;

        private LevelText(Selection selection) {
            this.selection = selection;
        }

        @Override
        public Bounds getBounds() {
            return NONE;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;
            int lineHeight = font.lineHeight;
            graphics.drawString(font, font.plainSubstrByWidth(selection.enchantment.value().description().getString(), WIDTH), EmiCanvas.PAD,
                    1 + EmiCanvas.PAD, TEXT_COLOR, false);
            line(graphics, font, Component.translatable("nautec.jei.laser_enchanting.sustained", format(selection.sustained())), TOP + 2, TEXT_COLOR);
            line(graphics, font, Component.translatable("nautec.jei.laser_enchanting.total", format(selection.total())), TOP + 2 + lineHeight, TEXT_COLOR);
            line(graphics, font, Component.translatable("nautec.jei.laser_enchanting.experience", format(selection.experience())), TOP + 2 + 2 * lineHeight,
                    TEXT_COLOR);
            if (selection.overenchanted()) {
                line(graphics, font, Component.translatable("nautec.jei.laser_enchanting.overenchant",
                                factor(EnchantingRules.overFactor(selection.enchantment, selection.level()))),
                        TOP + 2 + 3 * lineHeight, OVERENCHANT_COLOR);
            }
        }

        private static void line(GuiGraphics graphics, Font font, Component text, int y, int color) {
            graphics.drawString(font, font.plainSubstrByWidth(text.getString(), WIDTH - TEXT_X), TEXT_X + EmiCanvas.PAD, y + EmiCanvas.PAD, color, false);
        }
    }

    private static final class LevelSelector extends Widget {
        private static final int DOWN_X = SELECTOR_X + EmiCanvas.PAD;
        private static final int UP_X = SELECTOR_X + SELECTOR_WIDTH - BUTTON_SIZE + EmiCanvas.PAD;
        private static final int TOP = SELECTOR_Y + EmiCanvas.PAD;

        private final Selection selection;

        private LevelSelector(Selection selection) {
            this.selection = selection;
        }

        @Override
        public Bounds getBounds() {
            return new Bounds(DOWN_X, TOP, SELECTOR_WIDTH, BUTTON_SIZE);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            Font font = Minecraft.getInstance().font;
            button(graphics, font, DOWN_X, "<", mouseX, mouseY);
            button(graphics, font, UP_X, ">", mouseX, mouseY);
            Component label = Component.translatable("enchantment.level." + selection.level());
            graphics.drawString(font, label, SELECTOR_X + EmiCanvas.PAD + SELECTOR_WIDTH / 2 - font.width(label) / 2, TOP + 2,
                    selection.overenchanted() ? OVERENCHANT_COLOR : TEXT_COLOR, false);
        }

        private static void button(GuiGraphics graphics, Font font, int x, String symbol, int mouseX, int mouseY) {
            graphics.fill(x, TOP, x + BUTTON_SIZE, TOP + BUTTON_SIZE, 0xFF1E2221);
            graphics.fill(x + 1, TOP + 1, x + BUTTON_SIZE - 1, TOP + BUTTON_SIZE - 1, inside(x, mouseX, mouseY) ? 0xFF2F6E73 : 0xFF2A3634);
            graphics.drawString(font, symbol, x + BUTTON_SIZE / 2 - font.width(symbol) / 2 + 1, TOP + 2, 0xFFE0F4F2, false);
        }

        private static boolean inside(int x, int mouseX, int mouseY) {
            return mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= TOP && mouseY < TOP + BUTTON_SIZE;
        }

        private static boolean onSelector(int mouseX, int mouseY) {
            return mouseX >= DOWN_X && mouseX < UP_X + BUTTON_SIZE && mouseY >= TOP && mouseY < TOP + BUTTON_SIZE;
        }

        @Override
        public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
            if (!onSelector(mouseX, mouseY)) {
                return List.of();
            }
            Component text = Component.translatable("nautec.jei.laser_enchanting.level", Component.translatable("enchantment.level." + selection.level()),
                    Component.translatable("enchantment.level." + selection.levelCap()));
            return List.of(ClientTooltipComponent.create(text.getVisualOrderText()));
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            int delta = inside(DOWN_X, mouseX, mouseY) ? -1 : inside(UP_X, mouseX, mouseY) ? 1 : 0;
            if (button != 0 || delta == 0) {
                return false;
            }
            int cap = selection.levelCap();
            int next = selection.level() + delta;
            if (next > cap) {
                next = 1;
            } else if (next < 1) {
                next = cap;
            }
            selection.setLevel(next);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
    }
}
