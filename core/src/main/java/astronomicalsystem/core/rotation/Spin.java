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

    /**
     * @return the axes of the body at the date
     */
    public @NotNull Axes axesAt(@NotNull JulianDate date) {
        return new Axes(meridian, meridian, pole);
    }
}
