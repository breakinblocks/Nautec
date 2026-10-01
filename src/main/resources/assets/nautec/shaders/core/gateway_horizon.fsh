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
    vec2 p = texCoord0 * 2.0 - 1.0;
    float r = length(p);
    float open = vertexColor.a;
    float reach = open * 1.08;
    if (r >= 1.0 || r > reach) {
        discard;
    }

    float angle = atan(p.y, p.x);
    float swirl = angle + time * 0.12 + (1.0 - r) * 1.4;
    vec2 polar = vec2(cos(swirl), sin(swirl)) * r;

    vec2 flow = polar * 3.2 + vec2(time * 0.07, -time * 0.05);
    float body = fbm(flow + fbm(flow * 1.7 - time * 0.11) * 1.8);
    float filaments = 1.0 - abs(fbm(polar * 5.5 + vec2(-time * 0.09, time * 0.13)) * 2.0 - 1.0);
    filaments = pow(filaments, 9.0);

    float ripple = sin(r * 26.0 - time * 2.2 + body * 4.0) * 0.5 + 0.5;
    ripple = pow(ripple, 3.0) * (0.35 + 0.65 * r);

    float rim = smoothstep(0.82, 1.0, r);
    float centreGlow = exp(-r * r * 3.5);
    float front = smoothstep(reach - 0.12, reach, r) * step(open, 0.999);

    vec3 deep = vec3(0.03, 0.22, 0.42);
    vec3 mid = vec3(0.16, 0.58, 0.86);
    vec3 bright = vec3(0.78, 0.97, 1.0);

    vec3 color = mix(deep, mid, smoothstep(0.25, 0.75, body));
    color += mid * ripple * 0.35;
    color += bright * filaments * 0.9;
    color += bright * centreGlow * 0.45;
    color += bright * rim * 0.6;
    color += bright * front * 1.5;
    color *= vertexColor.rgb;

    float flash = clamp((1.0 - open) * 2.0, 0.0, 1.0);
    color = mix(color, bright, flash * 0.5);

    float alpha = clamp(0.72 + 0.18 * body + 0.2 * filaments + 0.3 * rim, 0.0, 0.97);

    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    float fog = total_fog_value(fragmentDistance, fragmentDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    color = mix(color, FogColor.rgb, fog * FogColor.a);

    fragColor = vec4(color, alpha);
}
