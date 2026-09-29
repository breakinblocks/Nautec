#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;
    vec2 p = texCoord0 * 2.0 - 1.0;
    float r = length(p);
    if (r >= 1.0) {
        discard;
    }

    float throb = sin(time * 7.0) * 0.5 + 0.5;
    float flicker = sin(time * 43.0) * 0.5 + 0.5;
    float angle = atan(p.y, p.x);
    float rays = pow(abs(sin(angle * 2.0 + time * 1.5)), 24.0) + pow(abs(sin(angle * 3.0 - time * 2.3)), 32.0) * 0.6;
    rays *= (1.0 - r) * (1.0 - r);

    float halo = exp(-r * r * 5.0) * (1.0 - r);
    float coreSize = 0.16 + 0.05 * throb;
    float core = 1.0 - smoothstep(coreSize * 0.4, coreSize, r);

    vec3 tint = vertexColor.rgb;
    vec3 white = mix(tint, vec3(1.0), 0.88);
    vec3 color = tint * (halo * (0.75 + 0.25 * flicker) + rays * 0.55) + white * core;
    color *= vertexColor.a;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    color *= 1.0 - fog * 0.8;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
