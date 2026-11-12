#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;
    float x = texCoord0.x * 2.0 - 1.0;
    float along = texCoord0.y;

    float phase = along * 4.5 - time * 16.0;
    float sway = 0.52 * sin(phase);
    float strandA = exp(-pow((x - sway) * 6.0, 2.0)) * (0.55 + 0.45 * cos(phase));
    float strandB = exp(-pow((x + sway) * 6.0, 2.0)) * (0.55 - 0.45 * cos(phase));
    float rungs = pow(sin(along * 18.0 - time * 64.0) * 0.5 + 0.5, 14.0) * step(abs(x), abs(sway) + 0.05) * 0.5;
    float motes = pow(sin(along * 11.0 - time * 44.0 + sin(along * 3.0) * 2.0) * 0.5 + 0.5, 18.0) * exp(-x * x * 7.0);
    float haze = (1.0 - abs(x)) * (1.0 - abs(x)) * 0.12;

    vec3 tint = vertexColor.rgb;
    vec3 white = mix(tint, vec3(1.0), 0.8);
    vec3 color = tint * (strandA + strandB + haze + rungs) + white * (motes + (strandA + strandB) * 0.25);
    color *= vertexColor.a;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    color *= 1.0 - fog * 0.8;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
