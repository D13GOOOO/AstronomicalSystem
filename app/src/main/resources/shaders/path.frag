#version 330 core

in vec3 pathColour;

out vec4 fragmentColour;

void main() {
    fragmentColour = vec4(pathColour, 1.0);
}
