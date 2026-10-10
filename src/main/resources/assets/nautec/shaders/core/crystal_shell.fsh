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

const vec3 DEEP = vec3(0.03, 0.32, 0.45);
const vec3 BRIGHT = vec3(0.42, 0.96, 1.0);
const vec3 WHITE = vec3(0.88, 1.0, 1.0);

void main() {
    float time = GameTime * 1200.0;
    vec4 glass = texture(Sampler0, texCoord0);
    vec4 glyphTexel = texture(Sampler0, texCoord0 + vec2(0.5, 0.0));
    float glyph = glyphTexel.r * glyphTexel.a;

    vec3 n = normalize(viewNormal);
    vec3 v = ProjMat[3][3] == 1.0 ? vec3(0.0, 0.0, 1.0) : normalize(-viewPosition);
    float facing = abs(dot(n, v));
    float fresnel = pow(1.0 - facing, 2.2);

    vec3 sn = gl_FrontFacing ? n : -n;
    float glintA = pow(max(dot(sn, normalize(normalize(vec3(0.45, 0.8, 0.4)) + v)), 0.0), 60.0);
    float glintB = pow(max(dot(sn, normalize(normalize(vec3(-0.6, 0.25, 0.75)) + v)), 0.0), 90.0);
    float glint = glintA + glintB * 0.7;

    float height = texCoord0.y;
    float breathe = 0.78 + 0.22 * sin(time * 1.35);
    float rising = pow(0.5 + 0.5 * sin(height * 46.0 + time * 2.4), 6.0);
    float surge = pow(0.5 + 0.5 * sin(height * 9.0 + time * 0.8), 3.0);
    float sheen = pow(0.5 + 0.5 * sin((texCoord0.x * 8.0 - height * 2.5) * 6.2831853 - time * 0.7), 28.0);

    vec3 color = mix(DEEP, glass.rgb * 1.15, 0.6) * (0.85 + 0.25 * breathe);
    color = mix(color, BRIGHT, fresnel * 0.8);
    color += BRIGHT * surge * 0.1;
    color += WHITE * sheen * 0.3;
    color += WHITE * glint;
    color += mix(BRIGHT, WHITE, rising * 0.7) * glyph * (0.3 + 1.0 * rising) * breathe;

    float alpha = glass.a * 0.55 * (0.8 + 0.2 * breathe) + fresnel * 0.55 + glyph * (0.2 + 0.55 * rising) + glint * 0.7 + sheen * 0.12;

    if (!gl_FrontFacing) {
        color *= 0.65;
        alpha *= 0.5;
    }

    color *= vertexColor.rgb;
    alpha = clamp(alpha * vertexColor.a, 0.0, 1.0);

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
