package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlock;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterBlockEntity;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterItem;
import com.breakinblocks.nautec.content.enchanter.AbyssalEnchanterPartBlock;
import com.breakinblocks.nautec.content.enchanter.EnchanterStructure;
import com.breakinblocks.nautec.content.enchanter.EnchantingOutcome;
import com.breakinblocks.nautec.content.enchanter.EnchantingRules;
import com.breakinblocks.nautec.content.blockentities.PrismarineLaserRelayBlockEntity;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.tags.NTTags;
import com.breakinblocks.nautec.NTConfig;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.nautec.transfer.TransferCapabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.transfer.ResourceHandler;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.transaction.Transaction;

import java.util.List;

public final class AbyssalEnchanterTests {
    private static final BlockPos CORE = new BlockPos(4, 1, 4);

    private AbyssalEnchanterTests() {
    }

    private static InteractionResult place(NTGameTestHelper helper, Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos clicked = helper.absolutePos(CORE);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false);
        return ((AbyssalEnchanterItem) stack.getItem()).place(new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }

    private static AbyssalEnchanterBlockEntity placed(NTGameTestHelper helper) {
        InteractionResult result = place(helper, helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(NTBlocks.ABYSSAL_ENCHANTER.get()));
        helper.assertTrue(result.consumesAction(), "placing the enchanter works, got " + result);
        return helper.getBlockEntity(CORE, AbyssalEnchanterBlockEntity.class);
    }

