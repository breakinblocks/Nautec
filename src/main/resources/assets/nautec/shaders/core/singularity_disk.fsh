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

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec2(1.7, 9.2);
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    float time = GameTime * 1200.0;
    float energy = vertexColor.a;
    float angle = texCoord0.x * 6.2831853;
    float r = texCoord0.y;

    float twist = angle - time * 0.9 + r * 7.0;
    vec2 swirl = vec2(cos(twist), sin(twist)) * (1.2 + r * 3.0);
    float body = fbm(swirl * 1.6 + vec2(r * 5.0, -time * 0.3));
    float streaks = 1.0 - abs(fbm(vec2(angle * 3.0 + r * 11.0 - time * 1.8, r * 4.0)) * 2.0 - 1.0);
    streaks = pow(streaks, 6.0);

    vec3 cyan = vec3(0.30, 0.88, 1.0);
    vec3 violet = vec3(0.60, 0.34, 1.0);
    vec3 lilac = vec3(0.94, 0.84, 1.0);
    vec3 color = mix(cyan, violet, smoothstep(0.1, 0.9, r + (body - 0.5) * 0.5));
    color = mix(color, lilac, (1.0 - smoothstep(0.0, 0.2, r)) * 0.85);
    color += lilac * streaks * 0.6;
    color *= vertexColor.rgb;

    float inner = smoothstep(0.0, 0.05, r);
    float outer = 1.0 - smoothstep(0.5, 1.0, r);
    float beaming = 0.7 + 0.5 * sin(angle + time * 0.15);
    float alpha = (0.2 + 0.75 * body + 0.65 * streaks) * inner * outer * energy * beaming;

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    alpha *= 1.0 - fog * 0.85;

    fragColor = vec4(color, clamp(alpha, 0.0, 1.0));
}
