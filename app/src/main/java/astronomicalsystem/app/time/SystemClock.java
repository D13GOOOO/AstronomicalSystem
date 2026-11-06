package astronomicalsystem.app.time;

/**
 * Real time measured by the monotonic clock of the JVM, from the creation of the clock.
 */
public final class SystemClock implements Clock {

    private static final double SECONDS_PER_NANOSECOND = 1e-9;

    // Only differences of System.nanoTime() are meaningful, and its values can be huge: they are
    // subtracted as long integers before the conversion, so that no precision is lost
    private final long origin = System.nanoTime();

    @Override
    public double seconds() {
        return (System.nanoTime() - origin) * SECONDS_PER_NANOSECOND;
    }
}
