package com.breakinblocks.nautec.client.sonar;

import com.breakinblocks.nautec.Nautec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Nautec.MODID, value = Dist.CLIENT)
public final class NautecSonarManager {
    public static final int HOSTILE_COLOR = 0xFFFF5A3C;
    private static final int MAX_SCANS = 4;
    private static final int SECTIONS_PER_TICK = 4;
    private static final Map<Integer, SonarScan> scans = new LinkedHashMap<>();
    private static @Nullable ClientLevel scanLevel;
    private static List<SonarScan> visible = List.of();

    private NautecSonarManager() { }

    public record Mark(AABB box, int color, float revealAt, Vec3 previousOffset) {
        public Mark(AABB box, int color, float revealAt) { this(box, color, revealAt, Vec3.ZERO); }
        public AABB interpolatedBox(float partialTick) {
            return previousOffset == Vec3.ZERO ? box : box.move(previousOffset.scale(1F - partialTick));
        }
    }

    public static void begin(Vec3 origin, float range, int lifetime) {
        begin(-1, origin, range, range, lifetime);
    }

    public static void begin(int source, Vec3 origin, float range, float hostileRange, int lifetime) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level != scanLevel) clear();
        scanLevel = level;
        scans.remove(source);
        if (lifetime > 0) {
            if (scans.size() == MAX_SCANS) scans.remove(scans.keySet().iterator().next());
            scans.put(source, new SonarScan(level, origin, range, hostileRange, lifetime));
        }
        visible = List.copyOf(scans.values());
    }

    public static void clear() {
        scans.clear();
        visible = List.of();
        scanLevel = null;
    }

    public static List<SonarScan> scans() {
        return scanLevel == Minecraft.getInstance().level ? visible : List.of();
    }
    public static boolean isActive() { return !scans().isEmpty(); }
    public static boolean isPulseActive() { return scans().stream().anyMatch(SonarScan::isPulseActive); }
    public static Vec3 center() { return isActive() ? visible.getLast().center() : Vec3.ZERO; }
    public static float range() { return isActive() ? visible.getLast().range() : 0F; }
    public static List<Mark> marks() { return scans().stream().flatMap(scan -> scan.marks().stream()).toList(); }
    public static List<Mark> hostiles() { return scans().stream().flatMap(scan -> scan.hostiles().stream()).toList(); }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level != scanLevel) { clear(); return; }
        int pending = (int) visible.stream().filter(SonarScan::needsScan).count();
        int budget = SECTIONS_PER_TICK;
        for (SonarScan scan : visible) {
            int share = scan.needsScan() ? budget / pending-- : 0;
            budget -= share;
            scan.tick(level, share);
        }
        if (scans.values().removeIf(scan -> !scan.active())) visible = List.copyOf(scans.values());
    }
}
