package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.sonar.SonarHighlightRenderer;
import com.breakinblocks.nautec.client.sonar.SonarPulseRenderer;
import com.breakinblocks.nautec.client.teleport.PortalRenderer;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineWorldFx {
    private SubmarineWorldFx() {
    }

    @SubscribeEvent
    public static void onSubmitGeometry(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        SonarHighlightRenderer.render(poseStack, collector, cameraPos);
        SonarPulseRenderer.render(poseStack, collector, cameraPos);
        PortalRenderer.render(poseStack, collector, cameraPos, partialTick);

        for (net.minecraft.world.entity.Entity entity : level.entitiesForRendering()) {
            if (entity instanceof SubmarineEntity submarine && submarine.isLaserActive()) {
                SubmarineLaserRenderer.render(submarine, poseStack, collector, cameraPos, partialTick);
            }
        }
    }
}
