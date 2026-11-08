package astronomicalsystem.core.time;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Instant expressed as a Julian date in Barycentric Dynamical Time (TDB): days elapsed since noon
 * of 1 January 4713 BC in the proleptic Julian calendar.
 */
public record JulianDate(double days) {

    private static final double UNIX_EPOCH = 2440587.5;
    private static final double SECONDS_PER_DAY = 86_400.0;
    // TT - UTC = (TAI - UTC) + 32.184 s, and TAI - UTC is 37 s since the leap second at the end of
    // 2016; TDB differs from TT by less than 2 ms, which is neglected
    private static final double TDB_MINUS_UTC_SECONDS = 69.184;

    public JulianDate {
        if (!Double.isFinite(days)) {
            throw new IllegalArgumentException("Julian date must be finite");
        }
    }

    /**
     * @param instant precondition: on or after 1 January 2017, since earlier instants had fewer leap
     *                seconds
     * @return the Julian date in TDB of a UTC instant, to the second
     */
    public static @NotNull JulianDate at(@NotNull Instant instant) {
        return new JulianDate(UNIX_EPOCH + (instant.getEpochSecond() + TDB_MINUS_UTC_SECONDS) / SECONDS_PER_DAY);
    }

    /**
     * @return the UTC instant of this date, to the millisecond; precondition: on or after 1 January
     * 2017, like {@link #at(Instant)}
     */
    public @NotNull Instant toInstant() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
