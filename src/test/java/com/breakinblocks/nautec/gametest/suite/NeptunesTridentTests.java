package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.ThrownNeptunesTrident;
import com.breakinblocks.nautec.content.entities.TidalShockwave;
import com.breakinblocks.nautec.content.items.NeptunesTridentItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ShockwaveCooldown;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.List;

public final class NeptunesTridentTests {
    private static final Vec3 CENTER = new Vec3(4.5D, 1D, 4.5D);
    private static final String TEAM = "nautec_shockwave_test";
    private static final List<ResourceKey<Enchantment>> SUPPORTED = List.of(
            Enchantments.RIPTIDE, Enchantments.CHANNELING, Enchantments.IMPALING,
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.FIRE_ASPECT, Enchantments.KNOCKBACK, Enchantments.LOOTING,
            Enchantments.SWEEPING_EDGE, Enchantments.VANISHING_CURSE);

    public static void register(NTTestRegistrar r) {
        r.add("neptunes_trident/throw_launches_its_own_entity", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            Player thrower = thrower(helper, stack);
            NeptunesTridentItem trident = NTItems.NEPTUNES_TRIDENT.get();

            trident.releaseUsing(stack, level, thrower, trident.getUseDuration(stack, thrower) - 20);

            List<ThrownNeptunesTrident> flying = level.getEntities(NTEntities.NEPTUNES_TRIDENT.get(),
                    AABB.ofSize(thrower.position(), 8.0, 8.0, 8.0), entity -> true);
            flying.forEach(ThrownNeptunesTrident::discard);
            if (flying.size() != 1) {
                helper.fail("Releasing the throw launched " + flying.size() + " Neptune's Tridents");
                return;
            }
            if (!flying.getFirst().getWeaponItem().is(NTItems.NEPTUNES_TRIDENT.get())) {
                helper.fail("The thrown entity carries " + flying.getFirst().getWeaponItem() + " instead of Neptune's Trident");
                return;
            }
            if (!thrower.getMainHandItem().isEmpty()) {
                helper.fail("The thrower still holds " + thrower.getMainHandItem() + " after throwing");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/refuses_loyalty_but_takes_trident_and_sword_enchants", 20, 1, helper -> {
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            for (ResourceKey<Enchantment> key : SUPPORTED) {
                Holder<Enchantment> enchantment = enchantment(helper.getLevel(), key);
                if (!stack.supportsEnchantment(enchantment) || !stack.isPrimaryItemFor(enchantment)) {
                    helper.fail("Neptune's Trident does not take " + key.location() + " at the anvil and the table");
                    return;
                }
            }
            Holder<Enchantment> loyalty = enchantment(helper.getLevel(), Enchantments.LOYALTY);
            if (stack.supportsEnchantment(loyalty) || stack.isPrimaryItemFor(loyalty)) {
                helper.fail("Neptune's Trident accepts Loyalty even though it already returns on its own");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/unbreakable_sweeping_and_hits_for_eighteen", 20, 1, helper -> {
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            if (stack.isDamageableItem() || !stack.has(DataComponents.UNBREAKABLE)) {
                helper.fail("Neptune's Trident can take damage");
                return;
            }
            if (!stack.canPerformAction(ItemAbilities.SWORD_SWEEP) || !stack.canPerformAction(ItemAbilities.TRIDENT_THROW)) {
                helper.fail("Neptune's Trident should both sweep like a sword and throw like a trident");
                return;
            }
            ItemAttributeModifiers.Builder damageOnly = ItemAttributeModifiers.builder();
            for (ItemAttributeModifiers.Entry entry : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                    damageOnly.add(entry.attribute(), entry.modifier(), entry.slot());
                }
            }
            double attack = damageOnly.build().compute(1.0, EquipmentSlot.MAINHAND);
            if (Math.abs(attack - (1.0 + NeptunesTridentItem.DAMAGE)) > 0.001 || NeptunesTridentItem.DAMAGE != 18.0F) {
                helper.fail("Neptune's Trident melee damage is " + attack + ", expected 19 from an 18 point bonus");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/returns_without_a_loyalty_enchant", 80, 1, helper -> {
            ServerLevel level = helper.getLevel();
            helper.setBlock(new BlockPos(4, 1, 4), Blocks.STONE);
            Player thrower = thrower(helper, ItemStack.EMPTY);
            ThrownNeptunesTrident trident = new ThrownNeptunesTrident(level, thrower, new ItemStack(NTItems.NEPTUNES_TRIDENT.get()));
            Vec3 start = helper.absoluteVec(new Vec3(4.5D, 5D, 4.5D));
            trident.setPos(start.x, start.y, start.z);
            trident.setDeltaMovement(0.0, -1.0, 0.0);
            level.addFreshEntity(trident);

            helper.onEachTick(() -> {
                if (trident.isNoPhysics() && trident.getWeaponItem().has(NTDataComponents.SHOCKWAVE_COOLDOWN.get())) {
                    trident.discard();
                    helper.succeed();
                }
            });
            helper.runAfterDelay(70, () -> helper.fail("A landed Neptune's Trident with no enchantments never turned back toward its owner"
                    + " or never released its shockwave: noPhysics=" + trident.isNoPhysics() + ", weapon=" + trident.getWeaponItem()));
        });

        r.add("neptunes_trident/landing_on_a_mob_hits_it_and_releases_the_shockwave", 60, 1, helper -> {
            ServerLevel level = helper.getLevel();
            IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 1, 4));
            float before = golem.getHealth();
            Player thrower = thrower(helper, ItemStack.EMPTY);
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            float expected = Math.max(NeptunesTridentItem.DAMAGE, TidalShockwave.damage(level, stack));
            ThrownNeptunesTrident trident = new ThrownNeptunesTrident(level, thrower, stack);
            Vec3 start = golem.position().add(0.0, golem.getBbHeight() + 1.5, 0.0);
            trident.setPos(start.x, start.y, start.z);
            trident.setDeltaMovement(0.0, -1.5, 0.0);
            level.addFreshEntity(trident);

            helper.onEachTick(() -> {
                if (before - golem.getHealth() > 0.0F && trident.getWeaponItem().has(NTDataComponents.SHOCKWAVE_COOLDOWN.get())) {
                    float lost = before - golem.getHealth();
                    trident.discard();
                    if (Math.abs(lost - expected) > 0.01F) {
                        helper.fail("The golem lost " + lost + " health, expected " + expected);
                        return;
                    }
                    helper.succeed();
                }
            });
            helper.runAfterDelay(50, () -> helper.fail("The trident never hit the golem and released its shockwave: golem at "
                    + golem.getHealth() + ", weapon=" + trident.getWeaponItem()));
        });

