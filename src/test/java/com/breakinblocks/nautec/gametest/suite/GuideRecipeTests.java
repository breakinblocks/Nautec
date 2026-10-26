package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.recipes.display.MachineRecipeDisplay;
import com.breakinblocks.nautec.gametest.GuideReferencesGameTest;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GuideRecipeTests {
    private GuideRecipeTests() {
    }

    private static RecipeHolder<?> recipe(NTGameTestHelper helper, String name) {
        return helper.getLevel().getRecipeManager().byKey(Nautec.rl(name)).orElseThrow();
    }

    public static void register(NTTestRegistrar r) {
        r.add("guideme/sampler_result_inputs_and_fluid_are_discoverable", 40, helper -> {
            var display = MachineRecipeDisplay.of(recipe(helper, "mob_sampler_mixing").value());
            helper.assertTrue(display.hasResult(NTItems.MOB_SAMPLER.get()),
                    "RecipeFor can find the sampler through the native result display");
            List<ItemStack> inputs = display.inputs().stream().flatMap(slot -> slot.items().stream()).toList();
            helper.assertTrue(inputs.stream().anyMatch(stack -> stack.is(NTItems.AQUARINE_COPPER_INGOT.get()) && stack.getCount() == 2), "two copper ingots shown");
            helper.assertTrue(inputs.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 1), "one diamond shown");
            List<FluidStack> fluids = display.inputs().stream().flatMap(slot -> slot.fluids().stream()).toList();
            helper.assertTrue(fluids.stream().anyMatch(fluid -> fluid.is(NTFluids.EAS.getStillFluid()) && fluid.getAmount() == 250), "250 mB EAS shown");
            helper.succeed();
        });

        r.add("guideme/all_machine_recipe_displays_present", 100, helper -> {
            Set<RecipeType<?>> types = new HashSet<>();
            int checked = 0;
            for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
                if (!MachineRecipeDisplay.supports(holder.value())) continue;
                types.add(holder.value().getType());
                var display = MachineRecipeDisplay.of(holder.value());
                helper.assertTrue(!display.craftingStation().items().getFirst().isEmpty(), "station for " + holder.id());
                helper.assertTrue(!display.inputs().isEmpty(), "inputs for " + holder.id());
                checked++;
            }
            helper.assertValueEqual(NTRecipes.TYPES.getEntries().size(), types.size(), "all custom recipe types covered");
            helper.assertTrue(checked > 0, "machine recipe displays checked");
            helper.succeed();
        });

        r.add("guideme/laser_crafting_keeps_multiple_outputs_and_fluids", 40, helper -> {
            var display = MachineRecipeDisplay.of(recipe(helper, "laser_crafting/gametest_full_matrix").value());
            helper.assertValueEqual(4, display.inputs().size(), "two item and two fluid inputs");
            helper.assertValueEqual(4, display.outputs().size(), "two item and two fluid outputs");
            List<ItemStack> results = display.outputs().stream().flatMap(slot -> slot.items().stream()).toList();
            helper.assertTrue(results.stream().anyMatch(stack -> stack.is(Items.COMMAND_BLOCK) && stack.getCount() == 2), "secondary item output and count kept");
            helper.assertTrue(display.hasResult(Items.WATER_BUCKET), "fluid bucket discovery alias provided");
            List<FluidStack> fluids = display.outputs().stream().flatMap(slot -> slot.fluids().stream()).toList();
            helper.assertValueEqual(2, fluids.size(), "both fluid outputs kept");
            helper.assertTrue(fluids.stream().anyMatch(fluid -> fluid.getAmount() == 200), "fluid amount kept");
            helper.succeed();
        });

        r.add("guideme/guide_references_resolve", 100, GuideReferencesGameTest::allGuideReferencesResolve);
    }
}
