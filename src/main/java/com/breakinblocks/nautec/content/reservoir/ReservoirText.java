package com.breakinblocks.nautec.content.reservoir;

import java.util.Locale;

public final class ReservoirText {
    private static final String[] NUMERALS = {"I", "II", "III", "IV"};

    private ReservoirText() {
    }

    public static String numeral(int tier) {
        return NUMERALS[Math.clamp(tier, 1, NUMERALS.length) - 1];
    }

    public static String buckets(long millibuckets) {
        return compact(millibuckets / 1000L);
    }

    public static String compact(long value) {
        if (value >= 1_000_000_000_000L) {
            return trimmed(value / 1.0e12, "T");
        }
        if (value >= 1_000_000_000L) {
            return trimmed(value / 1.0e9, "B");
        }
        if (value >= 1_000_000L) {
            return trimmed(value / 1.0e6, "M");
        }
        if (value >= 1_000L) {
            return trimmed(value / 1.0e3, "k");
        }
        return Long.toString(value);
    }

    public static String full(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static String trimmed(double value, String suffix) {
        String text = String.format(Locale.ROOT, value >= 100 ? "%.0f" : "%.1f", value);
        return (text.endsWith(".0") ? text.substring(0, text.length() - 2) : text) + suffix;
    }
}