        r.add("neptunes_trident/shockwave_scales_with_sharpness_and_knockback", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            ItemStack plain = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            ItemStack enchanted = plain.copy();
            enchanted.enchant(enchantment(level, Enchantments.SHARPNESS), 3);
            enchanted.enchant(enchantment(level, Enchantments.KNOCKBACK), 2);

            float damage = (float) (NTConfig.tridentShockwaveDamage + 3 * NTConfig.tridentShockwaveDamagePerSharpness);
            float knockback = (float) (NTConfig.tridentShockwaveKnockback + 2 * NTConfig.tridentShockwaveKnockbackPerLevel);
            if (TidalShockwave.damage(level, plain) != (float) NTConfig.tridentShockwaveDamage
                    || Math.abs(TidalShockwave.damage(level, enchanted) - damage) > 0.001F) {
                helper.fail("Shockwave damage is " + TidalShockwave.damage(level, plain) + " plain and "
                        + TidalShockwave.damage(level, enchanted) + " with Sharpness III, expected " + damage);
                return;
            }
            if (TidalShockwave.knockback(level, plain) != (float) NTConfig.tridentShockwaveKnockback
                    || Math.abs(TidalShockwave.knockback(level, enchanted) - knockback) > 0.001F) {
                helper.fail("Shockwave knockback is " + TidalShockwave.knockback(level, plain) + " plain and "
                        + TidalShockwave.knockback(level, enchanted) + " with Knockback II, expected " + knockback);
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/shockwave_hurts_mobs_and_spares_pets_and_team", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            Vec3 center = helper.absoluteVec(CENTER);
            Player thrower = thrower(helper, ItemStack.EMPTY);
            IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(4, 1, 7));
            Wolf pet = helper.spawnWithNoFreeWill(EntityType.WOLF, new BlockPos(2, 1, 4));
            pet.tame(thrower);
            Zombie teammate = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(6, 1, 4));
            Scoreboard scoreboard = level.getScoreboard();
            PlayerTeam existing = scoreboard.getPlayerTeam(TEAM);
            PlayerTeam team = existing != null ? existing : scoreboard.addPlayerTeam(TEAM);
            scoreboard.addPlayerToTeam(thrower.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(teammate.getScoreboardName(), team);
            float golemBefore = golem.getHealth();
            float petBefore = pet.getHealth();
            float teammateBefore = teammate.getHealth();
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());

            try {
                TidalShockwave.release(level, source(level, center, stack), thrower, stack, center);
            } finally {
                scoreboard.removePlayerTeam(team);
            }

