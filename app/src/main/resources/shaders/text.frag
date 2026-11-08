#version 330 core

in vec2 glyphPosition;

// How much of each pixel the glyphs cover, from 0 to 1
uniform sampler2D atlas;

out vec4 fragmentColour;

// The pale blue of a cockpit display
const vec3 TEXT_COLOUR = vec3(0.75, 0.9, 1.0);

void main() {
    fragmentColour = vec4(TEXT_COLOUR, texture(atlas, glyphPosition).r);
}
