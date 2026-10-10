package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.teleport.PortalRenderer;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SubmarineWorldFx {
    private static final List<SubmarineEntity> SUBMARINES = new ArrayList<>();

    private SubmarineWorldFx() {
    }

    @SubscribeEvent
    public static void onSubmitGeometry(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        PortalRenderer.render(poseStack, buffers, cameraPos, partialTick);

        if (SUBMARINES.isEmpty()) {
            buffers.endLastBatch();
            return;
        }
        SUBMARINES.removeIf(Entity::isRemoved);
        for (SubmarineEntity submarine : SUBMARINES) {
            if (submarine.level() == level && submarine.isLaserEngaged()) {
                SubmarineLaserRenderer.render(submarine, poseStack, buffers, cameraPos, partialTick);
            }
        }
        buffers.endLastBatch();
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof SubmarineEntity submarine && !SUBMARINES.contains(submarine)) {
            SUBMARINES.add(submarine);
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof SubmarineEntity submarine) {
            SUBMARINES.remove(submarine);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SUBMARINES.clear();
    }
}
