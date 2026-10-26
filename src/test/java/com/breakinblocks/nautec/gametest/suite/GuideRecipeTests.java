package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.display.MachineRecipe;
import com.breakinblocks.nautec.content.recipes.display.MachineRecipeDisplay;
import com.breakinblocks.nautec.gametest.GuideReferencesGameTest;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GuideRecipeTests {
    private GuideRecipeTests() {
    }

    private static RecipeHolder<?> recipe(GameTestHelper helper, String name) {
        return helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Nautec.rl(name))).orElseThrow();
    }

    public static void register(NTTestRegistrar r) {
        r.add("guideme/sampler_result_inputs_and_fluid_are_discoverable", 40, helper -> {
            var context = SlotDisplayContext.fromLevel(helper.getLevel());
            var display = (MachineRecipeDisplay) recipe(helper, "mob_sampler_mixing").value().display().getFirst();
            helper.assertTrue(display.result().resolveForStacks(context).stream().anyMatch(stack -> stack.is(NTItems.MOB_SAMPLER.get())),
                    "RecipeFor can find the sampler through the native result display");
            List<ItemStack> inputs = display.inputs().stream().flatMap(slot -> slot.resolveForStacks(context).stream()).toList();
            helper.assertTrue(inputs.stream().anyMatch(stack -> stack.is(NTItems.AQUARINE_COPPER_INGOT.get()) && stack.getCount() == 2), "two copper ingots shown");
            helper.assertTrue(inputs.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 1), "one diamond shown");
            List<FluidStack> fluids = display.inputs().stream().flatMap(slot -> slot.resolve(context, FluidStackContentsFactory.INSTANCE)).toList();
            helper.assertTrue(fluids.stream().anyMatch(fluid -> fluid.is(NTFluids.EAS.getStillFluid()) && fluid.getAmount() == 250), "250 mB EAS shown");
            helper.succeed();
        });

        r.add("guideme/all_machine_recipe_displays_roundtrip", 100, helper -> {
            Set<RecipeType<?>> types = new HashSet<>();
            var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            int checked = 0;
            try {
                for (RecipeHolder<?> holder : helper.getLevel().recipeAccess().recipeMap().values()) {
                    if (!(holder.value() instanceof MachineRecipe<?>)) continue;
                    types.add(holder.value().getType());
                    for (RecipeDisplay display : holder.value().display()) {
                        helper.assertTrue(display instanceof MachineRecipeDisplay, "machine recipe display for " + holder.id());
                        var serialized = RecipeDisplay.CODEC.encodeStart(ops, display).getOrThrow();
                        RecipeDisplay.CODEC.parse(ops, serialized).getOrThrow();
                        buffer.clear();
                        RecipeDisplay.STREAM_CODEC.encode(buffer, display);
                        RecipeDisplay decoded = RecipeDisplay.STREAM_CODEC.decode(buffer);
                        helper.assertTrue(serialized.equals(RecipeDisplay.CODEC.encodeStart(ops, decoded).getOrThrow()), "display network roundtrip for " + holder.id());
                        checked++;
                    }
                }
            } finally {
                buffer.release();
            }
            helper.assertValueEqual(NTRecipes.TYPES.getEntries().size(), types.size(), "all custom recipe types covered");
            helper.assertTrue(checked > 0, "machine recipe displays checked");
            helper.succeed();
        });

        r.add("guideme/laser_crafting_keeps_multiple_outputs_and_fluids", 40, helper -> {
            var context = SlotDisplayContext.fromLevel(helper.getLevel());
            var display = (MachineRecipeDisplay) recipe(helper, "laser_crafting/gametest_full_matrix").value().display().getFirst();
            helper.assertValueEqual(4, display.inputs().size(), "two item and two fluid inputs");
            helper.assertValueEqual(4, display.outputs().size(), "two item and two fluid outputs");
            List<ItemStack> results = display.result().resolveForStacks(context);
            helper.assertTrue(results.stream().anyMatch(stack -> stack.is(Items.COMMAND_BLOCK) && stack.getCount() == 2), "secondary item output and count kept");
            helper.assertTrue(results.stream().anyMatch(stack -> stack.is(Items.WATER_BUCKET)), "fluid bucket discovery alias provided");
            List<FluidStack> fluids = display.outputs().stream().flatMap(slot -> slot.resolve(context, FluidStackContentsFactory.INSTANCE)).toList();
            helper.assertValueEqual(2, fluids.size(), "both fluid outputs kept");
            helper.assertTrue(fluids.stream().anyMatch(fluid -> fluid.getAmount() == 200), "fluid amount kept");
            helper.succeed();
        });

        r.add("guideme/guide_references_resolve", 100, GuideReferencesGameTest::allGuideReferencesResolve);
    }
}
