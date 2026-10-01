package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.content.blockentities.ChargerBlockEntity;
import com.breakinblocks.nautec.content.blockentities.MixerBlockEntity;
import com.breakinblocks.nautec.content.blocks.PrismarineLaserRelayBlock;
import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTFluids;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Set;

public final class ShowcaseLaserPower {
    public static final int CATALYSTS = 2;

    private ShowcaseLaserPower() {
    }

    public record Layout(BlockPos junction, BlockPos charger, BlockPos mixer, List<BlockPos> catalysts, int suppliedPower) {
    }

    public static Layout build(ServerLevel level, ShowcaseFrame frame) {
        ShowcaseParts.pillar(level, frame, -3, 0, 1, 2);
        ShowcaseParts.pillar(level, frame, 3, 0, 1, 2);
        ShowcaseParts.pillar(level, frame, 0, 0, 1, 2);
        ShowcaseParts.pillar(level, frame, 0, 2, 1, 2);
        ShowcaseParts.pillar(level, frame, 0, 4, 1, 2);
        ShowcaseParts.pillar(level, frame, 0, -3, 1, 2);

        BlockPos west = ShowcaseParts.fedCatalyst(level, frame, -3, 3, 0, Direction.EAST);
        BlockPos east = ShowcaseParts.fedCatalyst(level, frame, 3, 3, 0, Direction.WEST);
        BlockPos junction = ShowcaseParts.junction(level, frame, 0, 3, 0,
                Set.of(Direction.WEST, Direction.EAST), Set.of(Direction.SOUTH, Direction.NORTH));

        ShowcaseParts.place(level, frame.at(0, 3, 2), NTBlocks.PRISMARINE_RELAY.get().defaultBlockState()
                .setValue(PrismarineLaserRelayBlock.FACING, frame.dir(Direction.SOUTH)));

        BlockPos charger = frame.at(0, 3, 4);
        ShowcaseParts.place(level, charger, NTBlocks.CHARGER.get().defaultBlockState());
        if (level.getBlockEntity(charger) instanceof ChargerBlockEntity chargerEntity) {
            chargerEntity.getItemStackHandler().setStackInSlot(0, new ItemStack(NTItems.PRISMATIC_BATTERY.get()));
            chargerEntity.setChanged();
        }

        BlockPos mixer = frame.at(0, 3, -3);
        ShowcaseParts.place(level, mixer, NTBlocks.MIXER.get().defaultBlockState());
        if (level.getBlockEntity(mixer) instanceof MixerBlockEntity mixerEntity) {
            mixerEntity.getFluidTank().setFluid(new FluidStack(NTFluids.SALT_WATER.getStillFluid(), 1000));
            mixerEntity.getItemStackHandler().setStackInSlot(0, new ItemStack(Items.RAW_IRON, 16));
            mixerEntity.getItemStackHandler().setStackInSlot(1, new ItemStack(Items.PRISMARINE_CRYSTALS, 8));
            mixerEntity.setChanged();
        }

        int supplied = CATALYSTS * ShowcaseParts.catalystPower(level);
        ShowcaseParts.sign(level, frame, 0, 1, -5,
                ShowcaseParts.title("nautec.showcase.sign.laser", "Laser Power"),
                ShowcaseParts.apLine(supplied));

        return new Layout(junction, charger, mixer, List.of(west, east), supplied);
    }
}
