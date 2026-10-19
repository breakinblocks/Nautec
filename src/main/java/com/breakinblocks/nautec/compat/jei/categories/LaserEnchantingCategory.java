package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import com.breakinblocks.nautec.content.recipes.LaserEnchantingRecipe;
import com.breakinblocks.nautec.content.recipes.utils.IngredientWithCount;
import com.breakinblocks.nautec.content.reservoir.ReservoirText;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.tags.NTTags;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IIngredientConsumer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawablesView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LaserEnchantingCategory extends AbstractRecipeCategory<LaserEnchantingCategory.Page> {
    public static final ResourceLocation UID = Nautec.rl(LaserEnchantingRecipe.NAME);
    public static final RecipeType<Page> RECIPE_TYPE = new RecipeType<>(UID, Page.class);

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
    private static final String TARGET_SLOT = "target";
    private static final String REAGENT_SLOT = "reagent";
    private static final String EXPERIENCE_SLOT = "experience";
    private static final String OUTPUT_SLOT = "output";

    public static final class Page {
        private final LaserEnchantingRecipe recipe;
        private final Holder<Enchantment> enchantment;
        private int level;
        private List<ItemStack> targets;

        private Page(LaserEnchantingRecipe recipe, Holder<Enchantment> enchantment) {
            this.recipe = recipe;
            this.enchantment = enchantment;
            this.level = maxLevel();
        }

        public LaserEnchantingRecipe recipe() {
            return recipe;
        }

        public Holder<Enchantment> enchantment() {
            return enchantment;
        }

        public int level() {
            return Math.clamp(level, 1, levelCap());
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int maxLevel() {
            return EnchantingRules.maxLevel(enchantment);
        }

        public int levelCap() {
            return EnchantingRules.levelCap(enchantment);
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

        public ItemStack book(int level) {
            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
        }

        public boolean overenchanted() {
            return level() > maxLevel();
        }

        public Ingredient target() {
            return overenchanted() ? Ingredient.of(targets().toArray(ItemStack[]::new)) : Ingredient.of(Items.BOOK);
        }

        private List<ItemStack> targets() {
            if (targets == null) {
                targets = new ArrayList<>();
                for (Holder<Item> item : enchantment.value().getSupportedItems()) {
                    targets.add(new ItemStack(item));
                }
            }
            return targets;
        }

        private List<ItemStack> enchantedTargets() {
            int shown = level();
            List<ItemStack> results = new ArrayList<>();
            for (ItemStack target : targets()) {
                ItemStack result = target.copy();
                result.enchant(enchantment, shown);
                results.add(result);
            }
            return results;
        }
    }

    public static List<Page> pages(@Nullable RecipeManager manager, HolderLookup.Provider registries) {
        List<Page> pages = new ArrayList<>();
        if (manager == null) {
            return pages;
        }
        List<RecipeHolder<LaserEnchantingRecipe>> holders = new ArrayList<>(manager.getAllRecipesFor(LaserEnchantingRecipe.Type.INSTANCE));
        holders.sort(Comparator.comparing(holder -> holder.id().toString()));
        for (RecipeHolder<LaserEnchantingRecipe> holder : holders) {
            LaserEnchantingRecipe recipe = holder.value();
            Holder<Enchantment> enchantment = recipe.enchantment(registries).orElse(null);
            if (enchantment == null || !EnchantingRules.allowed(enchantment)) {
                continue;
            }
            pages.add(new Page(recipe, enchantment));
        }
        return pages;
    }

    public LaserEnchantingCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.laser_enchanting"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.ABYSSAL_ENCHANTER.get())),
                WIDTH,
                HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Page page, IFocusGroup focuses) {
        NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, 1, TOP + 1).setSlotName(TARGET_SLOT).addItemStack(new ItemStack(Items.BOOK));
        IRecipeSlotBuilder reagent = NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, 1, TOP + 21).setSlotName(REAGENT_SLOT);
        NTJeiUtil.addIngredientWithCount(reagent, new IngredientWithCount(page.recipe().input().ingredient(), page.reagentCount()));
        IRecipeSlotBuilder tank = builder.addSlot(RecipeIngredientRole.INPUT, TANK_X, TANK_Y)
                .setFluidRenderer(1, false, TANK_WIDTH, TANK_HEIGHT)
                .setSlotName(EXPERIENCE_SLOT);
        addExperience(tank, page.experience());
        IRecipeSlotBuilder output = NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.OUTPUT, OUTPUT_X, ARROW_Y).setSlotName(OUTPUT_SLOT)
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("nautec.jei.laser_enchanting.stable",
                        String.format(Locale.ROOT, "%.1f", NTConfig.abyssalEnchanterStablePurity)).withStyle(ChatFormatting.GRAY)));
        for (int level = 1; level <= page.maxLevel(); level++) {
            output.addItemStack(page.book(level));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Page page, IFocusGroup focuses) {
        page.setLevel(focusedLevel(page, focuses));
        builder.addAnimatedRecipeArrow(ARROW_TICKS).setPosition(ARROW_X, ARROW_Y);
        LevelSelector selector = new LevelSelector(page, builder.getRecipeSlots());
        builder.addWidget(selector);
        builder.addInputHandler(selector);
        selector.apply();
    }

    @Override
    public void draw(@NotNull Page page, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.fill(TANK_X - 1, TANK_Y - 1, TANK_X + TANK_WIDTH + 1, TANK_Y + TANK_HEIGHT + 1, 0xFF1E2221);
        guiGraphics.fill(TANK_X, TANK_Y, TANK_X + TANK_WIDTH, TANK_Y + TANK_HEIGHT, 0xFF16201F);
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, font.plainSubstrByWidth(page.enchantment().value().description().getString(), WIDTH), 0, 1, TEXT_COLOR, false);
    }

    private static int focusedLevel(Page page, IFocusGroup focuses) {
        return focuses.getItemStackFocuses(RecipeIngredientRole.OUTPUT)
                .mapToInt(focus -> EnchantmentHelper.getEnchantmentsForCrafting(focus.getTypedValue().getIngredient()).getLevel(page.enchantment()))
                .filter(level -> level > 0)
                .findFirst()
                .orElse(page.maxLevel());
    }

    private static void addExperience(IIngredientConsumer acceptor, int amount) {
        for (Holder<Fluid> fluid : BuiltInRegistries.FLUID.getTagOrEmpty(NTTags.Fluids.EXPERIENCE)) {
            if (fluid.value().isSource(fluid.value().defaultFluidState())) {
                acceptor.addFluidStack(fluid.value(), Math.max(1, amount));
            }
        }
    }

    private static String format(long value) {
        return value < COMPACT_FROM ? String.format(Locale.ROOT, "%,d", value) : ReservoirText.compact(value);
    }

    private static String factor(double value) {
        return format(value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(value));
    }

    private static final class LevelSelector implements IRecipeWidget, IJeiInputHandler {
        private static final ScreenPosition ORIGIN = new ScreenPosition(0, 0);
        private static final int DOWN_X = SELECTOR_X;
        private static final int UP_X = SELECTOR_X + SELECTOR_WIDTH - BUTTON_SIZE;

        private final Page page;
        private final IRecipeSlotDrawablesView slots;

        private LevelSelector(Page page, IRecipeSlotDrawablesView slots) {
            this.page = page;
            this.slots = slots;
        }

        private void apply() {
            int level = page.level();
            boolean over = page.overenchanted();
            slots.findSlotByName(TARGET_SLOT).ifPresent(slot -> {
                if (over) {
                    slot.createDisplayOverrides().addItemStacks(page.targets());
                } else {
                    slot.createDisplayOverrides().addItemStack(new ItemStack(Items.BOOK));
                }
            });
            slots.findSlotByName(OUTPUT_SLOT).ifPresent(slot -> {
                if (over) {
                    slot.createDisplayOverrides().addItemStacks(page.enchantedTargets());
                } else {
                    slot.createDisplayOverrides().addItemStack(page.book(level));
                }
            });
            slots.findSlotByName(REAGENT_SLOT).ifPresent(slot -> {
                IIngredientConsumer reagent = slot.createDisplayOverrides();
                int count = page.reagentCount();
                for (ItemStack item : page.recipe().input().ingredient().getItems()) {
                    reagent.addItemStack(item.copyWithCount(count));
                }
            });
            slots.findSlotByName(EXPERIENCE_SLOT).ifPresent(slot -> addExperience(slot.createDisplayOverrides(), page.experience()));
        }

        private void step(int delta) {
            int cap = page.levelCap();
            int next = page.level() + delta;
            if (next > cap) {
                next = 1;
            } else if (next < 1) {
                next = cap;
            }
            page.setLevel(next);
            apply();
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        @Override
        public ScreenPosition getPosition() {
            return ORIGIN;
        }

        @Override
        public ScreenRectangle getArea() {
            return new ScreenRectangle(SELECTOR_X, SELECTOR_Y, SELECTOR_WIDTH, BUTTON_SIZE);
        }

        @Override
        public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            button(guiGraphics, font, DOWN_X, "<", mouseX, mouseY);
            button(guiGraphics, font, UP_X, ">", mouseX, mouseY);
            boolean over = page.level() > page.maxLevel();
            Component label = Component.translatable("enchantment.level." + page.level());
            guiGraphics.drawString(font, label, SELECTOR_X + SELECTOR_WIDTH / 2 - font.width(label) / 2, SELECTOR_Y + 2,
                    over ? OVERENCHANT_COLOR : TEXT_COLOR, false);

            int lineHeight = font.lineHeight;
            line(guiGraphics, font, Component.translatable("nautec.jei.laser_enchanting.sustained", format(page.sustained())), TOP + 2, TEXT_COLOR);
            line(guiGraphics, font, Component.translatable("nautec.jei.laser_enchanting.total", format(page.total())), TOP + 2 + lineHeight, TEXT_COLOR);
            line(guiGraphics, font, Component.translatable("nautec.jei.laser_enchanting.experience", format(page.experience())), TOP + 2 + 2 * lineHeight,
                    TEXT_COLOR);
            if (over) {
                line(guiGraphics, font, Component.translatable("nautec.jei.laser_enchanting.overenchant",
                                factor(EnchantingRules.overFactor(page.enchantment(), page.level()))),
                        TOP + 2 + 3 * lineHeight, OVERENCHANT_COLOR);
            }
        }

        private static void line(GuiGraphics guiGraphics, Font font, Component text, int y, int color) {
            guiGraphics.drawString(font, font.plainSubstrByWidth(text.getString(), WIDTH - TEXT_X), TEXT_X, y, color, false);
        }

        private static void button(GuiGraphics guiGraphics, Font font, int x, String symbol, double mouseX, double mouseY) {
            guiGraphics.fill(x, SELECTOR_Y, x + BUTTON_SIZE, SELECTOR_Y + BUTTON_SIZE, 0xFF1E2221);
            guiGraphics.fill(x + 1, SELECTOR_Y + 1, x + BUTTON_SIZE - 1, SELECTOR_Y + BUTTON_SIZE - 1, inside(x, mouseX, mouseY) ? 0xFF2F6E73 : 0xFF2A3634);
            guiGraphics.drawString(font, symbol, x + BUTTON_SIZE / 2 - font.width(symbol) / 2 + 1, SELECTOR_Y + 2, 0xFFE0F4F2, false);
        }

        private static boolean onSelector(double mouseX, double mouseY) {
            return mouseX >= SELECTOR_X && mouseX < SELECTOR_X + SELECTOR_WIDTH && mouseY >= SELECTOR_Y && mouseY < SELECTOR_Y + BUTTON_SIZE;
        }

        private static boolean inside(int x, double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= SELECTOR_Y && mouseY < SELECTOR_Y + BUTTON_SIZE;
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
            if (onSelector(mouseX, mouseY)) {
                tooltip.add(Component.translatable("nautec.jei.laser_enchanting.level", Component.translatable("enchantment.level." + page.level()),
                        Component.translatable("enchantment.level." + page.levelCap())));
            }
        }

        @Override
        public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
            InputConstants.Key key = input.getKey();
            if (key.getType() != InputConstants.Type.MOUSE || key.getValue() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                return false;
            }
            double x = mouseX + SELECTOR_X;
            double y = mouseY + SELECTOR_Y;
            int delta = inside(DOWN_X, x, y) ? -1 : inside(UP_X, x, y) ? 1 : 0;
            if (delta == 0) {
                return false;
            }
            if (!input.isSimulate()) {
                step(delta);
            }
            return true;
        }

        @Override
        public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
            if (scrollDeltaY == 0 || !onSelector(mouseX + SELECTOR_X, mouseY + SELECTOR_Y)) {
                return false;
            }
            step(scrollDeltaY > 0 ? 1 : -1);
            return true;
        }
    }
}
