#version 330 core

// From -1 to 1 across the square around the glow
in vec2 onTheGlow;
in vec3 glowColour;

out vec4 fragmentColour;

void main() {
    float squaredDistance = dot(onTheGlow, onTheGlow);
    if (squaredDistance > 1.0) {
        discard;
    }
    // (1 - r²)²: full in the middle, smooth to nothing at the edge
    float falloff = (1.0 - squaredDistance) * (1.0 - squaredDistance);
    fragmentColour = vec4(glowColour * falloff, 1.0);
}
