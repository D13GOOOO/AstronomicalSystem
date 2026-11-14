#version 330 core

// From -1 to 1 across the square around the disc, upwards
in vec2 onTheDisc;
in vec3 discColour;
in vec4 discLight;

out vec4 fragmentColour;

// The night side is not quite black: the faint light of the rest of the sky
const float SKY_LIGHT = 0.04;
// Linear limb darkening of a star, I = 1 - u (1 - mu), with the coefficient of the Sun in visible light
const float LIMB_DARKENING = 0.6;

void main() {
    float squaredDistance = dot(onTheDisc, onTheDisc);
    // Keep the inscribed circle
    if (squaredDistance > 1.0) {
        discard;
    }
    // The visible half of a unit sphere: its normal where the disc is seen
    vec3 normal = vec3(onTheDisc, sqrt(1.0 - squaredDistance));
    if (discLight.w == 0.0) {
        // mu, the cosine between the normal and the line of sight, is the z of the normal
        fragmentColour = vec4(discColour * (1.0 - LIMB_DARKENING * (1.0 - normal.z)), 1.0);
        return;
    }
    // Lambert's law
    float lit = max(dot(normal, discLight.xyz), 0.0);
    fragmentColour = vec4(discColour * (SKY_LIGHT + (1.0 - SKY_LIGHT) * lit), 1.0);
}
