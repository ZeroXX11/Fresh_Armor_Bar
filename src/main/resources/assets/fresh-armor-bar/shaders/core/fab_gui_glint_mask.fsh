#version 150

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

in vec2 glintCoord;
in vec2 maskCoord;
in vec4 vertexData;

out vec4 fragColor;

void main() {
    float localX = maskCoord.x;
    float maskU = round((localX < 4.5 ? vertexData.r : vertexData.g) * 255.0);
    ivec2 maskPixel = ivec2(floor(vec2(maskU + localX, maskCoord.y)));
    float maskAlpha;

    if (localX < 4.5) {
        ivec2 maskSize = textureSize(Sampler1, 0);
        maskAlpha = texelFetch(Sampler1, clamp(maskPixel, ivec2(0), maskSize - ivec2(1)), 0).a;
    } else {
        ivec2 maskSize = textureSize(Sampler2, 0);
        maskAlpha = texelFetch(Sampler2, clamp(maskPixel, ivec2(0), maskSize - ivec2(1)), 0).a;
    }

    if (maskAlpha <= 0.0625) {
        discard;
    }

    vec4 glint = texture(Sampler0, glintCoord);
    if (glint.a < 0.1) {
        discard;
    }

    fragColor = vec4(glint.rgb * vertexData.b, glint.a * vertexData.a) * ColorModulator;
}
