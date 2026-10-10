package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.utils.ItemTemplates;
import com.breakinblocks.nautec.utils.TemplateSanitizer;
import java.util.Arrays;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import com.breakinblocks.nautec.transfer.fluid.FluidResource;
import com.breakinblocks.nautec.transfer.item.ItemResource;

public final class TapFilter {
    public static final int ITEM_SLOTS = 27;
    public static final int BASIC_ITEM_SLOTS = 9;
    public static final int FLUID_SLOTS = 9;

    private final ItemStack[] items = new ItemStack[ITEM_SLOTS];
    private final ItemResource[] exactResources = new ItemResource[ITEM_SLOTS];
    private final FluidStack[] fluids = new FluidStack[FLUID_SLOTS];
    private final Fluid[] fluidTypes = new Fluid[FLUID_SLOTS];
    private int itemMask;
    private int dishMask;
    private int exactMask;
    private int fluidMask;
    private boolean whitelist = true;

    public TapFilter() {
        Arrays.fill(items, ItemStack.EMPTY);
        Arrays.fill(fluids, FluidStack.EMPTY);
    }

    public boolean whitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean isEmpty() {
        return itemMask == 0 && fluidMask == 0;
    }

    public boolean exact(int slot) {
        return (exactMask & (1 << slot)) != 0;
    }

    public void setExact(int slot, boolean exact) {
        exactMask = exact && !items[slot].isEmpty() ? exactMask | (1 << slot) : exactMask & ~(1 << slot);
    }

    public ItemStack item(int slot) {
        return items[slot];
    }

    public void setItem(int slot, ItemStack stack) {
        int bit = 1 << slot;
        if (stack.isEmpty()) {
            items[slot] = ItemStack.EMPTY;
            exactResources[slot] = null;
            itemMask &= ~bit;
            dishMask &= ~bit;
            exactMask &= ~bit;
            return;
        }
        ItemStack template = TemplateSanitizer.item(stack).copyWithCount(1);
        items[slot] = template;
        exactResources[slot] = ItemResource.of(template);
        itemMask |= bit;
        boolean dish = DishPort.isDish(template);
        dishMask = dish ? dishMask | bit : dishMask & ~bit;
        exactMask = dish && !DishPort.colonyOf(template).isEmpty() ? exactMask | bit : exactMask & ~bit;
    }

    public boolean dish(int slot) {
        return (dishMask & (1 << slot)) != 0;
    }

    public FluidStack fluid(int slot) {
        return fluids[slot];
    }

    public void setFluid(int slot, FluidStack stack) {
        int bit = 1 << slot;
        if (stack.isEmpty()) {
            fluids[slot] = FluidStack.EMPTY;
            fluidTypes[slot] = null;
            fluidMask &= ~bit;
            return;
        }
        FluidStack template = TemplateSanitizer.fluid(stack).copyWithAmount(1000);
        fluids[slot] = template;
        fluidTypes[slot] = template.getFluid();
        fluidMask |= bit;
    }

    private static int lowBits(int count) {
        return count >= Integer.SIZE ? -1 : (1 << count) - 1;
    }

    public boolean passes(ItemResource resource, int slots) {
        int bits = itemMask & lowBits(slots);
        if (bits == 0) {
            return true;
        }
        for (; bits != 0; bits &= bits - 1) {
            int slot = Integer.numberOfTrailingZeros(bits);
            int bit = 1 << slot;
            boolean matched;
            if ((exactMask & bit) == 0) {
                matched = resource.is(items[slot].getItem());
            } else if ((dishMask & bit) != 0) {
                matched = ItemTemplates.matches(items[slot], resource);
            } else {
                matched = exactResources[slot].equals(resource);
            }
            if (matched) {
                return whitelist;
            }
        }
        return !whitelist;
    }

    public boolean passes(FluidResource resource, boolean unlocked) {
        if (!unlocked || fluidMask == 0) {
            return true;
        }
        Fluid fluid = resource.getFluid();
        for (int bits = fluidMask; bits != 0; bits &= bits - 1) {
            if (fluidTypes[Integer.numberOfTrailingZeros(bits)] == fluid) {
                return whitelist;
            }
        }
        return !whitelist;
    }

