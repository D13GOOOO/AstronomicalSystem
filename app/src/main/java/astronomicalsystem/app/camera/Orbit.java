package astronomicalsystem.app.camera;

/**
 * Position of a camera on a sphere around its target, in the ecliptic frame.
 *
 * @param distance  from the target, in AU
 * @param azimuth   angle around the ecliptic north, in radians; 0 looks from the negative y-axis
 * @param elevation angle above the ecliptic, in radians
 */
public record Orbit(double distance, double azimuth, double elevation) {
}
