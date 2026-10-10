package com.breakinblocks.nautec.client.renderer.items;

import com.breakinblocks.nautec.NTConfig;
import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.client.render.AtlanteanRifleBeamRenderer;
import com.breakinblocks.nautec.client.render.NTRenderTypes;
import com.breakinblocks.nautec.client.render.ShaderPackOverlay;
import com.breakinblocks.nautec.content.items.AtlanteanRifleBeam;
import com.breakinblocks.nautec.content.items.AtlanteanRifleItem;
import com.breakinblocks.nautec.utils.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.List;
import java.util.function.BiConsumer;

public class AtlanteanRifleItemRenderer extends GeoItemRenderer<AtlanteanRifleItem> {
    private static final ResourceLocation CORE_MASK = Nautec.rl("textures/item/atlantean_rifle_core.png");
    private static final String CORE_BONE = "core";
    private static final String ROOT_BONE = "root";
    private static final Vector3f MUZZLE_LOCAL = new Vector3f(0F, 4F / 16F, -22F / 16F);
    private static final float SPIN_RADIANS_PER_TICK = (float) Math.toRadians(36.0D);
    private static final float TWO_PI = (float) (Math.PI * 2.0D);
    private static final float SHAKE_TRANSLATION = 0.32F;
    private static final float SHAKE_ROTATION = (float) Math.toRadians(1.0D);

    private @Nullable LivingEntity holder;
    private float useTicks = -1F;
    private float chargeTicks = NTConfig.rifleChargeTicks;
    private float rampTicks = NTConfig.rifleRampTicks;
    private float shakeScale = 1F;

    public AtlanteanRifleItemRenderer() {
        super(new Model());
        ((Model) this.model).renderer = this;
        addRenderLayer(new CoreChargeLayer(this));
        addRenderLayer(new MuzzleLayer(this));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        LivingEntity owner = HeldItemOwner.of(stack, displayContext);
        this.holder = owner;
        this.useTicks = owner != null && AtlanteanRifleItem.isUsing(owner) && owner.isUsingItem() ? owner.getTicksUsingItem() + partialTick : -1F;
        this.chargeTicks = owner == null ? NTConfig.rifleChargeTicks : AtlanteanRifleItem.chargeTicks(owner);
        this.rampTicks = owner == null ? (float) NTConfig.rifleRampTicks : AtlanteanRifleItem.rampTicks(owner);
        this.shakeScale = owner == null ? 1F : Math.max(0F, AtlanteanRifleItem.shakeScale(owner));
        try {
            super.renderByItem(stack, displayContext, poseStack, buffers, packedLight, packedOverlay);
        } finally {
            this.holder = null;
        }
    }

