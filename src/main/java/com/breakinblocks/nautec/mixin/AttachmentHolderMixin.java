package com.breakinblocks.nautec.mixin;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.LegacyAttachmentData;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AttachmentHolder.class)
public abstract class AttachmentHolderMixin {
    @WrapMethod(method = "deserializeAttachments")
    private void nautec$readLegacyAttachments(ValueInput input, Operation<Void> original) {
        CompoundTag attachments = input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElse(null);
        if (attachments == null || !LegacyAttachmentData.upgrade(attachments)) {
            original.call(input);
            return;
        }
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(Nautec.LOGGER)) {
            original.call(TagValueInput.create(reporter, input.lookup(), attachments));
        }
    }
}
