package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.api.multiblocks.MultiblockData;
import com.breakinblocks.nautec.api.multiblocks.MultiblockLayer;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.content.bacteria.SimpleCollapsedStats;
import com.breakinblocks.nautec.content.blockentities.AquaticCatalystBlockEntity;
import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.CrateBlockEntity;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MutatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.OilBarrelBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.DrainBlockEntity;
import com.breakinblocks.nautec.data.NTDataComponents;
import com.breakinblocks.nautec.data.components.ComponentPowerStorage;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.apache.commons.lang3.IntegerRange;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.breakinblocks.nautec.content.blockentities.EnergyConverterBlockEntity;

public final class PersistenceTests {
    private static final BlockPos SOURCE_POS = new BlockPos(2, 1, 2);
    private static final BlockPos TARGET_POS = new BlockPos(6, 1, 6);

    private PersistenceTests() {
    }

    public static void register(NTTestRegistrar r) {
        r.add("persistence/charger_item_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            ChargerBlockEntity source = placePair(helper, NTBlocks.CHARGER.get(), ChargerBlockEntity.class);
            ItemStack battery = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            battery.set(NTDataComponents.POWER, new ComponentPowerStorage(1234, 10000, 0.5f));
            source.getItemStackHandler().setStackInSlot(0, battery.copy());
            ChargerBlockEntity loaded = reload(helper, source, ChargerBlockEntity.class);
            ItemStack loadedStack = loaded.getItemStackHandler().getStackInSlot(0);
            helper.assertTrue(ItemStack.isSameItemSameComponents(battery, loadedStack),
                    "charger slot item or components changed across save/load");
            helper.assertValueEqual(1, loadedStack.getCount(), "charger slot count");
            helper.assertValueEqual(new ComponentPowerStorage(1234, 10000, 0.5f),
                    loadedStack.get(NTDataComponents.POWER), "charger battery power component");
            helper.succeed();
        }));

        r.add("persistence/energy_converter_fe_buffer_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            EnergyConverterBlockEntity source = placePair(helper, NTBlocks.ENERGY_CONVERTER.get(), EnergyConverterBlockEntity.class);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(2500, source.getFeBuffer().insert(2500, tx), "FE buffered before save");
                tx.commit();
            }

            EnergyConverterBlockEntity loaded = reload(helper, source, EnergyConverterBlockEntity.class);
            helper.assertValueEqual(2500, loaded.getFeBuffer().getAmountAsInt(), "FE buffer across save/load");
            helper.succeed();
        }));

        r.add("persistence/mixer_state_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            MixerBlockEntity source = placePair(helper, NTBlocks.MIXER.get(), MixerBlockEntity.class);
            source.getItemStackHandler().setStackInSlot(1, new ItemStack(Items.RAW_IRON, 7));
            source.getItemStackHandler().setStackInSlot(4, new ItemStack(NTItems.AQUARINE_STEEL_COMPOUND.get(), 12));
            source.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 750));
            source.getSecondaryFluidTank().setFluid(new FluidStack(NTFluids.OIL.getStillFluid(), 250));
            MixerBlockEntity loaded = reload(helper, source, MixerBlockEntity.class);
            ItemStack slot1 = loaded.getItemStackHandler().getStackInSlot(1);
            helper.assertTrue(slot1.is(Items.RAW_IRON), "mixer slot 1 item changed");
            helper.assertValueEqual(7, slot1.getCount(), "mixer slot 1 count");
            ItemStack slot4 = loaded.getItemStackHandler().getStackInSlot(4);
            helper.assertTrue(slot4.is(NTItems.AQUARINE_STEEL_COMPOUND.get()), "mixer output slot item changed");
            helper.assertValueEqual(12, slot4.getCount(), "mixer output slot count");
            helper.assertTrue(loaded.getItemStackHandler().getStackInSlot(0).isEmpty(), "mixer slot 0 not empty");
            FluidStack input = loaded.getFluidTank().getFluid();
            helper.assertTrue(input.is(NTFluids.SALT_WATER.getStillFluid()), "mixer input fluid changed");
            helper.assertValueEqual(750, input.getAmount(), "mixer input fluid amount");
            FluidStack output = loaded.getSecondaryFluidTank().getFluid();
            helper.assertTrue(output.is(NTFluids.OIL.getStillFluid()), "mixer output fluid changed");
            helper.assertValueEqual(250, output.getAmount(), "mixer output fluid amount");
            helper.succeed();
        }));

        r.add("persistence/aquatic_catalyst_item_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            AquaticCatalystBlockEntity source = placePair(helper, NTBlocks.AQUATIC_CATALYST.get(), AquaticCatalystBlockEntity.class);
            source.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 5));
            AquaticCatalystBlockEntity loaded = reload(helper, source, AquaticCatalystBlockEntity.class);
            ItemStack loadedStack = loaded.getItemStackHandler().getStackInSlot(0);
            helper.assertTrue(loadedStack.is(Items.PRISMARINE_CRYSTALS), "catalyst slot item changed");
            helper.assertValueEqual(5, loadedStack.getCount(), "catalyst slot count");
            helper.assertValueEqual(0, loaded.getDuration(), "catalyst duration");
            helper.succeed();
        }));

        r.add("persistence/incubator_bacteria_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            IncubatorBlockEntity source = placePair(helper, NTBlocks.INCUBATOR.get(), IncubatorBlockEntity.class);
            BacteriaInstance instance = BacteriaInstance.withMaxStats(NTBacterias.CYANOBACTERIA, helper.getLevel().registryAccess());
            source.getBacteriaStorage().setBacteria(0, instance);
            source.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.SUGAR));
            IncubatorBlockEntity loaded = reload(helper, source, IncubatorBlockEntity.class);
            BacteriaInstance loadedInstance = loaded.getBacteriaStorage().getBacteria(0);
            helper.assertValueEqual(NTBacterias.CYANOBACTERIA, loadedInstance.getBacteria(), "incubator bacteria key");
            helper.assertValueEqual(instance.getSize(), loadedInstance.getSize(), "incubator bacteria size");
            helper.assertTrue(loadedInstance.isAnalyzed(), "incubator analyzed flag lost");
            SimpleCollapsedStats expected = (SimpleCollapsedStats) instance.getStats();
            SimpleCollapsedStats actual = (SimpleCollapsedStats) loadedInstance.getStats();
            helper.assertValueEqual(expected.growthRate(), actual.growthRate(), "incubator bacteria growth rate");
            helper.assertValueEqual(expected.lifespan(), actual.lifespan(), "incubator bacteria lifespan");
            helper.assertValueEqual(expected.color(), actual.color(), "incubator bacteria color");
            ItemStack nutrient = loaded.getItemStackHandler().getStackInSlot(0);
            helper.assertTrue(nutrient.is(Items.SUGAR), "incubator nutrient item changed");
            helper.assertValueEqual(1, nutrient.getCount(), "incubator nutrient count");
            helper.assertValueEqual(0, loaded.getProgress(), "incubator progress");
            helper.succeed();
        }));

        r.add("bacteria/mutator_progress_persists", 220, helper -> {
            MutatorBlockEntity source = placePair(helper, NTBlocks.MUTATOR.get(), MutatorBlockEntity.class);
            MutatorBlockEntity target = helper.getBlockEntity(TARGET_POS, MutatorBlockEntity.class);
            if (target == null) {
                helper.fail("Expected MutatorBlockEntity at " + TARGET_POS);
                return;
            }
            BacteriaMachineTests.placeShieldedSource(helper, SOURCE_POS.above(3), Direction.DOWN);
            BacteriaMachineTests.placeShieldedSource(helper, TARGET_POS.above(3), Direction.DOWN);

            source.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.LITHOPHILES, 1000,
                    BacteriaMachineTests.stats(1f, 0f, 1f, 2000), 0));
            source.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.BONE_MEAL, 4));

            int[] saved = new int[1];
            helper.runAfterDelay(80, () -> {
                saved[0] = source.getProgress();
                helper.assertTrue(saved[0] > 0, "Mutator should have accumulated progress before the reload");
                CompoundTag tag = source.saveWithoutMetadata(helper.getLevel().registryAccess());
                target.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
                helper.assertValueEqual(saved[0], target.getProgress(), "mutator progress across save and load");
                target.onLoad();
            });

            helper.runAfterDelay(160, () -> {
                helper.assertTrue(target.getProgress() > saved[0],
                        "A reloaded mutator should re-derive its recipe and keep advancing, progress was " + target.getProgress());
                helper.succeed();
            });
        });

        r.add("bacteria/bio_reactor_progress_roundtrip", 160, helper -> {
            BioReactorBlockEntity source = placePair(helper, NTBlocks.BIO_REACTOR.get(), BioReactorBlockEntity.class);
            BioReactorBlockEntity target = helper.getBlockEntity(TARGET_POS, BioReactorBlockEntity.class);
            if (target == null) {
                helper.fail("Expected BioReactorBlockEntity at " + TARGET_POS);
                return;
            }
            BacteriaMachineTests.placeShieldedSource(helper, SOURCE_POS.south(2), Direction.NORTH);
            source.getBacteriaStorage().setBacteria(0, BacteriaMachineTests.colony(NTBacterias.FERROPHILES, 400,
                    BacteriaMachineTests.stats(1f, 0f, 0.3f, 24000), 0));

            helper.runAfterDelay(60, () -> {
                float progress = source.getProgress(0);
                helper.assertTrue(progress > 0.0f, "Bio reactor should have accumulated progress before the reload");
                helper.assertTrue(progress != Math.floor(progress), "Bio reactor progress should be fractional");

                CompoundTag tag = source.saveWithoutMetadata(helper.getLevel().registryAccess());
                target.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
                helper.assertValueEqual(progress, target.getProgress(0), "bio reactor progress across save and load");

                CompoundTag legacy = source.saveWithoutMetadata(helper.getLevel().registryAccess());
                legacy.remove("progress0");
                legacy.remove("progress1");
                legacy.remove("progress2");
                legacy.putIntArray("progress", new int[]{41, 52, 63});
                target.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), legacy));
                helper.assertValueEqual(41.0f, target.getProgress(0), "legacy progress slot 0");
                helper.assertValueEqual(52.0f, target.getProgress(1), "legacy progress slot 1");
                helper.assertValueEqual(63.0f, target.getProgress(2), "legacy progress slot 2");
                helper.succeed();
            });
        });

        r.add("persistence/oil_barrel_fluid_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            OilBarrelBlockEntity source = placePair(helper, NTBlocks.OIL_BARREL.get(), OilBarrelBlockEntity.class);
            source.getFluidTank().setFluid(new FluidStack(NTFluids.OIL.getStillFluid(), 4000));
            OilBarrelBlockEntity loaded = reload(helper, source, OilBarrelBlockEntity.class);
            FluidStack fluid = loaded.getFluidTank().getFluid();
            helper.assertTrue(fluid.is(NTFluids.OIL.getStillFluid()), "oil barrel fluid changed");
            helper.assertValueEqual(4000, fluid.getAmount(), "oil barrel fluid amount");
            helper.succeed();
        }));

        r.add("persistence/crate_inventory_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            CrateBlockEntity source = placePair(helper, NTBlocks.CRATE.get(), CrateBlockEntity.class);
            source.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
            source.setItem(13, new ItemStack(NTItems.SALT.get(), 5));
            source.setItem(26, new ItemStack(Items.DIAMOND, 3));
            CrateBlockEntity loaded = reload(helper, source, CrateBlockEntity.class);
            helper.assertValueEqual(27, loaded.getContainerSize(), "crate container size");
            ItemStack first = loaded.getItem(0);
            helper.assertTrue(first.is(Items.COBBLESTONE), "crate slot 0 item changed");
            helper.assertValueEqual(32, first.getCount(), "crate slot 0 count");
            ItemStack middle = loaded.getItem(13);
            helper.assertTrue(middle.is(NTItems.SALT.get()), "crate slot 13 item changed");
            helper.assertValueEqual(5, middle.getCount(), "crate slot 13 count");
            ItemStack last = loaded.getItem(26);
            helper.assertTrue(last.is(Items.DIAMOND), "crate slot 26 item changed");
            helper.assertValueEqual(3, last.getCount(), "crate slot 26 count");
            helper.assertTrue(loaded.getItem(1).isEmpty(), "crate slot 1 not empty");
            helper.succeed();
        }));

        r.add("persistence/drain_multiblock_data_roundtrip", 40, helper -> helper.runAfterDelay(1, () -> {
            DrainBlockEntity source = placePair(helper, NTBlocks.DRAIN.get(), DrainBlockEntity.class);
            MultiblockLayer layer = new MultiblockLayer(false, IntegerRange.of(1, 1), new int[]{1, 0, 1, 0, 1, 0, 1, 0, 1});
            MultiblockData data = new MultiblockData(true, HorizontalDirection.EAST, new MultiblockLayer[]{layer});
            source.setMultiblockData(data);
            source.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 500));
            DrainBlockEntity loaded = reload(helper, source, DrainBlockEntity.class);
            MultiblockData loadedData = loaded.getMultiblockData();
            helper.assertTrue(loadedData.valid(), "multiblock valid flag lost");
            helper.assertValueEqual(HorizontalDirection.EAST, loadedData.direction(), "multiblock direction");
            helper.assertValueEqual(1, loadedData.layers().length, "multiblock layer count");
            helper.assertValueEqual(layer.save(), loadedData.layers()[0].save(), "multiblock layer data");
            FluidStack fluid = loaded.getFluidTank().getFluid();
            helper.assertTrue(fluid.is(NTFluids.SALT_WATER.getStillFluid()), "drain fluid changed");
            helper.assertValueEqual(500, fluid.getAmount(), "drain fluid amount");
            helper.succeed();
        }));

        r.add("persistence/diving_chestplate_oxygen_component", 40, helper -> helper.runAfterDelay(1, () -> {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
            ItemStack stack = new ItemStack(NTItems.DIVING_CHESTPLATE.get());
            helper.assertValueEqual(0, stack.getOrDefault(NTDataComponents.OXYGEN, -1), "default oxygen");
            stack.set(NTDataComponents.OXYGEN, 450);
            Tag encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            ItemStack decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
            helper.assertTrue(decoded.is(NTItems.DIVING_CHESTPLATE.get()), "decoded item changed");
            helper.assertValueEqual(450, decoded.getOrDefault(NTDataComponents.OXYGEN, -1), "oxygen after roundtrip");
            helper.assertTrue(ItemStack.isSameItemSameComponents(stack, decoded), "chestplate components changed across roundtrip");
            helper.succeed();
        }));

        r.add("persistence/prismatic_battery_power_component", 40, helper -> helper.runAfterDelay(1, () -> {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, helper.getLevel().registryAccess());
            ItemStack stack = new ItemStack(NTItems.PRISMATIC_BATTERY.get());
            ComponentPowerStorage initial = stack.get(NTDataComponents.POWER);
            helper.assertTrue(initial != null, "battery has no default power component");
            helper.assertValueEqual(0, initial.powerStored(), "battery default power stored");
            helper.assertValueEqual(10000, initial.powerCapacity(), "battery default power capacity");
            ComponentPowerStorage charged = new ComponentPowerStorage(2500, 10000, 0.75f);
            stack.set(NTDataComponents.POWER, charged);
            Tag encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
            ItemStack decoded = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
            helper.assertValueEqual(charged, decoded.get(NTDataComponents.POWER), "power component after roundtrip");
            helper.succeed();
        }));
    }

    private static <T extends BlockEntity> T placePair(GameTestHelper helper, Block block, Class<T> type) {
        helper.setBlock(SOURCE_POS, block);
        helper.setBlock(TARGET_POS, block);
        T be = helper.getBlockEntity(SOURCE_POS, type);
        helper.assertTrue(be != null, "block entity not created at " + SOURCE_POS);
        return be;
    }

    private static <T extends BlockEntity> T reload(GameTestHelper helper, BlockEntity source, Class<T> type) {
        CompoundTag saved = source.saveWithoutMetadata(helper.getLevel().registryAccess());
        T target = helper.getBlockEntity(TARGET_POS, type);
        helper.assertTrue(target != null, "block entity not created at " + TARGET_POS);
        target.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));
        return target;
    }
}