    private static boolean isHeld(@Nullable ItemDisplayContext perspective) {
        return perspective != null && (perspective.firstPerson()
                || perspective == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || perspective == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
    }

    private float rampProgress() {
        return AtlanteanRifleItem.rampProgress(this.useTicks - this.chargeTicks, this.rampTicks);
    }

    private void renderBeamFromMuzzle(PoseStack poseStack, MultiBufferSource buffers, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LivingEntity owner = this.holder;
        ItemDisplayContext perspective = this.renderPerspective;
        if (level == null || owner == null || !isHeld(perspective)) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Matrix4f pose = poseStack.last().pose();
        Matrix4f worldToRoot = worldToRoot(perspective, camera);
        Vec3 cameraPos = camera.getPosition();
        Vector3f muzzleRelative = worldToRoot.invert(new Matrix4f()).transformPosition(pose.transformPosition(new Vector3f(MUZZLE_LOCAL)));
        AtlanteanRifleBeamRenderer.trackMuzzle(owner.getId(), cameraPos.add(muzzleRelative.x, muzzleRelative.y, muzzleRelative.z));

        float firing = AtlanteanRifleItem.firingTicks(owner, partialTick);
        if (firing < 0F) {
            return;
        }

        float ramp = AtlanteanRifleItem.rampProgress(owner, firing);
        List<AtlanteanRifleBeam.Hit> segments = AtlanteanRifleBeamRenderer.trace(level, owner, partialTick);
        if (ShaderPackOverlay.shaderPackActive() && perspective.firstPerson()) {
            Vec3 muzzleWorld = cameraPos.add(muzzleRelative.x, muzzleRelative.y, muzzleRelative.z);
            for (int i = 0; i < segments.size(); i++) {
                AtlanteanRifleBeam.Hit hit = segments.get(i);
                AtlanteanRifleBeamRenderer.submitWorldBeam(cameraPos, buffers, i == 0 ? muzzleWorld : hit.origin(), hit.end(), ramp, hit.impact());
            }
            return;
        }

        Matrix4f rootToLocal = new Matrix4f(pose).invert();
        for (int i = 0; i < segments.size(); i++) {
            AtlanteanRifleBeam.Hit hit = segments.get(i);
            Vec3 from = i == 0
                    ? new Vec3(MUZZLE_LOCAL.x, MUZZLE_LOCAL.y, MUZZLE_LOCAL.z)
                    : toLocal(hit.origin(), cameraPos, worldToRoot, rootToLocal);
            AtlanteanRifleBeamRenderer.submitBeam(poseStack, buffers, from,
                    toLocal(hit.end(), cameraPos, worldToRoot, rootToLocal), ramp, hit.impact(), true);
        }
    }

    private static Vec3 toLocal(Vec3 world, Vec3 cameraPos, Matrix4f worldToRoot, Matrix4f rootToLocal) {
        Vec3 relative = world.subtract(cameraPos);
        Vector3f root = worldToRoot.transformPosition(new Vector3f((float) relative.x, (float) relative.y, (float) relative.z));
        Vector3f local = rootToLocal.transformPosition(root);
        return new Vec3(local.x, local.y, local.z);
    }

    private static Matrix4f worldToRoot(ItemDisplayContext perspective, Camera camera) {
        if (!perspective.firstPerson()) {
            return new Matrix4f();
        }
        Matrix4f viewRotation = new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
        return new Matrix4f(RenderSystem.getModelViewMatrix()).invert().mul(viewRotation);
    }

    private void adjustBones(Model model) {
        GeoBone core = model.getBone(CORE_BONE).orElse(null);
        GeoBone root = model.getBone(ROOT_BONE).orElse(null);
        float ticks = this.useTicks;
        if (ticks < 0F) {
            return;
        }
        float charge = this.chargeTicks;
        if (core != null) {
            core.setRotX(core.getInitialSnapshot().getRotX() + spinAngle(ticks, charge));
        }

        float ramp = AtlanteanRifleItem.rampProgress(ticks - charge, this.rampTicks);
        if (ramp <= 0F || root == null) {
            return;
        }
        float shake = ramp * ramp * this.shakeScale;
        root.setPosX(root.getInitialSnapshot().getOffsetX() + SHAKE_TRANSLATION * shake * wobble(ticks, 7.3F, 13.1F));
        root.setPosY(root.getInitialSnapshot().getOffsetY() + SHAKE_TRANSLATION * shake * wobble(ticks, 9.7F, 15.9F));
        root.setPosZ(root.getInitialSnapshot().getOffsetZ() + SHAKE_TRANSLATION * shake * 0.5F * wobble(ticks, 5.9F, 11.3F));
        root.setRotX(root.getInitialSnapshot().getRotX() + SHAKE_ROTATION * shake * wobble(ticks, 8.1F, 14.7F));
        root.setRotY(root.getInitialSnapshot().getRotY() + SHAKE_ROTATION * shake * wobble(ticks, 6.7F, 12.5F));
        root.setRotZ(root.getInitialSnapshot().getRotZ() + SHAKE_ROTATION * shake * wobble(ticks, 10.3F, 16.3F));
    }

    private static float wobble(float ticks, float slow, float fast) {
        return Mth.sin(ticks * slow) * 0.6F + Mth.sin(ticks * fast) * 0.4F;
    }

    static float spinAngle(float ticks, float charge) {
        float angle = ticks < charge
                ? SPIN_RADIANS_PER_TICK * ticks * ticks / (2F * charge)
                : SPIN_RADIANS_PER_TICK * (ticks - charge / 2F);
        return angle % TWO_PI;
    }

    private static final class Model extends DefaultedItemGeoModel<AtlanteanRifleItem> {
        private @Nullable AtlanteanRifleItemRenderer renderer;

        private Model() {
            super(Nautec.rl("atlantean_rifle"));
        }

        @Override
        public void addAdditionalStateData(AtlanteanRifleItem animatable, long instanceId, BiConsumer<DataTicket<AtlanteanRifleItem>, AtlanteanRifleItem> dataConsumer) {
            super.addAdditionalStateData(animatable, instanceId, dataConsumer);
            GeoStateData.put(dataConsumer, AtlanteanRifleItem.USE_TICKS, this.renderer == null ? -1F : this.renderer.useTicks);
        }

        @Override
        public void setCustomAnimations(AtlanteanRifleItem animatable, long instanceId, AnimationState<AtlanteanRifleItem> animationState) {
            super.setCustomAnimations(animatable, instanceId, animationState);
            if (this.renderer != null) {
                this.renderer.adjustBones(this);
            }
        }
    }

    private static final class CoreChargeLayer extends GeoRenderLayer<AtlanteanRifleItem> {
        private final AtlanteanRifleItemRenderer rifle;

        CoreChargeLayer(AtlanteanRifleItemRenderer renderer) {
            super(renderer);
            this.rifle = renderer;
        }

        @Override
        public void render(PoseStack poseStack, AtlanteanRifleItem animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (this.rifle.useTicks < 0F) {
                return;
            }
            float ramp = this.rifle.rampProgress();
            if (ramp <= 0F) {
                return;
            }
            int color = getRenderer().getRenderColor(animatable, partialTick, packedLight).argbInt();
            RenderType glow = NTRenderTypes.emissiveOverlay(CORE_MASK);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                    LightTexture.FULL_BRIGHT, packedOverlay, ARGB.color(Math.round(ramp * 255F), color));
        }
    }

    private static final class MuzzleLayer extends GeoRenderLayer<AtlanteanRifleItem> {
        private final AtlanteanRifleItemRenderer rifle;

        MuzzleLayer(GeoRenderer<AtlanteanRifleItem> renderer) {
            super(renderer);
            this.rifle = (AtlanteanRifleItemRenderer) renderer;
        }

        @Override
        public void renderForBone(PoseStack poseStack, AtlanteanRifleItem animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (ROOT_BONE.equals(bone.getName())) {
                this.rifle.renderBeamFromMuzzle(poseStack, bufferSource, partialTick);
            }
        }
    }
}
