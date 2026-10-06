package com.breakinblocks.nautec.content.conduits;

import com.breakinblocks.nautec.NTConfig;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

public record TapRates(int itemInterval, int itemsPerOp, int fluidRate, int energyRate) {
    private static TapRates[] tiers;

    public static TapRates of(int tier) {
        TapRates[] cached = tiers;
        if (cached == null) {
            cached = build();
            tiers = cached;
        }
        return cached[Math.max(0, Math.min(cached.length - 1, tier))];
    }

    public static void reload() {
        tiers = null;
    }

    private static TapRates[] build() {
        int count = NTConfig.conduitItemIntervals.length;
        TapRates[] built = new TapRates[count];
        for (int i = 0; i < count; i++) {
            int energy = NTConfig.conduitEnergyRates[i];
            built[i] = new TapRates(Math.max(1, NTConfig.conduitItemIntervals[i]), Math.max(1, NTConfig.conduitItemsPerOp[i]),
                    Math.max(1, NTConfig.conduitFluidRates[i]), energy <= 0 ? Integer.MAX_VALUE : energy);
        }
        return built;
    }

    public int limit(ConduitChannel channel) {
        return switch (channel) {
            case ITEMS -> itemsPerOp;
            case FLUIDS -> fluidRate;
            case ENERGY -> energyRate;
        };
    }

    public int window(ConduitChannel channel) {
        return channel == ConduitChannel.ITEMS ? itemInterval : 1;
    }

    public static String compact(int value) {
        if (value >= 1_000_000) {
            return trim(value / 1_000_000.0) + "M";
        }
        if (value >= 10_000) {
            return trim(value / 1_000.0) + "k";
        }
        return Integer.toString(value);
    }

    private static String trim(double value) {
        long whole = (long) value;
        return value - whole < 0.05 ? Long.toString(whole) : String.format(Locale.ROOT, "%.1f", value);
    }

    public void describe(Consumer<Component> lines) {
        lines.accept(Component.translatable("nautec.conduit.readout.items", compact(itemsPerOp), itemInterval));
        lines.accept(Component.translatable("nautec.conduit.readout.fluids", compact(fluidRate)));
        lines.accept(energyRate == Integer.MAX_VALUE
                ? Component.translatable("nautec.conduit.readout.energy.uncapped")
                : Component.translatable("nautec.conduit.readout.energy", compact(energyRate)));
    }
}
