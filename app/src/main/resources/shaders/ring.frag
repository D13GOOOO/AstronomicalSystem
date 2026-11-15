#version 330 core

const int MOST_BANDS = 8;

// From the centre of the sphere, in radii of the sphere: x to the right, y up
in vec2 onTheImage;

// Unit vector perpendicular to the rings, in the frame of the image
uniform vec3 axis;
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
    vec4 shown = vec4(0.0);
    for (int band = 0; band < bandCount; band++) {
        // How much of the pixel the band covers: all of it inside, fading across a pixel at each edge
        float covered = clamp((radius - edges[band].x) / pixel + 0.5, 0.0, 1.0)
                * clamp((edges[band].y - radius) / pixel + 0.5, 0.0, 1.0);
        shown += covered * lights[band];
    }
    fragmentColour = shown;
}
