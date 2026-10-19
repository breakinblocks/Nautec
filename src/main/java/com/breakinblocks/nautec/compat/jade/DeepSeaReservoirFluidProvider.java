package com.breakinblocks.nautec.compat.jade;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirBlockEntity;
import com.breakinblocks.nautec.content.reservoir.DeepSeaReservoirPartBlock;
import com.breakinblocks.nautec.content.reservoir.ReservoirFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public enum DeepSeaReservoirFluidProvider implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
    INSTANCE;

    private static final ResourceLocation UID = Nautec.rl("deep_sea_reservoir");

    private static @Nullable DeepSeaReservoirBlockEntity reservoir(Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor block)) {
            return null;
        }
        if (block.getBlockEntity() instanceof DeepSeaReservoirBlockEntity reservoir) {
            return reservoir;
        }
        if (block.getBlock() instanceof DeepSeaReservoirPartBlock) {
            return DeepSeaReservoirPartBlock.core(block.getLevel(), block.getBlockState(), block.getPosition());
        }
        return null;
    }

    @Override
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        DeepSeaReservoirBlockEntity reservoir = reservoir(accessor);
        if (reservoir == null) {
            return null;
        }
        ReservoirFluid contents = reservoir.contents();
        JadeFluidObject fluid = contents.isEmpty()
                ? JadeFluidObject.of(Fluids.EMPTY, 0)
                : JadeFluidObject.of(contents.resource().getFluid(), contents.amount(), contents.resource().getComponentsPatch());
        return List.of(new ViewGroup<>(List.of(FluidView.writeDefault(fluid, reservoir.capacity()))));
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, null);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
