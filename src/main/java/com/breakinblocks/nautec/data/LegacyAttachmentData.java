package com.breakinblocks.nautec.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public final class LegacyAttachmentData {
    private static final List<String> MAP_ATTACHMENTS = List.of("nautec:augments", "nautec:augments_extra_data");

    public static boolean upgrade(CompoundTag attachments) {
        boolean changed = false;
        Tag guide = attachments.get("nautec:has_nautec_guide");
        if (guide instanceof NumericTag) {
            wrap(attachments, "nautec:has_nautec_guide", guide);
            changed = true;
        }
        for (String name : MAP_ATTACHMENTS) {
            Tag value = attachments.get(name);
            if (value instanceof CompoundTag map && !map.contains("value")) {
                wrap(attachments, name, value);
                changed = true;
            }
        }
        return changed;
    }

    private static void wrap(CompoundTag attachments, String name, Tag value) {
        CompoundTag wrapped = new CompoundTag();
        wrapped.put("value", value);
        attachments.put(name, wrapped);
    }

    private LegacyAttachmentData() {}
}
