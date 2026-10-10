#version 150

#moj_import <fog.glsl>

uniform mat4 ProjMat;
uniform float GameTime;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 viewNormal;
in vec3 viewPosition;
in float vertexDistance;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;
    vec4 maskTexel = texture(Sampler0, texCoord0 + vec2(0.5, 0.0));
    float mask = maskTexel.r * maskTexel.a;
    if (mask <= 0.0) {
        discard;
    }

    float energy = vertexColor.a;
    float sweep = pow(0.5 + 0.5 * sin(texCoord0.x * 70.0 + texCoord0.y * 30.0 - time * 3.2), 6.0);
    float breathe = 0.75 + 0.25 * sin(time * 1.6);

    vec3 cyan = vec3(0.32, 0.92, 1.0);
    vec3 white = vec3(0.85, 1.0, 1.0);
    vec3 color = vertexColor.rgb * mask * (0.25 + 0.75 * energy * breathe) + mix(cyan, white, sweep) * mask * sweep * energy;

    float fog = (1.0 - linear_fog_fade(vertexDistance, FogStart, FogEnd));
    color *= 1.0 - fog;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
