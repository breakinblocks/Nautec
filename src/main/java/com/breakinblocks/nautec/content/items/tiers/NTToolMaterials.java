package com.breakinblocks.nautec.content.items.tiers;

import com.breakinblocks.nautec.tags.NTTags;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.common.Tags;

public class NTToolMaterials {
    public static final ToolMaterial AQUARINE = new ToolMaterial(
            Tags.Blocks.NEEDS_GOLD_TOOL,
            350,
            7f,
            3f,
            20,
            NTTags.Items.REPAIRS_AQUARINE_TOOLS
    );
}
