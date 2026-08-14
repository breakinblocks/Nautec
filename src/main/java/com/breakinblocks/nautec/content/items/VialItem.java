package com.breakinblocks.nautec.content.items;

import com.breakinblocks.nautec.api.items.IFluidItem;
import com.breakinblocks.nautec.data.NTDataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

public class VialItem extends Item implements IFluidItem {
    public VialItem(Properties properties) {
        super(properties.component(NTDataComponents.FLUID.get(), SimpleFluidContent.EMPTY));
    }

    @Override
    public int getFluidCapacity() {
        return 100;
    }
}
