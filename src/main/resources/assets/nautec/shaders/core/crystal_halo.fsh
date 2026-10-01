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

    float edge = 1.0 - r;
    float pool = exp(-r * r * 6.0);
    float ripple = pow(0.5 + 0.5 * sin(r * 22.0 - time * 2.6), 10.0) * edge * edge;
    float angle = atan(p.y, p.x);
    float sigil = step(0.62, r) * step(r, 0.66) * (0.6 + 0.4 * step(0.0, sin(angle * 12.0 + time * 0.5)));

    vec3 color = vertexColor.rgb * (pool * 0.85 + ripple * 0.55 + sigil * 0.5 * edge) * vertexColor.a;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    color *= 1.0 - fog;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
