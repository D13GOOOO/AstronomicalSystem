package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Position of a camera on a sphere around its target, in the ecliptic frame.
 *
 * @param distance  from the target, in AU
 * @param azimuth   angle around the ecliptic north, in radians; 0 looks from the negative y-axis
 * @param elevation angle above the ecliptic, in radians
 */
public record Orbit(double distance, double azimuth, double elevation) {

    // Close enough to see a planet as a disc, far enough to see the orbit of Neptune (30 AU)
    private static final double NEAREST = 0.02;
    private static final double FARTHEST = 100.0;
    // Short of the poles, where the ecliptic north would be parallel to the line of sight
    private static final double ELEVATION_LIMIT = Math.toRadians(89.0);

    public Orbit {
        // Written so that NaN fails the comparisons
        if (!(distance >= NEAREST && distance <= FARTHEST)) {
            throw new IllegalArgumentException("Orbit distance must be between 0.02 and 100 AU");
        }
        if (!Double.isFinite(azimuth)) {
            throw new IllegalArgumentException("Orbit azimuth must be finite");
        }
        if (!(Math.abs(elevation) <= ELEVATION_LIMIT)) {
            throw new IllegalArgumentException("Orbit elevation must be within 89 degrees of the ecliptic");
        }
    }

    /**
     * @return where the camera stands when it orbits the target
     */
    public @NotNull Vector3 eye(@NotNull Vector3 target) {
        double alongTheEcliptic = distance * Math.cos(elevation);
        return target.plus(new Vector3(alongTheEcliptic * Math.sin(azimuth), -alongTheEcliptic * Math.cos(azimuth),
                distance * Math.sin(elevation)));
    }

    /**
     * @param azimuthAngle   added to the azimuth, in radians; precondition: finite
     * @param elevationAngle added to the elevation, in radians, which stops at the limit; precondition:
     *                       finite
     */
    public @NotNull Orbit rotated(double azimuthAngle, double elevationAngle) {
        double turnedElevation = Math.clamp(elevation + elevationAngle, -ELEVATION_LIMIT, ELEVATION_LIMIT);
        return new Orbit(distance, azimuth + azimuthAngle, turnedElevation);
    }
}
