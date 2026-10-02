#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    float time = GameTime * 1200.0;
    float strength = vertexColor.a;

    vec2 p = texCoord0 * 2.2;
    const vec2 s = vec2(1.0, 1.7320508);
    vec4 centres = floor(vec4(p, p - vec2(0.5, 1.0)) / s.xyxy) + 0.5;
    vec4 offsets = vec4(p - centres.xy * s, p - (centres.zw + 0.5) * s);
    bool first = dot(offsets.xy, offsets.xy) < dot(offsets.zw, offsets.zw);
    vec2 cell = first ? offsets.xy : offsets.zw;
    vec2 id = first ? centres.xy : centres.zw + 0.5;

    vec2 q = abs(cell);
    float hex = max(dot(q, vec2(0.8660254, 0.5)), q.y);
    float line = smoothstep(0.40, 0.47, hex);

    float wave = pow(0.5 + 0.5 * sin(texCoord0.y * 1.6 - time * 2.4 + texCoord0.x * 0.4), 6.0);
    float flicker = step(0.93, hash(id + floor(time * 3.0)));
    float cellGlow = flicker * (1.0 - smoothstep(0.0, 0.45, hex)) * 0.6;

    vec3 color = vec3(0.30, 0.86, 1.0) * vertexColor.rgb;
    float alpha = (line * (0.08 + 0.42 * wave) + cellGlow * 0.6) * strength;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    alpha *= 1.0 - fog * 0.85;

    fragColor = vec4(color, clamp(alpha, 0.0, 1.0));
}
