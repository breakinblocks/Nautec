package com.breakinblocks.nautec.gametest.suite;

import com.breakinblocks.nautec.data.LegacyAttachmentData;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.registries.NTAttachmentTypes;
import com.breakinblocks.nautec.registries.NTAugmentSlots;
import com.breakinblocks.nautec.registries.NTAugments;
import com.breakinblocks.nautec.registries.NTRecipes;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.attachment.AttachmentHolder;

public final class ClientAcceptanceRegressionTests {
    public static void register(NTTestRegistrar r) {
        r.add("acceptance/custom_recipe_network_roundtrip", 40, helper -> {
            helper.assertValueEqual(9, NTRecipes.TYPES.getEntries().size(), "Custom recipe type count");
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                for (var type : NTRecipes.TYPES.getEntries()) {
                    ByteBufCodecs.registry(Registries.RECIPE_TYPE).encode(buffer, type.get());
                    helper.assertTrue(ByteBufCodecs.registry(Registries.RECIPE_TYPE).decode(buffer) == type.get(), "Recipe type identity lost");
                }
                int recipes = 0;
                for (RecipeHolder<?> holder : helper.getLevel().getServer().getRecipeManager().recipeMap().values()) {
                    if (!holder.id().identifier().getNamespace().equals("nautec")) continue;
                    RecipeHolder.STREAM_CODEC.encode(buffer, holder);
                    RecipeHolder<?> decoded = RecipeHolder.STREAM_CODEC.decode(buffer);
                    helper.assertValueEqual(holder.id(), decoded.id(), "Recipe id after network roundtrip");
                    helper.assertTrue(holder.value().getType() == decoded.value().getType(), "Recipe type after network roundtrip");
                    recipes++;
                }
                helper.assertTrue(recipes > 0, "No recipes exercised");
                helper.assertValueEqual(0, buffer.readableBytes(), "Unread network bytes");
            } finally { buffer.release(); }
            helper.succeed();
        });
        r.add("acceptance/legacy_player_attachments_preserved", 40, helper -> {
            CompoundTag legacy = new CompoundTag();
            legacy.putBoolean("nautec:has_nautec_guide", true);
            CompoundTag augment = new CompoundTag();
            augment.putString("type", "nautec:guardian_eye");
            augment.putString("slot", "nautec:eyes");
            CompoundTag augments = new CompoundTag();
            augments.put("nautec:eyes", augment);
            legacy.put("nautec:augments", augments);
            CompoundTag state = new CompoundTag();
            state.putInt("cooldown", 73);
            CompoundTag states = new CompoundTag();
            states.put("nautec:eyes", state);
            legacy.put("nautec:augments_extra_data", states);
            var holder = new AttachmentHolder() {
                public void read(ValueInput input) { deserializeAttachments(input); }
            };
            holder.read(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), legacy));
            helper.assertTrue(holder.getData(NTAttachmentTypes.HAS_NAUTEC_GUIDE), "Legacy guide flag was lost");
            helper.assertTrue(holder.hasData(NTDataAttachments.AUGMENTS), "Legacy augment map was not loaded");
            helper.assertTrue(holder.hasData(NTDataAttachments.AUGMENTS_EXTRA_DATA), "Legacy augment state was not loaded");
            helper.assertTrue(holder.getData(NTDataAttachments.AUGMENTS).get(NTAugmentSlots.EYES.get()).getAugmentType()
                    == NTAugments.GUARDIAN_EYE.get(), "Installed augment changed during migration");
            helper.assertValueEqual(73, holder.getData(NTDataAttachments.AUGMENTS_EXTRA_DATA)
                    .get(NTAugmentSlots.EYES.get()).getIntOr("cooldown", 0), "Saved augment state changed during migration");
            helper.assertTrue(LegacyAttachmentData.upgrade(legacy), "Old data not detected");
            helper.assertTrue(!LegacyAttachmentData.upgrade(legacy), "Upgrade must be idempotent");
            helper.succeed();
        });
    }
}
