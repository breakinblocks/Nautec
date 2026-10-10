#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 viewNormal;
out vec3 viewPosition;
out float vertexDistance;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;

    texCoord0 = UV0;
    vertexColor = Color;
    viewNormal = mat3(ModelViewMat) * Normal;
    viewPosition = view.xyz;
    vertexDistance = fog_distance(Position, FogShape);
}
