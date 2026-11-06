package astronomicalsystem.app.view;

/**
 * Point in the homogeneous clip coordinates of OpenGL: it is visible when -w <= x, y, z <= w, and
 * w is its depth in front of the camera.
 */
public record ClipPoint(double x, double y, double z, double w) {

    /**
     * @return whether the point is beyond the plane of the eye, where perspective is defined
     */
    public boolean isInFrontOfTheCamera() {
        return w > 0.0;
    }
}
