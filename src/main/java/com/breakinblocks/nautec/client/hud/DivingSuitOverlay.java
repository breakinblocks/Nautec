package com.breakinblocks.nautec.client.hud;

import com.breakinblocks.nautec.NTClientConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.data.NTDataAttachments;
import com.breakinblocks.nautec.data.NTDataComponentsUtils;
import com.breakinblocks.nautec.registries.NTItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;


public final class DivingSuitOverlay {
    private static final Identifier OXYGEN_SPRITE = Nautec.rl("hud/oxygen");
    private static final Identifier OXYGEN_BURSTING_SPRITE = Nautec.rl("hud/oxygen_bursting");
    private static final Identifier OXYGEN_EMPTY_SPRITE = Nautec.rl("hud/oxygen_empty");

    private static boolean isWearingFullDivingSuit(@NotNull Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(NTItems.DIVING_HELMET) &&
                player.getItemBySlot(EquipmentSlot.CHEST).is(NTItems.DIVING_CHESTPLATE) &&
                player.getItemBySlot(EquipmentSlot.LEGS).is(NTItems.DIVING_LEGGINGS) &&
                player.getItemBySlot(EquipmentSlot.FEET).is(NTItems.DIVING_BOOTS);
    }

    private static boolean isInAirlessSpace(@NotNull Player player) {
        return NTClientConfig.showDivingSuitAirInSpace()
                && player.level().getGameTime() < player.getData(NTDataAttachments.AIRLESS_UNTIL);
    }

    public static void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        int rightOffset = 59;
        int maxOxygen = 600;
        int spriteSize = 9;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int oxygenLevels = NTDataComponentsUtils.getOxygenLevels(player.getItemBySlot(EquipmentSlot.CHEST));
        boolean inSpace = isInAirlessSpace(player);
        if (!(player.isUnderWater() || inSpace) || !isWearingFullDivingSuit(player)) {
            return;
        }

        int xBase = guiGraphics.guiWidth() / 2 + 91;
        int yBase = guiGraphics.guiHeight() - rightOffset;

        if (oxygenLevels <= 0) {
            for (int i = 0; i < 10; i++) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OXYGEN_EMPTY_SPRITE,
                        xBase - i * spriteSize - spriteSize,
                        yBase, spriteSize, spriteSize);
            }
            return;
        }

        int visibleOxygen = Math.min(oxygenLevels, maxOxygen);
        int fullBubbles = Mth.ceil((double) (visibleOxygen - 2) * 10.0 / maxOxygen);
        int burstingBubbles = Mth.ceil((double) visibleOxygen * 10.0 / maxOxygen) - fullBubbles;


        if (player.isEyeInFluid(FluidTags.WATER) || inSpace || visibleOxygen < maxOxygen) {
            for (int i = 0; i < fullBubbles + burstingBubbles; i++) {
                boolean isBursting = i >= fullBubbles;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, isBursting ? OXYGEN_BURSTING_SPRITE : OXYGEN_SPRITE,
                        xBase - i * spriteSize - spriteSize,
                        yBase, spriteSize, spriteSize);
            }
        }

    }
}
