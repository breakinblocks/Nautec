package com.breakinblocks.nautec.api.sides;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumMap;
import java.util.Map;

public final class SideConfig {
    private final Map<RelativeFace, SideMode> items = new EnumMap<>(RelativeFace.class);
    private final Map<RelativeFace, SideMode> fluids = new EnumMap<>(RelativeFace.class);

    public SideConfig() {
        reset();
    }

    public void reset() {
        for (RelativeFace face : RelativeFace.values()) {
            items.put(face, SideMode.BOTH);
            fluids.put(face, SideMode.BOTH);
        }
    }

    private Map<RelativeFace, SideMode> map(SideKind kind) {
        return kind == SideKind.ITEMS ? items : fluids;
    }

    public SideMode get(SideKind kind, RelativeFace face) {
        return map(kind).get(face);
    }

    public void set(SideKind kind, RelativeFace face, SideMode mode) {
        map(kind).put(face, mode);
    }

    public void copyFrom(SideConfig other) {
        items.putAll(other.items);
        fluids.putAll(other.fluids);
    }

    public void save(ValueOutput out) {
        for (SideKind kind : SideKind.values()) {
            ValueOutput child = out.child(kind.getSerializedName());
            for (RelativeFace face : RelativeFace.values()) {
                child.store(face.getSerializedName(), SideMode.CODEC, get(kind, face));
            }
        }
    }

    public void load(ValueInput in) {
        reset();
        for (SideKind kind : SideKind.values()) {
            in.child(kind.getSerializedName()).ifPresent(child -> {
                for (RelativeFace face : RelativeFace.values()) {
                    child.read(face.getSerializedName(), SideMode.CODEC).ifPresent(mode -> set(kind, face, mode));
                }
            });
        }
    }
}
