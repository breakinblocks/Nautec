package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.gateways.GatewayAddress;
import com.breakinblocks.nautec.api.gateways.GatewayFarEnd;
import com.breakinblocks.nautec.api.gateways.GatewayIndex;
import com.breakinblocks.nautec.api.gateways.GatewayRing;
import com.breakinblocks.nautec.api.gateways.PackedGateway;
import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.blocks.GatewayBlock;
import com.breakinblocks.nautec.content.blocks.GatewayRingPartBlock;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTEntities;
import com.breakinblocks.nautec.registries.NTItems;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class GatewayTests {
    private static final Identifier ARENA = Nautec.rl("empty_34x16x14");
    private static final BlockPos CORE_A = new BlockPos(6, 1, 6);
    private static final BlockPos CORE_B = new BlockPos(25, 1, 6);

    private GatewayTests() {
    }

    private static GatewayAddress unique(int id) {
        return GatewayAddress.unpack(3000 + id);
    }

    private static GatewayBlockEntity ring(GameTestHelper helper, BlockPos relativeCore, GatewayAddress address) {
        return ringAt(helper, helper.absolutePos(relativeCore), address);
    }

    private static GatewayBlockEntity ringAt(GameTestHelper helper, BlockPos core, GatewayAddress address) {
        ServerLevel level = helper.getLevel();
        level.setBlockAndUpdate(core, NTBlocks.GATEWAY.get().defaultBlockState());
        for (BlockPos cell : GatewayRing.ringCells(core, HorizontalDirection.NORTH)) {
            level.setBlockAndUpdate(cell, NTBlocks.GATEWAY_RING.get().defaultBlockState());
        }
        if (!(level.getBlockEntity(core) instanceof GatewayBlockEntity gateway)) {
            throw helper.assertionException("Expected a GatewayBlockEntity at " + core);
        }
        gateway.markPlacedByPlayer();
        gateway.setAddress(address);
        if (!MultiblockHelper.form(NTMultiblocks.GATEWAY.get(), core, level)) {
            throw helper.assertionException("The ring around " + core + " did not form");
        }
        return gateway;
    }

    private static void keepAwake(GameTestHelper helper, GatewayBlockEntity... rings) {
        helper.onEachTick(() -> {
            for (GatewayBlockEntity ring : rings) {
                ring.wake(20);
                ring.setEnergy(GatewayBlockEntity.ENERGY_CAPACITY);
            }
        });
    }

    private static Player sneakingWith(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setShiftKeyDown(true);
        return player;
    }

    private static BlockHitResult hitOn(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false);
    }

    private static ItemEntity launch(GameTestHelper helper, Vec3 relative, Vec3 velocity) {
        Vec3 at = helper.absoluteVec(relative);
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(Items.PRISMARINE_SHARD), velocity.x, velocity.y, velocity.z);
        item.setNoGravity(true);
        item.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    private static Vec3 centre(GameTestHelper helper, BlockPos relativeCore) {
        return GatewayRing.centre(helper.absolutePos(relativeCore));
    }

    private static void fillWall(GameTestHelper helper, BlockPos relativeCore, int z, BlockState state) {
        for (int x = -2; x <= 2; x++) {
            for (int y = 4; y <= 8; y++) {
                helper.setBlock(new BlockPos(relativeCore.getX() + x, relativeCore.getY() + y, z), state);
            }
        }
    }

    public static void register(NTTestRegistrar r) {
        r.add("gateway/address_packs_and_unpacks", 20, helper -> {
            helper.assertValueEqual(4096, GatewayAddress.addressCount(), "total addressable combinations");

            for (int packed : new int[]{0, 1, 7, 8, 63, 512, 4095}) {
                GatewayAddress address = GatewayAddress.unpack(packed);
                helper.assertValueEqual(packed, address.pack(), "round trip of packed address " + packed);
            }

            GatewayAddress cyan = GatewayAddress.DEFAULT;
            helper.assertValueEqual(DyeColor.CYAN, cyan.slots().get(0), "default address colour");

            GatewayAddress changed = cyan.withSlot(2, DyeColor.BLACK);
            helper.assertValueEqual(DyeColor.BLACK, changed.slots().get(2), "recoloured slot");
            helper.assertValueEqual(DyeColor.CYAN, changed.slots().get(0), "untouched slot");
            helper.assertFalse(changed.equals(cyan), "Recolouring should produce a different address");
            helper.succeed();
        });

        r.add("gateway/dye_click_changes_the_address_for_free", ARENA, 40, 0, helper -> {
            GatewayBlockEntity gateway = ring(helper, CORE_A, unique(20));
            BlockPos core = helper.absolutePos(CORE_A);
            ItemStack dye = new ItemStack(Items.LIME_DYE, 3);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            Vec3 centre = GatewayRing.centre(core);
            double angle = Math.toRadians(GatewayRing.SLOT_ANGLES[1]);
            Vec3 right = GatewayRing.right(GatewayRing.normal(gateway.getFront()));
            Vec3 hit = centre.add(right.scale(Math.cos(angle) * 5.8)).add(0, Math.sin(angle) * 5.8, 0);
            GatewayBlock.useOnRing(dye, helper.getLevel(), gateway, player, new BlockHitResult(hit, Direction.UP, BlockPos.containing(hit), false));
            helper.assertValueEqual(DyeColor.LIME, gateway.getAddress().slots().get(1), "the upper right chevron after a lime dye click");
            helper.assertValueEqual(3, dye.getCount(), "dyes left after recolouring a chevron");
            helper.succeed();
        });

        r.add("gateway/dye_slot_from_face_quadrant", 20, helper -> {
            BlockPos pos = new BlockPos(4, 1, 4);
            helper.assertValueEqual(0, GatewayBlock.slotFor(pos, Direction.SOUTH, new Vec3(4.2, 1.8, 5.0)), "upper left of the south face");
            helper.assertValueEqual(1, GatewayBlock.slotFor(pos, Direction.SOUTH, new Vec3(4.8, 1.8, 5.0)), "upper right of the south face");
            helper.assertValueEqual(2, GatewayBlock.slotFor(pos, Direction.SOUTH, new Vec3(4.2, 1.2, 5.0)), "lower left of the south face");
            helper.assertValueEqual(3, GatewayBlock.slotFor(pos, Direction.NORTH, new Vec3(4.2, 1.2, 4.0)), "lower right of the north face");
            helper.succeed();
        });

        r.add("gateway/dye_slot_from_nearest_chevron", 20, helper -> {
            BlockPos core = new BlockPos(100, 64, 100);
            Vec3 centre = GatewayRing.centre(core);
            for (int slot = 0; slot < 4; slot++) {
                double angle = Math.toRadians(GatewayRing.SLOT_ANGLES[slot]);
                Vec3 right = GatewayRing.right(GatewayRing.normal(Direction.SOUTH));
                Vec3 hit = centre.add(right.scale(Math.cos(angle) * 5.8)).add(0, Math.sin(angle) * 5.8, 0);
                helper.assertValueEqual(slot, GatewayRing.slotAt(hit, core, Direction.SOUTH), "chevron slot nearest to angle " + GatewayRing.SLOT_ANGLES[slot]);
            }
            helper.succeed();
        });

        r.add("gateway/ring_forms_and_parts_find_their_core", ARENA, 40, 0, helper -> {
            GatewayBlockEntity gateway = ring(helper, CORE_A, unique(1));
            BlockPos core = helper.absolutePos(CORE_A);
            helper.assertTrue(gateway.isFormed(), "The core should be formed");
            List<BlockPos> cells = GatewayRing.ringCells(core, HorizontalDirection.NORTH);
            helper.assertValueEqual(67, cells.size(), "ring segments in a 13 wide ring");
            for (BlockPos cell : cells) {
                BlockState state = helper.getLevel().getBlockState(cell);
                helper.assertTrue(state.is(NTBlocks.GATEWAY_RING_PART.get()), "Formed ring part at " + cell);
                helper.assertValueEqual(core, GatewayRingPartBlock.corePos(state, cell), "core found from the part at " + cell);
            }
            helper.assertTrue(GatewayIndex.get(helper.getLevel()).addressAt(core) != null, "A formed ring should be in the index");
            helper.succeed();
        });

        r.add("gateway/breaking_a_segment_unforms_the_ring", ARENA, 40, 0, helper -> {
            GatewayBlockEntity gateway = ring(helper, CORE_A, unique(2));
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos top = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            helper.getLevel().destroyBlock(top, true);

            helper.assertFalse(gateway.isFormed(), "Breaking a segment should unform the core");
            helper.assertFalse(helper.getLevel().getBlockState(core).getValue(Multiblock.FORMED), "The core block should be unformed");
            for (BlockPos cell : GatewayRing.ringCells(core, HorizontalDirection.NORTH)) {
                if (!cell.equals(top)) {
                    helper.assertTrue(helper.getLevel().getBlockState(cell).is(NTBlocks.GATEWAY_RING.get()), "Unformed segment at " + cell);
                }
            }
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(top).inflate(2));
            int segments = drops.stream().filter(item -> item.getItem().is(NTBlocks.GATEWAY_RING.asItem())).mapToInt(item -> item.getItem().getCount()).sum();
            helper.assertValueEqual(0, segments, "segments dropped by the broken part");
            helper.assertTrue(GatewayIndex.get(helper.getLevel()).addressAt(core) == null, "An unformed ring should leave the index");
            helper.succeed();
        });

        r.add("gateway/rings_with_matching_addresses_link", ARENA, 80, 0, helper -> {
            GatewayAddress code = unique(3);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            helper.succeedWhen(() -> {
                helper.assertTrue(a.isLinked(), "The first ring should open");
                helper.assertTrue(b.isLinked(), "The second ring should open");
            });
        });

        r.add("gateway/mismatched_rings_stay_idle", ARENA, 60, 0, helper -> {
            GatewayBlockEntity a = ring(helper, CORE_A, unique(4));
            GatewayBlockEntity b = ring(helper, CORE_B, unique(5));
            helper.runAfterDelay(45, () -> {
                helper.assertFalse(a.isLinked(), "A ring with no matching partner should stay idle");
                helper.assertFalse(b.isLinked(), "A ring with no matching partner should stay idle");
                helper.succeed();
            });
        });

        r.add("gateway/passing_through_exits_the_partner_front", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(6);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            keepAwake(helper, a);
            Vec3 centreB = centre(helper, CORE_B);
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen() && b.isLinked(), "Waiting for the near ring to open"))
                    .thenExecute(() -> item[0] = launch(helper, new Vec3(8.5, 7.5, 3.5), new Vec3(0, 0, 0.35)))
                    .thenWaitUntil(() -> helper.assertTrue(item[0].position().distanceTo(centreB) < 4.0,
                            "The item should come out of the far ring, but is at " + item[0].position()))
                    .thenExecute(() -> {
                        Vec3 n = GatewayRing.normal(b.getFront());
                        Vec3 offset = item[0].position().subtract(centreB);
                        helper.assertTrue(offset.dot(n) > 0.3, "The item should be on the far ring's front side, offset " + offset);
                        helper.assertTrue(item[0].getDeltaMovement().dot(n) > 0.0, "The item should keep moving away from the far ring");
                        double across = offset.dot(GatewayRing.right(n));
                        helper.assertTrue(Math.abs(Math.abs(across) - 2.0) < 0.6, "The item should keep its position across the opening, offset " + across);
                        helper.assertTrue(item[0].isOnPortalCooldown(), "A travelled item should be on cooldown");
                    })
                    .thenSucceed();
        });

        r.add("gateway/entering_from_behind_still_exits_the_front", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(7);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            keepAwake(helper, a);
            Vec3 centreB = centre(helper, CORE_B);
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen() && b.isLinked(), "Waiting for the near ring to open"))
                    .thenExecute(() -> {
                        Vec3 n = GatewayRing.normal(a.getFront());
                        Vec3 start = new Vec3(6.5, 7.5, 6.5).add(n.scale(2.5));
                        item[0] = launch(helper, start, n.scale(-0.35));
                    })
                    .thenWaitUntil(() -> helper.assertTrue(item[0].position().distanceTo(centreB) < 4.0,
                            "The item should come out of the far ring, but is at " + item[0].position()))
                    .thenExecute(() -> {
                        Vec3 n = GatewayRing.normal(b.getFront());
                        helper.assertTrue(item[0].position().subtract(centreB).dot(n) > 0.3, "Entering from behind should still exit the front");
                        helper.assertTrue(item[0].getDeltaMovement().dot(n) > 0.0, "The item should move away from the far ring");
                    })
                    .thenSucceed();
        });

        r.add("gateway/idle_ring_lets_things_pass", ARENA, 80, 0, helper -> {
            ring(helper, CORE_A, unique(8));
            ring(helper, CORE_B, unique(9));
            ItemEntity item = launch(helper, new Vec3(6.5, 7.5, 3.5), new Vec3(0, 0, 0.35));
            helper.runAfterDelay(40, () -> {
                helper.assertTrue(item.position().z > helper.absoluteVec(new Vec3(0, 0, 8)).z, "The item should have flown straight through the idle ring");
                helper.assertFalse(item.isOnPortalCooldown(), "Nothing should have travelled");
                helper.succeed();
            });
        });

        r.add("gateway/blocked_front_exits_the_back", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(10);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            keepAwake(helper, a);
            Vec3 centreB = centre(helper, CORE_B);
            Direction front = b.getFront();
            fillWall(helper, CORE_B, CORE_B.getZ() + front.getStepZ(), Blocks.STONE.defaultBlockState());
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen() && b.isLinked(), "Waiting for the near ring to open"))
                    .thenExecute(() -> item[0] = launch(helper, new Vec3(6.5, 7.5, 3.5), new Vec3(0, 0, 0.35)))
                    .thenWaitUntil(() -> helper.assertTrue(item[0].position().distanceTo(centreB) < 4.0,
                            "The item should come out of the far ring's back, but is at " + item[0].position()))
                    .thenExecute(() -> helper.assertTrue(item[0].position().subtract(centreB).dot(GatewayRing.normal(front)) < -0.3,
                            "With the front walled off the item should come out of the back"))
                    .thenSucceed();
        });

        r.add("gateway/fully_blocked_exit_leaves_the_traveller", ARENA, 100, 0, helper -> {
            GatewayAddress code = unique(11);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            keepAwake(helper, a);
            fillWall(helper, CORE_B, CORE_B.getZ() + 1, Blocks.STONE.defaultBlockState());
            fillWall(helper, CORE_B, CORE_B.getZ() - 1, Blocks.STONE.defaultBlockState());
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen() && b.isLinked(), "Waiting for the near ring to open"))
                    .thenExecute(() -> item[0] = launch(helper, new Vec3(6.5, 7.5, 3.5), new Vec3(0, 0, 0.35)))
                    .thenIdle(30)
                    .thenExecute(() -> {
                        helper.assertTrue(item[0].position().distanceTo(centre(helper, CORE_A)) < 6.0, "A blocked exit should leave the item near its own ring");
                        helper.assertFalse(item[0].isOnPortalCooldown(), "Failed travel must not apply cooldown");
                    })
                    .thenSucceed();
        });

        r.add("gateway/forgets_a_missing_unloaded_partner", ARENA, 120, 0, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos far = helper.absolutePos(CORE_A).offset(0, 0, 2048);
            helper.assertFalse(level.isLoaded(far), "The far position should start unloaded");
            GatewayAddress code = unique(13);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayIndex.get(level).put(far, code);
            keepAwake(helper, a);
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "The ring trusts the unloaded index entry until something travels"))
                    .thenExecute(() -> item[0] = launch(helper, new Vec3(6.5, 7.5, 3.5), new Vec3(0, 0, 0.35)))
                    .thenIdle(30)
                    .thenExecute(() -> {
                        helper.assertTrue(GatewayIndex.get(level).addressAt(far) == null,
                                "An index entry with no ring behind it should be dropped once its chunk is checked");
                        helper.assertFalse(item[0].isOnPortalCooldown(), "Failed travel must not apply cooldown");
                    })
                    .thenSucceed();
        });

        r.add("gateway/travels_to_an_unloaded_partner", ARENA, 900, 0, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos far = helper.absolutePos(CORE_A).offset(1024, 0, 0);
            GatewayAddress code = unique(14);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            keepAwake(helper, a);
            ItemEntity[] item = new ItemEntity[1];
            helper.startSequence()
                    .thenExecute(() -> {
                        for (BlockPos pos : BlockPos.betweenClosed(far.offset(-7, 0, -3), far.offset(7, 13, 3))) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                        ringAt(helper, far, code);
                    })
                    .thenWaitUntil(() -> helper.assertFalse(level.isLoaded(far), "Waiting for the far ring's chunk to unload"))
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "The near ring should open towards the unloaded ring"))
                    .thenExecute(() -> item[0] = launch(helper, new Vec3(6.5, 7.5, 3.5), new Vec3(0, 0, 0.35)))
                    .thenWaitUntil(() -> helper.assertTrue(item[0].position().distanceTo(GatewayRing.centre(far)) < 4.0,
                            "The item should have travelled to the unloaded ring at " + far + ", but is at " + item[0].position()))
                    .thenExecute(() -> {
                        item[0].discard();
                        if (level.getBlockEntity(far) instanceof GatewayBlockEntity partner) {
                            partner.unformRing();
                        }
                        for (BlockPos cell : GatewayRing.ringCells(far, HorizontalDirection.NORTH)) {
                            level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                        }
                        level.setBlockAndUpdate(far, Blocks.AIR.defaultBlockState());
                    })
                    .thenSucceed();
        });

        r.add("gateway/forms_underwater_without_air_pockets", ARENA, 40, 0, helper -> {
            ServerLevel level = helper.getLevel();
            BlockPos core = helper.absolutePos(CORE_A);
            for (int x = -7; x <= 7; x++) {
                for (int y = 0; y <= 13; y++) {
                    for (int z = -1; z <= 1; z++) {
                        level.setBlockAndUpdate(core.offset(x, y, z), Blocks.WATER.defaultBlockState());
                    }
                }
            }
            ring(helper, CORE_A, unique(12));
            helper.assertTrue(level.getBlockState(core).getValue(BlockStateProperties.WATERLOGGED), "The formed core should hold water");
            for (BlockPos cell : GatewayRing.ringCells(core, HorizontalDirection.NORTH)) {
                helper.assertTrue(level.getBlockState(cell).getValue(BlockStateProperties.WATERLOGGED), "The formed segment at " + cell + " should hold water");
            }
            helper.succeed();
        });

        r.add("gateway/old_pad_builds_its_own_ring", ARENA, 200, 0, helper -> {
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity pad = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.assertTrue(pad.needsSelfHeal(), "A gateway with no ring version should be treated as an old pad");
            helper.succeedWhen(() -> {
                helper.assertTrue(pad.isFormed(), "The old pad should have grown a ring");
                helper.assertTrue(pad.isWild(), "An old cyan pad should become a wild ring");
                helper.assertFalse(pad.needsPower(), "A wild ring should need no power");
                helper.assertFalse(pad.getAddress().equals(GatewayAddress.DEFAULT), "A wild ring should get its own address");
                for (BlockPos cell : GatewayRing.ringCells(helper.absolutePos(CORE_A), pad.getMultiblockData().direction())) {
                    helper.assertTrue(helper.getLevel().getBlockState(cell).is(NTBlocks.GATEWAY_RING_PART.get()), "Formed ring part at " + cell);
                }
            });
        });

        r.add("gateway/old_pad_clears_seagrass_and_coral", ARENA, 200, 0, helper -> {
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos grassCell = GatewayRing.cellPos(core, HorizontalDirection.NORTH, 4, 0);
            BlockPos coralCell = GatewayRing.cellPos(core, HorizontalDirection.NORTH, 8, 0);
            helper.getLevel().setBlockAndUpdate(grassCell, Blocks.SEAGRASS.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(coralCell, Blocks.TUBE_CORAL.defaultBlockState());
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity pad = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.succeedWhen(() -> {
                helper.assertTrue(pad.isFormed(), "Seagrass and coral should not stop an old pad from growing its ring");
                helper.assertTrue(helper.getLevel().getBlockState(grassCell).is(NTBlocks.GATEWAY_RING_PART.get()), "The seagrass cell should hold a ring part");
                helper.assertTrue(helper.getLevel().getBlockState(coralCell).is(NTBlocks.GATEWAY_RING_PART.get()), "The coral cell should hold a ring part");
            });
        });

        r.add("gateway/old_pad_never_breaks_blocks_to_heal", ARENA, 260, 0, helper -> {
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos inNorthPlane = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            BlockPos inEastPlane = GatewayRing.cellPos(core, HorizontalDirection.EAST, 0, GatewayRing.CENTRE);
            helper.getLevel().setBlockAndUpdate(inNorthPlane, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(inEastPlane, Blocks.STONE.defaultBlockState());
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity pad = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.runAfterDelay(220, () -> {
                helper.assertFalse(pad.isFormed(), "A blocked pad should not form");
                helper.assertTrue(helper.getLevel().getBlockState(inNorthPlane).is(Blocks.STONE), "The blocking stone should be untouched");
                helper.assertTrue(helper.getLevel().getBlockState(inEastPlane).is(Blocks.STONE), "The blocking stone should be untouched");
                helper.assertTrue(pad.getBlockedAt() != null, "The pad should report what blocks it");
                for (BlockPos cell : GatewayRing.ringCells(core, HorizontalDirection.NORTH)) {
                    helper.assertFalse(helper.getLevel().getBlockState(cell).is(NTBlocks.GATEWAY_RING.get()), "No segments should be placed when blocked");
                }
                helper.succeed();
            });
        });

        r.add("gateway/placed_core_waits_for_a_built_ring", ARENA, 200, 0, helper -> {
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity core = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            core.markPlacedByPlayer();
            helper.runAfterDelay(160, () -> {
                helper.assertFalse(core.isFormed(), "A core placed by a player should never build its own ring");
                helper.succeed();
            });
        });

        r.add("gateway/ring_waits_for_someone_near", ARENA, 80, 0, helper -> {
            GatewayAddress code = unique(21);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            GatewayBlockEntity b = ring(helper, CORE_B, code);
            a.setEnergy(GatewayBlockEntity.ENERGY_CAPACITY);
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isLinked() && b.isLinked(), "Waiting for the rings to link"))
                    .thenIdle(10)
                    .thenExecute(() -> helper.assertFalse(a.isOpen(), "A linked ring with nobody near should stay shut"))
                    .thenExecute(() -> a.wake(40))
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "Someone near should open the ring"))
                    .thenSucceed();
        });

        r.add("gateway/someone_near_means_players_and_submarines", 20, helper -> {
            helper.assertTrue(GatewayBlockEntity.wakesGateway(helper.makeMockPlayer(GameType.SURVIVAL)), "A player should wake a ring");
            helper.assertTrue(GatewayBlockEntity.wakesGateway(new SubmarineEntity(NTEntities.SUBMARINE.get(), helper.getLevel())), "A submarine should wake a ring");
            helper.assertFalse(GatewayBlockEntity.wakesGateway(new ItemEntity(helper.getLevel(), 0, 0, 0, new ItemStack(Items.STONE))), "An item should not wake a ring");
            helper.succeed();
        });

        r.add("gateway/crafted_ring_needs_power", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(22);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            ring(helper, CORE_B, code);
            helper.onEachTick(() -> a.wake(20));
            int[] before = new int[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isLinked(), "Waiting for the rings to link"))
                    .thenIdle(5)
                    .thenExecute(() -> helper.assertFalse(a.isOpen(), "A crafted ring with an empty battery should stay shut"))
                    .thenExecute(() -> a.setEnergy(GatewayBlockEntity.ENERGY_CAPACITY))
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "A charged ring should open"))
                    .thenExecute(() -> before[0] = a.getEnergy())
                    .thenIdle(10)
                    .thenExecute(() -> helper.assertValueEqual(before[0] - 10 * GatewayBlockEntity.ENERGY_PER_TICK, a.getEnergy(), "energy after ten open ticks"))
                    .thenWaitUntil(() -> helper.assertFalse(a.isOpen(), "The ring should close once its battery runs out"))
                    .thenSucceed();
        });

        r.add("gateway/low_power_ring_stays_shut_instead_of_flickering", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(28);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            ring(helper, CORE_B, code);
            helper.onEachTick(() -> a.wake(20));
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isLinked(), "Waiting for the rings to link"))
                    .thenExecute(() -> a.setEnergy(GatewayBlockEntity.ENERGY_PER_TICK * 3))
                    .thenIdle(10)
                    .thenExecute(() -> helper.assertFalse(a.isOpen(), "A ring with only a few ticks of power should stay shut"))
                    .thenExecute(() -> a.setEnergy(GatewayBlockEntity.ENERGY_TO_OPEN))
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "A ring holding enough to open should open"))
                    .thenIdle(GatewayBlockEntity.ENERGY_TO_OPEN / GatewayBlockEntity.ENERGY_PER_TICK - 2)
                    .thenExecute(() -> helper.assertTrue(a.isOpen(), "An open ring should stay open until its buffer runs out"))
                    .thenWaitUntil(() -> helper.assertFalse(a.isOpen(), "The ring should close once its buffer runs out"))
                    .thenExecute(() -> a.setEnergy(GatewayBlockEntity.ENERGY_PER_TICK * 3))
                    .thenIdle(10)
                    .thenExecute(() -> helper.assertFalse(a.isOpen(), "A drained ring should wait for a proper charge before reopening"))
                    .thenSucceed();
        });

        r.add("gateway/laser_charges_the_core", ARENA, 80, 0, helper -> {
            GatewayBlockEntity a = ring(helper, CORE_A, unique(23));
            BlockPos source = CORE_A.relative(a.getFront(), 3);
            helper.setBlock(source, NTBlocks.CREATIVE_POWER_SOURCE.get().defaultBlockState());
            helper.succeedWhen(() -> helper.assertTrue(a.getEnergy() > 0, "A beam into the core should charge its battery"));
        });

        r.add("gateway/redstone_holds_it_shut", ARENA, 120, 0, helper -> {
            GatewayAddress code = unique(24);
            GatewayBlockEntity a = ring(helper, CORE_A, code);
            ring(helper, CORE_B, code);
            keepAwake(helper, a);
            BlockPos torch = CORE_A.below();
            helper.setBlock(torch, Blocks.REDSTONE_BLOCK.defaultBlockState());
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(a.isLinked(), "Waiting for the rings to link"))
                    .thenIdle(5)
                    .thenExecute(() -> helper.assertFalse(a.isOpen(), "A redstone signal should hold the ring shut"))
                    .thenExecute(() -> helper.setBlock(torch, Blocks.STONE.defaultBlockState()))
                    .thenWaitUntil(() -> helper.assertTrue(a.isOpen(), "Removing the signal should let it open"))
                    .thenSucceed();
        });

        r.add("gateway/wrench_packs_the_ring", ARENA, 40, 0, helper -> {
            GatewayAddress code = unique(25);
            GatewayBlockEntity gateway = ring(helper, CORE_A, code);
            BlockPos core = helper.absolutePos(CORE_A);
            Player player = sneakingWith(helper, new ItemStack(NTItems.AQUARINE_WRENCH.get()));
            BlockPos part = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            GatewayBlock.useOnRing(player.getMainHandItem(), helper.getLevel(), gateway, player, hitOn(part));
            helper.assertTrue(helper.getLevel().getBlockState(core).isAir(), "The core should be gone after packing");
            for (BlockPos cell : GatewayRing.ringCells(core, HorizontalDirection.NORTH)) {
                helper.assertTrue(helper.getLevel().getBlockState(cell).isAir(), "Packed cell at " + cell + " should be empty");
            }
            ItemStack packed = ItemStack.EMPTY;
            for (ItemStack stack : player.getInventory()) {
                if (stack.is(NTBlocks.GATEWAY.asItem())) {
                    packed = stack;
                }
            }
            helper.assertFalse(packed.isEmpty(), "The player should get the packed gateway");
            helper.assertTrue(packed.has(NTDataComponents.GATEWAY_PACKED.get()), "The item should carry a packed ring");
            helper.assertValueEqual(code, packed.get(NTDataComponents.GATEWAY_ADDRESS.get()), "address kept on the packed gateway");
            helper.assertTrue(GatewayIndex.get(helper.getLevel()).addressAt(core) == null, "A packed ring should leave the index");
            helper.succeed();
        });

        r.add("gateway/sneak_wrench_click_packs_the_ring", ARENA, 40, 0, helper -> {
            GatewayAddress code = unique(27);
            ring(helper, CORE_A, code);
            BlockPos core = helper.absolutePos(CORE_A);
            Player player = sneakingWith(helper, new ItemStack(NTItems.AQUARINE_WRENCH.get()));
            BlockPos part = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, part, hitOn(part));
            helper.assertTrue(event.isCanceled(), "A sneaking wrench click should be taken before the wrench or the ring sees it");
            helper.assertTrue(helper.getLevel().getBlockState(core).isAir(), "The core should be gone after a sneaking wrench click");
            boolean packed = false;
            for (ItemStack stack : player.getInventory()) {
                if (stack.is(NTBlocks.GATEWAY.asItem()) && code.equals(stack.get(NTDataComponents.GATEWAY_ADDRESS.get()))) {
                    packed = true;
                }
            }
            helper.assertTrue(packed, "The player should get the packed gateway");
            helper.succeed();
        });

        r.add("gateway/packed_gateway_rebuilds_facing_the_placer", ARENA, 40, 0, helper -> {
            GatewayAddress code = unique(26);
            ItemStack packed = new ItemStack(NTBlocks.GATEWAY.asItem());
            packed.set(NTDataComponents.GATEWAY_PACKED.get(), PackedGateway.CRAFTED);
            packed.set(NTDataComponents.GATEWAY_ADDRESS.get(), code);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setYRot(0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, packed);
            helper.placeAt(player, packed, CORE_A.below(), Direction.UP);
            GatewayBlockEntity gateway = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.assertTrue(gateway.isFormed(), "Placing a packed gateway should rebuild the ring at once");
            helper.assertValueEqual(HorizontalDirection.NORTH, gateway.getMultiblockData().direction(), "ring plane for a player facing south");
            helper.assertValueEqual(Direction.NORTH, gateway.getFront(), "front faces back towards the placer");
            helper.assertValueEqual(code, gateway.getAddress(), "address after unpacking");
            helper.assertTrue(gateway.needsPower(), "A moved or crafted ring needs power");
            helper.succeed();
        });

        r.add("gateway/packed_gateway_waits_when_blocked", ARENA, 200, 0, helper -> {
            ItemStack packed = new ItemStack(NTBlocks.GATEWAY.asItem());
            packed.set(NTDataComponents.GATEWAY_PACKED.get(), PackedGateway.CRAFTED);
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos blocker = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            helper.getLevel().setBlockAndUpdate(blocker, Blocks.STONE.defaultBlockState());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setYRot(0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, packed);
            helper.placeAt(player, packed, CORE_A.below(), Direction.UP);
            GatewayBlockEntity gateway = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.assertFalse(gateway.isFormed(), "A blocked packed gateway should wait as a bare core");
            helper.assertTrue(helper.getLevel().getBlockState(blocker).is(Blocks.STONE), "Unpacking should never break the blocking block");
            helper.getLevel().setBlockAndUpdate(blocker, Blocks.AIR.defaultBlockState());
            helper.succeedWhen(() -> {
                helper.assertTrue(gateway.isFormed(), "Clearing the block should let the ring finish");
                helper.assertValueEqual(HorizontalDirection.NORTH, gateway.getMultiblockData().direction(), "It should still face the placer");
            });
        });

        r.add("gateway/wrench_retries_a_blocked_packed_gateway", ARENA, 40, 0, helper -> {
            ItemStack packed = new ItemStack(NTBlocks.GATEWAY.asItem());
            packed.set(NTDataComponents.GATEWAY_PACKED.get(), PackedGateway.CRAFTED);
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos blocker = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            helper.getLevel().setBlockAndUpdate(blocker, Blocks.STONE.defaultBlockState());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setYRot(0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, packed);
            helper.placeAt(player, packed, CORE_A.below(), Direction.UP);
            GatewayBlockEntity gateway = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);

            gateway.retryBuild(player);
            helper.assertFalse(gateway.isFormed(), "A retry while blocked should leave a bare core");
            helper.assertValueEqual(blocker, gateway.getBlockedAt(), "the reported blocking block");
            helper.assertValueEqual(List.of(blocker), gateway.blockingCells(), "the blocks the monocle highlights");

            helper.getLevel().setBlockAndUpdate(blocker, Blocks.AIR.defaultBlockState());
            gateway.retryBuild(player);
            helper.assertTrue(gateway.isFormed(), "A retry after clearing should form the ring at once");
            helper.assertTrue(gateway.blockingCells().isEmpty(), "A formed ring should highlight nothing");
            helper.assertValueEqual(HorizontalDirection.NORTH, gateway.getMultiblockData().direction(), "It should still face the placer");
            helper.succeed();
        });

        r.add("gateway/creative_force_build_clears_the_way", ARENA, 40, 0, helper -> {
            ItemStack packed = new ItemStack(NTBlocks.GATEWAY.asItem());
            packed.set(NTDataComponents.GATEWAY_PACKED.get(), PackedGateway.CRAFTED);
            BlockPos core = helper.absolutePos(CORE_A);
            List<BlockPos> blockers = List.of(
                    GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12),
                    GatewayRing.cellPos(core, HorizontalDirection.NORTH, 1, GatewayRing.CENTRE));
            for (BlockPos blocker : blockers) {
                helper.getLevel().setBlockAndUpdate(blocker, Blocks.STONE.defaultBlockState());
            }
            Player player = helper.makeMockPlayer(GameType.CREATIVE);
            player.setYRot(0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, packed);
            helper.placeAt(player, packed, CORE_A.below(), Direction.UP);
            GatewayBlockEntity gateway = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.assertFalse(gateway.isFormed(), "The blocked ring should wait before forcing");

            helper.assertTrue(gateway.forceBuild(player), "Forcing should report a built ring");
            helper.assertTrue(gateway.isFormed(), "Forcing should form the ring");
            helper.assertValueEqual(HorizontalDirection.NORTH, gateway.getMultiblockData().direction(), "It should face the player");
            helper.assertTrue(gateway.isWild(), "A creative force build should make a wild ring");
            helper.assertFalse(gateway.needsPower(), "A creative force build should not need power");
            helper.assertFalse(gateway.getAddress().equals(GatewayAddress.DEFAULT), "A force built ring with no address should get a wild one");
            for (BlockPos blocker : blockers) {
                helper.assertFalse(helper.getLevel().getBlockState(blocker).is(Blocks.STONE), "Forcing should clear " + blocker);
            }
            helper.succeed();
        });

        r.add("gateway/formed_ring_mines_like_obsidian_and_drops_nothing", ARENA, 40, 0, helper -> {
            ring(helper, CORE_A, unique(27));
            BlockPos core = helper.absolutePos(CORE_A);
            BlockPos part = GatewayRing.cellPos(core, HorizontalDirection.NORTH, GatewayRing.CENTRE, 12);
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NETHERITE_PICKAXE));
            ServerLevel level = helper.getLevel();
            helper.assertValueEqual(GatewayBlock.FORMED_DESTROY_PROGRESS, level.getBlockState(part).getDestroyProgress(player, level, part), "progress per tick on a formed part");
            helper.assertValueEqual(GatewayBlock.FORMED_DESTROY_PROGRESS, level.getBlockState(core).getDestroyProgress(player, level, core), "progress per tick on a formed core");
            helper.assertFalse(player.hasCorrectToolForDrops(level.getBlockState(part)), "No tool should be correct for a formed part");
            level.destroyBlock(core, true, player);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(core).inflate(2));
            helper.assertTrue(drops.isEmpty(), "Breaking a formed core should drop nothing, but dropped " + drops);
            helper.assertTrue(level.getBlockState(part).is(NTBlocks.GATEWAY_RING.get()), "Breaking the core should turn the rest of the ring back into segments");
            helper.succeed();
        });

        r.add("gateway/old_dyed_pad_counts_as_crafted", ARENA, 200, 0, helper -> {
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity pad = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            GatewayAddress code = unique(28);
            pad.setAddress(code);
            helper.succeedWhen(() -> {
                helper.assertTrue(pad.isFormed(), "An old dyed pad should still grow its ring");
                helper.assertFalse(pad.isWild(), "A pad a player had dyed is not wild");
                helper.assertTrue(pad.needsPower(), "A pad a player had dyed counts as crafted and needs power");
                helper.assertValueEqual(code, pad.getAddress(), "The player's address should be kept");
            });
        });

        r.add("gateway/wild_ring_builds_its_far_end", ARENA, 900, 0, helper -> {
            ServerLevel level = helper.getLevel();
            helper.setBlock(CORE_A, NTBlocks.GATEWAY.get().defaultBlockState());
            GatewayBlockEntity pad = helper.getBlockEntity(CORE_A, GatewayBlockEntity.class);
            helper.onEachTick(() -> pad.wake(20));
            ItemEntity[] item = new ItemEntity[1];
            BlockPos[] far = new BlockPos[1];
            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(pad.isFormed() && pad.isOpen(), "Waiting for the wild ring to grow and open"))
                    .thenExecute(() -> {
                        Vec3 n = GatewayRing.normal(pad.getFront());
                        Vec3 start = new Vec3(6.5, 7.5, 6.5).add(n.scale(2.5));
                        item[0] = launch(helper, start, n.scale(-0.35));
                    })
                    .thenWaitUntil(() -> helper.assertTrue(item[0].isOnPortalCooldown(), "The item should have travelled through the wild ring"))
                    .thenExecute(() -> {
                        BlockPos origin = helper.absolutePos(CORE_A);
                        double dx = item[0].getX() - origin.getX();
                        double dz = item[0].getZ() - origin.getZ();
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        helper.assertTrue(distance >= GatewayFarEnd.MIN_DISTANCE - 20 && distance <= GatewayFarEnd.MAX_DISTANCE + 20,
                                "The far end should be 1250 to 1750 blocks away, but the item is " + distance + " away");
                        far[0] = GatewayIndex.get(level).findNearest(level, origin, pad.getAddress());
                        helper.assertTrue(far[0] != null, "The far end should be in the index");
                        if (!(level.getBlockEntity(far[0]) instanceof GatewayBlockEntity partner)) {
                            throw helper.assertionException("Expected a far end ring at " + far[0]);
                        }
                        helper.assertTrue(partner.isFormed() && partner.isWild() && !partner.needsPower(), "The far end should be a formed wild ring needing no power");
                        helper.assertValueEqual(pad.getAddress(), partner.getAddress(), "far end address");
                        item[0].discard();
                        partner.unformRing();
                        for (BlockPos cell : GatewayRing.ringCells(far[0], HorizontalDirection.NORTH)) {
                            level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                        }
                        for (BlockPos cell : GatewayRing.ringCells(far[0], HorizontalDirection.EAST)) {
                            level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                        }
                        level.setBlockAndUpdate(far[0], Blocks.AIR.defaultBlockState());
                    })
                    .thenSucceed();
        });
    }
}
