package com.breakinblocks.nautec.content.showcase;

import com.breakinblocks.nautec.api.multiblocks.Multiblock;
import com.breakinblocks.nautec.api.multiblocks.MultiblockLayer;
import com.breakinblocks.nautec.api.utils.HorizontalDirection;
import com.breakinblocks.nautec.registries.NTMultiblocks;
import com.breakinblocks.nautec.utils.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;

public final class ShowcaseMultiblocks {
    private ShowcaseMultiblocks() {
    }

    public static boolean buildAndForm(ServerLevel level, Multiblock multiblock, BlockPos controllerPos) {
        HorizontalDirection direction = multiblock.getFixedDirection() != null ? multiblock.getFixedDirection() : HorizontalDirection.NORTH;
        Vec3i relative = MultiblockHelper.getRelativeControllerPos(multiblock);
        BlockPos first = MultiblockHelper.getFirstBlockPos(direction, controllerPos, relative);
        Map<Integer, Block> definition = multiblock.getDefinition();
        MultiblockLayer[] layout = multiblock.getLayout();

        for (int y = 0; y < layout.length; y++) {
            int width = multiblock.getWidths().get(y).leftInt();
            int[] layer = layout[y].layer();
            for (int i = 0; i < layer.length; i++) {
                Block block = definition.get(layer[i]);
                if (block == null) {
                    continue;
                }
                BlockPos pos = MultiblockHelper.getCurPos(first, new Vec3i(i % width, y, i / width), direction);
                ShowcaseParts.place(level, pos, block.defaultBlockState());
            }
        }

        return MultiblockHelper.form(multiblock, controllerPos, level);
    }

    public static List<BlockPos> build(ServerLevel level, ShowcaseFrame frame) {
        BlockPos station = frame.at(-5, 1, 0);
        BlockPos drain = frame.at(5, 1, 0);
        buildAndForm(level, NTMultiblocks.AUGMENTATION_STATION.get(), station);
        buildAndForm(level, NTMultiblocks.DRAIN.get(), drain);
        return List.of(station, drain);
    }
}
