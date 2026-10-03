package com.breakinblocks.nautec.content.structures;

import com.breakinblocks.nautec.registries.NTBlocks;
import com.breakinblocks.nautec.registries.NTLootTables;
import com.breakinblocks.nautec.registries.NTStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.Filterable;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public class ResearchOutpostPiece extends StructurePiece {
    public static final int SIZE = 31;
    public static final int FOUNDATION = 8;
    public static final int HEIGHT = 8;
    public static final int LOGS = 4;
    public static final int LOG_PAGES = 3;

    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;

    public ResearchOutpostPiece(BlockPos origin, Rotation rotation, long seed) {
        super(NTStructures.RESEARCH_OUTPOST_PIECE.get(), 0, box(origin));
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
        setOrientation(null);
    }

    public ResearchOutpostPiece(CompoundTag tag) {
        super(NTStructures.RESEARCH_OUTPOST_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getIntOr("OX", 0), tag.getIntOr("OY", 0), tag.getIntOr("OZ", 0));
        this.rotation = Rotation.values()[Math.floorMod(tag.getIntOr("Rot", 0), Rotation.values().length)];
        this.seed = tag.getLongOr("Seed", 0L);
        setOrientation(null);
    }

    private static BoundingBox box(BlockPos origin) {
        return new BoundingBox(origin.getX(), origin.getY() - FOUNDATION, origin.getZ(),
                origin.getX() + SIZE - 1, origin.getY() + HEIGHT, origin.getZ() + SIZE - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OX", origin.getX());
        tag.putInt("OY", origin.getY());
        tag.putInt("OZ", origin.getZ());
        tag.putInt("Rot", rotation.ordinal());
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        new Builder(level, chunkBB, random).build();
    }

    public void buildAll(WorldGenLevel level, RandomSource random) {
        new Builder(level, getBoundingBox(), random).build();
    }

    public BlockPos world(int x, int y, int z) {
        int last = SIZE - 1;
        return switch (rotation) {
            case NONE -> origin.offset(x, y, z);
            case CLOCKWISE_90 -> origin.offset(last - z, y, x);
            case CLOCKWISE_180 -> origin.offset(last - x, y, last - z);
            case COUNTERCLOCKWISE_90 -> origin.offset(z, y, last - x);
        };
    }

    private int roll(int x, int y, int z, int salt, int bound) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (y * 0xC2B2AE3D27D4EB4FL) ^ (z * 0x165667B19E3779F9L) ^ (salt * 0x27D4EB2F165667C5L);
        h ^= h >>> 31;
        h *= 0x7FB5D329728EA185L;
        h ^= h >>> 27;
        return (int) Math.floorMod(h, (long) bound);
    }

    public static ItemStack researchLog(int index) {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (int page = 1; page <= LOG_PAGES; page++) {
            pages.add(Filterable.passThrough(Component.translatable("nautec.research_log." + index + ".page." + page)));
        }
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Research Log " + index),
                "Outpost Survey Team", 0, pages, true));
        return book;
    }

    private final class Builder {
        private final WorldGenLevel level;
        private final BoundingBox clip;
        private final RandomSource random;

        private Builder(WorldGenLevel level, BoundingBox clip, RandomSource random) {
            this.level = level;
            this.clip = clip;
            this.random = random;
        }

        private boolean set(int x, int y, int z, BlockState state) {
            BlockPos pos = world(x, y, z);
            if (!clip.isInside(pos)) {
                return false;
            }
            level.setBlock(pos, state.rotate(rotation), Block.UPDATE_CLIENTS);
            return true;
        }

        private BlockState wall(int x, int y, int z) {
            int r = roll(x, y, z, 1, 100);
            if (r < 30) {
                return NTBlocks.CAST_IRON_BLOCK.get().defaultBlockState();
            }
            if (r < 65) {
                return Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState();
            }
            return Blocks.WAXED_WEATHERED_CUT_COPPER.defaultBlockState();
        }

        private BlockState floor(int x, int y, int z) {
            int r = roll(x, y, z, 2, 100);
            if (r < 20) {
                return Blocks.DARK_PRISMARINE.defaultBlockState();
            }
            if (r < 26) {
                return Blocks.GRAVEL.defaultBlockState();
            }
            return NTBlocks.POLISHED_PRISMARINE.get().defaultBlockState();
        }

        private BlockState water() {
            return Blocks.WATER.defaultBlockState();
        }

        private void foundation(int x0, int z0, int x1, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = -1; y >= -FOUNDATION; y--) {
                        BlockPos pos = world(x, y, z);
                        if (!clip.isInside(pos)) {
                            break;
                        }
                        BlockState here = level.getBlockState(pos);
                        if (here.isSolidRender() && !here.getFluidState().is(FluidTags.WATER)) {
                            break;
                        }
                        set(x, y, z, Blocks.PRISMARINE_BRICKS.defaultBlockState());
                    }
                }
            }
        }

        private void room(int x0, int z0, int x1, int z1, int top) {
            foundation(x0, z0, x1, z1);
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = 0; y <= top; y++) {
                        boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                        BlockState state;
                        if (y == 0) {
                            state = floor(x, y, z);
                        } else if (y == top) {
                            state = roll(x, y, z, 3, 100) < 8 ? water() : wall(x, y, z);
                        } else if (edge) {
                            boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                            if (!corner && y >= 2 && y <= 3 && (x + z) % 3 == 0) {
                                state = Blocks.GLASS.defaultBlockState();
                            } else {
                                state = roll(x, y, z, 4, 100) < 6 ? water() : wall(x, y, z);
                            }
                        } else {
                            state = water();
                        }
                        set(x, y, z, state);
                    }
                    for (int y = top + 1; y <= top + 2; y++) {
                        BlockPos pos = world(x, y, z);
                        if (clip.isInside(pos) && !level.getBlockState(pos).isSolidRender()) {
                            set(x, y, z, water());
                        }
                    }
                }
            }
        }

        private void open(int x0, int y0, int z0, int x1, int y1, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int y = y0; y <= y1; y++) {
                    for (int z = z0; z <= z1; z++) {
                        set(x, y, z, water());
                    }
                }
            }
        }

        private void lantern(int x, int y, int z) {
            set(x, y, z, roll(x, y, z, 5, 100) < 30 ? Blocks.GLASS.defaultBlockState() : Blocks.SEA_LANTERN.defaultBlockState());
        }

        private void seagrass(int x0, int z0, int x1, int z1) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    if (roll(x, 1, z, 6, 100) < 12) {
                        set(x, 1, z, Blocks.SEAGRASS.defaultBlockState());
                    }
                }
            }
        }

        private void container(int x, int y, int z, BlockState state, ResourceKey<LootTable> table) {
            if (set(x, y, z, state)) {
                RandomizableContainer.setBlockEntityLootTable(level, random, world(x, y, z), table);
            }
        }

        private BlockState chest(Direction facing) {
            return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing).setValue(ChestBlock.WATERLOGGED, true);
        }

        private BlockState crate() {
            BlockState state = NTBlocks.RUSTY_CRATE.get().defaultBlockState();
            return state.hasProperty(BlockStateProperties.WATERLOGGED) ? state.setValue(BlockStateProperties.WATERLOGGED, true) : state;
        }

        private void build() {
            room(10, 10, 20, 20, 6);
            room(12, 0, 18, 6, 5);
            room(24, 12, 30, 18, 5);
            room(0, 12, 6, 18, 5);
            room(13, 6, 17, 10, 4);
            room(20, 13, 24, 17, 4);
            room(6, 13, 10, 17, 4);
            open(14, 1, 6, 16, 3, 10);
            open(20, 1, 14, 24, 3, 16);
            open(6, 1, 14, 10, 3, 16);

            for (int x = 14; x <= 16; x++) {
                for (int z = 14; z <= 16; z++) {
                    set(x, 6, z, Blocks.GLASS.defaultBlockState());
                }
            }
            lantern(12, 6, 12);
            lantern(18, 6, 12);
            lantern(12, 6, 18);
            lantern(18, 6, 18);
            lantern(15, 5, 3);
            lantern(27, 5, 15);
            lantern(3, 5, 15);

            seagrass(11, 11, 19, 19);
            seagrass(13, 1, 17, 5);
            seagrass(25, 13, 29, 17);
            seagrass(1, 13, 5, 17);

            BlockPos lectern = world(15, 1, 13);
            if (set(15, 1, 13, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.SOUTH).setValue(LecternBlock.HAS_BOOK, true))
                    && level.getBlockEntity(lectern) instanceof LecternBlockEntity entity) {
                entity.setBook(researchLog(1 + roll(0, 0, 0, 7, LOGS)));
            }
            set(13, 1, 12, Blocks.CRAFTING_TABLE.defaultBlockState());
            set(17, 1, 12, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
            container(11, 1, 11, crate(), NTLootTables.CRATE);
            container(19, 1, 19, crate(), NTLootTables.CRATE);
            container(19, 1, 11, chest(Direction.WEST), NTLootTables.RESEARCH_OUTPOST);

            BlockState hatch = NTBlocks.PRESSURE_HATCH.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
            set(15, 1, 20, hatch.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(15, 2, 20, hatch.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));

            set(13, 1, 1, Blocks.BREWING_STAND.defaultBlockState());
            set(15, 1, 1, Blocks.CAULDRON.defaultBlockState());
            container(17, 1, 1, chest(Direction.SOUTH), NTLootTables.RESEARCH_OUTPOST);

            container(29, 1, 13, crate(), NTLootTables.CRATE);
            container(29, 1, 17, crate(), NTLootTables.CRATE);
            if (roll(29, 2, 13, 8, 2) == 0) {
                container(29, 2, 13, crate(), NTLootTables.CRATE);
            }
            container(29, 1, 15, Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST), NTLootTables.RESEARCH_OUTPOST);

            for (int y = 1; y <= 4; y++) {
                set(1, y, 13, NTBlocks.CAST_IRON_BLOCK.get().defaultBlockState());
                set(1, y, 17, NTBlocks.CAST_IRON_BLOCK.get().defaultBlockState());
            }
            set(1, 1, 14, Blocks.WAXED_COPPER_BULB.defaultBlockState());
            set(1, 1, 16, Blocks.WAXED_COPPER_BULB.defaultBlockState());
            container(1, 1, 15, chest(Direction.EAST), NTLootTables.RESEARCH_OUTPOST);
        }
    }
}