    public void copyFrom(TapFilter other) {
        whitelist = other.whitelist;
        for (int slot = 0; slot < ITEM_SLOTS; slot++) {
            setItem(slot, other.items[slot]);
        }
        exactMask = other.exactMask & itemMask;
        for (int slot = 0; slot < FLUID_SLOTS; slot++) {
            setFluid(slot, other.fluids[slot]);
        }
    }

    public void sanitize(HolderLookup.Provider registries) {
        for (int bits = itemMask; bits != 0; bits &= bits - 1) {
            int slot = Integer.numberOfTrailingZeros(bits);
            boolean exact = exact(slot);
            setItem(slot, TemplateSanitizer.item(items[slot], registries));
            setExact(slot, exact);
        }
    }

    public void save(ValueOutput out) {
        out.putBoolean("whitelist", whitelist);
        out.putInt("exact_mask", exactMask);
        if (itemMask != 0) {
            ValueOutput.ValueOutputList list = out.childrenList("items");
            for (int bits = itemMask; bits != 0; bits &= bits - 1) {
                int slot = Integer.numberOfTrailingZeros(bits);
                ValueOutput child = list.addChild();
                child.putInt("slot", slot);
                child.store("stack", ItemStack.CODEC, items[slot]);
            }
        }
        if (fluidMask != 0) {
            ValueOutput.ValueOutputList list = out.childrenList("fluids");
            for (int bits = fluidMask; bits != 0; bits &= bits - 1) {
                int slot = Integer.numberOfTrailingZeros(bits);
                ValueOutput child = list.addChild();
                child.putInt("slot", slot);
                child.store("stack", FluidStack.CODEC, fluids[slot]);
            }
        }
    }

    public void clear() {
        whitelist = true;
        for (int bits = itemMask; bits != 0; bits &= bits - 1) {
            setItem(Integer.numberOfTrailingZeros(bits), ItemStack.EMPTY);
        }
        for (int bits = fluidMask; bits != 0; bits &= bits - 1) {
            setFluid(Integer.numberOfTrailingZeros(bits), FluidStack.EMPTY);
        }
    }

    public void load(ValueInput in) {
        clear();
        whitelist = in.getBooleanOr("whitelist", true);
        for (ValueInput child : in.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            if (slot >= 0 && slot < ITEM_SLOTS) {
                setItem(slot, child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY));
            }
        }
        for (ValueInput child : in.childrenListOrEmpty("fluids")) {
            int slot = child.getIntOr("slot", -1);
            if (slot >= 0 && slot < FLUID_SLOTS) {
                setFluid(slot, child.read("stack", FluidStack.CODEC).orElse(FluidStack.EMPTY));
            }
        }
        exactMask = in.getIntOr("exact_mask", 0) & itemMask;
    }

    void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(whitelist);
        buffer.writeInt(exactMask);
        buffer.writeVarInt(Integer.bitCount(itemMask));
        for (int bits = itemMask; bits != 0; bits &= bits - 1) {
            int slot = Integer.numberOfTrailingZeros(bits);
            buffer.writeByte(slot);
            ItemStack.STREAM_CODEC.encode(buffer, items[slot]);
        }
        buffer.writeVarInt(Integer.bitCount(fluidMask));
        for (int bits = fluidMask; bits != 0; bits &= bits - 1) {
            int slot = Integer.numberOfTrailingZeros(bits);
            buffer.writeByte(slot);
            FluidStack.STREAM_CODEC.encode(buffer, fluids[slot]);
        }
    }

    void read(RegistryFriendlyByteBuf buffer) {
        clear();
        whitelist = buffer.readBoolean();
        int exact = buffer.readInt();
        int itemCount = Math.min(buffer.readVarInt(), ITEM_SLOTS);
        for (int i = 0; i < itemCount; i++) {
            int slot = buffer.readByte();
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            if (slot >= 0 && slot < ITEM_SLOTS) {
                setItem(slot, stack);
            }
        }
        int fluidCount = Math.min(buffer.readVarInt(), FLUID_SLOTS);
        for (int i = 0; i < fluidCount; i++) {
            int slot = buffer.readByte();
            FluidStack stack = FluidStack.STREAM_CODEC.decode(buffer);
            if (slot >= 0 && slot < FLUID_SLOTS) {
                setFluid(slot, stack);
            }
        }
        exactMask = exact & itemMask;
    }
}