            double distance = Math.sqrt(golem.getBoundingBox().distanceToSqr(center));
            float expected = TidalShockwave.damage(level, stack) * TidalShockwave.falloff(distance, NTConfig.tridentShockwaveRadius);
            if (Math.abs(golemBefore - golem.getHealth() - expected) > 0.01F) {
                helper.fail("The golem " + String.format("%.2f", distance) + " blocks out lost " + (golemBefore - golem.getHealth())
                        + " health, expected " + expected);
                return;
            }
            if (pet.getHealth() != petBefore) {
                helper.fail("The thrower's own wolf took " + (petBefore - pet.getHealth()) + " shockwave damage");
                return;
            }
            if (teammate.getHealth() != teammateBefore) {
                helper.fail("A mob on the thrower's team took " + (teammateBefore - teammate.getHealth()) + " shockwave damage");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/shockwave_knocks_mobs_away_from_the_impact", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            Vec3 center = helper.absoluteVec(CENTER);
            Zombie east = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 1, 4));
            Zombie west = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 4));
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());

            TidalShockwave.release(level, source(level, center, stack), null, stack, center);

            if (east.getDeltaMovement().x <= 0.0 || west.getDeltaMovement().x >= 0.0) {
                helper.fail("The shockwave pushed the east zombie by " + east.getDeltaMovement()
                        + " and the west zombie by " + west.getDeltaMovement() + " instead of away from the impact");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/shockwave_waits_for_its_cooldown", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            Vec3 center = helper.absoluteVec(CENTER);
            ItemStack stack = new ItemStack(NTItems.NEPTUNES_TRIDENT.get());
            long now = level.getGameTime();
            if (!TidalShockwave.isReady(stack, now)) {
                helper.fail("A fresh Neptune's Trident is not ready to release a shockwave");
                return;
            }

            TidalShockwave.release(level, source(level, center, stack), null, stack, center);

            ShockwaveCooldown cooldown = stack.get(NTDataComponents.SHOCKWAVE_COOLDOWN.get());
            if (cooldown == null || cooldown.readyAt() != now + NTConfig.tridentShockwaveCooldown
                    || TidalShockwave.isReady(stack, now) || !TidalShockwave.isReady(stack, now + NTConfig.tridentShockwaveCooldown)) {
                helper.fail("After a shockwave the trident carries " + cooldown + ", expected ready at "
                        + (now + NTConfig.tridentShockwaveCooldown));
                return;
            }

            stack.set(NTDataComponents.SHOCKWAVE_COOLDOWN.get(), new ShockwaveCooldown(now - 1, NTConfig.tridentShockwaveCooldown));
            NTItems.NEPTUNES_TRIDENT.get().inventoryTick(stack, level, thrower(helper, ItemStack.EMPTY), 0, false);
            if (stack.has(NTDataComponents.SHOCKWAVE_COOLDOWN.get())) {
                helper.fail("An expired shockwave cooldown was left on the trident");
                return;
            }
            helper.succeed();
        });

        r.add("neptunes_trident/crafted_from_pressure_forged_parts", 20, 1, helper -> {
            RecipeHolder<?> holder = helper.getLevel().getRecipeManager()
                    .byKey(Nautec.rl("neptunes_trident"))
                    .orElse(null);
            if (holder == null || !(holder.value() instanceof ShapedRecipe recipe)) {
                helper.fail("nautec:neptunes_trident is not a loaded shaped recipe: " + holder);
                return;
            }

            ItemStack empty = ItemStack.EMPTY;
            ItemStack crystal = new ItemStack(NTItems.FLAWLESS_PRISMARINE_CRYSTAL.get());
            ItemStack shard = new ItemStack(NTItems.RESONANT_SHARD.get());
            ItemStack heart = new ItemStack(Items.HEART_OF_THE_SEA);
            ItemStack plating = new ItemStack(NTItems.DEEP_STEEL_PLATING.get());
            CraftingInput grid = CraftingInput.of(3, 3, List.of(
                    shard, crystal, shard,
                    plating, heart, plating,
                    empty, plating, empty));
            if (!recipe.matches(grid, helper.getLevel())) {
                helper.fail("The trident recipe does not match a crystal, two shards, a Heart of the Sea and three platings");
                return;
            }
            ItemStack result = recipe.assemble(grid, helper.getLevel().registryAccess());
            if (!result.is(NTItems.NEPTUNES_TRIDENT.get()) || result.getCount() != 1) {
                helper.fail("Crafting produced " + result + " instead of one Neptune's Trident");
                return;
            }
            helper.succeed();
        });
    }

    private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static ThrownNeptunesTrident source(ServerLevel level, Vec3 center, ItemStack stack) {
        return new ThrownNeptunesTrident(level, center.x, center.y, center.z, stack.copy());
    }

    private static Player thrower(NTGameTestHelper helper, ItemStack stack) {
        Player thrower = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(4.5D, 1D, 1.5D));
        thrower.setPos(at.x, at.y, at.z);
        thrower.setYRot(0.0F);
        thrower.setXRot(0.0F);
        thrower.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return thrower;
    }

    private NeptunesTridentTests() {
    }
}
