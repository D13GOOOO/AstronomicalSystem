package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Quantities;

/**
 * Optics of a perspective camera.
 *
 * @param verticalFieldOfView angle between the top and the bottom of the image, in radians
 * @param near                distance of the nearest visible plane, in AU
 * @param far                 distance of the farthest visible plane, in AU
 */
public record Lens(double verticalFieldOfView, double near, double far) {

    public Lens {
        // Written so that NaN fails the comparisons
        if (!(verticalFieldOfView > 0.0 && verticalFieldOfView < Math.PI)) {
            throw new IllegalArgumentException("Field of view must be between 0 and pi radians");
        }
        Quantities.requirePositiveFinite(near, "Near plane");
        if (!(far > near && Double.isFinite(far))) {
            throw new IllegalArgumentException("Far plane must be finite and beyond the near plane");
        }
    }
}
