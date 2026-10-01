#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 viewNormal;
out vec3 viewPosition;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;

void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;

    texCoord0 = UV0;
    vertexColor = Color;
    viewNormal = mat3(ModelViewMat) * Normal;
    viewPosition = view.xyz;
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
