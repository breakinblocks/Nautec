#version 330

uniform sampler2D SceneSampler;
layout(std140) uniform BlurInfo {
    vec4 Params; // strength, width/max dimension, height/max dimension, unused
};
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 direction = texCoord - vec2(0.5);
    float strength = clamp(Params.x, 0.0, 1.0);
    float edge = smoothstep(0.08, 0.5, length(direction * Params.yz));
    float span = 0.18 * strength * strength * edge;
    vec4 original = texture(SceneSampler, texCoord);
    vec3 sum = original.rgb;
    // Sample only the saved scene, never the color attachment being written.
    for (int i = 1; i < 12; ++i) {
        sum += texture(SceneSampler, texCoord - direction * span * (float(i) / 11.0)).rgb;
    }
    fragColor = vec4(mix(original.rgb, sum / 12.0, strength), original.a);
}
