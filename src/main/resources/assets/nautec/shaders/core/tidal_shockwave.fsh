#version 330

uniform sampler2D DepthSampler;

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform ShockwaveInfo {
    mat4 InvViewMat;
    vec4 Center;
    vec4 Params;
};

in vec2 texCoord;

out vec4 fragColor;

const vec3 deepColor = vec3(0.02, 0.16, 0.34);
const vec3 waterColor = vec3(0.04, 0.42, 0.62);
const vec3 crestColor = vec3(0.30, 0.82, 0.94);
const vec3 foamColor = vec3(0.93, 0.98, 1.0);

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec2(17.0, 9.0);
        amplitude *= 0.5;
    }
    return value;
}

vec3 worldpos(float depth) {
    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = inverse(ProjMat) * clip;
    view /= view.w;
    return (InvViewMat * view).xyz;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    if (depth >= 1.0) {
        discard;
    }

    vec3 rel = worldpos(depth) - Center.xyz;
    float front = Params.x;
    float maxRadius = Params.y;
    float dist = length(rel);
    if (dist > front + 0.6 || dist > maxRadius) {
        discard;
    }

    float time = Center.w;
    vec2 flow = rel.xz + Params.w;
    float warp = (fbm(flow * 0.55 + time * 0.7) - 0.5) * 1.1;
    float d = dist + warp;
    float behind = front - d;

    float crest = exp(-behind * behind * 2.5);
    float trail = behind > 0.0 ? exp(-behind * 0.35) : 0.0;
    float swell = 0.5 + 0.5 * sin((d - time * 7.0) * 2.4);
    float churn = fbm(flow * 2.2 - vec2(time * 1.3, time * 0.9));
    float foam = smoothstep(0.5, 0.85, crest * 0.8 + churn * 0.45 * (crest + trail * swell * 0.5));

    vec3 color = mix(deepColor, waterColor, clamp(trail + crest * 0.5, 0.0, 1.0));
    color = mix(color, crestColor, clamp(crest * 0.85 + swell * trail * 0.25, 0.0, 1.0));
    color = mix(color, foamColor, foam);

    float edge = 1.0 - smoothstep(maxRadius * 0.8, maxRadius, dist);
    float alpha = crest * 0.8 + trail * (0.22 + 0.18 * swell) + foam * 0.45;
    alpha = clamp(alpha, 0.0, 0.88) * edge * Params.z;
    if (alpha <= 0.003) {
        discard;
    }
    fragColor = vec4(color, alpha);
}
