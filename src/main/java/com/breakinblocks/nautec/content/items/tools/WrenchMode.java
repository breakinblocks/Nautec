package com.breakinblocks.nautec.content.items.tools;

import com.breakinblocks.nautec.api.sides.SideKind;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum WrenchMode {
    ROTATE(null),
    ITEM_SIDES(SideKind.ITEMS),
    FLUID_SIDES(SideKind.FLUIDS);

    private final @Nullable SideKind kind;

    WrenchMode(@Nullable SideKind kind) {
        this.kind = kind;
    }

    public @Nullable SideKind kind() {
        return kind;
    }

    public WrenchMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static WrenchMode byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : ROTATE;
    }

    public String translationKey() {
        return "nautec.wrench.mode." + name().toLowerCase(Locale.ROOT);
    }
}
