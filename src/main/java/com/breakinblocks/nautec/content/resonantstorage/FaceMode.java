package com.breakinblocks.nautec.content.resonantstorage;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum FaceMode implements StringRepresentable {
    OPEN(0xFF45504A),
    PUSH(0xFFE3A72B),
    PULL(0xFF4CA3E0),
    CLOSED(0xFF8C2F2F);

    public static final FaceMode[] ALL = values();
    public static final StringRepresentable.EnumCodec<FaceMode> CODEC = StringRepresentable.fromEnum(FaceMode::values);

    private final int color;

    FaceMode(int color) {
        this.color = color;
    }

    public static FaceMode byId(int id) {
        return id >= 0 && id < ALL.length ? ALL[id] : OPEN;
    }

    public int color() {
        return color;
    }

    public boolean exposed() {
        return this != CLOSED;
    }

    public boolean automatic() {
        return this == PUSH || this == PULL;
    }

    public FaceMode next() {
        return ALL[(ordinal() + 1) % ALL.length];
    }

    public FaceMode previous() {
        return ALL[(ordinal() + ALL.length - 1) % ALL.length];
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "nautec.resonant_storage.face." + getSerializedName();
    }
}
