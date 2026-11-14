#version 330 core

// From -1 to 1 across the square around the disc, upwards
in vec2 onTheDisc;
in vec3 discColour;
in vec4 discLight;

out vec4 fragmentColour;

// The night side is not quite black: the faint light of the rest of the sky
const float SKY_LIGHT = 0.04;

void main() {
    float squaredDistance = dot(onTheDisc, onTheDisc);
    // Keep the inscribed circle
    if (squaredDistance > 1.0) {
        discard;
    }
    if (discLight.w == 0.0) {
        fragmentColour = vec4(discColour, 1.0);
        return;
    }
    // The visible half of a unit sphere: its normal where the disc is seen, and Lambert's law
    vec3 normal = vec3(onTheDisc, sqrt(1.0 - squaredDistance));
    float lit = max(dot(normal, discLight.xyz), 0.0);
    fragmentColour = vec4(discColour * (SKY_LIGHT + (1.0 - SKY_LIGHT) * lit), 1.0);
}
