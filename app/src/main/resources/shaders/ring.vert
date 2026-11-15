#version 330 core

// The centre of the sphere comes already in clip coordinates: the camera is applied on the CPU
layout(location = 0) in vec4 clipPosition;
// Corner of the square around the rings, from -1 to 1 in each direction
layout(location = 1) in vec2 corner;
// Of the sphere, in pixels
layout(location = 2) in float diameter;

// In pixels
uniform vec2 imageSize;
// Radius of the outer edge of the outermost band, in radii of the sphere
uniform float extent;

// From the centre of the sphere, in radii of the sphere: x to the right, y up
out vec2 onTheImage;

void main() {
    // Half the diameter in pixels is diameter / imageSize in normalised device coordinates; times w,
    // so that the perspective division leaves it as it is
    gl_Position = clipPosition + vec4(corner * extent * diameter / imageSize * clipPosition.w, 0.0, 0.0);
    onTheImage = corner * extent;
}
