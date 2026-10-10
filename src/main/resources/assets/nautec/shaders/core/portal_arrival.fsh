#version 150

uniform sampler2D DepthSampler;

uniform mat4 ProjMat;

uniform mat4 InvViewMat;
uniform vec4 Center;
uniform vec4 Params;

in vec2 texCoord;

out vec4 fragColor;

const vec3 violet = vec3(0.69, 0.48, 1.0);
const vec3 cyan = vec3(0.35, 0.86, 1.0);
const vec3 white = vec3(0.95, 0.97, 1.0);

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

float noise3(vec3 p) {
    return (fbm(p.xy) + fbm(p.yz + 31.0) + fbm(p.zx + 57.0)) / 3.0;
}

vec3 worldpos(float depth) {
    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 view = inverse(ProjMat) * clip;
    view /= view.w;
    return (InvViewMat * view).xyz;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    bool sky = depth >= 1.0;
    vec3 surface = worldpos(sky ? 0.9999 : depth);
    float sceneDist = sky ? 1.0e6 : length(surface);
    vec3 dir = normalize(surface);

    float time = Center.w;
    vec3 center = Center.xyz;
    float front = Params.x;
    float maxRadius = Params.y;
    float fade = Params.z;
    float seed = Params.w;

    vec3 color = vec3(0.0);

    float along = dot(dir, center);
    float miss = max(dot(center, center) - along * along, 0.0);
    float disc = front * front - miss;
    if (disc > 0.0 && front > 0.05) {
        float halfChord = sqrt(disc);
        float near = along - halfChord;
        float t = near > 0.0 ? near : along + halfChord;
        if (t > 0.0 && t < sceneDist) {
            vec3 normal = (dir * t - center) / front;
            float rim = pow(1.0 - abs(dot(normal, dir)), 2.5);
            float veins = smoothstep(0.5, 0.62, noise3(normal * 3.5 + vec3(time * 0.9, -time * 0.6, seed)));
            float shell = rim * 0.85 + veins * (0.45 + 0.4 * rim) + 0.05;
            color += mix(violet, cyan, rim * 0.6) * shell + cyan * veins * 0.25;
        }
    }

    float progress = clamp(front / maxRadius, 0.0, 1.0);
    if (along > 0.0 && along < sceneDist) {
        float flash = exp(-miss * (0.4 + progress * 3.0)) * (1.0 - progress) * (1.0 - progress);
        color += (white * 1.4 + violet) * flash;
    }

    if (!sky) {
        vec3 rel = surface - center;
        float dist = length(rel);
        if (dist < maxRadius) {
            float warp = (noise3(rel * 0.45 + vec3(time * 0.8, seed, -time * 0.5)) - 0.5) * 1.2;
            float behind = front - (dist + warp);
            float crest = exp(-behind * behind * 3.0);
            float trail = behind > 0.0 ? exp(-behind * 0.5) * 0.35 : 0.0;
            float glyph = smoothstep(0.6, 0.8, noise3(rel * 1.3 - vec3(0.0, time * 1.5, 0.0))) * crest;
            float edge = 1.0 - smoothstep(maxRadius * 0.75, maxRadius, dist);
            color += (violet * (crest * 0.9 + trail) + cyan * glyph * 0.8 + white * pow(crest, 6.0) * 0.6) * edge;
        }
    }

    color *= fade;
    if (max(color.r, max(color.g, color.b)) <= 0.003) {
        discard;
    }
    fragColor = vec4(color, 1.0);
}
