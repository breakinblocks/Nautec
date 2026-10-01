package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.api.bacteria.BacteriaInstance;
import com.breakinblocks.nautec.content.blockentities.IncubatorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.controller.BioReactorBlockEntity;
import com.breakinblocks.nautec.content.blockentities.multiblock.part.BioReactorPartBlockEntity;
import com.breakinblocks.nautec.content.multiblocks.BioReactorMultiblock;
import com.breakinblocks.nautec.registries.NTBacterias;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ShowcaseIronProduction {
    public static final int COLONIES = 3;
    public static final int REACTOR_CATALYSTS = 9;
    public static final int INCUBATOR_CATALYSTS = 2;
    private static final int CHAIN_HEIGHT = 5;
    private static final int[] CHAIN_JUNCTIONS = {-6, -4, -2, 0};

    private ShowcaseIronProduction() {
    }

    public record Layout(BlockPos controller, BlockPos hatch, BlockPos outputChest, BlockPos incubator,
                         List<BlockPos> catalysts, int reactorPower, int incubatorPower) {
    }

    public static Layout build(ServerLevel level, ShowcaseFrame frame) {
        List<BlockPos> catalysts = new ArrayList<>();

        BlockPos controller = frame.at(0, 3, 0);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 1) {
                    ShowcaseParts.place(level, frame.at(x, 1, z), Blocks.DARK_PRISMARINE.defaultBlockState());
                }
            }
        }
        ShowcaseMultiblocks.buildAndForm(level, NTMultiblocks.BIO_REACTOR.get(), controller);

        BlockPos hatch = frame.at(0, 3, -1);
        BlockState hatchState = level.getBlockState(hatch);
        if (hatchState.hasProperty(BioReactorMultiblock.HATCH)) {
            ShowcaseParts.place(level, hatch, hatchState.setValue(BioReactorMultiblock.HATCH, true));
        }
        if (level.getBlockEntity(hatch) instanceof BioReactorPartBlockEntity part) {
            part.setLaserInput(true);
            part.setChanged();
        }

        if (level.getBlockEntity(controller) instanceof BioReactorBlockEntity reactor) {
            for (int slot = 0; slot < COLONIES; slot++) {
                reactor.getBacteriaStorage().setBacteria(slot, BacteriaInstance.withMaxStats(NTBacterias.FERROPHILES, level.registryAccess()));
            }
            for (int nutrient = 0; nutrient < reactor.getNutrientSlotCount(); nutrient++) {
                reactor.getItemStackHandler().setStackInSlot(reactor.nutrientSlot(nutrient), new ItemStack(Items.IRON_ORE, 64));
            }
            reactor.setChanged();
        }

        BlockPos outputHopper = frame.at(0, 1, 1);
        ShowcaseParts.place(level, outputHopper, Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING, frame.dir(Direction.SOUTH)));
        BlockPos outputChest = frame.at(0, 1, 2);
        ShowcaseParts.place(level, outputChest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, frame.dir(Direction.SOUTH)));

        ShowcaseParts.pillar(level, frame, -8, -1, 1, 4);
        catalysts.add(ShowcaseParts.fedCatalyst(level, frame, -8, CHAIN_HEIGHT, -1, Direction.EAST));
        for (int x : CHAIN_JUNCTIONS) {
            boolean last = x == 0;
            ShowcaseParts.pillar(level, frame, x, -3, 1, 4);
            if (last) {
                ShowcaseParts.place(level, frame.at(x, 4, 1), NTBlocks.DARK_PRISMARINE_PILLAR.get().defaultBlockState());
            } else {
                ShowcaseParts.pillar(level, frame, x, 1, 1, 4);
                ShowcaseParts.pillar(level, frame, x, -1, 1, 4);
            }
            catalysts.add(ShowcaseParts.fedCatalyst(level, frame, x, CHAIN_HEIGHT, -3, Direction.SOUTH));
            catalysts.add(ShowcaseParts.fedCatalyst(level, frame, x, CHAIN_HEIGHT, 1, Direction.NORTH));
            ShowcaseParts.junction(level, frame, x, CHAIN_HEIGHT, -1,
                    Set.of(Direction.WEST, Direction.NORTH, Direction.SOUTH),
                    Set.of(last ? Direction.DOWN : Direction.EAST));
        }

        BlockPos incubator = buildIncubator(level, frame, catalysts);

        int perCatalyst = ShowcaseParts.catalystPower(level);
        int reactorPower = REACTOR_CATALYSTS * perCatalyst;
        ShowcaseParts.sign(level, frame, 0, 1, -5,
                ShowcaseParts.title("nautec.showcase.sign.iron", "Iron Production"),
                ShowcaseParts.apLine(reactorPower));

        return new Layout(controller, hatch, outputChest, incubator, catalysts, reactorPower, INCUBATOR_CATALYSTS * perCatalyst);
    }

    private static BlockPos buildIncubator(ServerLevel level, ShowcaseFrame frame, List<BlockPos> catalysts) {
        BlockPos incubator = frame.at(5, 1, 0);
        ShowcaseParts.place(level, incubator, NTBlocks.INCUBATOR.get().defaultBlockState());
        if (level.getBlockEntity(incubator) instanceof IncubatorBlockEntity incubatorEntity) {
            incubatorEntity.getBacteriaStorage().setBacteria(0, BacteriaInstance.roll(NTBacterias.FERROPHILES, level.registryAccess()));
            incubatorEntity.setChanged();
        }
        ShowcaseParts.hopperWithChest(level, frame, 5, 1, 1, Direction.NORTH, new ItemStack(Items.IRON_ORE), 2);

        ShowcaseParts.pillar(level, frame, 7, 0, 1, 2);
        ShowcaseParts.pillar(level, frame, 5, -2, 1, 2);
        catalysts.add(ShowcaseParts.fedCatalyst(level, frame, 7, 3, 0, Direction.WEST));
        catalysts.add(ShowcaseParts.fedCatalyst(level, frame, 5, 3, -2, Direction.SOUTH));
        ShowcaseParts.junction(level, frame, 5, 3, 0, Set.of(Direction.EAST, Direction.NORTH), Set.of(Direction.DOWN));
        return incubator;
    }
}
