#version 330 core

// The position comes already in clip coordinates: the camera is applied on the CPU
layout(location = 0) in vec4 clipPosition;
layout(location = 1) in float diameter;
layout(location = 2) in vec3 colour;

out vec3 discColour;

void main() {
    gl_Position = clipPosition;
    gl_PointSize = diameter;
    discColour = colour;
}
