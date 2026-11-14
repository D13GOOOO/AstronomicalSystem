#version 330 core

// From -1 to 1 across the square around the disc, upwards
in vec2 onTheDisc;
in vec3 discColour;
in vec4 discLight;
// The axes of the body in the frame of the image
in vec3 discX;
in vec3 discY;
in vec3 discZ;

// The map of the surface, for a disc wrapped in one
uniform sampler2D map;
uniform bool mapped;

out vec4 fragmentColour;

const float PI = 3.14159265358979;
// The night side is not quite black: the faint light of the rest of the sky
const float SKY_LIGHT = 0.04;
// Linear limb darkening of a star, I = 1 - u (1 - mu), with the coefficient of the Sun in visible light
const float LIMB_DARKENING = 0.6;

// The colour of the surface where its normal, in the frame of the image, is the one given
vec3 surfaceColour(vec3 normal) {
    if (!mapped) {
        return discColour;
    }
    // The same direction in the frame of the body, then its longitude and latitude: the map has the
    // longitudes from -pi to pi across it and the latitudes from pi/2 at the top to -pi/2
    vec3 onTheBody = vec3(dot(normal, discX), dot(normal, discY), dot(normal, discZ));
    float longitude = atan(onTheBody.y, onTheBody.x);
    float latitude = asin(clamp(onTheBody.z, -1.0, 1.0));
    return texture(map, vec2(longitude / (2.0 * PI) + 0.5, 0.5 - latitude / PI)).rgb;
}

void main() {
    float squaredDistance = dot(onTheDisc, onTheDisc);
    // Keep the inscribed circle
    if (squaredDistance > 1.0) {
        discard;
    }
    // The visible half of a unit sphere: its normal where the disc is seen
    vec3 normal = vec3(onTheDisc, sqrt(1.0 - squaredDistance));
    vec3 colour = surfaceColour(normal);
    if (discLight.w == 0.0) {
        // mu, the cosine between the normal and the line of sight, is the z of the normal
        fragmentColour = vec4(colour * (1.0 - LIMB_DARKENING * (1.0 - normal.z)), 1.0);
        return;
    }
    // Lambert's law
    float lit = max(dot(normal, discLight.xyz), 0.0);
    fragmentColour = vec4(colour * (SKY_LIGHT + (1.0 - SKY_LIGHT) * lit), 1.0);
}
