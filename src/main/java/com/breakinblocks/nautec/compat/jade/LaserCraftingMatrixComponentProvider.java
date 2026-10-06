package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.LaserCraftingMatrixBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

import java.util.List;
import java.util.Locale;

public enum LaserCraftingMatrixComponentProvider implements StreamServerDataProvider<BlockAccessor, LaserCraftingMatrixComponentProvider.Data> {
    INSTANCE;

    private static final Identifier UID = Nautec.rl("laser_crafting_matrix");

    public record Data(int status, int progress, int maxProgress, int power, int requiredPower, float purity,
                       float requiredPurity, List<ItemStack> results, List<FluidStack> fluidResults) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::status,
                ByteBufCodecs.VAR_INT, Data::progress,
                ByteBufCodecs.VAR_INT, Data::maxProgress,
                ByteBufCodecs.VAR_INT, Data::power,
                ByteBufCodecs.VAR_INT, Data::requiredPower,
                ByteBufCodecs.FLOAT, Data::purity,
                ByteBufCodecs.FLOAT, Data::requiredPurity,
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(3)), Data::results,
                FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(2)), Data::fluidResults,
                Data::new
        );
    }

    @Override
    public Data streamData(BlockAccessor accessor) {
        LaserCraftingMatrixBlockEntity matrix = (LaserCraftingMatrixBlockEntity) accessor.getBlockEntity();
        LaserCraftingMatrixBlockEntity.Status status = matrix.status();
        float requiredPurity = matrix.hasRecipe() ? matrix.getRecipePurity() : matrix.getNeededPurity();
        return new Data(status.ordinal(), matrix.getProgress(), matrix.getMaxProgress(), matrix.getPower(),
                matrix.getRequiredPower(), matrix.getPurity(), requiredPurity, matrix.previewResults(), matrix.previewFluidResults());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public enum Client implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            LaserCraftingMatrixComponentProvider.INSTANCE.decodeFromData(accessor).ifPresent(data -> {
                LaserCraftingMatrixBlockEntity.Status status = LaserCraftingMatrixBlockEntity.Status.values()[data.status()];
                String purity = String.format(Locale.ROOT, "%.2f", data.purity());
                String required = String.format(Locale.ROOT, "%.2f", data.requiredPurity());
                switch (status) {
                    case NO_RECIPE -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.no_recipe").withStyle(ChatFormatting.GRAY));
                    case WORKING -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.working").withStyle(ChatFormatting.GREEN));
                    case NO_POWER -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.no_power").withStyle(ChatFormatting.RED));
                    case LOW_POWER -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.low_power",
                            data.power(), data.requiredPower()).withStyle(ChatFormatting.RED));
                    case LOW_PURITY -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.low_purity",
                            purity, required).withStyle(ChatFormatting.RED));
                    case OUTPUT_FULL -> tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.output_full").withStyle(ChatFormatting.YELLOW));
                }
                for (ItemStack result : data.results()) {
                    tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.result", result.getCount(), result.getHoverName())
                            .withStyle(ChatFormatting.GRAY));
                }
                for (FluidStack result : data.fluidResults()) {
                    tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.fluid_result", result.getAmount(), result.getHoverName())
                            .withStyle(ChatFormatting.GRAY));
                }
                if (data.maxProgress() > 0) {
                    tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.progress",
                            Math.round(100F * data.progress() / data.maxProgress()), data.progress(), data.maxProgress()));
                    tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.purity", purity, required)
                            .withStyle(data.purity() >= data.requiredPurity() ? ChatFormatting.AQUA : ChatFormatting.RED));
                } else if (data.power() > 0 || data.purity() > 0) {
                    tooltip.add(Component.translatable("nautec.jade.laser_crafting_matrix.beam", data.power(), purity).withStyle(ChatFormatting.GRAY));
                }
            });
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
