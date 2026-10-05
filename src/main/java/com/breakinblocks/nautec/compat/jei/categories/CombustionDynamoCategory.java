package com.breakinblocks.nautec.compat.jei.categories;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.generators.CombustionDynamoBlockEntity;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.tags.NTTags;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class CombustionDynamoCategory extends AbstractRecipeCategory<CombustionDynamoCategory.Fuel> {
    public static final Identifier UID = Nautec.rl("combustion_dynamo");
    public static final IRecipeType<Fuel> RECIPE_TYPE = IRecipeType.create(UID, Fuel.class);
    private static final int WIDTH = 150;

    public CombustionDynamoCategory(IGuiHelper helper) {
        super(RECIPE_TYPE,
                Component.translatable("nautec.jei.category.combustion_dynamo"),
                helper.createDrawableItemStack(new ItemStack(NTBlocks.COMBUSTION_DYNAMO.get())),
                WIDTH,
                22 + 3 * Minecraft.getInstance().font.lineHeight);
    }

    public static List<Fuel> recipes() {
        return List.of(new Fuel());
    }

    private static List<Fluid> oils() {
        List<Fluid> oils = new ArrayList<>();
        for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(NTTags.Fluids.OIL)) {
            Fluid fluid = holder.value();
            if (fluid.isSource(fluid.defaultFluidState())) {
                oils.add(fluid);
            }
        }
        return oils;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Fuel recipe, IFocusGroup focuses) {
        IRecipeSlotBuilder oil = NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, WIDTH / 2 - 20, 0);
        for (Fluid fluid : oils()) {
            oil.add(fluid, 1000);
            ItemStack bucket = new ItemStack(fluid.getBucket());
            if (!bucket.isEmpty()) {
                builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).add(bucket);
            }
        }
        oil.setFluidRenderer(1000, false, 16, 16);
        NTJeiUtil.addFramedSlot(builder, RecipeIngredientRole.INPUT, WIDTH / 2 + 2, 0)
                .add(Fluids.WATER, 1000)
                .setFluidRenderer(1000, false, 16, 16);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, Fuel recipe, IFocusGroup focuses) {
        int line = Minecraft.getInstance().font.lineHeight;
        Component[] text = {
                Component.translatable("nautec.jei.combustion.output", CombustionDynamoBlockEntity.baseRate()),
                Component.translatable("nautec.jei.combustion.oil", 1, NTConfig.combustionDynamoTicksPerOil),
                Component.translatable("nautec.jei.combustion.water", NTConfig.combustionDynamoWaterPerTick),
        };
        for (int i = 0; i < text.length; i++) {
            builder.addText(text[i], getWidth(), line)
                    .setPosition(0, 22 + i * line)
                    .setTextAlignment(HorizontalAlignment.CENTER)
                    .setColor(0xFFFFFFFF)
                    .setShadow(true);
        }
    }

    public record Fuel() {
    }
}
