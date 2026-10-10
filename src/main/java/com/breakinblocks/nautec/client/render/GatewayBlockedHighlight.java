package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.blockentities.GatewayBlockEntity;
import com.breakinblocks.nautec.content.items.PrismMonocleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class GatewayBlockedHighlight {
    private static final int COLOR = 0x66FF2020;
    private static final float INFLATE = 0.004F;

    private GatewayBlockedHighlight() {
    }

    @SubscribeEvent
    public static void onSubmitGeometry(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null || !wearsMonocle(player)
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || !(level.getBlockEntity(hit.getBlockPos()) instanceof GatewayBlockEntity gateway)) {
            return;
        }

        List<BlockPos> blocked = gateway.blockingCells();
        if (blocked.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.debugQuads());
        PoseStack.Pose pose = poseStack.last();
        for (BlockPos pos : blocked) {
            box(buffer, pose, pos.getX() - INFLATE, pos.getY() - INFLATE, pos.getZ() - INFLATE,
                    pos.getX() + 1 + INFLATE, pos.getY() + 1 + INFLATE, pos.getZ() + 1 + INFLATE);
        }
        buffers.endBatch(RenderType.debugQuads());
        poseStack.popPose();
    }

    private static boolean wearsMonocle(Player player) {
        return PrismMonocleItem.isWorn(player);
    }

    private static void box(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1) {
        quad(buffer, pose, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(buffer, pose, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(buffer, pose, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(buffer, pose, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(buffer, pose, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(buffer, pose, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        buffer.addVertex(pose, ax, ay, az).setColor(COLOR);
        buffer.addVertex(pose, bx, by, bz).setColor(COLOR);
        buffer.addVertex(pose, cx, cy, cz).setColor(COLOR);
        buffer.addVertex(pose, dx, dy, dz).setColor(COLOR);
    }
}
