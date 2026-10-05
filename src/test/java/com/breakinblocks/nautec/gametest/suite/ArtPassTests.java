package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.registries.NTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Supplier;

public final class ArtPassTests {
    private static final BlockPos FLOOR = new BlockPos(4, 1, 4);
    private static final BlockPos PLANT = FLOOR.above();

    private ArtPassTests() {
    }

    public static void register(NTTestRegistrar r) {
        List<Supplier<? extends Block>> flora = List.of(NTBlocks.LUMINESCENT_ALGAE, NTBlocks.VENT_TUBEWORM);
        for (Supplier<? extends Block> plant : flora) {
            r.add("art/" + name(plant.get()) + "_lights_the_water", 100, helper -> lightsWater(helper, plant.get().defaultBlockState()));
        }

        r.add("art/hydrothermal_vent_is_a_waterlogged_cone", 20, helper -> {
            helper.setBlock(FLOOR, Blocks.STONE);
            helper.setBlock(PLANT, Blocks.WATER);
            helper.setBlock(PLANT, NTBlocks.HYDROTHERMAL_VENT.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
            BlockState vent = helper.getBlockState(PLANT);
            VoxelShape shape = vent.getShape(helper.getLevel(), helper.absolutePos(PLANT));
            helper.assertFalse(Block.isShapeFullBlock(shape), "the vent should no longer be a full cube");
            helper.assertValueEqual(shape.max(Direction.Axis.Y), 1.0, "the rim reaches the top of the block");
            helper.assertTrue(shape.bounds().getXsize() == 1.0, "the base spans the block");
            helper.assertTrue(helper.getLevel().getFluidState(helper.absolutePos(PLANT)).is(Fluids.WATER), "the cone keeps its water");
            helper.assertFalse(vent.emissiveRendering(helper.getLevel(), helper.absolutePos(PLANT)), "only the emissive layer glows");
            helper.assertTrue(vent.getLightEmission() > 0, "the vent still lights the sea floor");
            helper.succeed();
        });
    }

    private static String name(Block block) {
        return block.builtInRegistryHolder().key().identifier().getPath();
    }

    private static void lightsWater(GameTestHelper helper, BlockState plant) {
        helper.assertTrue(plant.getLightEmission() > 0, name(plant.getBlock()) + " should emit light");
        helper.setBlock(FLOOR, Blocks.STONE);
        helper.setBlock(PLANT.above(), Blocks.WATER);
        helper.setBlock(PLANT, plant);
        BlockPos probe = helper.absolutePos(PLANT.above());
        helper.succeedWhen(() -> {
            int light = helper.getLevel().getBrightness(LightLayer.BLOCK, probe);
            helper.assertTrue(light >= plant.getLightEmission() - 2,
                    name(plant.getBlock()) + " should light the water above it, block light " + light);
        });
    }
}
