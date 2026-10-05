package com.breakinblocks.nautec.content.biometank;

import java.util.Locale;
import com.breakinblocks.nautec.api.blockentities.ContainerBlockEntity;
import com.breakinblocks.nautec.api.blocks.blockentities.ContainerBlock;
import com.breakinblocks.nautec.registries.NTBlockEntityTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


public class BiomeTankBlock extends ContainerBlock {
    private static final Codec<BiomeTankType> TYPE_CODEC = Codec.STRING.xmap(id -> BiomeTankType.valueOf(id.toUpperCase(Locale.ROOT)), BiomeTankType::id);
    public static final MapCodec<BiomeTankBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TYPE_CODEC.fieldOf("tank_type").forGetter(BiomeTankBlock::getType),
            propertiesCodec()
    ).apply(instance, BiomeTankBlock::new));

    private final BiomeTankType type;

    public BiomeTankBlock(BiomeTankType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public BiomeTankType getType() {
        return type;
    }

    @Override
    public boolean tickingEnabled() {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BiomeTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack taken = tank.takeOutput();
            if (!taken.isEmpty()) {
                player.getInventory().placeItemBackInInventory(taken);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntityType<? extends ContainerBlockEntity> getBlockEntityType() {
        return NTBlockEntityTypes.BIOME_TANK.get();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
