#version 330 core

in vec3 discColour;

out vec4 fragmentColour;

void main() {
    // gl_PointCoord goes from 0 to 1 across the square point: keep the inscribed circle
    vec2 fromCentre = gl_PointCoord - vec2(0.5);
    if (dot(fromCentre, fromCentre) > 0.25) {
        discard;
    }
    fragmentColour = vec4(discColour, 1.0);
}
