package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.capabilities.NTCapabilities;
import com.breakinblocks.nautec.capabilities.power.IPowerStorage;
import com.breakinblocks.nautec.content.entities.SubmarineCollision;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModuleContainer;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.breakinblocks.nautec.content.items.SubmarineAnvilRepair;
import com.breakinblocks.nautec.content.items.submarine.SubmarineModuleType;
import com.breakinblocks.nautec.content.items.submarine.TeleportModuleItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.List;

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

        r.add("submarine/armor_mitigates_damage", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            float max = submarine.getMaxHealth();
            helper.assertValueEqual(max, submarine.getHealth(), "a fresh submarine should launch at full hull");

            Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
            helper.assertTrue(submarine.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(attacker), 10F),
                    "the submarine refused a survival attack");
            helper.assertFalse(submarine.isRemoved(), "one hit should not destroy the submarine");
            helper.assertValueEqual(max - 3F, submarine.getHealth(), "hull left after 10 damage through 20 armor and 8 toughness");
            helper.succeed();
        }));

        r.add("submarine/passenger_cannot_punch_hull", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);
            pilot.startRiding(submarine);

            submarine.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(pilot), 10F);
            helper.assertValueEqual(submarine.getMaxHealth(), submarine.getHealth(), "the pilot damaged their own hull");
            helper.succeed();
        }));

        r.add("submarine/death_drops_damaged_item", 60, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(7_000);
            LivingEntity rider = helper.spawn(EntityType.PIG, SUB_POS);
            rider.startRiding(submarine);

            submarine.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), 1000F);

            helper.assertTrue(submarine.isRemoved(), "a destroyed submarine should be gone");
            helper.assertTrue(rider.isAlive(), "the rider should have been ejected alive");
            helper.assertFalse(rider.isPassenger(), "the rider should no longer be seated");

            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, submarine.getBoundingBox().inflate(8D),
                    item -> item.getItem().is(NTItems.SUBMARINE.get()));
            helper.assertValueEqual(1, drops.size(), "submarine items dropped on death");

            ItemStack dropped = drops.getFirst().getItem();
            helper.assertTrue(SubmarineAnvilRepair.isBreached(dropped), "the dropped submarine should be marked as breached");
            ComponentPowerStorage power = dropped.get(NTDataComponents.POWER);
            helper.assertTrue(power != null && power.powerStored() == 7_000, "the wreck should keep its stored power");
            helper.succeed();
        }));

        r.add("submarine/breached_item_refuses_launch", 40, helper -> helper.runAfterDelay(1, () -> {
            ItemStack healthy = new ItemStack(NTItems.SUBMARINE.get());
            helper.assertFalse(SubmarineAnvilRepair.isBreached(healthy), "an untouched submarine item should launch");

            ItemStack breached = healthy.copy();
            breached.set(NTDataComponents.SUBMARINE_HEALTH, 0F);
            helper.assertTrue(SubmarineAnvilRepair.isBreached(breached), "a hull at zero should refuse to launch");

            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.applyStack(breached);
            helper.assertTrue(submarine.getHealth() > 0F, "a submarine restored from a wreck should never spawn dead");
            helper.succeed();
        }));

        r.add("submarine/anvil_repair_math", 40, helper -> helper.runAfterDelay(1, () -> {
            float max = SubmarineAnvilRepair.maxHealth();
            ItemStack wreck = new ItemStack(NTItems.SUBMARINE.get());
            wreck.set(NTDataComponents.SUBMARINE_HEALTH, 0F);

            helper.assertTrue(SubmarineAnvilRepair.compute(wreck, new ItemStack(Items.IRON_INGOT, 8)) == null,
                    "the wrong material should not repair a submarine");

            SubmarineAnvilRepair.Result partial = SubmarineAnvilRepair.compute(wreck, new ItemStack(SubmarineAnvilRepair.repairItem(), 3));
            helper.assertTrue(partial != null, "three repair items should give a partial repair");
            helper.assertValueEqual(3, partial.materialCost(), "repair items consumed");
            helper.assertValueEqual(max * 0.6F, SubmarineAnvilRepair.healthOf(partial.output()), "hull after three repair items");

            SubmarineAnvilRepair.Result full = SubmarineAnvilRepair.compute(wreck, new ItemStack(SubmarineAnvilRepair.repairItem(), 8));
            helper.assertTrue(full != null, "eight repair items should fully repair a submarine");
            helper.assertValueEqual(5, full.materialCost(), "a full repair should only consume what it needs");
            helper.assertValueEqual(max, SubmarineAnvilRepair.healthOf(full.output()), "hull after a full repair");

            helper.assertTrue(SubmarineAnvilRepair.compute(full.output(), new ItemStack(SubmarineAnvilRepair.repairItem(), 8)) == null,
                    "an intact submarine should not accept repairs");
            helper.succeed();
        }));

        r.add("submarine/aggro_moves_to_the_hull", 60, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            LivingEntity rider = helper.spawn(EntityType.PIG, SUB_POS);
            rider.startRiding(submarine);

            Drowned drowned = helper.spawn(EntityType.DROWNED, new BlockPos(1, 2, 1));
            drowned.setTarget(rider);
            helper.assertTrue(drowned.getTarget() == submarine, "a mob targeting the crew should retarget onto the hull");

            drowned.setTarget(null);
            helper.assertTrue(drowned.getTarget() == null, "clearing a target should still be allowed");
            helper.succeed();
        }));

        r.add("submarine/boarding_pulls_aggro", 60, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            LivingEntity rider = helper.spawn(EntityType.PIG, SUB_POS);
            Drowned drowned = helper.spawn(EntityType.DROWNED, new BlockPos(1, 2, 1));
            drowned.setTarget(rider);

            rider.startRiding(submarine);
            helper.assertTrue(drowned.getTarget() == submarine, "boarding should pull an existing lock onto the hull");
            helper.succeed();
        }));

        r.add("submarine/module_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            helper.assertFalse(submarine.hasModule(SubmarineModuleType.SONAR), "a fresh submarine should have no modules");

            submarine.setModule(0, new ItemStack(NTItems.SONAR_MODULE.get()));
            ItemStack teleport = new ItemStack(NTItems.TELEPORT_MODULE.get());
            teleport.set(NTDataComponents.TELEPORT_ANCHOR,
                    new TeleportAnchor(GlobalPos.of(Level.OVERWORLD, new BlockPos(10, 20, 30)), 90F));
            submarine.setModule(4, teleport);

            helper.assertTrue(submarine.hasModule(SubmarineModuleType.SONAR), "the sonar module should be installed");
            helper.assertTrue(submarine.hasModule(SubmarineModuleType.TELEPORT), "the teleport module should be installed");
            helper.assertFalse(submarine.hasModule(SubmarineModuleType.SHIELD), "no shield module was installed");
            helper.assertValueEqual(SubmarineEntity.MODULE_SLOTS, submarine.getModuleStacks().size(), "module slots");

            SubmarineEntity rebuilt = spawnSubmarine(helper);
            rebuilt.applyStack(submarine.toStack());

            helper.assertTrue(rebuilt.hasModule(SubmarineModuleType.SONAR), "modules were lost on the way through the item");
            helper.assertTrue(rebuilt.getModule(4).is(NTItems.TELEPORT_MODULE.get()), "the teleport module changed slots");
            TeleportAnchor anchor = TeleportModuleItem.anchorOf(rebuilt.getModule(4));
            helper.assertTrue(anchor != null, "the bound anchor was lost");
            helper.assertValueEqual(new BlockPos(10, 20, 30), anchor.pos().pos(), "bound anchor position");
            helper.assertValueEqual(90F, anchor.yaw(), "bound anchor yaw");
            helper.succeed();
        }));

        r.add("submarine/module_slot_filter", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            SubmarineModuleContainer container = new SubmarineModuleContainer(submarine);

            helper.assertValueEqual(SubmarineEntity.MODULE_SLOTS, container.getContainerSize(), "module container size");
            helper.assertTrue(container.isEmpty(), "a fresh module container should be empty");
            helper.assertTrue(container.canPlaceItem(0, new ItemStack(NTItems.ARMOR_MODULE.get())),
                    "modules should fit in a module slot");
            helper.assertFalse(container.canPlaceItem(0, new ItemStack(Items.DIAMOND)),
                    "anything that is not a module should be refused");
            helper.assertValueEqual(1, container.getMaxStackSize(), "a module slot should hold a single module");

            container.setItem(2, new ItemStack(NTItems.ARMOR_MODULE.get()));
            helper.assertTrue(submarine.hasModule(SubmarineModuleType.ARMOR), "the container did not reach the submarine");
            helper.assertFalse(container.isEmpty(), "the container should report its contents");

            container.removeItemNoUpdate(2);
            helper.assertFalse(submarine.hasModule(SubmarineModuleType.ARMOR), "the module was not removed");
            helper.succeed();
        }));

        r.add("submarine/ability_cooldown_gate", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(NTConfig.submarinePowerCapacity);
            submarine.setModule(0, new ItemStack(NTItems.SONAR_MODULE.get()));
            submarine.setModule(1, new ItemStack(NTItems.ARMOR_MODULE.get()));

            SubmarineModules modules = submarine.getModules();
            Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);

            helper.assertTrue(modules.isReady(0), "a freshly installed module should be ready to fire");
            modules.activate(0, pilot);
            helper.assertFalse(modules.isReady(0), "firing a module should put it on cooldown");
            helper.assertValueEqual(NTConfig.submarineSonarCooldownTicks, modules.remainingCooldown(0), "sonar cooldown");

            modules.activate(1, pilot);
            helper.assertTrue(modules.isReady(1), "a passive module should never go on cooldown");

            modules.activate(8, pilot);
            helper.assertTrue(modules.isReady(8), "an empty slot should do nothing at all");
            helper.succeed();
        }));

        r.add("submarine/ability_needs_power", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(0);
            submarine.setModule(0, new ItemStack(NTItems.BOOSTER_MODULE.get()));

            SubmarineModules modules = submarine.getModules();
            modules.activate(0, helper.makeMockPlayer(GameType.SURVIVAL));
            helper.assertTrue(modules.isReady(0), "an empty hull should not be able to fire a module");

            modules.activate(0, helper.makeMockPlayer(GameType.CREATIVE));
            helper.assertFalse(modules.isReady(0), "a creative pilot should fire without power");
            helper.assertValueEqual(NTConfig.submarineBoostDurationTicks + NTConfig.submarineBoostCooldownTicks,
                    modules.remainingCooldown(0), "boost lockout");

            submarine.setPowerStored(NTConfig.submarinePowerCapacity);
            modules.clearCooldowns();
            modules.activate(0, helper.makeMockPlayer(GameType.SURVIVAL));
            helper.assertFalse(modules.isReady(0), "a charged hull should fire the module");
            helper.succeed();
        }));

        r.add("submarine/hull_self_repairs", NTConfig.submarineAutorepairIntervalTicks + 40, helper -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setHealth(submarine.getMaxHealth() - 10F);
            float damaged = submarine.getHealth();

            submarine.heal(20F);
            helper.assertValueEqual(damaged, submarine.getHealth(), "the hull should ignore healing");

            helper.runAfterDelay(NTConfig.submarineAutorepairIntervalTicks + 10, () -> {
                helper.assertTrue(submarine.getHealth() > damaged,
                        "the hull should have self repaired, got " + submarine.getHealth());
                helper.succeed();
            });
        });

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
