package com.breakinblocks.nautec.client.renderer.blockentities;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.LaserBeamRenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.PrismarineCrystalRenderer;
import com.breakinblocks.nautec.content.blockentities.BacterialEnrichmentCatalystBlockEntity;
import com.breakinblocks.nautec.content.blocks.BacterialEnrichmentCatalystBlock;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class BacterialEnrichmentCatalystRenderer extends GeoBlockRenderer<BacterialEnrichmentCatalystBlockEntity, BlockEntityRenderState> {
    private static final Identifier GLOWMASK = Nautec.rl("textures/block/bacterial_enrichment_catalyst_glowmask.png");
    private static final DataTicket<Fx> FX = DataTicket.create("nautec:enrichment_catalyst_fx", Fx.class);

    private static final int SAFE_COLOR = 0x46E8C8;
    private static final int RISK_COLOR = 0xFF4F9A;
    private static final int SUCCESS_COLOR = 0xA8F4FF;
    private static final int REGRESSION_COLOR = 0xFF3B5C;
    private static final float PULSE_TICKS = 26F;
    private static final float NOZZLE_Y = 10F / 16F;
    private static final float NOZZLE_REACH = 9F / 16F;
    private static final float HELIX_HALF_WIDTH = 0.62F;
    private static final float HELIX_FADE = 1.6F;
    private static final float HELIX_OVERSHOOT = 0.6F;

    public BacterialEnrichmentCatalystRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(Nautec.rl("bacterial_enrichment_catalyst")));
        withRenderLayer(new GlowLayer(this));
    }

    @Override
    public void addRenderData(BacterialEnrichmentCatalystBlockEntity catalyst, Void relatedObject, BlockEntityRenderState renderState, float partialTick) {
        super.addRenderData(catalyst, relatedObject, renderState, partialTick);
        boolean active = catalyst.getBlockState().getValue(BacterialEnrichmentCatalystBlock.ACTIVE);
        renderState.addGeckolibData(BacterialEnrichmentCatalystBlockEntity.WORKING, active);
        Level level = catalyst.getLevel();
        if (level == null) {
            return;
        }
        BlockPos core = catalyst.targetCore();
        float ticks = (float) (level.getGameTime() % PrismarineCrystalRenderer.TICK_WRAP) + partialTick;
        float age = level.getGameTime() - catalyst.getOutcomeTick() + partialTick;
        renderState.addGeckolibData(FX, new Fx(active, catalyst.facing().getOpposite(), ticks, risk(catalyst),
                core != null, core == null ? 0 : core.getY() - catalyst.getBlockPos().getY(), catalyst.getLastOutcome(), age));
    }

    @Override
    public void submit(BlockEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(renderState, poseStack, collector, camera);
        Fx fx = renderState.getOrDefaultGeckolibData(FX, (Fx) null);
        if (fx == null) {
            return;
        }
        Direction back = fx.back();
        Vector3f axis = new Vector3f(0.5F + back.getStepX(), NOZZLE_Y, 0.5F + back.getStepZ());

        if (fx.active()) {
            int tint = PrismarineCrystalRenderer.blend(0xFF000000 | SAFE_COLOR, 0xFF000000 | RISK_COLOR, fx.risk()) & 0xFFFFFF;
            float flicker = 1F - fx.risk() * 0.55F * noise((int) (fx.ticks() / 2F));
            Vector3f nozzle = new Vector3f(0.5F + back.getStepX() * NOZZLE_REACH, NOZZLE_Y, 0.5F + back.getStepZ() * NOZZLE_REACH);
            LaserBeamRenderer.submitBeam(poseStack, collector, NTRenderTypes.enrichmentStream(), nozzle, axis, 0.16F,
                    argb(flicker, tint), argb(flicker, tint), 0F, true);

            if (fx.hasCore()) {
                float bottom = fx.coreOffset() - 3 - HELIX_OVERSHOOT;
                float top = fx.coreOffset() + 3 + HELIX_OVERSHOOT;
                int full = argb(0.85F * flicker, tint);
                int clear = argb(0F, tint);
                helix(poseStack, collector, axis, bottom, bottom + HELIX_FADE, clear, full, 0F);
                helix(poseStack, collector, axis, bottom + HELIX_FADE, top - HELIX_FADE, full, full, HELIX_FADE);
                helix(poseStack, collector, axis, top - HELIX_FADE, top, full, clear, top - HELIX_FADE - bottom);
            }
            LaserBeamRenderer.submitFlare(poseStack, collector, axis, 0.5F + 0.08F * Mth.sin(fx.ticks() * 0.3F), argb(0.7F * flicker, tint), true);
        }

        if (fx.outcome() != BacterialEnrichmentCatalystBlockEntity.OUTCOME_NONE && fx.age() >= 0F && fx.age() < PULSE_TICKS) {
            float progress = fx.age() / PULSE_TICKS;
            float eased = 1F - (1F - progress) * (1F - progress);
            boolean regressed = fx.outcome() == BacterialEnrichmentCatalystBlockEntity.OUTCOME_REGRESSION;
            float radius = (regressed ? 0.7F : 0.5F) + (regressed ? 2.6F : 1.6F) * eased;
            LaserBeamRenderer.submitFlare(poseStack, collector, NTRenderTypes.enrichmentPulse(), axis, radius,
                    argb(1F - progress, regressed ? REGRESSION_COLOR : SUCCESS_COLOR), true);
        }
    }

    private static void helix(PoseStack poseStack, SubmitNodeCollector collector, Vector3f axis, float fromY, float toY,
                              int colorFrom, int colorTo, float lengthOffset) {
        LaserBeamRenderer.submitBeam(poseStack, collector, NTRenderTypes.enrichmentStream(),
                new Vector3f(axis.x, fromY, axis.z), new Vector3f(axis.x, toY, axis.z), HELIX_HALF_WIDTH, colorFrom, colorTo, lengthOffset, true);
    }

    private static float risk(BacterialEnrichmentCatalystBlockEntity catalyst) {
        float overreach = catalyst.getShownCrystalPurity() - catalyst.getShownSourcePurity();
        return Mth.clamp(overreach / (float) Math.max(0.1, NTConfig.enrichmentMaxOverreach), 0F, 1F);
    }

    private static float noise(int step) {
        float value = Mth.sin(step * 12.9898F) * 43758.547F;
        return value - Mth.floor(value);
    }

    private static int argb(float alpha, int rgb) {
        return ((int) (Mth.clamp(alpha, 0F, 1F) * 255F) << 24) | (rgb & 0xFFFFFF);
    }

    @Override
    public AABB getRenderBoundingBox(BacterialEnrichmentCatalystBlockEntity catalyst) {
        return new AABB(catalyst.getBlockPos()).inflate(3.0, 6.0, 3.0);
    }

    private record Fx(boolean active, Direction back, float ticks, float risk, boolean hasCore, int coreOffset, int outcome, float age) {
    }

    private static class GlowLayer extends AutoGlowingGeoLayer<BacterialEnrichmentCatalystBlockEntity, Void, BlockEntityRenderState> {
        GlowLayer(GeoRenderer<BacterialEnrichmentCatalystBlockEntity, Void, BlockEntityRenderState> renderer) {
            super(renderer);
        }

        @Override
        protected Identifier getTextureResource(BlockEntityRenderState state) {
            return GLOWMASK;
        }

        @Override
        protected boolean shouldAddZOffset(BlockEntityRenderState state) {
            return true;
        }
    }
}
