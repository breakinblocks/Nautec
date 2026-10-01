package com.breakinblocks.nautec.datagen;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.registries.NTDamageTypes;
import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagProvider extends TagsProvider<DamageType> {
    public DamageTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Registries.DAMAGE_TYPE, registries, Nautec.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(DamageTypeTags.BYPASSES_COOLDOWN).add(NTDamageTypes.PARTICLE_BEAM);
        tag(DamageTypeTags.NO_KNOCKBACK).add(NTDamageTypes.TIDAL_SHOCKWAVE);
        tag(NTTags.DamageTypes.ATLANTEAN_RIFLE).add(NTDamageTypes.PARTICLE_BEAM);
    }

    private TagAppender<ResourceKey<DamageType>, DamageType> tag(TagKey<DamageType> tag) {
        return TagAppender.forBuilder(getOrCreateRawBuilder(tag));
    }
}
