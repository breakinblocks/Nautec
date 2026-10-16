package com.breakinblocks.nautec.compat.emi;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import com.breakinblocks.nautec.network.RecipeTransferPayload;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class MachineRecipeHandler<C extends NTAbstractContainerMenu<?>> implements EmiRecipeHandler<C> {
    private static final int MISSING = 0x44FF0000;

    private final EmiRecipeCategory category;
    private final BiFunction<C, Object, List<RecipeTransfer.Entry>> entries;

    public MachineRecipeHandler(EmiRecipeCategory category, BiFunction<C, Object, List<RecipeTransfer.Entry>> entries) {
        this.category = category;
        this.entries = entries;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<C> screen) {
        List<EmiStack> stacks = new ArrayList<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    stacks.add(EmiStack.of(stack));
                }
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == category && recipe instanceof NTEmiRecipe<?>;
    }

    private List<RecipeTransfer.Entry> wanted(C menu, EmiRecipe recipe) {
        if (recipe instanceof NTEmiRecipe<?> machineRecipe) {
            return entries.apply(menu, machineRecipe.recipe());
        }
        return List.of();
    }

    private @Nullable RecipeTransfer.Plan plan(C menu, List<RecipeTransfer.Entry> wanted, boolean max) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || wanted.isEmpty()) {
            return null;
        }
        return RecipeTransfer.plan(player, menu, menu.blockEntity, wanted, max);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<C> context) {
        RecipeTransfer.Plan plan = plan(context.getScreenHandler(), wanted(context.getScreenHandler(), recipe), false);
        return plan != null && plan.sets() > 0;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<C> context) {
        C menu = context.getScreenHandler();
        List<RecipeTransfer.Entry> wanted = wanted(menu, recipe);
        boolean max = context.getAmount() > 1;
        RecipeTransfer.Plan plan = plan(menu, wanted, max);
        if (plan == null || plan.sets() == 0) {
            return false;
        }
        PacketDistributor.sendToServer(new RecipeTransferPayload(menu.containerId, wanted, max));
        Minecraft.getInstance().setScreen(context.getScreen());
        return true;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<C> context) {
        C menu = context.getScreenHandler();
        List<RecipeTransfer.Entry> wanted = wanted(menu, recipe);
        if (wanted.isEmpty()) {
            return tooltip(Component.translatable("nautec.jei.transfer.nothing"));
        }
        RecipeTransfer.Plan plan = plan(menu, wanted, false);
        if (plan == null) {
            return tooltip(NOT_ENOUGH_INGREDIENTS);
        }
        if (plan.sets() > 0) {
            return List.of();
        }
        for (boolean absent : plan.missing()) {
            if (absent) {
                return tooltip(NOT_ENOUGH_INGREDIENTS);
            }
        }
        return tooltip(Component.translatable("nautec.jei.transfer.full"));
    }

    private static List<ClientTooltipComponent> tooltip(Component text) {
        return List.of(ClientTooltipComponent.create(text.getVisualOrderText()));
    }

    @Override
    public void render(EmiRecipe recipe, EmiCraftContext<C> context, List<Widget> widgets, GuiGraphics draw) {
        C menu = context.getScreenHandler();
        List<RecipeTransfer.Entry> wanted = wanted(menu, recipe);
        RecipeTransfer.Plan plan = plan(menu, wanted, false);
        if (plan == null || plan.sets() > 0) {
            return;
        }
        for (int e = 0; e < wanted.size(); e++) {
            if (!plan.missing()[e]) {
                continue;
            }
            Ingredient ingredient = wanted.get(e).ingredient();
            for (Widget widget : widgets) {
                if (widget instanceof SlotWidget slot && !(widget instanceof TankWidget) && slot.getRecipe() == null && shows(slot, ingredient)) {
                    Bounds bounds = slot.getBounds();
                    draw.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), MISSING);
                    break;
                }
            }
        }
    }

    private static boolean shows(SlotWidget slot, Ingredient ingredient) {
        for (EmiStack stack : slot.getStack().getEmiStacks()) {
            ItemStack item = stack.getItemStack();
            if (!item.isEmpty() && ingredient.test(item)) {
                return true;
            }
        }
        return false;
    }
}
