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
    // Obliquity of the ecliptic at J2000 (IAU 1976), 84381.448 arcseconds: the angle between the
    // equator of the ICRF and the ecliptic, which share the x-axis towards the equinox
    private static final double OBLIQUITY = Math.toRadians(84_381.448 / 3600.0);

    /**
     * @return the rotation of the body in the frame of the simulation, ecliptic of J2000
     */
    public @NotNull Spin spin() {
        double alpha = Math.toRadians(rightAscension);
        double delta = Math.toRadians(declination);
        Vector3 pole = new Vector3(Math.cos(delta) * Math.cos(alpha), Math.cos(delta) * Math.sin(alpha),
                Math.sin(delta));
        // The ascending node of the equator of the body on the equator of the ICRF, a quarter turn
        // after the right ascension of the pole
        Vector3 node = new Vector3(-Math.sin(alpha), Math.cos(alpha), 0.0);
        // W along the equator of the body from the node, eastwards: about the pole
        Vector3 meridian = node.rotatedAbout(pole, Math.toRadians(primeMeridian));
        return new Spin(inTheEclipticFrame(pole), inTheEclipticFrame(meridian), J2000, 0.0);
    }

    // The ecliptic frame is the equatorial one turned about their common x-axis by the obliquity
    private static @NotNull Vector3 inTheEclipticFrame(@NotNull Vector3 equatorial) {
        return equatorial.rotatedAboutX(-OBLIQUITY);
    }
}
