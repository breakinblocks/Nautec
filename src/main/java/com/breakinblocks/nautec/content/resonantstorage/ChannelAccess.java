package com.breakinblocks.nautec.content.resonantstorage;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ChannelAccess implements StringRepresentable {
    PRIVATE,
    TEAM,
    PUBLIC;

    public static final ChannelAccess[] ALL = values();
    public static final StringRepresentable.EnumCodec<ChannelAccess> CODEC = StringRepresentable.fromEnum(ChannelAccess::values);
    public static final StreamCodec<ByteBuf, ChannelAccess> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(ChannelAccess::byId, Enum::ordinal);

    public static ChannelAccess byId(int id) {
        return id >= 0 && id < ALL.length ? ALL[id] : PRIVATE;
    }

    public static boolean validId(int id) {
        return id >= 0 && id < ALL.length;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "nautec.resonant_storage.access." + getSerializedName();
    }
}
