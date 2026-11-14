#version 330 core

// The position comes already in clip coordinates: the camera is applied on the CPU
layout(location = 0) in vec4 clipPosition;
layout(location = 1) in float diameter;
layout(location = 2) in vec3 colour;
// Towards the light in the frame of the image, and 1 for a lit disc, 0 for one with its own light
layout(location = 3) in vec4 light;

out vec3 discColour;
out vec4 discLight;

void main() {
    gl_Position = clipPosition;
    gl_PointSize = diameter;
    discColour = colour;
    discLight = light;
}
