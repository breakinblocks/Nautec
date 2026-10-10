package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.content.items.WaveJetHands;
import com.breakinblocks.nautec.content.items.WaveJetSpotlight;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.AABB;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class WaveJetTests {
    public static void register(NTTestRegistrar r) {
        r.add("wave_jet/spotlight_lights_the_block_it_lands_on", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.AIR.defaultBlockState());
            Player holder = aiming(helper, floor);

            WaveJetSpotlight.aim(level, holder);

            BlockPos lit = floor.above();
            if (!level.getBlockState(lit).is(Blocks.LIGHT)) {
                helper.fail("Nothing lit above the floor, found " + level.getBlockState(lit));
                return;
            }
            if (level.getBlockState(lit).getValue(LightBlock.WATERLOGGED)) {
                helper.fail("The light placed in air came out waterlogged");
                return;
            }

            WaveJetSpotlight.extinguish(holder);
            if (!level.getBlockState(lit).isAir()) {
                helper.fail("Extinguishing left " + level.getBlockState(lit) + " behind instead of air");
            }
            helper.succeed();
        });

        r.add("wave_jet/spotlight_gives_water_back_when_it_moves_on", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.WATER.defaultBlockState());
            Player holder = aiming(helper, floor);

            WaveJetSpotlight.aim(level, holder);

            BlockPos lit = floor.above();
            BlockState state = level.getBlockState(lit);
            if (!state.is(Blocks.LIGHT)) {
                helper.fail("Nothing lit under water, found " + state);
                return;
            }
            if (!state.getValue(LightBlock.WATERLOGGED)) {
                helper.fail("The light placed in water is not waterlogged, so putting it out would drain the block");
                return;
            }

            WaveJetSpotlight.extinguish(holder);
            if (!level.getBlockState(lit).is(Blocks.WATER)) {
                helper.fail("Extinguishing under water left " + level.getBlockState(lit) + " instead of water");
            }
            helper.succeed();
        });

        r.add("wave_jet/spotlight_never_eats_a_real_block", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.AIR.defaultBlockState());
            BlockPos lit = floor.above();
            level.setBlockAndUpdate(lit, Blocks.GOLD_BLOCK.defaultBlockState());

            Player holder = aiming(helper, floor);
            WaveJetSpotlight.aim(level, holder);

            if (!level.getBlockState(lit).is(Blocks.GOLD_BLOCK)) {
                helper.fail("The spotlight replaced a solid block with " + level.getBlockState(lit));
                return;
            }
            WaveJetSpotlight.extinguish(holder);
            helper.succeed();
        });

        r.add("wave_jet/overlapping_lights_keep_water_until_last_holder", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.WATER.defaultBlockState());
            Player first = aiming(helper, floor);
            Player second = aiming(helper, floor);
            second.setUUID(UUID.randomUUID());
            WaveJetSpotlight.aim(level, first);
            WaveJetSpotlight.aim(level, second);
            helper.assertTrue(level.getBlockState(floor.above()).getValue(LightBlock.WATERLOGGED), "Overlapping light lost water");
            WaveJetSpotlight.extinguish(first);
            helper.assertTrue(level.getBlockState(floor.above()).is(Blocks.LIGHT), "First holder removed the second holder's light");
            WaveJetSpotlight.extinguish(second);
            helper.assertTrue(level.getBlockState(floor.above()).is(Blocks.WATER), "Final holder did not restore water");
            helper.succeed();
        });
        r.add("wave_jet/preexisting_light_is_not_owned", 20, 1, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos floor = shaft(helper, Blocks.AIR.defaultBlockState());
            BlockState original = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 7);
            level.setBlockAndUpdate(floor.above(), original);
            Player holder = aiming(helper, floor);
            WaveJetSpotlight.aim(level, holder);
            WaveJetSpotlight.extinguish(holder);
            helper.assertValueEqual(original, level.getBlockState(floor.above()), "Map light changed");
            helper.succeed();
        });

        registerHands(r);
        registerBreath(r);
    }

    private static void registerBreath(NTTestRegistrar r) {
        r.add("wave_jet/thrusting_holds_the_air_you_have", 20, helper -> {
            Player player = thrusting(helper, 0.0);
            int before = player.getMaxAirSupply() / 2;
            player.setAirSupply(before);

            for (int tick = 0; tick < 40; tick++) {
                player.tick();
            }

            helper.assertTrue(player.isUsingItem(), "The Wave Jet stopped thrusting inside the tank");
            helper.assertValueEqual(before, player.getAirSupply(), "Air after 40 ticks of thrust");
            helper.succeed();
        });

        r.add("wave_jet/respiration_does_not_refill_while_thrusting", 20, helper -> {
            Player player = thrusting(helper, 3.0);
            int before = player.getMaxAirSupply() / 2;
            player.setAirSupply(before);

            int highest = before;
            for (int tick = 0; tick < 200; tick++) {
                player.tick();
                highest = Math.max(highest, player.getAirSupply());
            }

            helper.assertTrue(player.isUsingItem(), "The Wave Jet stopped thrusting inside the tank");
            helper.assertValueEqual(before, highest, "Highest air while thrusting with Respiration III");
            helper.assertValueEqual(before, player.getAirSupply(), "Air after 200 ticks of thrust with Respiration III");
            helper.succeed();
        });

        r.add("wave_jet/thrusting_does_not_rescue_you_from_empty", 20, helper -> {
            Player player = thrusting(helper, 0.0);
            player.setAirSupply(0);

            for (int tick = 0; tick < 10; tick++) {
                player.tick();
            }

            helper.assertTrue(player.isUsingItem(), "The Wave Jet stopped thrusting inside the tank");
            helper.assertTrue(player.getAirSupply() < 0,
                    "An empty bar stayed at " + player.getAirSupply() + ", which would stop you drowning for free");
            helper.succeed();
        });

        r.add("wave_jet/riding_a_submarine_does_not_thrust", 20, helper -> {
            tank(helper);
            SubmarineEntity submarine = helper.spawn(NTEntities.SUBMARINE.get(), new BlockPos(4, 2, 4));
            Player pilot = helper.makeMockPlayer(GameType.SURVIVAL);
            pilot.moveTo(submarine.getX(), submarine.getY(), submarine.getZ(), 0.0F, 0.0F);
            Player passenger = diver(helper, 0.0);
            try {
                helper.assertTrue(pilot.startRiding(submarine), "The pilot was refused a seat");
                helper.assertTrue(passenger.startRiding(submarine), "The passenger was refused a seat");
                helper.assertTrue(submarine.getControllingPassenger() == pilot,
                        "The Wave Jet holder took the pilot's seat instead of the rear seat");

                passenger.tick();
                helper.assertTrue(passenger.isInWater(), "The seated passenger should be under water");

                ItemStack stack = passenger.getItemInHand(InteractionHand.MAIN_HAND);
                int power = stack.getOrDefault(NTDataComponents.POWER, ComponentPowerStorage.EMPTY).powerStored();
                stack.use(helper.getLevel(), passenger, InteractionHand.MAIN_HAND);
                for (int tick = 0; tick < 5; tick++) {
                    passenger.tick();
                }

                helper.assertFalse(passenger.isUsingItem(), "The rear seat passenger started thrusting");
                helper.assertFalse(passenger.getPose() == Pose.SWIMMING, "The rear seat passenger was laid flat in a swimming pose");
                helper.assertValueEqual(power, stack.getOrDefault(NTDataComponents.POWER, ComponentPowerStorage.EMPTY).powerStored(),
                        "Wave Jet power after using it from a seat");
                helper.succeed();
            } finally {
                passenger.stopRiding();
                pilot.stopRiding();
                submarine.discard();
            }
        });

        r.add("wave_jet/thrust_outpaces_swimming", 20, helper -> {
            Player player = lane(helper);
            for (int tick = 0; tick < 10; tick++) {
                player.tick();
            }

            double speed = player.getDeltaMovement().horizontalDistance();
            helper.assertTrue(player.isUsingItem(), "The Wave Jet stopped thrusting inside the tank");
            helper.assertTrue(speed >= 0.25, "After 10 ticks of thrust the Wave Jet was doing " + speed
                    + " blocks per tick, no faster than sprint swimming at about 0.18");
            helper.succeed();
        });

        r.add("wave_jet/skims_along_the_surface", 20, helper -> {
            for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
                boolean wall = pos.getX() == 0 || pos.getX() == 8 || pos.getY() == 0 || pos.getY() == 8
                        || pos.getZ() == 0 || pos.getZ() == 8;
                helper.setBlock(pos, wall ? Blocks.GLASS : pos.getY() <= 5 ? Blocks.WATER : Blocks.AIR);
            }
            Player player = diver(helper, 0.0);
            Vec3 start = helper.absoluteVec(new Vec3(1.3, 3.0, 4.5));
            Vec3 end = helper.absoluteVec(new Vec3(7.5, 3.0, 4.5));
            float yaw = (float) Mth.atan2(end.z - start.z, end.x - start.x) * Mth.RAD_TO_DEG - 90.0F;
            player.moveTo(start.x, start.y, start.z, yaw, -30.0F);
            player.startUsingItem(InteractionHand.MAIN_HAND);
            double surface = helper.absoluteVec(new Vec3(0.0, 6.0, 0.0)).y;

            boolean surfaced = false;
            for (int tick = 0; tick < 200; tick++) {
                player.tick();
                surfaced |= !player.isUnderWater();
                helper.assertTrue(player.isInWater(), "Thrusting up at the surface left the water on tick " + tick
                        + ", feet at " + String.format("%.2f", player.getY()) + " with the surface at " + surface);
                helper.assertTrue(player.isUsingItem(), "The Wave Jet cut out at the surface on tick " + tick);
            }
            helper.assertTrue(surfaced, "The player never reached the surface");
            helper.succeed();
        });

        r.add("wave_jet/dolphins_grace_stays_under_the_cap", 20, helper -> {
            Player player = lane(helper);
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 1));
            player.setDeltaMovement(player.getLookAngle().scale(0.35));

            double fastest = 0.0;
            for (int tick = 0; tick < 12; tick++) {
                player.tick();
                fastest = Math.max(fastest, player.getDeltaMovement().length());
            }

            helper.assertTrue(player.isUsingItem(), "The Wave Jet stopped thrusting inside the tank");
            helper.assertTrue(fastest <= NTConfig.waveJetMaxSpeed, "With Dolphin's Grace the Wave Jet reached " + fastest
                    + " blocks per tick, over the " + NTConfig.waveJetMaxSpeed + " cap");
            helper.succeed();
        });
    }

    private static Player lane(NTGameTestHelper helper) {
        tank(helper);
        Player player = diver(helper, 0.0);
        Vec3 start = helper.absoluteVec(new Vec3(1.3, 2.0, 4.5));
        Vec3 end = helper.absoluteVec(new Vec3(7.5, 2.0, 4.5));
        float yaw = (float) Mth.atan2(end.z - start.z, end.x - start.x) * Mth.RAD_TO_DEG - 90.0F;
        player.moveTo(start.x, start.y, start.z, yaw, 0.0F);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        return player;
    }

    private static Player thrusting(NTGameTestHelper helper, double oxygenBonus) {
        tank(helper);
        Player player = diver(helper, oxygenBonus);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        return player;
    }

    private static void tank(NTGameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(0, 0, 0, 8, 8, 8)) {
            boolean wall = pos.getX() == 0 || pos.getX() == 8 || pos.getY() == 0 || pos.getY() == 8
                    || pos.getZ() == 0 || pos.getZ() == 8;
            helper.setBlock(pos, wall ? Blocks.GLASS : Blocks.WATER);
        }
    }

    private static Player diver(NTGameTestHelper helper, double oxygenBonus) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos centre = helper.absolutePos(new BlockPos(4, 1, 4));
        player.moveTo(centre.getX() + 0.5, centre.getY(), centre.getZ() + 0.5, 0.0F, 90.0F);
        player.getAttribute(Attributes.OXYGEN_BONUS).setBaseValue(oxygenBonus);

        ItemStack stack = new ItemStack(NTItems.WAVE_JET.get());
        stack.set(NTDataComponents.POWER, new ComponentPowerStorage(6000, 6000, 0F));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return player;
    }

    private static void registerHands(NTTestRegistrar r) {
        r.add("wave_jet/a_full_inventory_drops_it", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
            player.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
            for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));

            helper.assertTrue(WaveJetHands.enforce(player), "A torch in the offhand did not displace the Wave Jet");
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                helper.assertFalse(WaveJetHands.isWaveJet(player.getItemBySlot(slot)),
                        "With a full inventory the Wave Jet was put in the " + slot.getName() + " slot");
            }
            List<ItemEntity> dropped = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(3.0),
                    entity -> WaveJetHands.isWaveJet(entity.getItem()));
            helper.assertValueEqual(1, dropped.size(), "Wave Jets dropped at the player's feet");
            dropped.forEach(Entity::discard);
            helper.succeed();
        });

        r.add("wave_jet/an_occupied_offhand_unequips_it", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TORCH));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("A torch in the offhand did not displace the Wave Jet");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                helper.fail("The Wave Jet stayed in the main hand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).is(Items.TORCH)) {
                helper.fail("The torch was taken instead of the Wave Jet");
                return;
            }
            if (!player.getInventory().contains(stack -> stack.is(NTItems.WAVE_JET.get()))) {
                helper.fail("The Wave Jet was not put back into the inventory");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/an_occupied_main_hand_unequips_it", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(NTItems.WAVE_JET.get()));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("A sword in the main hand did not displace the Wave Jet");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                helper.fail("The Wave Jet stayed in the offhand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.IRON_SWORD)) {
                helper.fail("The sword was taken instead of the Wave Jet");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/one_free_hand_is_left_alone", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);

            if (WaveJetHands.enforce(player)) {
                helper.fail("The Wave Jet was displaced even though the offhand was empty");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(NTItems.WAVE_JET.get())) {
                helper.fail("The Wave Jet left a hand it was entitled to");
                return;
            }
            helper.succeed();
        });

        r.add("wave_jet/two_of_them_leaves_the_main_hand_holding_one", 20, helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NTItems.WAVE_JET.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(NTItems.WAVE_JET.get()));

            if (!WaveJetHands.enforce(player)) {
                helper.fail("Holding two Wave Jets was allowed to stand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(NTItems.WAVE_JET.get())) {
                helper.fail("The main hand lost its Wave Jet instead of the offhand");
                return;
            }
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                helper.fail("The offhand kept its Wave Jet");
                return;
            }
            helper.succeed();
        });
    }

    private static BlockPos shaft(NTGameTestHelper helper, BlockState fill) {
        ServerLevel level = helper.getLevel();
        BlockPos floor = helper.absolutePos(new BlockPos(4, 1, 4));
        level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        for (int y = 1; y <= 6; y++) {
            level.setBlockAndUpdate(floor.above(y), fill);
        }
        return floor;
    }

    private static Player aiming(NTGameTestHelper helper, BlockPos floor) {
        Player holder = helper.makeMockPlayer(GameType.SURVIVAL);
        holder.setPos(floor.getX() + 0.5, floor.getY() + 3, floor.getZ() + 0.5);
        holder.setYRot(0.0F);
        holder.setXRot(90.0F);

        ItemStack stack = new ItemStack(NTItems.WAVE_JET.get());
        NTDataComponentsUtils.setAbilityStatus(stack, true);
        holder.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return holder;
    }

    private WaveJetTests() {
    }
}
