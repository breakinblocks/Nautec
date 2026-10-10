package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public final class NTShaders {
    private static @Nullable ShaderInstance crystalShell;
    private static @Nullable ShaderInstance crystalCore;
    private static @Nullable ShaderInstance crystalHalo;
    private static @Nullable ShaderInstance gatewayGlow;
    private static @Nullable ShaderInstance gatewayHorizon;
    private static @Nullable ShaderInstance fusionPlasma;
    private static @Nullable ShaderInstance fusionField;
    private static @Nullable ShaderInstance laserBeam;
    private static @Nullable ShaderInstance laserFlare;
    private static @Nullable ShaderInstance sonarResult;
    private static @Nullable ShaderInstance sonarWave;
    private static @Nullable ShaderInstance tidalShockwave;
    private static @Nullable ShaderInstance portalArrival;
    private static @Nullable ShaderInstance teleportBlur;

    private NTShaders() {
    }

    public static void register(RegisterShadersEvent event) {
        ResourceProvider provider = event.getResourceProvider();
        VertexFormat lit = DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;
        VertexFormat glow = DefaultVertexFormat.POSITION_TEX_COLOR;
        VertexFormat screen = DefaultVertexFormat.POSITION;
        register(event, provider, "crystal_shell", lit, s -> crystalShell = s);
        register(event, provider, "crystal_core", lit, s -> crystalCore = s);
        register(event, provider, "gateway_glow", lit, s -> gatewayGlow = s);
        register(event, provider, "crystal_halo", glow, s -> crystalHalo = s);
        register(event, provider, "gateway_horizon", glow, s -> gatewayHorizon = s);
        register(event, provider, "fusion_plasma", glow, s -> fusionPlasma = s);
        register(event, provider, "fusion_field", glow, s -> fusionField = s);
        register(event, provider, "laser_beam", glow, s -> laserBeam = s);
        register(event, provider, "laser_flare", glow, s -> laserFlare = s);
        register(event, provider, "sonar_result", glow, s -> sonarResult = s);
        register(event, provider, "sonar_wave", screen, s -> sonarWave = s);
        register(event, provider, "tidal_shockwave", screen, s -> tidalShockwave = s);
        register(event, provider, "portal_arrival", screen, s -> portalArrival = s);
        register(event, provider, "teleport_blur", screen, s -> teleportBlur = s);
    }

    private static void register(RegisterShadersEvent event, ResourceProvider provider, String name, VertexFormat format, Consumer<ShaderInstance> onLoaded) {
        try {
            event.registerShader(new ShaderInstance(provider, Nautec.rl(name), format), onLoaded);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load NauTec shader " + name, e);
        }
    }

    public static @Nullable ShaderInstance crystalShell() {
        return crystalShell;
    }

    public static @Nullable ShaderInstance crystalCore() {
        return crystalCore;
    }

    public static @Nullable ShaderInstance crystalHalo() {
        return crystalHalo;
    }

    public static @Nullable ShaderInstance gatewayGlow() {
        return gatewayGlow;
    }

    public static @Nullable ShaderInstance gatewayHorizon() {
        return gatewayHorizon;
    }

    public static @Nullable ShaderInstance fusionPlasma() {
        return fusionPlasma;
    }

    public static @Nullable ShaderInstance fusionField() {
        return fusionField;
    }

    public static @Nullable ShaderInstance laserBeam() {
        return laserBeam;
    }

    public static @Nullable ShaderInstance laserFlare() {
        return laserFlare;
    }

    public static @Nullable ShaderInstance sonarResult() {
        return sonarResult;
    }

    public static @Nullable ShaderInstance sonarWave() {
        return sonarWave;
    }

    public static @Nullable ShaderInstance tidalShockwave() {
        return tidalShockwave;
    }

    public static @Nullable ShaderInstance portalArrival() {
        return portalArrival;
    }

    public static @Nullable ShaderInstance teleportBlur() {
        return teleportBlur;
    }
}
