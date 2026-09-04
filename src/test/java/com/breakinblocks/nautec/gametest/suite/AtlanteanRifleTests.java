package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public final class AtlanteanRifleTests {
    private static final BlockPos TARGET = new BlockPos(4, 1, 6);
    private static final Vec3 SHOOTER = new Vec3(4.5D, 1D, 2.5D);

    public static void register(NTTestRegistrar r) {
        r.add("atlantean_rifle/beam_hits_through_the_hurt_cooldown", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            IronGolem target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, TARGET);
            Player shooter = shooter(helper, fullRifle());
            float before = target.getHealth();

            Entity first = AtlanteanRifleItem.fire(level, shooter, AtlanteanRifleItem.damageFor(0));
            if (first != target) {
                helper.fail("The beam hit " + first + " instead of the golem");
                return;
            }
            AtlanteanRifleItem.fire(level, shooter, AtlanteanRifleItem.damageFor(2));

            float lost = before - target.getHealth();
            float expected = AtlanteanRifleItem.damageFor(0) + AtlanteanRifleItem.damageFor(2);
            if (Math.abs(lost - expected) > 0.01F) {
                helper.fail("Two beam hits in one tick took " + lost + " health, expected " + expected
                        + "; the second hit was swallowed by the hurt cooldown");
                return;
            }
            helper.succeed();
        });

        r.add("atlantean_rifle/damage_and_draw_build_over_ten_seconds", 20, 1, helper -> {
            int ramp = NTConfig.rifleRampTicks;
            if (AtlanteanRifleItem.damageFor(0) != (float) NTConfig.rifleBaseDamage
                    || AtlanteanRifleItem.damageFor(ramp) != (float) NTConfig.rifleMaxDamage
                    || AtlanteanRifleItem.damageFor(ramp * 4) != (float) NTConfig.rifleMaxDamage) {
                helper.fail("Damage does not run from " + NTConfig.rifleBaseDamage + " to " + NTConfig.rifleMaxDamage
                        + " over the ramp, got " + AtlanteanRifleItem.damageFor(0) + " and " + AtlanteanRifleItem.damageFor(ramp));
                return;
            }
            if (AtlanteanRifleItem.drainFor(0) != NTConfig.rifleBaseDrain
                    || AtlanteanRifleItem.drainFor(ramp) != NTConfig.rifleMaxDrain) {
                helper.fail("Power draw does not run from " + NTConfig.rifleBaseDrain + " to " + NTConfig.rifleMaxDrain
                        + " over the ramp, got " + AtlanteanRifleItem.drainFor(0) + " and " + AtlanteanRifleItem.drainFor(ramp));
                return;
            }

            ServerLevel level = helper.getLevel();
            IronGolem target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, TARGET);
            ItemStack stack = fullRifle();
            Player shooter = shooter(helper, stack);
            AtlanteanRifleItem rifle = NTItems.ATLANTEAN_RIFLE.get();
            int capacity = stored(stack);
            float health = target.getHealth();

            rifle.onUseTick(level, shooter, stack, AtlanteanRifleItem.USE_DURATION - NTConfig.rifleChargeTicks);
            int afterFirst = stored(stack);
            if (capacity - afterFirst != NTConfig.rifleBaseDrain) {
                helper.fail("The first firing tick drew " + (capacity - afterFirst) + " power, expected " + NTConfig.rifleBaseDrain);
                return;
            }
            if (Math.abs(health - target.getHealth() - NTConfig.rifleBaseDamage) > 0.01F) {
                helper.fail("The first firing tick dealt " + (health - target.getHealth()) + ", expected " + NTConfig.rifleBaseDamage);
                return;
            }

            rifle.onUseTick(level, shooter, stack, AtlanteanRifleItem.USE_DURATION - NTConfig.rifleChargeTicks - ramp);
            if (afterFirst - stored(stack) != NTConfig.rifleMaxDrain) {
                helper.fail("The fully ramped firing tick drew " + (afterFirst - stored(stack)) + " power, expected " + NTConfig.rifleMaxDrain);
                return;
            }
            float expected = (float) (NTConfig.rifleBaseDamage + NTConfig.rifleMaxDamage);
            if (Math.abs(health - target.getHealth() - expected) > 0.01F) {
                helper.fail("Base plus fully ramped hits dealt " + (health - target.getHealth()) + ", expected " + expected);
                return;
            }
            helper.succeed();
        });

        r.add("atlantean_rifle/infinity_halves_the_power_draw", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, TARGET);
            ItemStack stack = fullRifle();
            Holder<Enchantment> infinity = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY);
            stack.enchant(infinity, 1);
            Player shooter = shooter(helper, stack);
            int before = stored(stack);

            NTItems.ATLANTEAN_RIFLE.get().onUseTick(level, shooter, stack, AtlanteanRifleItem.USE_DURATION - NTConfig.rifleChargeTicks);

            int expected = NTConfig.rifleBaseDrain / 2;
            if (before - stored(stack) != expected) {
                helper.fail("An Infinity rifle drew " + (before - stored(stack)) + " power, expected " + expected);
                return;
            }
            helper.succeed();
        });

        r.add("atlantean_rifle/charge_up_neither_fires_nor_draws", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            IronGolem target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, TARGET);
            ItemStack stack = fullRifle();
            Player shooter = shooter(helper, stack);
            int before = stored(stack);
            float health = target.getHealth();

            for (int tick = 0; tick < NTConfig.rifleChargeTicks; tick++) {
                NTItems.ATLANTEAN_RIFLE.get().onUseTick(level, shooter, stack, AtlanteanRifleItem.USE_DURATION - tick);
            }

            if (stored(stack) != before) {
                helper.fail("Charging up drew " + (before - stored(stack)) + " power before the beam existed");
                return;
            }
            if (target.getHealth() != health) {
                helper.fail("Charging up already hurt the golem for " + (health - target.getHealth()));
                return;
            }
            helper.succeed();
        });

        r.add("atlantean_rifle/an_empty_buffer_refuses_to_fire", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            IronGolem target = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, TARGET);
            ItemStack stack = new ItemStack(NTItems.ATLANTEAN_RIFLE.get());
            int trickle = NTConfig.rifleBaseDrain / 2;
            stack.set(NTDataComponents.POWER, new ComponentPowerStorage(trickle, NTConfig.riflePowerCapacity, 0F));
            Player shooter = shooter(helper, stack);
            float health = target.getHealth();

            NTItems.ATLANTEAN_RIFLE.get().onUseTick(level, shooter, stack, AtlanteanRifleItem.USE_DURATION - NTConfig.rifleChargeTicks);

            if (stored(stack) != trickle) {
                helper.fail("A half-empty buffer was drained to " + stored(stack) + " even though a shot costs " + NTConfig.rifleBaseDrain);
                return;
            }
            if (target.getHealth() != health) {
                helper.fail("The rifle fired without enough power, dealing " + (health - target.getHealth()));
                return;
            }
            helper.succeed();
        });
    }

    private static ItemStack fullRifle() {
        ItemStack stack = new ItemStack(NTItems.ATLANTEAN_RIFLE.get());
        int capacity = NTConfig.riflePowerCapacity;
        stack.set(NTDataComponents.POWER, new ComponentPowerStorage(capacity, capacity, 0F));
        return stack;
    }

    private static int stored(ItemStack stack) {
        return stack.getOrDefault(NTDataComponents.POWER, ComponentPowerStorage.EMPTY).powerStored();
    }

    private static Player shooter(GameTestHelper helper, ItemStack stack) {
        Player shooter = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(SHOOTER);
        shooter.setPos(at.x, at.y, at.z);
        shooter.setYRot(0.0F);
        shooter.setXRot(0.0F);
        shooter.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return shooter;
    }

    private AtlanteanRifleTests() {
    }
}