    private static Holder<Enchantment> enchantment(NTGameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static int insert(ResourceHandler<FluidResource> handler, FluidResource fluid, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(fluid, amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    private static int extract(ResourceHandler<FluidResource> handler, FluidResource fluid, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(fluid, amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    private static void beam(AbyssalEnchanterBlockEntity enchanter, int power, float purity) {
        enchanter.receiveBeam(power, purity, Direction.EAST, enchanter.getBlockPos().west(5), LongSets.EMPTY_SET);
    }

    private static void loadSharpnessOne(NTGameTestHelper helper, AbyssalEnchanterBlockEntity enchanter) {
        enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.BOOK));
        enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
        enchanter.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 10_000));
        enchanter.commonTick();
        helper.assertValueEqual(5, enchanter.getEnchantLevel(), "quartz picks Sharpness at its top level");
        helper.assertValueEqual(5, enchanter.getLevelCap(), "a book stops at the normal max");
        enchanter.pressButton(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_UP);
        helper.assertValueEqual(1, enchanter.getEnchantLevel(), "raising past the top wraps to level I");
        helper.assertValueEqual(12, enchanter.getReagentNeeded(), "level I uses one set of reagent");
    }

    public static void register(NTTestRegistrar r) {
        r.add("enchanter/places_a_3x3x2", 20, helper -> {
            placed(helper);
            BlockPos core = helper.absolutePos(CORE);
            for (int cell = 0; cell < EnchanterStructure.CELLS; cell++) {
                BlockPos pos = EnchanterStructure.cellPos(core, cell);
                BlockState state = helper.getLevel().getBlockState(pos);
                if (cell == EnchanterStructure.CORE_CELL) {
                    helper.assertTrue(state.is(NTBlocks.ABYSSAL_ENCHANTER.get()), "the core is the bottom center");
                    continue;
                }
                helper.assertTrue(state.is(NTBlocks.ABYSSAL_ENCHANTER_PART.get()), "cell " + cell + " holds a part");
                helper.assertValueEqual(core, AbyssalEnchanterPartBlock.corePos(state, pos), "part finds its core");
            }
            helper.assertTrue(helper.getLevel().getBlockState(core.above(2)).isAir(), "nothing above the second layer");
            helper.succeed();
        });

        r.add("enchanter/needs_a_clear_space", 20, helper -> {
            helper.setBlock(CORE.offset(1, 1, 1), Blocks.STONE);
            ItemStack stack = new ItemStack(NTBlocks.ABYSSAL_ENCHANTER.get());
            InteractionResult result = place(helper, helper.makeMockPlayer(GameType.SURVIVAL), stack);
            helper.assertFalse(result.consumesAction(), "a blocked space refuses the enchanter");
            helper.assertBlockNotPresent(NTBlocks.ABYSSAL_ENCHANTER.get(), CORE);
            helper.assertValueEqual(1, stack.getCount(), "the item is kept");
            helper.succeed();
        });

        r.add("enchanter/breaking_a_part_drops_one_item", 20, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 7));
            helper.getLevel().destroyBlock(helper.absolutePos(CORE.offset(1, 1, -1)), true);
            BlockPos core = helper.absolutePos(CORE);
            for (int cell = 0; cell < EnchanterStructure.CELLS; cell++) {
                helper.assertTrue(helper.getLevel().getBlockState(EnchanterStructure.cellPos(core, cell)).isAir(), "cell " + cell + " is cleared");
            }
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(core).inflate(3));
            int machines = 0;
            int quartz = 0;
            for (ItemEntity drop : drops) {
                if (drop.getItem().is(NTBlocks.ABYSSAL_ENCHANTER.asItem())) {
                    machines += drop.getItem().getCount();
                }
                if (drop.getItem().is(Items.QUARTZ)) {
                    quartz += drop.getItem().getCount();
                }
            }
            helper.assertValueEqual(1, machines, "one enchanter drops");
            helper.assertValueEqual(7, quartz, "its inventory drops too");
            helper.succeed();
        });

        r.add("enchanter/tank_takes_only_experience", 20, helper -> {
            placed(helper);
            ServerLevel level = helper.getLevel();
            ResourceHandler<FluidResource> side = level.getCapability(TransferCapabilities.Fluid.BLOCK, helper.absolutePos(CORE.offset(-1, 0, 0)), Direction.WEST);
            helper.assertTrue(side != null, "a part exposes the experience tank");
            FluidResource xp = FluidResource.of(NTFluids.EXPERIENCE_ALGAE.getStillFluid());
            helper.assertValueEqual(0, insert(side, FluidResource.of(Fluids.WATER), 1_000), "water is refused");
            helper.assertValueEqual(1_000, insert(side, xp, 1_000), "liquid experience goes in");
            helper.assertValueEqual(400, extract(side, xp, 400), "it can be piped back out");
            helper.assertValueEqual(Integer.MAX_VALUE, helper.getBlockEntity(CORE, AbyssalEnchanterBlockEntity.class).getFluidTank().getCapacity(),
                    "the tank holds a full int of mB");
            helper.succeed();
        });

        r.add("enchanter/costs_follow_level_and_rarity", 20, helper -> {
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Holder<Enchantment> fortune = enchantment(helper, Enchantments.FORTUNE);
            Holder<Enchantment> mending = enchantment(helper, Enchantments.MENDING);
            helper.assertValueEqual(10, EnchantingRules.sustainedPower(sharpness, 1, 1F), "Sharpness I beam");
            helper.assertValueEqual(250, EnchantingRules.sustainedPower(sharpness, 5, 1F), "Sharpness V beam");
            helper.assertValueEqual(250_000L, EnchantingRules.totalPower(sharpness, 5, 1F), "Sharpness V total");
            helper.assertValueEqual(21_780, EnchantingRules.experienceMb(sharpness, 5, 1F), "Sharpness V experience");
            helper.assertValueEqual(360, EnchantingRules.sustainedPower(fortune, 3, 1F), "Fortune III beam, a rare enchantment");
            helper.assertValueEqual(80, EnchantingRules.sustainedPower(mending, 1, 1F), "treasure enchantments count double");
            helper.assertValueEqual(500, EnchantingRules.sustainedPower(sharpness, 5, 2F), "the recipe multiplier scales the cost");
            helper.succeed();
        });

        r.add("enchanter/overenchant_costs_rise_exponentially", 20, helper -> {
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            boolean previousEnabled = NTConfig.abyssalEnchanterOverenchant;
            double previousFactor = NTConfig.abyssalEnchanterOverenchantFactor;
            try {
                helper.assertValueEqual(10, EnchantingRules.levelCap(sharpness), "five levels past the normal max by default");
                NTConfig.abyssalEnchanterOverenchantFactor = 1.0;
                long flatTotal = EnchantingRules.totalPower(sharpness, 7, 1F);
                int flatExperience = EnchantingRules.experienceMb(sharpness, 7, 1F);
                NTConfig.abyssalEnchanterOverenchantFactor = previousFactor;
                helper.assertValueEqual(1_728_000L, EnchantingRules.totalPower(sharpness, 6, 1F), "Sharpness VI costs 4x its flat total");
                helper.assertValueEqual(flatTotal * 16, EnchantingRules.totalPower(sharpness, 7, 1F), "two levels past cost 16x");
                helper.assertValueEqual(flatExperience * 16, EnchantingRules.experienceMb(sharpness, 7, 1F), "experience rises the same way");
                helper.assertValueEqual(490, EnchantingRules.sustainedPower(sharpness, 7, 1F), "the beam follows the normal curve");
                helper.assertValueEqual(250_000L, EnchantingRules.totalPower(sharpness, 5, 1F), "levels up to the max are unchanged");
                NTConfig.abyssalEnchanterOverenchant = false;
                helper.assertValueEqual(5, EnchantingRules.levelCap(sharpness), "turned off, the cap is the normal max");
            } finally {
                NTConfig.abyssalEnchanterOverenchant = previousEnabled;
                NTConfig.abyssalEnchanterOverenchantFactor = previousFactor;
            }
            helper.succeed();
        });

        r.add("enchanter/overenchants_past_the_max", 200, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(sharpness, 5);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, sword);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
            enchanter.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 10_000_000));
            enchanter.commonTick();
            enchanter.pressButton(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_UP);
            helper.assertValueEqual(6, enchanter.getEnchantLevel(), "the selector goes past Sharpness V");
            helper.assertValueEqual(60, enchanter.getReagentNeeded(), "reagent stops rising at the normal max");
            int experience = enchanter.getExperienceMb();
            helper.onEachTick(() -> beam(enchanter, 100_000, 3F));
            helper.succeedWhen(() -> {
                ItemStack output = enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.OUTPUT_SLOT);
                helper.assertValueEqual(6, EnchantmentHelper.getEnchantmentsForCrafting(output).getLevel(sharpness), "the sword comes out with Sharpness VI");
                helper.assertValueEqual(10_000_000 - experience, enchanter.getFluidTank().getFluidAmount(), "the raised experience cost is drained");
            });
        });

        r.add("enchanter/books_never_overenchant", 20, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.DIAMOND_SWORD));
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
            enchanter.commonTick();
            helper.assertValueEqual(10, enchanter.getLevelCap(), "an item can go past Sharpness V");
            helper.assertFalse(enchanter.isBookCapped(), "an item is not held to the normal max");
            enchanter.pressButton(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_UP);
            enchanter.pressButton(AbyssalEnchanterBlockEntity.BUTTON_LEVEL_UP);
            helper.assertValueEqual(7, enchanter.getEnchantLevel(), "Sharpness VII is picked for the sword");
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.BOOK));
            enchanter.commonTick();
            helper.assertValueEqual(5, enchanter.getLevelCap(), "a book stops at the normal max");
            helper.assertValueEqual(5, enchanter.getEnchantLevel(), "the level drops back to Sharpness V");
            helper.assertTrue(enchanter.isBookCapped(), "the screen is told why");
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.DIAMOND_SWORD));
            enchanter.commonTick();
            helper.assertValueEqual(7, enchanter.getEnchantLevel(), "the picked level comes back with an item");
            helper.succeed();
        });

        r.add("enchanter/full_purity_is_exact", 20, helper -> {
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            for (int i = 0; i < 200; i++) {
                EnchantingOutcome outcome = EnchantingOutcome.roll(helper.getLevel().registryAccess(), new ItemStack(Items.BOOK), sharpness, 3, 3F,
                        helper.getLevel().getRandom());
                helper.assertValueEqual(EnchantingOutcome.Kind.EXACT, outcome.kind(), "purity 3 gives the exact enchantment");
                helper.assertTrue(outcome.curse() == null, "no curse at purity 3");
                helper.assertValueEqual(3, EnchantmentHelper.getEnchantmentsForCrafting(outcome.result()).getLevel(sharpness), "Sharpness III on the book");
            }
            helper.succeed();
        });

        r.add("enchanter/low_purity_varies_and_curses", 20, helper -> {
            Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
            int changed = 0;
            int cursed = 0;
            for (int i = 0; i < 400; i++) {
                EnchantingOutcome outcome = EnchantingOutcome.roll(helper.getLevel().registryAccess(), new ItemStack(Items.DIAMOND_SWORD), sharpness, 3, 0F,
                        helper.getLevel().getRandom());
                helper.assertTrue(outcome.result().is(Items.DIAMOND_SWORD), "the item is never destroyed");
                helper.assertFalse(EnchantmentHelper.getEnchantmentsForCrafting(outcome.result()).isEmpty(), "something is always enchanted");
                if (outcome.kind() != EnchantingOutcome.Kind.EXACT) {
                    changed++;
                }
                if (outcome.curse() != null) {
                    cursed++;
                }
            }
            helper.assertTrue(changed > 200, "purity 0 changes most results, got " + changed);
            helper.assertTrue(cursed > 10, "purity 0 sometimes curses, got " + cursed);
            for (int i = 0; i < 200; i++) {
                EnchantingOutcome outcome = EnchantingOutcome.roll(helper.getLevel().registryAccess(), new ItemStack(Items.DIAMOND_SWORD), sharpness, 3, 0.6F,
                        helper.getLevel().getRandom());
                helper.assertTrue(outcome.curse() == null, "no curses at purity 0.5 or higher");
            }
            helper.succeed();
        });

        r.add("enchanter/enchants_a_book_on_a_beam", 200, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            loadSharpnessOne(helper, enchanter);
            helper.onEachTick(() -> beam(enchanter, 20, 3F));
            helper.succeedWhen(() -> {
                ItemStack output = enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.OUTPUT_SLOT);
                helper.assertTrue(output.is(Items.ENCHANTED_BOOK), "an enchanted book comes out");
                helper.assertValueEqual(1, EnchantmentHelper.getEnchantmentsForCrafting(output).getLevel(enchantment(helper, Enchantments.SHARPNESS)),
                        "it holds Sharpness I");
                helper.assertTrue(enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT).isEmpty(), "the book is used");
                helper.assertValueEqual(52, enchanter.getItemStackHandler().getStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT).getCount(),
                        "12 quartz are used");
                helper.assertValueEqual(10_000 - EnchantingRules.experienceMb(enchantment(helper, Enchantments.SHARPNESS), 1, 1F),
                        enchanter.getFluidTank().getFluidAmount(), "the experience is drained");
            });
        });

        r.add("enchanter/weak_beam_makes_no_progress", 60, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            loadSharpnessOne(helper, enchanter);
            helper.onEachTick(() -> beam(enchanter, 5, 3F));
            helper.runAfterDelay(40, () -> {
                helper.assertValueEqual(AbyssalEnchanterBlockEntity.Status.LOW_POWER, enchanter.status(), "a 5 AP beam is below the 10 AP needed");
                helper.assertValueEqual(0L, enchanter.getProgress(), "nothing builds up");
                helper.assertFalse(enchanter.getBlockState().getValue(AbyssalEnchanterBlock.ACTIVE),
                        "the model stays idle");
                helper.succeed();
            });
        });

        r.add("enchanter/working_lights_the_model", 60, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            enchanter.getFluidTank().setFluid(new FluidStack(NTFluids.EXPERIENCE_ALGAE.getStillFluid(), 1_000_000));
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.BOOK));
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
            helper.onEachTick(() -> beam(enchanter, 300, 3F));
            helper.succeedWhen(() -> helper.assertTrue(
                    enchanter.getBlockState().getValue(AbyssalEnchanterBlock.ACTIVE),
                    "the active model shows while it works"));
        });

        r.add("enchanter/already_enchanted_item_waits", 20, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
            sword.enchant(enchantment(helper, Enchantments.SHARPNESS), 5);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, sword);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
            enchanter.commonTick();
            helper.assertValueEqual(AbyssalEnchanterBlockEntity.Status.ALREADY_HAS, enchanter.status(), "a Sharpness V sword is not enchanted again");
            ItemStack bow = new ItemStack(Items.BOW);
            enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, bow);
            enchanter.commonTick();
            helper.assertValueEqual(AbyssalEnchanterBlockEntity.Status.CANNOT_APPLY, enchanter.status(), "Sharpness does not go on a bow");
            helper.succeed();
        });

        r.add("enchanter/relay_beam_reaches_a_part", 80, helper -> {
            AbyssalEnchanterBlockEntity enchanter = placed(helper);
            BlockPos relayPos = CORE.offset(-4, 0, 0);
            helper.setBlock(relayPos, NTBlocks.PRISMARINE_RELAY.get().defaultBlockState().setValue(PrismarineLaserRelayBlock.FACING, Direction.EAST));
            PrismarineLaserRelayBlockEntity relay = helper.getBlockEntity(relayPos, PrismarineLaserRelayBlockEntity.class);
            helper.onEachTick(() -> {
                relay.setPowerPerSide(Direction.WEST, 40);
                relay.setPurityPerSide(Direction.WEST, 2F);
            });
            helper.succeedWhen(() -> helper.assertTrue(enchanter.getPower() > 0, "a relay aimed at a part powers the enchanter"));
        });
        r.add("enchanter/blacklist_tag_loads_without_its_mods", 20, helper -> {
            var tag = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(NTTags.Enchantments.ABYSSAL_ENCHANTER_BLACKLIST);
            helper.assertTrue(tag.isPresent(), "the blacklist tag loads even though Ars Elemental is missing");
            helper.assertValueEqual(0, tag.get().size(), "its optional Ars Elemental entry is skipped");
            helper.assertTrue(EnchantingRules.allowed(enchantment(helper, Enchantments.SHARPNESS)), "unlisted enchantments are allowed");
            helper.succeed();
        });

        r.add("enchanter/config_blacklist_blocks_enchantments", 20, helper -> {
            List<String> previous = NTConfig.abyssalEnchanterBlacklist;
            try {
                NTConfig.abyssalEnchanterBlacklist = List.of("minecraft:sharpness");
                AbyssalEnchanterBlockEntity enchanter = placed(helper);
                enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.TARGET_SLOT, new ItemStack(Items.BOOK));
                enchanter.getItemStackHandler().setStackInSlot(AbyssalEnchanterBlockEntity.REAGENT_SLOT, new ItemStack(Items.QUARTZ, 64));
                enchanter.commonTick();
                helper.assertValueEqual(AbyssalEnchanterBlockEntity.Status.BLOCKED, enchanter.status(), "a blocked enchantment is refused");
                helper.assertTrue(EnchantingRules.allowed(enchantment(helper, Enchantments.SMITE)), "an id entry blocks only that enchantment");
                NTConfig.abyssalEnchanterBlacklist = List.of("minecraft:*");
                helper.assertFalse(EnchantingRules.allowed(enchantment(helper, Enchantments.MENDING)), "a namespace wildcard blocks the whole mod");
            } finally {
                NTConfig.abyssalEnchanterBlacklist = previous;
            }
            helper.succeed();
        });

        r.add("enchanter/blacklisted_enchantments_never_roll", 20, helper -> {
            List<String> previous = NTConfig.abyssalEnchanterBlacklist;
            try {
                NTConfig.abyssalEnchanterBlacklist = List.of("minecraft:smite", "minecraft:bane_of_arthropods", "minecraft:vanishing_curse");
                Holder<Enchantment> sharpness = enchantment(helper, Enchantments.SHARPNESS);
                Holder<Enchantment> smite = enchantment(helper, Enchantments.SMITE);
                Holder<Enchantment> bane = enchantment(helper, Enchantments.BANE_OF_ARTHROPODS);
                Holder<Enchantment> vanishing = enchantment(helper, Enchantments.VANISHING_CURSE);
                for (int i = 0; i < 400; i++) {
                    EnchantingOutcome outcome = EnchantingOutcome.roll(helper.getLevel().registryAccess(), new ItemStack(Items.DIAMOND_SWORD), sharpness, 3, 0F,
                            helper.getLevel().getRandom());
                    var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(outcome.result());
                    helper.assertValueEqual(0, enchantments.getLevel(smite) + enchantments.getLevel(bane) + enchantments.getLevel(vanishing),
                            "a blocked enchantment never comes out of a low purity roll");
                }
            } finally {
                NTConfig.abyssalEnchanterBlacklist = previous;
            }
            helper.succeed();
        });
    }
}
