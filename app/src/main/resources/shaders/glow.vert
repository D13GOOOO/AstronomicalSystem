#version 330 core

// The centre comes already in clip coordinates: the camera is applied on the CPU
layout(location = 0) in vec4 clipPosition;
// Corner of the square around the glow, from -1 to 1 in each direction
layout(location = 1) in vec2 corner;
layout(location = 2) in float diameter;
layout(location = 3) in vec3 colour;

// In pixels
uniform vec2 imageSize;

out vec2 onTheGlow;
out vec3 glowColour;

void main() {
    // Half the diameter in pixels is diameter / imageSize in normalised device coordinates; times w,
    // so that the perspective division leaves it as it is
    gl_Position = clipPosition + vec4(corner * diameter / imageSize * clipPosition.w, 0.0, 0.0);
    onTheGlow = corner;
    glowColour = colour;
}
