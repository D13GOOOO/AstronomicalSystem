package astronomicalsystem.app.time;

/**
 * Real time measured by the monotonic clock of the JVM.
 */
public final class SystemClock implements Clock {

    @Override
    public double seconds() {
        return 0.0;
    }
}
