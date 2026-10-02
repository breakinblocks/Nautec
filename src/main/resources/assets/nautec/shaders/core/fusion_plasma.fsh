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
    float height = texCoord0.y;

    float twist = height * 5.0 - time * 0.7;
    vec2 swirl = vec2(cos(angle + twist), sin(angle + twist)) * 1.9;
    float body = fbm(swirl + vec2(height * 4.0, -time * 0.35));
    float filaments = 1.0 - abs(fbm(swirl * 2.4 + vec2(-time * 0.55, height * 7.0)) * 2.0 - 1.0);
    filaments = pow(filaments, 7.0);
    float pulse = pow(0.5 + 0.5 * sin(height * 18.0 - time * 5.0 + body * 6.0), 4.0);

    float ends = smoothstep(0.0, 0.1, height) * smoothstep(1.0, 0.9, height);

    vec3 deep = vec3(0.02, 0.20, 0.34);
    vec3 cyan = vec3(0.22, 0.86, 0.96);
    vec3 hot = vec3(0.88, 0.72, 1.0);

    vec3 color = mix(deep, cyan, smoothstep(0.25, 0.8, body));
    color += hot * filaments * (0.5 + 0.9 * energy);
    color += cyan * pulse * 0.35 * energy;
    color *= vertexColor.rgb;

    float alpha = clamp((0.18 + 0.55 * body + 0.7 * filaments + 0.25 * pulse) * energy * ends, 0.0, 1.0);

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    alpha *= 1.0 - fog * 0.85;

    fragColor = vec4(color, alpha);
}
