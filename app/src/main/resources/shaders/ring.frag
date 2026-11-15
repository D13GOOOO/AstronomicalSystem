#version 330 core

const int MOST_BANDS = 8;

// From the centre of the sphere, in radii of the sphere: x to the right, y up
in vec2 onTheImage;

// Unit vector perpendicular to the rings, in the frame of the image
uniform vec3 axis;
// Unit vector from the sphere towards the light, in the frame of the image
uniform vec3 towardsTheLight;
uniform int bandCount;
// The inner and the outer radius of each band, in radii of the sphere
uniform vec2 edges[MOST_BANDS];
// The light each band sends to the viewer, and how much of what is behind it hides
uniform vec4 lights[MOST_BANDS];

out vec4 fragmentColour;

void main() {
    // The sphere is seen from afar: the lines of sight are parallel to z, and the one through the
    // pixel crosses the plane of the rings, axis . P = 0, at this point
    vec3 onTheRings = vec3(onTheImage, -dot(axis.xy, onTheImage) / axis.z);
    float radius = length(onTheRings);
    // How much the radius changes from this pixel to the next
    float pixel = fwidth(radius);
    // Inside the outline of the sphere the rings, which go round it, are either in front of it or
    // behind it: behind where they are beyond its centre
    if (dot(onTheImage, onTheImage) < 1.0 && onTheRings.z < 0.0) {
        discard;
    }
    vec4 shown = vec4(0.0);
    for (int band = 0; band < bandCount; band++) {
        // How much of the pixel the band covers: all of it inside, fading across a pixel at each edge
        float covered = clamp((radius - edges[band].x) / pixel + 0.5, 0.0, 1.0)
                * clamp((edges[band].y - radius) / pixel + 0.5, 0.0, 1.0);
        shown += covered * lights[band];
    }
    // The shadow of the sphere is a cylinder as wide as the sphere, away from the light: the rings
    // are in it where they are beyond the centre along the rays and less than a radius from the
    // ray through the centre. There they send no light, and hide what is behind them all the same
    float alongTheRays = dot(onTheRings, towardsTheLight);
    vec3 fromTheMiddleRay = onTheRings - alongTheRays * towardsTheLight;
    bool inTheShadow = alongTheRays < 0.0 && dot(fromTheMiddleRay, fromTheMiddleRay) < 1.0;
    fragmentColour = vec4(inTheShadow ? vec3(0.0) : shown.rgb, shown.a);
}
