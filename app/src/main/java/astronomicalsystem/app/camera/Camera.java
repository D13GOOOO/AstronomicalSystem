package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Perspective camera that looks at a target from an eye, with a given direction pointing up in the
 * image.
 */
public final class Camera {

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
        this.eye = eye;
        this.forward = target.minus(eye).normalized();
        this.right = forward.cross(up).normalized();
        this.imageUp = right.cross(forward);
        this.lens = lens;
        this.aspectRatio = viewport.aspectRatio();
        this.pixelsPerDeviceUnit = viewport.height() / 2.0;
    }

    /**
     * @return where the point lands in the clip coordinates of OpenGL
     */
    public @NotNull ClipPoint clipPoint(@NotNull Vector3 point) {
        Vector3 offset = point.minus(eye);
        double depth = offset.dot(forward);
        double focalFactor = lens.focalFactor();
        return new ClipPoint(focalFactor / aspectRatio * offset.dot(right), focalFactor * offset.dot(imageUp),
                lens.clipZ(depth), depth);
    }

    /**
     * @param size   length across the line of sight, in AU
     * @param centre clip point of the middle of the length; precondition: in front of the camera
     * @return how many pixels the length spans in the image
     */
    public double apparentSizeInPixels(double size, @NotNull ClipPoint centre) {
        return size * lens.focalFactor() / centre.w() * pixelsPerDeviceUnit;
    }
}
