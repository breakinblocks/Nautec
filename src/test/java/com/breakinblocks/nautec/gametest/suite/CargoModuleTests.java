package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineCargoContainer;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class CargoModuleTests {
    private static final BlockPos SUB_POS = new BlockPos(4, 2, 4);

    private CargoModuleTests() {
    }

    private static SubmarineEntity spawn(NTGameTestHelper helper) {
        return helper.spawn(NTEntities.SUBMARINE.get(), SUB_POS);
    }

    public static void register(NTTestRegistrar r) {
        r.add("submarine/cargo_slots_follow_modules", 20, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawn(helper);
            helper.assertValueEqual(submarine.getCargoSlots(), 0, "no hold without a module");
            submarine.setModule(0, new ItemStack(NTItems.CARGO_MODULE.get()));
            helper.assertValueEqual(submarine.getCargoSlots(), 27, "one module is a chest");
            submarine.setModule(4, new ItemStack(NTItems.CARGO_MODULE.get()));
            helper.assertValueEqual(submarine.getCargoSlots(), 54, "two modules are a double chest");
            submarine.setModule(8, new ItemStack(NTItems.CARGO_MODULE.get()));
            helper.assertValueEqual(submarine.getCargoSlots(), 54, "a third module adds nothing");
            helper.succeed();
        }));

        r.add("submarine/cargo_survives_pickup", 20, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawn(helper);
            submarine.setModule(0, new ItemStack(NTItems.CARGO_MODULE.get()));
            submarine.getCargo().set(3, new ItemStack(Items.DIAMOND, 12));
            submarine.getCargo().set(26, new ItemStack(Items.COD, 5));
            ItemStack item = submarine.toStack();
            submarine.discard();
            SubmarineEntity placed = spawn(helper);
            placed.applyStack(item);
            helper.assertTrue(ItemStack.matches(placed.getCargo().get(3), new ItemStack(Items.DIAMOND, 12)), "diamonds come back");
            helper.assertTrue(ItemStack.matches(placed.getCargo().get(26), new ItemStack(Items.COD, 5)), "cod comes back");
            helper.assertValueEqual(placed.getCargoSlots(), 27, "the module comes back with it");
            helper.succeed();
        }));

        r.add("submarine/cargo_rules", 20, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawn(helper);
            submarine.setModule(0, new ItemStack(NTItems.CARGO_MODULE.get()));
            SubmarineCargoContainer hold = new SubmarineCargoContainer(submarine, submarine.getCargo(), submarine.getCargoSlots());
            helper.assertFalse(hold.canPlaceItem(0, new ItemStack(NTItems.SUBMARINE.get())), "a hull cannot go in its own hold");
            helper.assertTrue(hold.canPlaceItem(0, new ItemStack(Items.IRON_INGOT)), "ordinary items fit");
            hold.setItem(5, new ItemStack(Items.IRON_INGOT, 3));
            helper.assertTrue(submarine.getCargo().get(5).is(Items.IRON_INGOT), "the hold writes to the hull");
            submarine.setModule(0, ItemStack.EMPTY);
            helper.assertValueEqual(submarine.getCargoSlots(), 0, "the hold closes with the module out");
            helper.assertTrue(submarine.getCargo().get(5).is(Items.IRON_INGOT), "the cargo is kept while the module is out");

            Vec3 forward = Vec3.directionFromRotation(0F, submarine.getYRot());
            Vec3 side = new Vec3(-forward.z, 0, forward.x);
            helper.assertTrue(submarine.isBehind(forward.scale(-4)), "a player standing behind the hull opens the hold");
            helper.assertTrue(submarine.isBehind(forward.scale(-4).add(side.scale(1.5))), "slightly off to one side still counts");
            helper.assertFalse(submarine.isBehind(forward.scale(4)), "a player in front climbs in");
            helper.assertFalse(submarine.isBehind(side.scale(4)), "a player beside the hull climbs in");
            helper.assertFalse(submarine.isBehind(Vec3.ZERO), "a player on top of the hull climbs in");
            helper.succeed();
        }));
    }
}
