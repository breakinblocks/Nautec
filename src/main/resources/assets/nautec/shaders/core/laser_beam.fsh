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
    float across = abs(texCoord0.x * 2.0 - 1.0);
    float along = texCoord0.y;

    float travel = pow(sin(along * 2.5 - time * 30.0) * 0.5 + 0.5, 4.0);
    float packets = pow(sin(along * 7.0 - time * 52.0) * 0.5 + 0.5, 12.0);
    float ripple = sin(along * 19.0 - time * 41.0 + sin(along * 2.5 + time * 4.0) * 2.0) * 0.5 + 0.5;
    float throb = sin(time * 7.0) * 0.5 + 0.5;
    float pulse = 0.62 + 0.3 * travel + 0.05 * ripple + 0.12 * throb;

    float coreWidth = 0.1 + 0.08 * travel + 0.04 * packets + 0.02 * throb;
    float core = 1.0 - smoothstep(coreWidth * 0.3, coreWidth, across);
    float halo = (1.0 - across) * (1.0 - across) * 0.5 + exp(-across * across * 9.0) * 0.55;
    float sheath = exp(-across * across * 40.0) * (0.55 + 0.45 * packets);

    vec3 tint = vertexColor.rgb;
    vec3 white = mix(tint, vec3(1.0), 0.9);
    vec3 color = tint * (halo * pulse + sheath * (0.8 + 0.2 * ripple)) + white * core * (0.8 + 0.2 * throb + 0.3 * packets);
    color *= vertexColor.a;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = (1.0 - linear_fog_fade(fragmentDistance, FogStart, FogEnd));
    color *= 1.0 - fog * 0.8;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
