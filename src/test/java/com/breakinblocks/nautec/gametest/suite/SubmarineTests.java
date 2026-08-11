package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.SubmarineCollision;
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

        r.add("submarine/deploys_in_water", 60, helper -> {
            helper.setBlock(SUB_POS, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
            helper.runAfterDelay(5, () -> {
                SubmarineEntity submarine = spawnSubmarine(helper);
                helper.runAfterDelay(5, () -> {
                    helper.assertTrue(submarine.isDeployed(), "an empty submarine floating in water should stay deployed");
                    helper.succeed();
                });
            });
        });

        r.add("submarine/survives_being_punched", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000F);
            helper.assertFalse(submarine.isRemoved(), "punching the submarine should not destroy it");
            helper.assertValueEqual(0F, submarine.getDamage(), "submarine damage taken");
            helper.succeed();
        }));

        r.add("submarine/oriented_collision", com.breakinblocks.nautec.Nautec.rl("empty_19x11x19"), 80, 0, helper -> {
            for (int x = 3; x <= 15; x++) {
                for (int y = 1; y <= 9; y++) {
                    helper.setBlock(new BlockPos(x, y, 15), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                }
            }

            helper.runAfterDelay(5, () -> {
                SubmarineEntity submarine = helper.spawn(NTEntities.SUBMARINE.get(), new BlockPos(9, 4, 9));
                net.minecraft.world.phys.Vec3 pos = submarine.position();
                net.minecraft.server.level.ServerLevel level = helper.getLevel();

                helper.assertFalse(SubmarineCollision.blocked(level, submarine, pos, 0F, 0F),
                        "the hull should clear the wall at rest");
                helper.assertTrue(SubmarineCollision.blocked(level, submarine, pos.add(0, 0, 1), 0F, 0F),
                        "the nose should hit the wall one block ahead, well before the core box does");
                helper.assertFalse(SubmarineCollision.blocked(level, submarine, pos, 90F, 0F),
                        "parallel to the wall the beam should clear it");

                net.minecraft.world.phys.Vec3 clamped = SubmarineCollision.clampMotion(level, submarine, pos,
                        new net.minecraft.world.phys.Vec3(0, 0, 1), 0F, 0F);
                helper.assertTrue(clamped.z < 0.62 && clamped.z >= 0.25,
                        "forward motion should clamp at the wall, got " + clamped.z);
                helper.succeed();
            });
        });

        r.add("submarine/creative_pilot_no_upkeep", 60, helper -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(5_000);
            net.minecraft.world.entity.player.Player pilot = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
            pilot.startRiding(submarine);

            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(5_000, submarine.getPowerStored(), "power drained despite a creative pilot");
                helper.succeed();
            });
        });

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
