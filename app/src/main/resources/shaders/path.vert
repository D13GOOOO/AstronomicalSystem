#version 330 core

// The position comes already in clip coordinates: the camera is applied on the CPU
layout(location = 0) in vec4 clipPosition;
layout(location = 1) in vec3 colour;

out vec3 pathColour;

void main() {
    gl_Position = clipPosition;
    pathColour = colour;
}
