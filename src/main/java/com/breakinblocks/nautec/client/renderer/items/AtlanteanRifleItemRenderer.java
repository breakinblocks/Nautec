package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.AtlanteanRifleBeamRenderer;
import com.breakinblocks.nautec.content.items.AtlanteanRifleBeam;
import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedItemGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AtlanteanRifleItemRenderer extends GeoItemRenderer<AtlanteanRifleItem> {
    public static final DataTicket<Integer> OWNER_ID = DataTicket.create("nautec:atlantean_rifle_owner", Integer.class);

    private static final Identifier CORE_MASK = Nautec.rl("textures/item/atlantean_rifle_core.png");
    private static final String CORE_BONE = "core";
    private static final String ROOT_BONE = "root";
    private static final Vector3f MUZZLE_LOCAL = new Vector3f(0F, 4F / 16F, -22F / 16F);
    private static final float SPIN_RADIANS_PER_TICK = (float) Math.toRadians(36.0D);
    private static final float TWO_PI = (float) (Math.PI * 2.0D);
    private static final int FULL_BRIGHT = 15728880;
    private static final float SHAKE_TRANSLATION = 0.32F;
    private static final float SHAKE_ROTATION = (float) Math.toRadians(1.0D);

    public AtlanteanRifleItemRenderer() {
        super(new DefaultedItemGeoModel<>(Nautec.rl("atlantean_rifle")));
        withRenderLayer(new CoreChargeLayer(this));
    }

    @Override
    public void addRenderData(AtlanteanRifleItem item, RenderData data, GeoRenderState state, float partialTick) {
        super.addRenderData(item, data, state, partialTick);
        LivingEntity holder = data.itemOwner() == null ? null : data.itemOwner().asLivingEntity();
        float ticks = holder != null && AtlanteanRifleItem.isUsing(holder) ? holder.getTicksUsingItem(partialTick) : -1F;
        state.addGeckolibData(AtlanteanRifleItem.USE_TICKS, ticks);
        state.addGeckolibData(OWNER_ID, holder == null ? -1 : holder.getId());
    }

    @Override
    public void preRenderPass(RenderPassInfo<GeoRenderState> pass, SubmitNodeCollector tasks) {
        super.preRenderPass(pass, tasks);
        ItemDisplayContext perspective = pass.renderState().getOrDefaultGeckolibData(DataTickets.ITEM_RENDER_PERSPECTIVE, ItemDisplayContext.NONE);
        if (!isHeld(perspective) || pass.getOrDefaultGeckolibData(OWNER_ID, -1) < 0) {
            return;
        }
        pass.model().getBone(ROOT_BONE).ifPresent(bone -> pass.addPerBoneRender(bone, AtlanteanRifleItemRenderer::renderBeamFromMuzzle));
    }

    private static boolean isHeld(ItemDisplayContext perspective) {
        return perspective.firstPerson()
                || perspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || perspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static void renderBeamFromMuzzle(RenderPassInfo<GeoRenderState> pass, GeoBone bone, SubmitNodeCollector tasks) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !(level.getEntity(pass.getOrDefaultGeckolibData(OWNER_ID, -1)) instanceof LivingEntity holder)) {
            return;
        }

        Matrix4f pose = pass.poseStack().last().pose();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
        Vector3f muzzleRelative = pose.transformPosition(new Vector3f(MUZZLE_LOCAL));
        AtlanteanRifleBeamRenderer.trackMuzzle(holder.getId(), cameraPos.add(muzzleRelative.x, muzzleRelative.y, muzzleRelative.z));

        float partialTick = pass.getOrDefaultGeckolibData(DataTickets.PARTIAL_TICK, 1.0F);
        float firing = AtlanteanRifleItem.firingTicks(holder, partialTick);
        if (firing < 0F) {
            return;
        }

        AtlanteanRifleBeam.Hit hit = AtlanteanRifleBeam.trace(level, holder, NTConfig.rifleRange, partialTick);
        Vec3 hitRelative = hit.end().subtract(cameraPos);
        Vector3f hitLocal = new Matrix4f(pose).invert().transformPosition(new Vector3f((float) hitRelative.x, (float) hitRelative.y, (float) hitRelative.z));
        float radiusScale = 1F / Math.max(0.01F, pose.getScale(new Vector3f()).x);

        AtlanteanRifleBeamRenderer.submitBeam(pass.poseStack(), tasks,
                new Vec3(MUZZLE_LOCAL.x, MUZZLE_LOCAL.y, MUZZLE_LOCAL.z),
                new Vec3(hitLocal.x, hitLocal.y, hitLocal.z),
                radiusScale, firing, level.getGameTime(), partialTick);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> pass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(pass, snapshots);
        float ticks = pass.getOrDefaultGeckolibData(AtlanteanRifleItem.USE_TICKS, -1F);
        if (ticks < 0F) {
            return;
        }
        float angle = spinAngle(ticks);
        snapshots.ifPresent(CORE_BONE, snapshot -> snapshot.setRotX(angle));

        float ramp = AtlanteanRifleItem.rampProgress(ticks - NTConfig.rifleChargeTicks);
        if (ramp <= 0F) {
            return;
        }
        float shake = ramp * ramp;
        snapshots.ifPresent(ROOT_BONE, snapshot -> snapshot
                .setTranslation(
                        SHAKE_TRANSLATION * shake * wobble(ticks, 7.3F, 13.1F),
                        SHAKE_TRANSLATION * shake * wobble(ticks, 9.7F, 15.9F),
                        SHAKE_TRANSLATION * shake * 0.5F * wobble(ticks, 5.9F, 11.3F))
                .setRotation(
                        SHAKE_ROTATION * shake * wobble(ticks, 8.1F, 14.7F),
                        SHAKE_ROTATION * shake * wobble(ticks, 6.7F, 12.5F),
                        SHAKE_ROTATION * shake * wobble(ticks, 10.3F, 16.3F)));
    }

    private static float wobble(float ticks, float slow, float fast) {
        return Mth.sin(ticks * slow) * 0.6F + Mth.sin(ticks * fast) * 0.4F;
    }

    static float spinAngle(float ticks) {
        float charge = NTConfig.rifleChargeTicks;
        float angle = ticks < charge
                ? SPIN_RADIANS_PER_TICK * ticks * ticks / (2F * charge)
                : SPIN_RADIANS_PER_TICK * (ticks - charge / 2F);
        return angle % TWO_PI;
    }

    private static class CoreChargeLayer extends TextureLayerGeoLayer<AtlanteanRifleItem, GeoItemRenderer.RenderData, GeoRenderState> {
        CoreChargeLayer(GeoRenderer<AtlanteanRifleItem, GeoItemRenderer.RenderData, GeoRenderState> renderer) {
            super(renderer, CORE_MASK, RenderTypes::entityTranslucentEmissive);
        }

        @Override
        public void submitRenderTask(RenderPassInfo<GeoRenderState> pass, SubmitNodeCollector tasks) {
            float ticks = pass.getOrDefaultGeckolibData(AtlanteanRifleItem.USE_TICKS, -1F);
            float ramp = AtlanteanRifleItem.rampProgress(ticks - NTConfig.rifleChargeTicks);
            if (ramp <= 0F) {
                return;
            }

            GeoRenderState state = pass.renderState();
            int color = pass.renderColor();
            int light = pass.packedLight();
            state.addGeckolibData(DataTickets.RENDER_COLOR, ARGB.color(Math.round(ramp * 255F), color));
            state.addGeckolibData(DataTickets.PACKED_LIGHT, FULL_BRIGHT);
            super.submitRenderTask(pass, tasks);
            state.addGeckolibData(DataTickets.RENDER_COLOR, color);
            state.addGeckolibData(DataTickets.PACKED_LIGHT, light);
        }
    }
}
