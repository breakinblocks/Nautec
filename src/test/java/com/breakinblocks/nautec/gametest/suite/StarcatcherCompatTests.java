package com.breakinblocks.nautec.gametest.suite;

import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import java.util.UUID;
import net.minecraft.server.level.ClientInformation;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.LuckyFishingZoneBlockEntity;
import com.breakinblocks.nautec.content.entities.NautecFishingHook;
import com.breakinblocks.nautec.content.fishing.LuckyZoneIndex;
import com.breakinblocks.nautec.mixin.FishingHookAccessor;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public final class StarcatcherCompatTests {
    public static void register(NTTestRegistrar r) {
        r.add("starcatcher/nautec_rod_catches_starcatcher_fish", 40, 1, StarcatcherCompatTests::nautecRodCatch);
        r.add("starcatcher/bobber_bites_faster_in_lucky_zone", 120, 1, StarcatcherCompatTests::bobberInZone);
        r.add("starcatcher/won_catch_in_lucky_zone_gets_winning_rolls", 40, 1, StarcatcherCompatTests::wonCatchInZone);
    }

    private static void nautecRodCatch(GameTestHelper helper) {
        if (!ModList.get().isLoaded("starcatcher")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos surface = pool(helper);
        Holder<Biome> reef = level.registryAccess().lookupOrThrow(Registries.BIOME)
                .getOrThrow(ResourceKey.create(Registries.BIOME, Nautec.rl("prismarine_reef")));
        FillBiomeCommand.fill(level, surface.offset(-3, -2, -3), surface.offset(3, 3, 3), reef);

        ServerPlayer owner = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "starcatcher_angler"),
                ClientInformation.createDefault());
        owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 3.5);
        ItemStack rod = new ItemStack(NTItems.NAUTEC_FISHING_ROD.get());
        owner.setItemInHand(InteractionHand.MAIN_HAND, rod);

        NautecFishingHook hook = new NautecFishingHook(owner, level, 0, 0);
        hook.snapTo(surface.getX() + 0.5, surface.getY() + 0.4, surface.getZ() + 0.5);
        hook.setDeltaMovement(Vec3.ZERO);
        level.addFreshEntity(hook);
        ((FishingHookAccessor) hook).nautec$setNibble(20);

        try {
            hook.retrieve(rod);
        } catch (RuntimeException e) {
            hook.discard();
            if (Arrays.stream(e.getStackTrace()).anyMatch(frame -> frame.getClassName().endsWith("StarcatcherCatches"))) {
                helper.succeed();
                return;
            }
            throw e;
        }
        hook.discard();
        helper.fail("The NauTec rod reeled in a bite in a Prismarine Reef without picking a Starcatcher fish");
    }

    private static void bobberInZone(GameTestHelper helper) {
        if (!ModList.get().isLoaded("starcatcher")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos surface = pool(helper);
        BlockPos zonePos = surface.above();
        level.setBlock(zonePos, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(zonePos) instanceof LuckyFishingZoneBlockEntity zone) {
            zone.setRadius(1);
        }
        LuckyZoneIndex.get(level).add(new LuckyZoneIndex.Zone(zonePos, 1, level.getGameTime() + 100000L));

        Entity inZone = bobber(helper, surface);
        Entity outside = bobber(helper, surface.offset(3, 0, 3));

        helper.runAfterDelay(40, () -> {
            int inside = StarcatcherTestBobs.ticksInFluid(inZone);
            int plain = StarcatcherTestBobs.ticksInFluid(outside);
            inZone.discard();
            outside.discard();
            if (inside <= plain) {
                helper.fail("A Starcatcher bobber in a lucky zone counted " + inside
                        + " ticks towards a bite, no more than the " + plain + " of one outside it");
                return;
            }
            helper.succeed();
        });
    }

    private static void wonCatchInZone(GameTestHelper helper) {
        if (!ModList.get().isLoaded("starcatcher")) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        BlockPos surface = pool(helper);
        BlockPos zonePos = surface.above();
        level.setBlock(zonePos, NTBlocks.LUCKY_FISHING_ZONE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(zonePos) instanceof LuckyFishingZoneBlockEntity zone) {
            zone.setRadius(3);
        }
        LuckyZoneIndex.get(level).add(new LuckyZoneIndex.Zone(zonePos, 3, level.getGameTime() + 100000L));

        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 3.5);
        Entity bob = StarcatcherTestBobs.cast(level, owner);

        FishingHook fakeHook = new FishingHook(owner, level, 0, 0);
        fakeHook.setPos(surface.getX() + 0.5, surface.getY() + 0.4, surface.getZ() + 0.5);

        AABB area = new AABB(zonePos).inflate(6);
        List<Entity> before = level.getEntities((Entity) null, area, StarcatcherCompatTests::isCatch);
        NeoForge.EVENT_BUS.post(new ItemFishedEvent(List.of(new ItemStack(Items.COD)), 0, fakeHook));
        List<Entity> caught = level.getEntities((Entity) null, area, entity -> isCatch(entity) && !before.contains(entity));
        caught.forEach(Entity::discard);
        fakeHook.discard();
        bob.discard();

        if (caught.size() < 2) {
            helper.fail("A won Starcatcher catch in a lucky zone got " + caught.size()
                    + " NauTec catches, not the two or three a won NauTec minigame gives");
            return;
        }
        helper.succeed();
    }

    private static boolean isCatch(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return !BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).getNamespace().equals("starcatcher");
        }
        return !(entity instanceof Player);
    }

    private static Entity bobber(GameTestHelper helper, BlockPos surface) {
        ServerLevel level = helper.getLevel();
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        owner.setPos(surface.getX() + 0.5, surface.getY() + 1.0, surface.getZ() + 2.5);
        Entity bob = StarcatcherTestBobs.cast(level, owner);
        bob.snapTo(surface.getX() + 0.5, surface.getY() + 0.6, surface.getZ() + 0.5);
        bob.setDeltaMovement(new Vec3(0.0, -0.1, 0.0));
        level.addFreshEntity(bob);
        return bob;
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

    private StarcatcherCompatTests() {
    }
}
