package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.entities.submarine.SubmarineModules;
import com.breakinblocks.nautec.content.fishing.FishingMinigame;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.content.fishing.MinigameKind;
import com.breakinblocks.nautec.content.recipes.ItemEtchingRecipe;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.TeleportAnchor;
import com.breakinblocks.nautec.events.helper.ItemEtching;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NavalFishingFixTests {
    private static final BlockPos SUB_POS = new BlockPos(4, 2, 4);

    private NavalFishingFixTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("naval_fix/duplicate_modules_share_a_cooldown", 40, helper -> helper.runAfterDelay(1, () -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(NTConfig.submarinePowerCapacity);
            submarine.setModule(0, new ItemStack(NTItems.SHIELD_MODULE.get()));
            submarine.setModule(3, new ItemStack(NTItems.SHIELD_MODULE.get()));

            SubmarineModules modules = submarine.getModules();
            Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);

            modules.activate(0, pilot);
            int afterFirst = NTConfig.submarinePowerCapacity - NTConfig.submarineShieldPowerCost;
            helper.assertValueEqual(afterFirst, submarine.getPowerStored(), "power after the first shield discharge");
            helper.assertFalse(modules.isReady(3), "the second shield module should share the first one's cooldown");
            helper.assertValueEqual(NTConfig.submarineShieldCooldownTicks, modules.remainingCooldown(3),
                    "cooldown shown on the second shield slot");

            modules.activate(3, pilot);
            helper.assertValueEqual(afterFirst, submarine.getPowerStored(),
                    "a second shield module fired straight after the first");
            helper.succeed();
        }));

        r.add("naval_fix/blocked_teleport_refunds_power_and_cooldown", SubmarineModules.TELEPORT_CHARGE_TICKS + 40, helper -> {
            SubmarineEntity submarine = spawnSubmarine(helper);
            submarine.setPowerStored(NTConfig.submarinePowerCapacity);
            SubmarineModules modules = submarine.getModules();
            Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);

            BlockPos target = new BlockPos(4, 5, 4);
            helper.setBlock(target, Blocks.WATER.defaultBlockState());
            ItemStack bound = new ItemStack(NTItems.TELEPORT_MODULE.get());
            bound.set(NTDataComponents.TELEPORT_ANCHOR,
                    new TeleportAnchor(GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(target)), 0F));
            submarine.setModule(0, bound);

            modules.activate(0, pilot);
            helper.assertTrue(submarine.isCharging(), "a bound anchor in water should start the jump");
            helper.assertValueEqual(NTConfig.submarinePowerCapacity - NTConfig.submarineTeleportPowerCost,
                    submarine.getPowerStored(), "power taken when the charge starts");
            helper.assertFalse(modules.isReady(0), "the module should be cooling while it charges");

            helper.setBlock(target, Blocks.STONE.defaultBlockState());

            helper.runAfterDelay(SubmarineModules.TELEPORT_CHARGE_TICKS + 5, () -> {
                helper.assertFalse(submarine.isCharging(), "the charge should have ended");
                helper.assertFalse(submarine.blockPosition().equals(helper.absolutePos(target)),
                        "the submarine jumped to a blocked anchor");
                helper.assertValueEqual(NTConfig.submarinePowerCapacity, submarine.getPowerStored(),
                        "power after a teleport that did not happen");
                helper.assertTrue(modules.isReady(0), "a teleport that did not happen should not start the cooldown");
                helper.succeed();
            });
        });

        r.add("naval_fix/minigame_win_keeps_the_fish_on_the_line", 40, 1, helper -> {
            BlockPos surface = pool(helper);
            NautecFishingHook hook = cast(helper, surface);
            FishingHookAccessor accessor = (FishingHookAccessor) hook;
            if (!forceBite(hook, accessor)) {
                helper.fail("The hook never got a bite");
                return;
            }
            int start = hook.tickCount;
            drain(helper);

            int during = probe(hook, accessor, start, 45);
            if (during < 2) {
                helper.fail("The bite was let go 45 ticks in, while the " + FishingMinigame.DURATION_TICKS
                        + " tick minigame was still open (nibble " + during + ")");
                return;
            }

            hook.tickCount = start + 70;
            hook.onMinigameReport(hook.minigameNonce(), winningReport(hook.minigameKind(), hook.minigameSeed()));
            if (!hook.minigameSucceeded()) {
                helper.fail("A winning report was not accepted");
                return;
            }

            int afterWin = probe(hook, accessor, start, 110);
            if (hook.isRemoved() || afterWin < 2) {
                helper.fail("After a late win the fish was let go before the player could reel it in (nibble " + afterWin + ")");
                return;
            }

            int released = probe(hook, accessor, start,
                    70 + NautecFishingHook.WIN_SCREEN_TICKS + NautecFishingHook.REEL_WINDOW_TICKS + 5);
            if (released != 1) {
                helper.fail("The bite was still being held after the reel window closed (nibble " + released + ")");
                return;
            }

            accessor.nautec$setNibble(0);
            hook.tick();
            if (hook.minigameSucceeded()) {
                helper.fail("A win carried over past the bite it was won on");
                return;
            }
            hook.discard();
            helper.succeed();
        });

        r.add("naval_fix/minigame_miss_still_leaves_time_to_reel", 40, 1, helper -> {
            BlockPos surface = pool(helper);
            NautecFishingHook hook = cast(helper, surface);
            FishingHookAccessor accessor = (FishingHookAccessor) hook;
            if (!forceBite(hook, accessor)) {
                helper.fail("The hook never got a bite");
                return;
            }
            int start = hook.tickCount;
            drain(helper);

            hook.tickCount = start + 20;
            hook.onMinigameReport(hook.minigameNonce(), List.of());
            if (hook.minigameSucceeded()) {
                helper.fail("An empty report counted as a win");
                return;
            }

            int afterBar = probe(hook, accessor, start, FishingMinigame.DURATION_TICKS + 10);
            if (afterBar < 2) {
                helper.fail("After a miss the bite was let go by the time the bar closed (nibble " + afterBar + ")");
                return;
            }

            int released = probe(hook, accessor, start, FishingMinigame.DURATION_TICKS + NautecFishingHook.REEL_WINDOW_TICKS + 5);
            if (released != 1) {
                helper.fail("After a miss the bite was still being held after the reel window closed (nibble " + released + ")");
                return;
            }
            hook.discard();
            helper.succeed();
        });

        r.add("naval_fix/rod_bonus_releases_live_catches", 40, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos surface = pool(helper);
            NautecFishingHook hook = cast(helper, surface);
            Player owner = hook.getPlayerOwner();
            if (owner == null) {
                helper.fail("The hook lost its owner");
                return;
            }

            ItemStack live = new ItemStack(Items.COD);
            live.set(NTDataComponents.CATCH_ENTITY.get(), EntityType.COD);
            AABB area = hook.getBoundingBox().inflate(8.0);
            int codBefore = level.getEntities(EntityType.COD, area, e -> true).size();
            int itemsBefore = level.getEntitiesOfClass(ItemEntity.class, area).size();

            NautecFishingHook.deliverCatch(level, owner, hook, List.of(live, new ItemStack(Items.SALMON)),
                    hook.getX(), hook.getY(), hook.getZ());

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
            for (ItemEntity item : items) {
                if (item.getItem().has(NTDataComponents.CATCH_ENTITY.get())) {
                    helper.fail("A live catch was dropped as an item");
                    return;
                }
            }
            helper.assertValueEqual(itemsBefore + 1, items.size(), "item drops from the bonus");
            helper.assertValueEqual(codBefore + 1, level.getEntities(EntityType.COD, area, e -> true).size(),
                    "live cod released by the bonus");
            hook.discard();
            helper.succeed();
        });

        r.add("naval_fix/other_rod_zone_bonus_releases_live_catches", 40, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos surface = pool(helper);
            BlockPos zonePos = surface.above();
            level.setBlock(zonePos, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
            if (level.getBlockEntity(zonePos) instanceof LuckyFishingZoneBlockEntity zone) {
                zone.setRadius(3);
            }
            LuckyZoneIndex index = LuckyZoneIndex.get(level);
            index.add(new LuckyZoneIndex.Zone(zonePos, 3, level.getGameTime() + 100000L));

            Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
            owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 3.5);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));

            FishingHook hook = new FishingHook(owner, level, 0, 0);
            hook.snapTo(surface.getX() + 0.5, surface.getY() + 0.4, surface.getZ() + 0.5);
            hook.setDeltaMovement(Vec3.ZERO);

            AABB area = new AABB(zonePos).inflate(8);
            int livingBefore = level.getEntitiesOfClass(LivingEntity.class, area).size();
            boolean consumed = NTConfig.luckyZoneConsumedOnCatch;
            try {
                NTConfig.luckyZoneConsumedOnCatch = false;
                for (int i = 0; i < 150; i++) {
                    NeoForge.EVENT_BUS.post(new ItemFishedEvent(List.of(new ItemStack(Items.COD)), 0, hook));
                }
            } finally {
                NTConfig.luckyZoneConsumedOnCatch = consumed;
                index.remove(zonePos);
                hook.discard();
            }

            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
                if (item.getItem().has(NTDataComponents.CATCH_ENTITY.get())) {
                    helper.fail("The lucky zone bonus for another mod's rod dropped a live catch as an item: " + item.getItem());
                    return;
                }
            }
            if (level.getEntitiesOfClass(LivingEntity.class, area).size() <= livingBefore) {
                helper.fail("150 lucky zone bonus rolls released no live catch at all");
                return;
            }
            helper.succeed();
        });

        r.add("naval_fix/etching_uses_the_recipe_duration", 40, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos acid = acidPit(helper);
            Optional<ItemEtchingRecipe> recipe = gearRecipe(level);
            if (recipe.isEmpty()) {
                helper.fail("No etching recipe for a rusty gear");
                return;
            }
            int duration = recipe.get().duration();
            helper.assertTrue(duration > 110, "the gear recipe should take longer than the old fixed 100 ticks");

            ItemEntity item = restingItem(level, acid);
            for (int i = 0; i < 110; i++) {
                ItemEtching.processItemEtching(item, level);
            }
            if (item.isRemoved()) {
                helper.fail("The gear finished etching after 110 ticks, but its recipe takes " + duration);
                return;
            }

            for (int i = 110; i < duration + 5 && !item.isRemoved(); i++) {
                ItemEtching.processItemEtching(item, level);
            }
            if (!item.isRemoved()) {
                helper.fail("The gear had not finished etching after " + (duration + 5) + " ticks");
                return;
            }
            boolean gear = level.getEntitiesOfClass(ItemEntity.class, new AABB(acid).inflate(3)).stream()
                    .anyMatch(entity -> entity.getItem().is(NTItems.GEAR.get()));
            helper.assertTrue(gear, "no cleaned gear came out of the acid");
            helper.succeed();
        });

        r.add("naval_fix/etching_consumes_the_acid_not_the_floor", 40, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos acid = acidPit(helper);
            BlockPos floor = acid.below();
            Optional<ItemEtchingRecipe> recipe = gearRecipe(level);
            if (recipe.isEmpty()) {
                helper.fail("No etching recipe for a rusty gear");
                return;
            }

            ItemEntity item = restingItem(level, acid);
            helper.assertValueEqual(floor, item.getOnPos(), "the resting item should report the floor as its on-pos");

            ItemEtching.transformItem(item, recipe.get(), level, true);

            helper.assertTrue(level.getBlockState(floor).is(Blocks.STONE), "etching removed the floor under the acid");
            helper.assertTrue(level.getBlockState(acid).isAir(), "etching did not use up the acid the item was in");
            helper.succeed();
        });
    }

    private static SubmarineEntity spawnSubmarine(GameTestHelper helper) {
        SubmarineEntity submarine = helper.spawn(NTEntities.SUBMARINE.get(), SUB_POS);
        helper.assertTrue(submarine != null, "the submarine failed to spawn");
        return submarine;
    }

    private static boolean forceBite(NautecFishingHook hook, FishingHookAccessor accessor) {
        for (int i = 0; i < 400 && accessor.nautec$getNibble() <= 0; i++) {
            if (accessor.nautec$getTimeUntilLured() > 1) {
                accessor.nautec$setTimeUntilLured(1);
            }
            if (accessor.nautec$getTimeUntilHooked() > 1) {
                accessor.nautec$setTimeUntilHooked(1);
            }
            hook.tickCount++;
            hook.tick();
        }
        return !hook.isRemoved() && accessor.nautec$getNibble() > 0;
    }

    private static int probe(NautecFishingHook hook, FishingHookAccessor accessor, int start, int elapsed) {
        hook.tickCount = start + elapsed;
        accessor.nautec$setNibble(1);
        hook.tick();
        return accessor.nautec$getNibble();
    }

    private static void drain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlockAndUpdate(centre.offset(dx, -1, dz), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static List<Integer> winningReport(MinigameKind kind, long seed) {
        List<int[]> windows = kind.windows(seed);
        if (kind == MinigameKind.HOLD) {
            int[] window = windows.getFirst();
            return List.of(window[0], window[0] + window[1] - 1);
        }
        List<Integer> ticks = new ArrayList<>();
        for (int[] window : windows) {
            ticks.add(window[0]);
        }
        return ticks;
    }

    private static NautecFishingHook cast(GameTestHelper helper, BlockPos surface) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 3.5);
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.NAUTEC_FISHING_ROD.get()));

        NautecFishingHook hook = new NautecFishingHook(owner, level, 0, 0);
        hook.snapTo(surface.getX() + 0.5, surface.getY() + 0.4, surface.getZ() + 0.5);
        hook.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(hook);
        return hook;
    }

    private static BlockPos pool(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setBlockAndUpdate(centre.offset(dx, -1, dz), Blocks.WATER.defaultBlockState());
                level.setBlockAndUpdate(centre.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
            }
        }
        return centre.below();
    }

    private static BlockPos acidPit(GameTestHelper helper) {
        BlockPos acid = new BlockPos(4, 2, 4);
        helper.setBlock(acid.below(), Blocks.STONE.defaultBlockState());
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            helper.setBlock(acid.relative(direction), Blocks.STONE.defaultBlockState());
        }
        helper.setBlock(acid, NTFluids.ETCHING_ACID.getStillFluid().defaultFluidState().createLegacyBlock());
        return helper.absolutePos(acid);
    }

    private static ItemEntity restingItem(ServerLevel level, BlockPos acid) {
        ItemEntity item = new ItemEntity(level, acid.getX() + 0.5, acid.getY(), acid.getZ() + 0.5,
                new ItemStack(NTItems.RUSTY_GEAR.get()));
        item.setDeltaMovement(Vec3.ZERO);
        return item;
    }

    private static Optional<ItemEtchingRecipe> gearRecipe(ServerLevel level) {
        return level.recipeAccess()
                .getRecipeFor(ItemEtchingRecipe.Type.INSTANCE, new SingleRecipeInput(new ItemStack(NTItems.RUSTY_GEAR.get())), level)
                .map(RecipeHolder::value);
    }
}
