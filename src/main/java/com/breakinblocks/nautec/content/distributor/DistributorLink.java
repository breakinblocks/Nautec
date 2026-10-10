package com.breakinblocks.nautec.content.distributor;

import com.breakinblocks.nautec.utils.TemplateSanitizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DistributorLink {
    public static final int ITEM_REQUESTS = 9;
    public static final int FLUID_REQUESTS = 3;
    public static final int MAX_ITEM_AMOUNT = 4096;
    public static final int MAX_FLUID_AMOUNT = 1_000_000;

    private final BlockPos pos;
    private final Direction face;
    private final ItemStack[] items = new ItemStack[ITEM_REQUESTS];
    private final int[] itemAmounts = new int[ITEM_REQUESTS];
    private final FluidStack[] fluids = new FluidStack[FLUID_REQUESTS];
    private final int[] fluidAmounts = new int[FLUID_REQUESTS];

    public DistributorLink(BlockPos pos, Direction face) {
        this.pos = pos.immutable();
        this.face = face;
        Arrays.fill(items, ItemStack.EMPTY);
        Arrays.fill(fluids, FluidStack.EMPTY);
    }

    public BlockPos pos() {
        return pos;
    }

    public Direction face() {
        return face;
    }

    public ItemStack item(int slot) {
        return items[slot];
    }

    public int itemAmount(int slot) {
        return itemAmounts[slot];
    }

    public FluidStack fluid(int slot) {
        return fluids[slot];
    }

    public int fluidAmount(int slot) {
        return fluidAmounts[slot];
    }

    public void setItem(int slot, ItemStack stack) {
        items[slot] = stack.isEmpty() ? ItemStack.EMPTY : TemplateSanitizer.item(stack).copyWithCount(1);
        itemAmounts[slot] = stack.isEmpty() ? 0 : Math.max(1, Math.min(stack.getCount(), stack.getMaxStackSize()));
    }

    public void setItemAmount(int slot, int amount) {
        if (!items[slot].isEmpty()) {
            itemAmounts[slot] = Math.max(1, Math.min(amount, MAX_ITEM_AMOUNT));
        }
    }

    public void setFluid(int slot, FluidStack stack) {
        fluids[slot] = TemplateSanitizer.fluid(stack);
        fluidAmounts[slot] = stack.isEmpty() ? 0 : Math.max(1000, Math.min(stack.getAmount(), MAX_FLUID_AMOUNT));
    }

    public void setFluidAmount(int slot, int amount) {
        if (!fluids[slot].isEmpty()) {
            fluidAmounts[slot] = Math.max(1, Math.min(amount, MAX_FLUID_AMOUNT));
        }
    }

    public void save(ValueOutput out) {
        out.store("pos", BlockPos.CODEC, pos);
        out.store("face", Direction.CODEC, face);
        List<ItemStack> itemList = new ArrayList<>(Arrays.asList(items));
        out.store("items", ItemStack.OPTIONAL_CODEC.listOf(), itemList);
        out.putIntArray("item_amounts", itemAmounts);
        out.store("fluids", FluidStack.OPTIONAL_CODEC.listOf(), new ArrayList<>(Arrays.asList(fluids)));
        out.putIntArray("fluid_amounts", fluidAmounts);
    }

    public static DistributorLink load(ValueInput in) {
        DistributorLink link = new DistributorLink(in.read("pos", BlockPos.CODEC).orElse(BlockPos.ZERO),
                in.read("face", Direction.CODEC).orElse(Direction.UP));
        List<ItemStack> itemList = in.read("items", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        int[] itemCounts = in.getIntArray("item_amounts").orElse(new int[0]);
        for (int i = 0; i < Math.min(itemList.size(), ITEM_REQUESTS); i++) {
            link.items[i] = itemList.get(i);
            link.itemAmounts[i] = i < itemCounts.length ? itemCounts[i] : 0;
        }
        List<FluidStack> fluidList = in.read("fluids", FluidStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        int[] fluidCounts = in.getIntArray("fluid_amounts").orElse(new int[0]);
        for (int i = 0; i < Math.min(fluidList.size(), FLUID_REQUESTS); i++) {
            link.fluids[i] = fluidList.get(i);
            link.fluidAmounts[i] = i < fluidCounts.length ? fluidCounts[i] : 0;
        }
        return link;
    }
}
