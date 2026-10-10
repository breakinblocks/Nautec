package com.breakinblocks.nautec.content.items.tiers;

import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.Tags;

public class NTToolMaterials {
    public static final Tier AQUARINE = new SimpleTier(
            Tags.Blocks.NEEDS_GOLD_TOOL,
            350,
            7f,
            3f,
            20,
            () -> Ingredient.of(NTTags.Items.REPAIRS_AQUARINE_TOOLS)
    );
}
