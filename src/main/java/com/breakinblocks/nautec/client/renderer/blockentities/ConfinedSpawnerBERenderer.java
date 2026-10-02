package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.api.client.renderer.blockentities.NTBERenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.content.blockentities.ConfinedSpawnerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ConfinedSpawnerBERenderer extends NTBERenderer<ConfinedSpawnerBlockEntity, ConfinedSpawnerBERenderer.ConfinedSpawnerRenderState> {
    public static final Identifier RUNES = Nautec.rl("textures/entity/confined_spawner_runes.png");

    private static final float BAND_BOTTOM = 1.0F / 3.0F;
    private static final float BAND_TOP = 2.0F / 3.0F;
    private static final float OUTSET = 0.004F;
    private static final float SIDE_SPAN = 0.25F;
    private static final int BAND_RGB = 0x52E8FF;
    private static final float IDLE_ENERGY = 0.3F;
    private static final float DISPLAY_SCALE = 0.53125F;

    public ConfinedSpawnerBERenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ConfinedSpawnerRenderState createRenderState() {
        return new ConfinedSpawnerRenderState();
    }

    @Override
    public void extractRenderState(ConfinedSpawnerBlockEntity blockEntity, ConfinedSpawnerRenderState state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumbling);
        state.energy = blockEntity.isActive() ? 1.0F : IDLE_ENERGY;
        Entity display = blockEntity.getOrCreateDisplayEntity();
        if (display == null) {
            state.displayEntity = null;
            return;
        }
        state.displayEntity = this.context.entityRenderer().extractEntity(display, partialTick);
        state.displayEntity.lightCoords = state.lightCoords;
        state.spin = blockEntity.getSpin(partialTick);
        float longest = Math.max(display.getBbWidth(), display.getBbHeight());
        state.scale = longest > 1.0F ? DISPLAY_SCALE / longest : DISPLAY_SCALE;
    }

    @Override
    public void submit(ConfinedSpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, collector, state.displayEntity, this.context.entityRenderer(), state.spin, state.scale, camera);
        }
        int color = ((int) (state.energy * 255.0F) << 24) | BAND_RGB;
        ShaderPackOverlay.submit(poseStack, collector, NTRenderTypes.gatewayGlow(RUNES), (pose, buffer) -> {
            float low = -OUTSET;
            float high = 1.0F + OUTSET;
            side(buffer, pose, 1, low, 0, low, 0.0F, 0.0F, -1.0F, color, 0);
            side(buffer, pose, high, 1, high, 0, 1.0F, 0.0F, 0.0F, color, 1);
            side(buffer, pose, 0, high, 1, high, 0.0F, 0.0F, 1.0F, color, 0);
            side(buffer, pose, low, 0, low, 1, -1.0F, 0.0F, 0.0F, color, 1);
        });
    }

    private static void side(VertexConsumer buffer, PoseStack.Pose pose, float x0, float z0, float x1, float z1,
                             float nx, float ny, float nz, int color, int segment) {
        float u0 = segment * SIDE_SPAN;
        float u1 = u0 + SIDE_SPAN;
        vertex(buffer, pose, x0, BAND_BOTTOM, z0, u0, 1.0F, color, nx, ny, nz);
        vertex(buffer, pose, x1, BAND_BOTTOM, z1, u1, 1.0F, color, nx, ny, nz);
        vertex(buffer, pose, x1, BAND_TOP, z1, u1, 0.0F, color, nx, ny, nz);
        vertex(buffer, pose, x0, BAND_TOP, z0, u0, 0.0F, color, nx, ny, nz);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int color,
                               float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(color).setNormal(pose, nx, ny, nz);
    }

    @Override
    public AABB getRenderBoundingBox(ConfinedSpawnerBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
    }

    public static class ConfinedSpawnerRenderState extends BlockEntityRenderState {
        public @Nullable EntityRenderState displayEntity;
        public float spin;
        public float scale;
        public float energy;
    }
}
