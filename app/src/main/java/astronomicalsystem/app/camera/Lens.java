package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * Optics of a perspective camera.
 *
 * @param verticalFieldOfView angle between the top and the bottom of the image, in radians
 * @param near                distance of the nearest visible plane, in AU
 * @param far                 distance of the farthest visible plane, in AU
 */
public record Lens(double verticalFieldOfView, double near, double far) {

    // The near plane of a camera close to what it looks at, as a fraction of the distance to it
    private static final double CLOSE_UP_NEAR = 1e-3;

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

    /**
     * @param distance from the camera to what it looks at, in AU; precondition: positive and finite
     * @return this lens while what the camera looks at is beyond its near plane; nearer, the same
     *         lens with the near plane at a thousandth of the distance, so that it stays in sight
     */
    @NotNull Lens closeUpTo(double distance) {
        return near <= distance ? this : new Lens(verticalFieldOfView, CLOSE_UP_NEAR * distance, far);
    }

    /**
     * @return 1 / tan(fov / 2): how much the image is magnified compared with a right-angle lens
     */
    double focalFactor() {
        return 1.0 / Math.tan(verticalFieldOfView / 2.0);
    }

    /**
     * @param depth distance in front of the camera, along its line of sight
     * @return the z clip coordinate, which divided by the depth goes from -1 on the near plane to 1
     *         on the far plane
     */
    double clipZ(double depth) {
        return (depth * (far + near) - 2.0 * far * near) / (far - near);
    }
}
