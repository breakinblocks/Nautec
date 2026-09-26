package com.breakinblocks.nautec.data;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.augments.Augment;
import com.breakinblocks.nautec.api.augments.AugmentSlot;
import com.breakinblocks.nautec.utils.codec.AugmentCodecs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.GlobalPos;
import com.breakinblocks.nautec.events.helper.ItemInfusion;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class NTDataAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nautec.MODID);

    private static final Codec<BlockPos> SPOTLIGHT_POSITION = Codec.STRING.comapFlatMap(value -> {
        try {
            return DataResult.success(BlockPos.of(Long.parseLong(value)));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid spotlight position: " + value);
        }
    }, pos -> Long.toString(pos.asLong()));
    public static final Codec<Map<BlockPos, BlockState>> SPOTLIGHT_CODEC = Codec.unboundedMap(SPOTLIGHT_POSITION, BlockState.CODEC);

    public static final Supplier<AttachmentType<Map<BlockPos, BlockState>>> SPOTLIGHT_ORIGINALS = ATTACHMENTS.register(
            "spotlight_originals", () -> AttachmentType.<Map<BlockPos, BlockState>>builder(Collections::emptyMap)
                    .serialize(SPOTLIGHT_CODEC.fieldOf("value")).build()
    );
    public static final Supplier<AttachmentType<Map<AugmentSlot, Augment>>> AUGMENTS = ATTACHMENTS.register(
            "augments", () -> AttachmentType.<Map<AugmentSlot, Augment>>builder(Collections::emptyMap)
                    .serialize(AugmentCodecs.AUGMENTS_CODEC.fieldOf("value")).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<Map<AugmentSlot, CompoundTag>>> AUGMENTS_EXTRA_DATA = ATTACHMENTS.register(
            "augments_extra_data", () -> AttachmentType.<Map<AugmentSlot, CompoundTag>>builder(Collections::emptyMap)
                    .serialize(AugmentCodecs.AUGMENTS_EXTRA_DATA_CODEC.fieldOf("value")).copyOnDeath().build()
    );
    public static final Supplier<AttachmentType<Integer>> AUGMENT_DATA_CHANGED = ATTACHMENTS.register(
            "augment_data_changed", () -> AttachmentType.builder(() -> -1).build()
    );
    public static final Supplier<AttachmentType<Optional<ItemInfusion>>> ITEM_INFUSION = ATTACHMENTS.register(
            "item_infusion", () -> AttachmentType.<Optional<ItemInfusion>>builder(Optional::empty).build()
    );
    public static final Supplier<AttachmentType<Optional<GlobalPos>>> AUGMENTATION_STATION = ATTACHMENTS.register(
            "augmentation_station", () -> AttachmentType.<Optional<GlobalPos>>builder(Optional::empty).build()
    );
}
