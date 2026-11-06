package astronomicalsystem.app.camera;

/**
 * Optics of a perspective camera.
 *
 * @param verticalFieldOfView angle between the top and the bottom of the image, in radians
 * @param near                distance of the nearest visible plane, in AU
 * @param far                 distance of the farthest visible plane, in AU
 */
public record Lens(double verticalFieldOfView, double near, double far) {
}
