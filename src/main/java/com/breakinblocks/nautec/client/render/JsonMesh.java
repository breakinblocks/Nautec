package com.breakinblocks.nautec.client.render;

import com.breakinblocks.nautec.Nautec;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonMesh {
    public static final JsonMesh PRISMARINE_CRYSTAL = new JsonMesh(Nautec.rl("meshes/prismarine_crystal.json"));
    public static final JsonMesh GATEWAY_RING = new JsonMesh(Nautec.rl("meshes/gateway_ring.json"));

    private static final List<JsonMesh> ALL = List.of(PRISMARINE_CRYSTAL, GATEWAY_RING);

    private final Identifier location;
    private Map<String, Part> parts = Map.of();

    private JsonMesh(Identifier location) {
        this.location = location;
    }

    public static void reloadAll(ResourceManager resourceManager) {
        for (JsonMesh mesh : ALL) {
            mesh.reload(resourceManager);
        }
    }

    public Part part(String name) {
        return parts.getOrDefault(name, Part.EMPTY);
    }

    private void reload(ResourceManager resourceManager) {
        Optional<Resource> resource = resourceManager.getResource(location);
        if (resource.isEmpty()) {
            Nautec.LOGGER.error("Missing mesh {}", location);
            parts = Map.of();
            return;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<String, Part> loaded = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("parts").entrySet()) {
                loaded.put(entry.getKey(), Part.parse(entry.getValue().getAsJsonObject()));
            }
            parts = Map.copyOf(loaded);
        } catch (Exception e) {
            Nautec.LOGGER.error("Failed to load mesh {}", location, e);
            parts = Map.of();
        }
    }

    public static void submitTranslucent(PoseStack poseStack, SubmitNodeCollector collector, Part part, RenderType renderType, int color) {
        if (part.triangles() == 0) {
            return;
        }
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            for (int t = 0; t < part.triangles(); t++) {
                for (int corner : Part.QUAD_CORNERS) {
                    int o = t * Part.FLOATS_PER_TRIANGLE + corner * 5;
                    buffer.addVertex(pose, part.vertices[o], part.vertices[o + 1], part.vertices[o + 2])
                            .setUv(part.vertices[o + 3], part.vertices[o + 4])
                            .setColor(color)
                            .setNormal(pose, part.normals[t * 3], part.normals[t * 3 + 1], part.normals[t * 3 + 2]);
                }
            }
        });
    }

    public static void submitLit(PoseStack poseStack, SubmitNodeCollector collector, Part part, RenderType renderType, int color, int light, int overlay) {
        if (part.triangles() == 0) {
            return;
        }
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            for (int t = 0; t < part.triangles(); t++) {
                for (int corner : Part.QUAD_CORNERS) {
                    int o = t * Part.FLOATS_PER_TRIANGLE + corner * 5;
                    litVertex(buffer, pose, part, o, t, color, light, overlay);
                }
            }
        });
    }

    private static void litVertex(VertexConsumer buffer, PoseStack.Pose pose, Part part, int o, int t, int color, int light, int overlay) {
        buffer.addVertex(pose, part.vertices[o], part.vertices[o + 1], part.vertices[o + 2])
                .setColor(color)
                .setUv(part.vertices[o + 3], part.vertices[o + 4])
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, part.normals[t * 3], part.normals[t * 3 + 1], part.normals[t * 3 + 2]);
    }

    public record Part(Vector3fc origin, float[] vertices, float[] normals) {
        public static final Part EMPTY = new Part(new Vector3f(), new float[0], new float[0]);

        public static final int FLOATS_PER_TRIANGLE = 15;

        private static final int[] QUAD_CORNERS = {0, 1, 2, 2};

        public int triangles() {
            return normals.length / 3;
        }

        private static Part parse(JsonObject json) {
            JsonArray originJson = json.getAsJsonArray("origin");
            Vector3f origin = new Vector3f(originJson.get(0).getAsFloat(), originJson.get(1).getAsFloat(), originJson.get(2).getAsFloat());
            boolean winding = json.has("winding") && json.get("winding").getAsBoolean();
            JsonArray triangles = json.getAsJsonArray("triangles");
            float[] vertices = new float[triangles.size() * FLOATS_PER_TRIANGLE];
            float[] normals = new float[triangles.size() * 3];
            for (int t = 0; t < triangles.size(); t++) {
                JsonArray tri = triangles.get(t).getAsJsonArray();
                for (int i = 0; i < FLOATS_PER_TRIANGLE; i++) {
                    vertices[t * FLOATS_PER_TRIANGLE + i] = tri.get(i).getAsFloat();
                }
                Vector3f normal = faceNormal(vertices, t * FLOATS_PER_TRIANGLE, winding);
                normals[t * 3] = normal.x;
                normals[t * 3 + 1] = normal.y;
                normals[t * 3 + 2] = normal.z;
            }
            return new Part(origin, vertices, normals);
        }

        private static Vector3f faceNormal(float[] v, int o, boolean winding) {
            Vector3f a = new Vector3f(v[o], v[o + 1], v[o + 2]);
            Vector3f b = new Vector3f(v[o + 5], v[o + 6], v[o + 7]);
            Vector3f c = new Vector3f(v[o + 10], v[o + 11], v[o + 12]);
            Vector3f normal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));
            if (normal.lengthSquared() < 1.0E-12F) {
                return new Vector3f(0F, 1F, 0F);
            }
            normal.normalize();
            if (winding) {
                return normal;
            }
            Vector3f centre = a.add(b).add(c).div(3F);
            Vector3f outward = new Vector3f(centre.x, 0F, centre.z);
            if (outward.lengthSquared() < 1.0E-6F) {
                outward.set(0F, centre.y, 0F);
            }
            return normal.dot(outward) < 0F ? normal.negate() : normal;
        }
    }
}
