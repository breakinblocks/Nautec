package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class SingularityLens {
    public static final int MAX = 4;

    private record Singularity(Vec3 centre, float radius, float strength, float glow) {
    }

    private static final List<Singularity> PENDING = new ArrayList<>();

    private SingularityLens() {
    }

    public static void submit(Vec3 centre, float radius, float strength, float glow) {
        if (PENDING.size() < 64) {
            PENDING.add(new Singularity(centre, radius, strength, glow));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || PENDING.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        ShaderInstance shader = NTShaders.singularityLens();
        if (shader == null || !ScreenPass.ready(target)) {
            PENDING.clear();
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        List<Singularity> chosen = new ArrayList<>(PENDING);
        PENDING.clear();
        chosen.sort(Comparator.comparingDouble(singularity -> singularity.centre().distanceToSqr(camera)));
        int count = Math.min(MAX, chosen.size());

        Matrix4f modelView = new Matrix4f(event.getModelViewMatrix());
        Matrix4f centres = new Matrix4f().zero();
        Matrix4f params = new Matrix4f().zero();
        for (int i = 0; i < count; i++) {
            Singularity singularity = chosen.get(i);
            Vec3 relative = singularity.centre().subtract(camera);
            Vector3f inView = modelView.transformPosition(new Vector3f((float) relative.x, (float) relative.y, (float) relative.z));
            centres.setColumn(i, new Vector4f(inView.x, inView.y, inView.z, singularity.radius()));
            params.setColumn(i, new Vector4f(singularity.strength(), singularity.glow(), 0F, 0F));
        }
        float seconds = (minecraft.level == null ? 0 : minecraft.level.getGameTime() % 72_000L) / 20F;

        int depth = ScreenPass.sceneDepth(target);
        shader.setSampler("SceneSampler", ScreenPass.sceneColor(target));
        shader.setSampler("DepthSampler", depth);
        shader.safeGetUniform("Screen").set((float) target.width, (float) target.height, (float) count, seconds);
        shader.safeGetUniform("Centers").set(centres);
        shader.safeGetUniform("LensParams").set(params);
        ScreenPass.draw(shader, ScreenPass.Blend.NONE, event.getProjectionMatrix());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING.clear();
    }
}
