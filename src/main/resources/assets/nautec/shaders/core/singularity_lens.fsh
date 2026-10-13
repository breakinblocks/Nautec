#version 330

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

layout(std140) uniform Projection {
    mat4 ProjMat;
};

layout(std140) uniform LensInfo {
    vec4 Screen;
    vec4 Centers[4];
    vec4 Params[4];
};

in vec2 texCoord;

out vec4 fragColor;

vec3 project(vec3 view) {
    vec4 clip = ProjMat * vec4(view, 1.0);
    return clip.xyz / clip.w;
}

float sceneViewZ(float depth) {
    if (depth >= 1.0) {
        return -1.0e9;
    }
    vec4 view = inverse(ProjMat) * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return view.z / view.w;
}

void main() {
    vec2 pixel = texCoord * Screen.xy;
    float sceneZ = sceneViewZ(texture(DepthSampler, texCoord).r);
    vec2 offset = vec2(0.0);
    float horizon = 0.0;
    vec3 glow = vec3(0.0);
    int count = int(Screen.z);
    for (int i = 0; i < 4; i++) {
        if (i >= count) {
            break;
        }
        vec3 centre = Centers[i].xyz;
        float radius = Centers[i].w;
        if (centre.z > -radius * 1.5) {
            continue;
        }
        if (sceneZ > centre.z + radius) {
            continue;
        }
        vec3 ndc = project(centre);
        vec2 screenCentre = (ndc.xy * 0.5 + 0.5) * Screen.xy;
        vec3 edge = project(centre + vec3(radius, 0.0, 0.0));
        float rs = length((edge.xy * 0.5 + 0.5) * Screen.xy - screenCentre);
        if (rs < 0.75) {
            continue;
        }
        vec2 delta = pixel - screenCentre;
        float r = length(delta);
        float strength = Params[i].x;
        float reach = 1.0 - smoothstep(rs * 6.0, rs * 12.0, r);
        if (reach <= 0.0) {
            continue;
        }
        vec2 direction = delta / max(r, 0.001);
        offset -= direction * strength * rs * rs / max(r, rs) * reach;
        horizon = max(horizon, 1.0 - smoothstep(rs * 0.92, rs * 1.04, r));
        float einstein = sqrt(strength) * rs;
        float ring = exp(-pow((r - einstein) / (rs * 0.16), 2.0));
        float halo = exp(-max(r - rs, 0.0) / (rs * 1.6)) * 0.35;
        glow += vec3(0.82, 0.66, 1.0) * (ring + halo) * Params[i].y * reach;
    }
    vec2 sampleUv = clamp((pixel + offset) / Screen.xy, vec2(0.001), vec2(0.999));
    vec3 color = texture(SceneSampler, sampleUv).rgb;
    color += glow;
    color = mix(color, vec3(0.0), clamp(horizon, 0.0, 1.0));
    fragColor = vec4(color, 1.0);
}
