#version 330 core

// In pixels from the top left corner of the image, like the quads of stb_truetype
layout(location = 0) in vec2 pixelPosition;
layout(location = 1) in vec2 atlasPosition;

uniform vec2 imageSize;

out vec2 glyphPosition;

void main() {
    vec2 fromTopLeft = pixelPosition / imageSize;
    gl_Position = vec4(fromTopLeft.x * 2.0 - 1.0, 1.0 - fromTopLeft.y * 2.0, 0.0, 1.0);
    glyphPosition = atlasPosition;
}
