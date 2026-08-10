package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class SubmarineTests {
    private static final BlockPos SUB_POS = new BlockPos(4, 2, 4);

    private SubmarineTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("submarine/passenger_capacity", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            LivingEntity first = helper.spawn(EntityType.PIG, SUB_POS);
            LivingEntity second = helper.spawn(EntityType.PIG, SUB_POS);
            LivingEntity third = helper.spawn(EntityType.PIG, SUB_POS);

            helper.assertTrue(first.startRiding(submarine), "first rider was refused a seat");
            helper.assertTrue(second.startRiding(submarine), "second rider was refused a seat");
            helper.assertFalse(third.startRiding(submarine), "third rider was let into a two seat submarine");
            helper.assertValueEqual(SubmarineEntity.MAX_PASSENGERS, submarine.getPassengers().size(), "submarine passengers");
            helper.assertTrue(submarine.getControllingPassenger() == first, "the first rider should be the pilot");
            helper.succeed();
        }));

        r.add("submarine/deploy_state", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            helper.assertFalse(submarine.isDeployed(), "a freshly placed submarine should be stowed");

            LivingEntity rider = helper.spawn(EntityType.PIG, SUB_POS);
            rider.startRiding(submarine);
            helper.assertTrue(submarine.isDeployed(), "boarding should deploy the submarine");

            rider.stopRiding();
            helper.assertFalse(submarine.isDeployed(), "the submarine should stow again once it is empty");
            helper.succeed();
        }));

        r.add("submarine/item_power_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            ItemStack stack = new ItemStack(NTItems.SUBMARINE.get());
            stack.set(NTDataComponents.POWER, new ComponentPowerStorage(9_000, NTConfig.submarinePowerCapacity, 1F));

            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.applyStack(stack);
            helper.assertValueEqual(9_000, submarine.getPowerStored(), "power carried from the item into the submarine");

            ItemStack dropped = submarine.toStack();
            ComponentPowerStorage power = dropped.get(NTDataComponents.POWER);
            helper.assertTrue(power != null, "the dropped submarine lost its power component");
            helper.assertValueEqual(9_000, power.powerStored(), "power carried back out into the item");
            helper.succeed();
        }));

        r.add("submarine/power_capability", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            IPowerStorage storage = NTCapabilities.PowerStorage.ENTITY.getCapability(submarine, null);
            helper.assertTrue(storage != null, "the submarine exposes no power capability");
            helper.assertValueEqual(NTConfig.submarinePowerCapacity, storage.getPowerCapacity(), "submarine power capacity");

            int filled = storage.tryFillPower(500, false);
            helper.assertValueEqual(Math.min(500, storage.getMaxInput()), filled, "power accepted by the submarine");
            helper.assertValueEqual(filled, submarine.getPowerStored(), "power stored after charging");
            helper.succeed();
        }));

        r.add("submarine/oxygen_and_drain", 60, helper -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(5_000);
            LivingEntity rider = helper.spawn(EntityType.PIG, SUB_POS);
            rider.startRiding(submarine);
            rider.setAirSupply(1);

            helper.runAfterDelay(10, () -> {
                helper.assertTrue(submarine.getPowerStored() < 5_000, "an occupied submarine should be drawing power");
                helper.assertValueEqual(rider.getMaxAirSupply(), rider.getAirSupply(), "rider air supply while sealed");
                helper.succeed();
            });
        });
    }

    private static SubmarineEntity spawnSubmarine(GameTestHelper helper) {
        SubmarineEntity submarine = helper.spawn(NTEntities.SUBMARINE.get(), SUB_POS);
        helper.assertTrue(submarine != null, "the submarine failed to spawn");
        return submarine;
    }
}
