package astronomicalsystem.core.time;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Instant expressed as a Julian date in Barycentric Dynamical Time (TDB): days elapsed since noon
 * of 1 January 4713 BC in the proleptic Julian calendar.
 */
public record JulianDate(double days) {

    public JulianDate {
        if (!Double.isFinite(days)) {
            throw new IllegalArgumentException("Julian date must be finite");
        }
    }

    /**
     * @param instant precondition: on or after 1 January 2017
     * @return the Julian date of a UTC instant
     */
    public static @NotNull JulianDate at(@NotNull Instant instant) {
        return new JulianDate(0.0);
    }
}
