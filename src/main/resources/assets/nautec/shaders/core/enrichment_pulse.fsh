#version 150

#moj_import <fog.glsl>

uniform mat4 ProjMat;
uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

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

    float angle = atan(p.y, p.x);
    float ring = exp(-pow((r - 0.82) * 14.0, 2.0));
    float inner = exp(-pow((r - 0.55) * 9.0, 2.0)) * 0.35;
    float shards = pow(0.5 + 0.5 * sin(angle * 9.0 + time * 3.0), 6.0) * smoothstep(0.25, 0.8, r) * (1.0 - r) * 1.6;
    float glow = (1.0 - r) * (1.0 - r) * 0.2;

    vec3 tint = vertexColor.rgb;
    vec3 white = mix(tint, vec3(1.0), 0.75);
    vec3 color = tint * (ring + inner + shards + glow) + white * ring * 0.6;
    color *= vertexColor.a;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = (1.0 - linear_fog_fade(fragmentDistance, FogStart, FogEnd));
    color *= 1.0 - fog * 0.8;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
