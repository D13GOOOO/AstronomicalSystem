package astronomicalsystem.core.time;

/**
 * Instant expressed as a Julian date in Barycentric Dynamical Time (TDB): days elapsed since noon
 * of 1 January 4713 BC in the proleptic Julian calendar.
 */
public record JulianDate(double days) {
}
