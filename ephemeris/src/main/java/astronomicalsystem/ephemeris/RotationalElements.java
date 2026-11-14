package astronomicalsystem.ephemeris;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rotation.Spin;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Orientation of a body as the IAU Working Group on Cartographic Coordinates and Rotational
 * Elements tabulates it, in degrees, in the equatorial frame of the ICRF at the epoch J2000.
 *
 * @param rightAscension of the north pole
 * @param declination    of the north pole
 * @param primeMeridian  angle W of the prime meridian at the epoch, measured along the equator of
 *                       the body, eastwards from its ascending node on the equator of the ICRF
 * @param degreesPerDay  rate of W; negative for a retrograde rotation
 */
public record RotationalElements(double rightAscension, double declination, double primeMeridian,
                                 double degreesPerDay) {

    private static final JulianDate J2000 = new JulianDate(2451545.0);

    /**
     * @return the rotation of the body in the frame of the simulation, ecliptic of J2000
     */
    public @NotNull Spin spin() {
        return new Spin(new Vector3(0.0, 0.0, 1.0), new Vector3(1.0, 0.0, 0.0), J2000, 0.0);
    }
}
