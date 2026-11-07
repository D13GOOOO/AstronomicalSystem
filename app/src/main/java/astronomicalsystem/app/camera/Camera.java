package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Perspective camera that looks at a target from an eye, with a given direction pointing up in the
 * image.
 */
public final class Camera implements Projection {

    private final Vector3 eye;
    // Orthonormal right-handed basis of the camera: right x imageUp = -forward, as in OpenGL, where the
    // camera looks down its negative z-axis
    private final Vector3 right;
    private final Vector3 imageUp;
    private final Vector3 forward;
    private final Lens lens;
    private final double aspectRatio;
    // Normalised device coordinates go from -1 to 1: one unit is half the height of the image
    private final double pixelsPerDeviceUnit;

    public Camera(@NotNull Vector3 eye, @NotNull Vector3 target, @NotNull Vector3 up, @NotNull Lens lens,
                  @NotNull Viewport viewport) {
        Vector3 lineOfSight = target.minus(eye);
        if (lineOfSight.lengthSquared() == 0.0) {
            throw new IllegalArgumentException("Camera eye and target must differ");
        }
        Vector3 side = lineOfSight.cross(up);
        if (side.lengthSquared() == 0.0) {
            throw new IllegalArgumentException("Camera up must not be parallel to the line of sight");
        }
        this.eye = eye;
        this.forward = lineOfSight.normalized();
        this.right = side.normalized();
        this.imageUp = right.cross(forward);
        this.lens = lens;
        this.aspectRatio = viewport.aspectRatio();
        this.pixelsPerDeviceUnit = viewport.height() / 2.0;
    }

    @Override
    public @NotNull ClipPoint clipPoint(@NotNull Vector3 point) {
        Vector3 offset = point.minus(eye);
        double depth = offset.dot(forward);
        double focalFactor = lens.focalFactor();
        return new ClipPoint(focalFactor / aspectRatio * offset.dot(right), focalFactor * offset.dot(imageUp),
                lens.clipZ(depth), depth);
    }

    @Override
    public double apparentSizeInPixels(double size, @NotNull ClipPoint centre) {
        return size * lens.focalFactor() / centre.w() * pixelsPerDeviceUnit;
    }
}
