package astronomicalsystem.core.rotation;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * How a body turns about its axis at a steady rate.
 *
 * @param pole          direction of the north pole, about which the body turns; kept as a unit vector
 * @param meridian      direction of the prime meridian at the epoch; kept as a unit vector
 *                      perpendicular to the pole
 * @param epoch         when the prime meridian is where given
 * @param radiansPerDay rate of the rotation, counterclockwise seen from above the north pole;
 *                      negative for a retrograde rotation
 */
public record Spin(@NotNull Vector3 pole, @NotNull Vector3 meridian, @NotNull JulianDate epoch,
                   double radiansPerDay) {

    public Spin {
        // Zero also when one of the two is zero
        if (pole.cross(meridian).lengthSquared() == 0.0) {
            throw new IllegalArgumentException("A spin needs a pole and a prime meridian across it");
        }
        if (!Double.isFinite(radiansPerDay)) {
            throw new IllegalArgumentException("Spin rate must be finite");
        }
        pole = pole.normalized();
        // Gram-Schmidt: without its component along the pole
        meridian = meridian.minus(pole.times(meridian.dot(pole))).normalized();
    }

    /**
     * @return the axes of the body at the date
     */
    public @NotNull Axes axesAt(@NotNull JulianDate date) {
        Vector3 x = meridian.rotatedAbout(pole, radiansPerDay * (date.days() - epoch.days()));
        return new Axes(x, pole.cross(x), pole);
    }
}
