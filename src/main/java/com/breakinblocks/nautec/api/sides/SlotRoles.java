package com.breakinblocks.nautec.api.sides;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

public record SlotRoles(IntSet inputs, IntSet outputs) {
    public static SlotRoles of(int[] inputs, int[] outputs) {
        return new SlotRoles(IntSets.unmodifiable(new IntOpenHashSet(inputs)), IntSets.unmodifiable(new IntOpenHashSet(outputs)));
    }

    public static int[] range(int from, int to) {
        int[] slots = new int[Math.max(0, to - from)];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = from + i;
        }
        return slots;
    }
}
