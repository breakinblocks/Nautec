package com.breakinblocks.nautec.compat.jei;

import com.breakinblocks.nautec.api.menu.NTAbstractContainerMenu;
import com.breakinblocks.nautec.content.menus.RecipeTransfer;
import com.breakinblocks.nautec.network.RecipeTransferPayload;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class MachineTransferHandler<C extends NTAbstractContainerMenu<?>, R> implements IRecipeTransferHandler<C, R> {
    private final IRecipeTransferHandlerHelper helper;
    private final Class<C> menuClass;
    private final Supplier<MenuType<C>> menuType;
    private final IRecipeType<R> recipeType;
    private final BiFunction<C, R, List<RecipeTransfer.Entry>> entries;

    public MachineTransferHandler(IRecipeTransferHandlerHelper helper, Class<C> menuClass, Supplier<MenuType<C>> menuType, IRecipeType<R> recipeType,
                                  BiFunction<C, R, List<RecipeTransfer.Entry>> entries) {
        this.helper = helper;
        this.menuClass = menuClass;
        this.menuType = menuType;
        this.recipeType = recipeType;
        this.entries = entries;
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return menuClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        return Optional.of(menuType.get());
    }

    @Override
    public IRecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(C menu, R recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        List<RecipeTransfer.Entry> wanted = entries.apply(menu, recipe);
        if (wanted.isEmpty()) {
            return helper.createUserErrorWithTooltip(Component.translatable("nautec.jei.transfer.nothing"));
        }
        RecipeTransfer.Plan plan = RecipeTransfer.plan(player, menu, menu.blockEntity, wanted, maxTransfer);
        if (plan == null) {
            return helper.createInternalError();
        }
        if (plan.sets() == 0) {
            List<IRecipeSlotView> missing = new ArrayList<>();
            for (int e = 0; e < wanted.size(); e++) {
                if (!plan.missing()[e]) {
                    continue;
                }
                RecipeTransfer.Entry entry = wanted.get(e);
                for (IRecipeSlotView view : recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)) {
                    if (view.getItemStacks().anyMatch(entry.ingredient()::test)) {
                        missing.add(view);
                        break;
                    }
                }
            }
            if (!missing.isEmpty()) {
                return helper.createUserErrorForMissingSlots(Component.translatable("jei.tooltip.error.recipe.transfer.missing"), missing);
            }
            for (boolean absent : plan.missing()) {
                if (absent) {
                    return helper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.error.recipe.transfer.missing"));
                }
            }
            return helper.createUserErrorWithTooltip(Component.translatable("nautec.jei.transfer.full"));
        }
        if (doTransfer) {
            ClientPacketDistributor.sendToServer(new RecipeTransferPayload(menu.containerId, wanted, maxTransfer));
        }
        return null;
    }
}
