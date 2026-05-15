#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in ivec2 UV2;

out vec2 glintCoord;
out vec2 maskCoord;
out vec4 vertexData;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    glintCoord = UV0;
    maskCoord = vec2(UV2) / 256.0;
    vertexData = Color;
}
