package com.breakinblocks.nautec.content.spawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.nautec.utils.valueio.ValueInput;
import com.breakinblocks.nautec.utils.valueio.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public final class SpawnerFilter {
    public static final int SIZE = 18;

    private static final Codec<Slot> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, SIZE - 1).fieldOf("slot").forGetter(Slot::slot),
            SpawnerFilterEntry.CODEC.fieldOf("entry").forGetter(Slot::entry)
    ).apply(instance, Slot::new));

    private final SpawnerFilterEntry[] entries = new SpawnerFilterEntry[SIZE];
    private boolean whitelist;
    private int count;

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public @Nullable SpawnerFilterEntry get(int slot) {
        return entries[slot];
    }

    public Optional<SpawnerFilterEntry> getOptional(int slot) {
        return Optional.ofNullable(entries[slot]);
    }

    public void set(int slot, @Nullable SpawnerFilterEntry entry) {
        entries[slot] = entry;
        count = (int) Arrays.stream(entries).filter(e -> e != null).count();
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean blocksEverything() {
        return whitelist && count == 0;
    }

    public boolean allows(ItemStack stack) {
        if (count == 0) {
            return !whitelist;
        }
        for (SpawnerFilterEntry entry : entries) {
            if (entry != null && entry.matches(stack)) {
                return whitelist;
            }
        }
        return !whitelist;
    }

    public void save(ValueOutput out) {
        out.putBoolean("whitelist", whitelist);
        ValueOutput.TypedOutputList<Slot> list = out.list("entries", SLOT_CODEC);
        for (int i = 0; i < SIZE; i++) {
            if (entries[i] != null) {
                list.add(new Slot(i, entries[i]));
            }
        }
    }

    public void load(ValueInput in) {
        whitelist = in.getBooleanOr("whitelist", false);
        Arrays.fill(entries, null);
        in.listOrEmpty("entries", SLOT_CODEC).stream().forEach(slot -> entries[slot.slot()] = slot.entry());
        count = (int) Arrays.stream(entries).filter(e -> e != null).count();
    }

    private record Slot(int slot, SpawnerFilterEntry entry) {
    }
}
