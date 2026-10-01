#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:projection.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 viewNormal;
in vec3 viewPosition;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

void main() {
    float time = GameTime * 1200.0;
    vec4 base = texture(Sampler0, texCoord0);
    vec4 glyphTexel = texture(Sampler0, texCoord0 + vec2(0.5, 0.0));
    float glyph = glyphTexel.r * glyphTexel.a;

    vec3 n = normalize(viewNormal);
    vec3 v = ProjMat[3][3] == 1.0 ? vec3(0.0, 0.0, 1.0) : normalize(-viewPosition);
    float facing = abs(dot(n, v));

    float pulse = 0.62 + 0.38 * pow(0.5 + 0.5 * sin(time * 2.1), 2.0);
    float flow = pow(0.5 + 0.5 * sin(texCoord0.y * 70.0 + time * 4.5), 4.0);

    vec3 color = base.rgb * (0.3 + 0.7 * facing) * pulse;
    color += vec3(0.75, 1.0, 1.0) * glyph * flow * 0.9;
    color *= vertexColor.rgb * vertexColor.a;
    if (!gl_FrontFacing) {
        color *= 0.4;
    }

    float fog = total_fog_value(sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd);
    color *= 1.0 - fog;

    fragColor = vec4(color, clamp(max(color.r, max(color.g, color.b)), 0.0, 1.0));
}
